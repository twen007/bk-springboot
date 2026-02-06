Ext.define("bcp.util.CommonFunctions", {
    singleton: true, // Makes it a singleton

    // return division preferences; if not found, create one with default value and return it
    getDivisionPreferences: function (divisionId) {
        var divPrefs = Ext.getStore("DivisionPreferences");
        var pref = divPrefs.findRecord("divId", divisionId);
        if (pref == null) {
            //create a init record
            pref = Ext.create("bcp.model.DivisionPreference", {
                divId: divisionId,
                justPrefVal: "O",
                financePrefVal: "O",
                shippingCostPrefVal: "R",
                shippingCostPrefValDetail: 0,
                upToPrefVal: "N",
                upToPrefValDetail: 0,
                addFcoRoutePrefVal: "Y"
            });
            divPrefs.add(pref);
        }
        return pref;
    },
    //for views that has the [Edit] button to allow users to edit requests
    //currently, Search and Purchase views has this button
    setupEditableRequestDetailView: function (record, currentController, view) {
        var readOnlyView = currentController.lookupReference("detail"),
            editableView = currentController.lookupReference("editableDetail");

        if (readOnlyView) readOnlyView.destroy();
        if (editableView) editableView.destroy();

        if (!editableView) {
            view.add({
                xtype: "editreq",
                reference: "editableDetail",
                data: record //this make data load in the view works every time
            });
        }

        //load data
        this.setupRequestDetailData(record);

        //if edit request from the search view, cannot change requester
        //so just give a status other than 1 or 12
        var viewModel = currentController.lookupReference("editableDetail").getViewModel(),
            form = currentController
                .lookupReference("editableDetail")
                .lookupController()
                .lookupReference("generalPanelEditable"),
            controller = form.getController(),
            formModel = controller.getViewModel();
        formModel.set("record", record);
        viewModel.set("generalInfo", record);
        controller.loadForm(record);
        controller.checkReqStatus(6);
    },

    //prep readonly detail view for views with the [View Detail] button
    setupReadOnlyRequestDetailView: function (record, currentController, view, financePanelReadOnly) {
        var readOnlyView = currentController.lookupReference("detail"),
            editableView = currentController.lookupReference("editableDetail");

        if (readOnlyView) readOnlyView.destroy();
        if (editableView) editableView.destroy();
        if (!readOnlyView) {
            view.add({
                xtype: "readonlyrequest",
                reference: "detail",
                data: record,
                flex: 1,
                //finance panel, for BCH's request, need to be editable in the purchase view
                //even when they use viewDetail to access the readonly view
                isFinancePanelReadOnly: financePanelReadOnly
            });
        }

        //load data
        this.setupRequestDetailData(record);
    },

    //whether the view is editable detail or detail, we need to populate the stores
    //bind to each tabview of the detail
    setupRequestDetailData: function (record) {
        var reqId = record.get("requestId"),
            fileStore = Ext.getStore("RequestFiles"),
            itemStore = Ext.getStore("RequestItems"),
            vendorStore = Ext.getStore("RequestVendors");

        //load data for each tab
        fileStore.proxy.url = "/empbc/v1/requests/" + reqId + "/attachments";
        itemStore.proxy.url = "/empbc/v1/requests/" + reqId + "/items";
        vendorStore.proxy.url = "/empbc/v1/vendors/" + reqId;
        fileStore.load();
        itemStore.load();
        vendorStore.load();
    },

    //return a promise with a bankcard request that can be chained for various senarioes
    loadBcpRequest: function (requestId) {
        return new Promise(function (resolve, reject) {
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + requestId,
                method: "GET",
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        var bcpRequest = result.data;
                        if (!bcpRequest) {
                            // Resolve with null if no DC found
                            resolve(null);
                        } else {
                            resolve(Ext.create("bcp.model.BcpRequest", bcpRequest));
                        } // Resolve the promise with the bcpRequest object
                    });
                },
                failure: function (response) {
                    // Reject the promise with an error message
                    reject(new Error("Failed to load request: " + response.error));
                }
            });
        });
    },

    //create a Division Chief route record
    prepareDcRecord: function (req, loggedInUserDivId) {
        var me = this,
            reqId = req.get("requestId"),
            //find the current route record
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                statusId: 17, // Status ID for 'Pending DC Approval'
                typeId: 14, // Type ID for 'Sent to DC'
                routeTo: req.get("divisionChiefId"),
                routeToName: req.get("dcName"),
                notes: "Routing for mission critical Division Chief approval."
            });

        // Return a new Promise
        return new Ext.Promise(function (resolve, reject) {
            //issue 625, DivisionChiefs store use logged in user or detailee's division to get the data
            //however, users can prepare a request for anyone within their ou, so the requester can be
            //someone outside the logged in user or detailee's division. for mission critial approval, the DC
            //should be the DC of the official requester
            //figure out who's the DC based on the requester's division
            var requesterId = req.get("requesterId"),
                requester = Ext.getStore("OuEmployees").findRecord("peopleId", requesterId),
                //app admin detailed in another ou but make a request for self will not find requester in OuEmployees
                //this senario shouldn't happen though
                requesterDivId = requester != null ? requester.get("divId") : loggedInUserDivId,
                divCode = Ext.getStore("Divisions").findRecord("divisionId", requesterDivId).get("code");
            //debugger;
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + divCode + "/divisionChiefs",
                method: "GET",
                scope: me,
                jsonData: Ext.encode(req.data),
                failure: function (response) {
                    //use reject to signal an error
                    reject(response);
                },
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        //should return an array of one record
                        let dc = result.data[0];
                        if (!dc) {
                            // Resolve with null if no DC found
                            resolve(null);
                        } else {
                            //only one dc per division, get that and assign it to the routeTo
                            newRecord.set("routeTo", dc.peopleId);
                            newRecord.set("routeToName", dc.fullName);
                            // Resolve with the fully populated newRecord
                            resolve(newRecord);
                        }
                    });
                }
            });
        });
    },

    //create a mission critical director approver (DR) route record
    prepareDrRecord: function (req, loggedInUserOuId) {
        var me = this,
            reqId = req.get("requestId"),
            //find the current route record
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                statusId: 18, // Status ID for 'Pending DR Approval'
                typeId: 15, // Type ID for 'Sent to DR'
                routeTo: 0,
                routeToName: "",
                notes: "Routing for mission critical Director approval."
            });

        // Return a new Promise
        return new Ext.Promise(function (resolve, reject) {
            var requesterId = req.get("requesterId"),
                requester = Ext.getStore("OuEmployees").findRecord("peopleId", requesterId),
                //app admin detailed in another ou but make a request for self will not find requester in OuEmployees
                //this senario shouldn't happen though
                requesterOuId = requester != null ? requester.get("ouId") : loggedInUserOuId,
                ouCode = Ext.getStore("Ous").findRecord("ouId", requesterOuId).get("code");
            //debugger;
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + ouCode + "/missionCritialDrApprover",
                method: "GET",
                scope: me,
                jsonData: Ext.encode(req.data),
                failure: function (response) {
                    //use reject to signal an error
                    reject(response);
                },
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        //should return an array of one record
                        let dr = result && result.data ? result.data[0] : null;
                        if (!dr) {
                            // Resolve with null if no DR found
                            resolve(null);
                        } else {
                            //only one dr per division, get that and assign it to the routeTo
                            newRecord.set("routeTo", dr.peopleId);
                            newRecord.set("routeToName", dr.fullName);
                            // Resolve with the fully populated newRecord
                            resolve(newRecord);
                        }
                    });
                }
            });
        });
    },

    /**
     * function to make sequential AJAX POST requests to the /routes endpoint.
     * It processes an array of route data objects one by one. If any request fails,
     * the sequence stops, and an error is shown. If all succeed, a success message
     * is displayed, and the user is redirected.
     *
     * @param {Object} config - Configuration object for the AJAX requests.
     * @param {Object} config.me - The scope (e.g., 'this' in the controller).
     * @param {Array<Object>} config.jsonDataArray - An array of data objects to send. Each object represents a route to be created.
     * @param {string} config.successMsg - The success message to display after all requests complete.
     * @param {string} [config.redirectTo='dashboard'] - The route to redirect to after success (optional, defaults to 'dashboard').
     * @param {string} config.failMsg - The base failure message to display if any request fails.
     *
     */
    makeRoutingAjaxRequest: function (config) {
        var me = config.me,
            jsonDataArray = config.jsonDataArray,
            successMsg = config.successMsg,
            failMsg = config.failMsg,
            redirectTo = config.redirectTo || "dashboard", // Default redirect
            url = bcp.config.Runtime.getServerBaseUrl() + "routes",
            method = "POST";

        // Ensure jsonDataArray is an array
        if (!Ext.isArray(jsonDataArray) || jsonDataArray.length === 0) {
            console.error("makeRoutingAjaxRequest: jsonDataArray is missing or empty.");
            bcp.util.CommonUtil.showError("No route data provided to send.");
            return;
        }

        // Use a recursive function with Promises to handle sequential requests
        function sendRequest(index) {
            if (index >= jsonDataArray.length) {
                // All requests succeeded
                bcp.util.CommonUtil.showAlert("Success", successMsg);
                me.redirectTo(redirectTo, true);
                return Promise.resolve(); // Indicate success
            }

            var currentJsonData = jsonDataArray[index];

            // Return the promise from makeAjaxRequest
            return bcp.util.CommonUtil.makeAjaxRequest({
                url: url,
                method: method,
                jsonData: Ext.encode(currentJsonData.data), // Send the current data object
                scope: me
            })
                .then(function (response) {
                    // Wrap the success handler in a promise to handle potential errors within it
                    return new Promise(function (resolve, reject) {
                        try {
                            // Successfully processed this request, move to the next one
                            resolve(sendRequest(index + 1));
                        } catch (handlerError) {
                            // Error occurred within ajaxSuccessHandler (e.g., JSON parsing)
                            reject({
                                response: response,
                                error: handlerError,
                                message: "Error processing server response for route " + (index + 1)
                            });
                        }
                    });
                })
                .catch(function (response) {
                    // Catch errors from makeAjaxRequest (network issues, non-2xx status)
                    // or errors propagated from the success handler's promise rejection
                    var errorText = "An unknown error occurred.";
                    if (response && response.statusText) {
                        errorText = response.statusText;
                    } else if (response && response.message) {
                        // Handle errors caught within the success handler promise
                        errorText = response.message;
                        console.error("Error details:", response.error);
                    }
                    bcp.util.CommonUtil.showAlert(failMsg + " (Route " + (index + 1) + ")", errorText);
                    // Explicitly reject to stop the promise chain
                    return Promise.reject(failMsg + " failed.");
                });
        }

        // Start the sequence
        return sendRequest(0);
    }
});
