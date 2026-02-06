/*
 * File: app/view/RequestSearchingViewController.js
 *
 */
Ext.define("bcp.view.RequestSearchingViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.requestsearching",

    showView: function (view) {
        var model = this.getViewModel(),
            layout = this.getView().getLayout(),
            refs = this.getReferences(),
            list = refs.list,
            reqList = refs.requestList,
            detail = refs.detail,
            editableDetail = refs.editableDetail;

        if (view === "list") {
            reqList.setSelection(null);
            model.set(
                "record",
                new bcp.model.BcpRequest({
                    requestId: 0
                })
            );
            //show list
            layout.setActiveItem(list);
        } else {
            if (view === "detail") {
                //show first tab
                detail.setActiveItem(0);
                //show detail
                layout.setActiveItem(detail);
            } else {
                //MB-186
                //show first tab
                editableDetail.setActiveItem(0);
                //show detail
                layout.setActiveItem(editableDetail);
            }
        }

        model.set("viewState", view);
    },

    onBackToList: function (button, e, eOpts) {
        var refs = this.getReferences();
        refs.requestList.setSelection(null);
        if (refs.btnEdit) refs.btnEdit.disable();
        //not doing redirect because we want to go back to the list with search results
        this.showView("list");
    },

    onViewDetail: function (button, e, eOpts) {
        var refs = this.getReferences(),
            rec = this.getViewModel().get("record");
        // Use the shared function
        if (rec) {
            if (refs.detail) refs.detail.destroy();
            bcp.util.CommonFunctions.setupReadOnlyRequestDetailView(rec, this, this.getView(), true);
        }
        //show editable views
        this.showView("detail");
    },

    onEdit: function (button, e, eOpts) {
        var refs = this.getReferences(),
            rec = this.getViewModel().get("record");
        // Use the shared function
        if (rec) {
            if (refs.editableDetail) refs.editableDetail.destroy();
            bcp.util.CommonFunctions.setupEditableRequestDetailView(rec, this, this.getView());
        }

        //show editable views
        this.showView("editableDetail");
    },

    onReassign: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            status = record.get("statusCode"),
            store = {},
            formWindow = null,
            reqId = record.get("requestId"),
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId
            });

        if (record.get("isDynamic") == 1) {
            bcp.util.CommonUtil.showWarning(
                "This request was dynamically routed (outside of the normal approval chain) and cannot be reassign now."
            );
            return;
        }

        if (
            !bcp.util.CommonUtil.isUserInRole([
                "Bankcard Approving Official",
                "Administrative Officer",
                "Bankcard Holder",
                "Funds Certifying Official",
                "App Admin"
            ])
        ) {
            bcp.util.CommonUtil.showWarning(
                "Only Bankcard Approving Officials, Funds Certifying Official, Bankcard Holders and AOs can reassign a request."
            );
        } else {
            if ([5, 6, 7, 8, 16].includes(status)) {
                //&& status != 12
                //decide the people showing in the approver combo
                if (status === 5) {
                    store = Ext.getStore("Reviewers");
                } else if (status === 6) {
                    store = Ext.getStore("Baos");
                } else if (status === 7 || status === 8) {
                    store = Ext.getStore("BankcardHolders");
                } else if (status === 16) {
                    store = Ext.getStore("AllFcos");
                }

                //set route type to "reroute"
                newRecord.set("typeId", 9);
                //reroute to the approver in the same step
                //e.g. if in review step, reroute to another reviewer to review
                newRecord.set("statusId", status);

                //create route window
                formWindow = Ext.create("widget.routewindow", {
                    store: store
                });
                //set record
                formWindow.lookupController().getViewModel().set("record", newRecord);

                //set original request record
                formWindow.lookupController().getViewModel().set("req", record);

                //load record
                formWindow.lookupController().lookupReference("form").getForm().loadRecord(newRecord);
                //set event listeners
                formWindow.on({
                    cancelRoute: {
                        fn: function (win, data) {
                            formWindow.destroy();
                        },
                        scope: this,
                        single: true
                    }
                });
                formWindow.on({
                    routed: {
                        fn: function (win, data) {
                            formWindow.destroy();
                            //send user to home view
                            //this.redirectTo('#dashboard');
                            //refresh the search view with this request to reflect the changes
                            this.redirectTo("requestsearching/" + reqId, true);
                        },
                        scope: this,
                        single: true
                    }
                });
                //add window to view
                this.view.add(formWindow);
                //show window
                formWindow.show();
            } else {
                Ext.Msg.alert("warning", "Only requests currently in the approval process can be reassigned.");
            }
        }
    },

    onRouteBackRequest: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            currentStatus = record.get("statusCode");

        // we can route back only when in these statuses (not supposed for 9 and 13 but BCHs may need to route back for weird situations)
        if ([6, 7, 8, 9, 13, 16, 17, 18].includes(currentStatus)) {
            var rbwindow = Ext.create("widget.windows.routeback", {
                reference: "rbwindow"
            });

            //add to the parent view
            this.view.add(rbwindow);

            // Show the window
            rbwindow.show();
        } else {
            Ext.Msg.alert(
                "Warning",
                "You cannot route back a request in this status: <b>[" + record.get("routeTypeName") + "]</b>"
            );
        }
    },

    onPullBackRequest: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            reqId = record.get("requestId"),
            currentStatus = record.get("statusCode");

        if (currentStatus === 1 || currentStatus === 12) {
            Ext.Msg.alert("Warning", "You cannot pull back a request in its initial step.");
            return;
        }

        // Ask user to confirm this action
        Ext.Msg.confirm(
            "Pull Back Request Confirmation",
            "Are you sure you want to pull back this request to its previous step?",
            function (result) {
                // User confirmed yes
                if (result == "yes") {
                    Ext.Ajax.request({
                        url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId + "/pullBackRoute",
                        method: "PUT",
                        scope: this,
                        success: function (response) {
                            try {
                                if (Ext.decode(response.responseText).success) {
                                    Ext.Msg.alert("Success", "Your request was pulled back successfully.");
                                } else {
                                    Ext.Msg.alert("Failed", Ext.decode(response.responseText).error.description);
                                }
                            } catch (ex) {
                                Ext.Msg.alert("Failed", "Unexpected Error happened.");
                            }
                            //bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                            //    Ext.Msg.alert('Success', 'Your request was pulled back successfully.');
                            //});
                        }
                    });
                }
            }
        );
    },

    onDescription: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            reqId = record.get("requestId"),
            description = record.get("description");

        var cwindow = Ext.create("widget.descriptionwindow", {
            reference: "descriptionwindow"
        });
        //set event listeners
        cwindow.on({
            cancelDescription: {
                fn: function (data) {
                    cwindow.destroy();
                },
                scope: this,
                single: true
            }
        });
        cwindow.on({
            saveDescription: {
                fn: function (data) {
                    record.set("description", data);
                    Ext.Ajax.request({
                        url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId + "/description",
                        method: "PUT",
                        scope: this,
                        jsonData: Ext.encode({
                            description: data
                        }),

                        success: function (response) {
                            //store.reload();
                            cwindow.destroy();
                        },
                        failure: function (response) {
                            //store.reload();
                            cwindow.destroy();
                        }
                    });
                },
                scope: this,
                single: true
            }
        });

        //add to the parent view
        this.view.add(cwindow);

        //MB-388
        if (Ext.getCmp("description") != undefined) Ext.getCmp("description").setValue(description);

        // Show the window
        cwindow.show();
    },

    //update FY
    saveFy: function (reqId, data, record, cwindow) {
        if (data == record.get("fy")) {
            cwindow.destroy();
            Ext.Msg.alert("Warning", "The request's FY is " + data + ". No update needed.");
        } else {
            // Update the record with new fy
            record.set("fy", data);
            bcp.util.CommonUtil.makeAjaxRequest({
                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId + "/requisitionNum",
                method: "PUT",
                jsonData: Ext.encode(record.data)
            })
                .then(function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        if (result.requisitionNumber) {
                            record.set("requisitionNumber", result.requisitionNumber);
                            cwindow.destroy();
                            Ext.Msg.alert(
                                "Success",
                                "The request's FY was updated. The new Requisition Number is " +
                                    result.requisitionNumber
                            );
                        } else {
                            Ext.Msg.alert("Failure", "The request was not updated.");
                        }
                    });
                })
                .catch(function (error) {
                    cwindow.destroy();
                    Ext.Msg.alert("Failure", "The request was not updated.");
                });
        }
    },

    onFy: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            reqId = record.get("requestId");

        var cwindow = Ext.create("widget.requestfywindow", {
            reference: "fywindow"
        });

        // Set event listeners
        cwindow.on({
            cancelFyChange: {
                fn: function (data) {
                    cwindow.destroy();
                },
                scope: this,
                single: true
            },
            applyFyChange: {
                fn: function (data) {
                    this.saveFy(reqId, data, record, cwindow);
                },
                scope: this,
                single: true
            }
        });

        // Add to the parent view
        this.view.add(cwindow);

        // Show the window
        cwindow.show();
    },

    //save BCH comments
    saveBchComments: function (reqId, data, record, cwindow) {
        // Update the record with new comments
        record.set("bchComments", data);
        //prep data
        var comments = Ext.encode({
            bchComments: data
        });
        // Make AJAX request to save comments
        bcp.util.CommonUtil.makeAjaxRequest({
            url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId + "/bchComments",
            method: "PUT",
            jsonData: comments
        })
            .then(function (response) {
                cwindow.destroy();
            })
            .catch(function (error) {
                cwindow.destroy();
                Ext.Msg.alert("Failure", "The [BCH Comments] was not updated.");
            });
    },

    onComment: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record"),
            reqId = record.get("requestId"),
            bchComments = record.get("bchComments");

        // Create the comment window
        var cwindow = Ext.create("widget.bchcommentwindow", {
            reference: "bchcommentwindow"
        });

        // Set event listeners
        cwindow.on({
            cancelComment: {
                fn: function (data) {
                    cwindow.destroy();
                },
                scope: this,
                single: true
            },
            saveComment: {
                fn: function (data) {
                    this.saveBchComments(reqId, data, record, cwindow);
                },
                scope: this,
                single: true
            }
        });

        //add to the parent view
        this.view.add(cwindow);

        //MB-388
        if (Ext.getCmp("bchComments") != undefined) Ext.getCmp("bchComments").setValue(bchComments);

        // Show the window
        cwindow.show();
    },

    onExport: function (button, e, eOpts) {
        var grid = this.lookupReference("requestList");
        grid.saveDocumentAs({
            type: "xlsx",
            title: "SearchRequestExport",
            fileName: "SearchRequestResult.xlsx"
        });
    },

    onReload: function (button, e, eOpts) {
        this.onSearch();
    },

    onSearch: function (button, e, eOpts) {
        var store = this.getStore("searchedRequests"),
            refs = this.getReferences(),
            list = refs.requestList,
            ouId = refs.comboOu.value,
            divisionId = refs.comboDiv.value,
            groupId = refs.comboGrp.value,
            requesterId = refs.comboEmp.value,
            reqId = refs.reqIdTf.value,
            reqNum = refs.reqNumTf.value,
            fromDate = Ext.Date.format(this.lookupReference("dateFrom").value, "Y-m-d"),
            toDate = Ext.Date.format(this.lookupReference("dateTo").value, "Y-m-d"),
            requestStatusId = refs.comboReqStatus.value,
            vendorName = refs.tfVendor.value,
            transactionNum = refs.tfTransactionNum.value,
            itemName = refs.tfItemName.value,
            actualTotal = refs.nfActualTotal.value,
            bchId = refs.comboBch.value,
            reviewerId = refs.comboReviewers.value,
            fy = refs.comboFy.value,
            ptc = refs.comboPtc.value,
            partialOrder = refs.cbPartialOrder.value,
            taggable = refs.cbTaggable.value,
            delivToHome = refs.cbDelivToHome.value,
            //isItPurchase = refs.cbIsItPurchase.value,
            purchaseTypeId = refs.comboPurchaseType.value,
            itemStatuses = refs.tagItemStatus.value,
            stmtDate = Ext.Date.format(refs.dfStatementDate.value, "Y-m-d"),
            url = "/empbc/v1/requests?";

        //add longer timeout(3min) because some search may return a lot records taking very long time
        store.proxy.setTimeout(180000);
        //requestStatusId=10&ouId=13204&divisionId=13225&requesterId=23826
        //&fromDate=2017-05-30&toDate=2017-06-06&groupId=13316
        if (delivToHome) {
            url += "delivToHome=" + delivToHome + "&";
        }

        if (partialOrder) {
            url += "partialOrder=" + partialOrder + "&";
        }

        if (taggable) {
            url += "taggable=" + taggable + "&";
        }

        if (itemStatuses) {
            url += "itemStatuses=" + itemStatuses + "&";
        }

        //if (isItPurchase) {
        //  url += "isItPurchase=" + isItPurchase + "&";
        //}

        if (purchaseTypeId) {
            url += "purchaseTypeId=" + purchaseTypeId + "&";
        }

        if (vendorName && vendorName.trim().length > 0) {
            url += "vendorName=" + vendorName + "&";
        }

        if (transactionNum && transactionNum.trim().length > 0) {
            url += "transactionNum=" + transactionNum + "&";
        }

        if (itemName && itemName.trim().length > 0) {
            url += "itemName=" + itemName + "&";
        }

        if (actualTotal) {
            url += "actualTotal=" + actualTotal + "&";
        }

        if (bchId) {
            url += "bchId=" + bchId + "&";
        }

        if (reviewerId) {
            url += "reviewerId=" + reviewerId + "&";
        }

        if (fy) {
            url += "fy=" + fy + "&";
        }

        if (ptc) {
            url += "ptc=" + ptc + "&";
        }

        if (stmtDate) {
            url += "statementDate=" + stmtDate + "&";
        }

        if (ouId) {
            url += "ouId=" + ouId + "&";
        }
        if (divisionId) {
            url += "divisionId=" + divisionId + "&";
        }
        if (groupId) {
            url += "groupId=" + groupId + "&";
        }
        if (requesterId) {
            url += "requesterId=" + requesterId + "&";
        }
        if (reqId) {
            url += "requestId=" + reqId + "&";
        }
        if (reqNum) {
            reqNum = bcp.util.CommonUtil.removeSafetySuffix(reqNum);
            url += "requisitionNumber=" + reqNum + "&";
        }
        if (fromDate && fromDate !== "") {
            url += "fromDate=" + fromDate + "&";
        }
        if (toDate && toDate !== "") {
            url += "toDate=" + toDate + "&";
        }
        if (requestStatusId) {
            url += "routeTypeId=" + requestStatusId + "&";
        }

        //set url for store
        store.getProxy().setUrl(url);
        //do a search based on user selected criteria
        store.load();
        //clear grid selection
        list.setSelection(null);
        //hide route history
        refs.historyview.hide();
    },

    onReset: function (button, e, eOpts) {
        Ext.suspendLayouts();

        var refs = this.getReferences(),
            tb1 = refs.filterTb1,
            tb2 = refs.filterTb2,
            tb3 = refs.filterTb3,
            tb4 = refs.filterTb4,
            tb5 = refs.filterTb5,
            tb6 = refs.filterTb6,
            fields = tb1.items.items,
            f,
            fLen = fields.length;

        //toolbar one has some special rules for reset fields
        for (f = 0; f < fLen; f++) {
            //prevent reset to clean FY and OU
            if (
                fields[f].xtype !== "button" &&
                fields[f].reference !== "comboFy" &&
                fields[f].reference !== "comboOu"
            ) {
                fields[f].reset();
            }
        }

        //other toolbars can use generic reset
        this.resetTbFields(tb2);
        this.resetTbFields(tb3);
        this.resetTbFields(tb4);
        this.resetTbFields(tb5);
        this.resetTbFields(tb6);

        //BANK-567
        this.setupCombos();

        Ext.resumeLayouts(true);
    },

    //reset fields in a toolbar
    resetTbFields: function (tb) {
        var fields = tb.items.items,
            fLen = fields.length;

        for (var f = 0; f < fLen; f++) {
            if (fields[f].xtype !== "button") {
                fields[f].reset();
            }
        }
    },

    onSelect: function (rowmodel, record, index, eOpts) {
        var model = this.getViewModel(),
            btnReassign = this.lookupReference("btnReassign"),
            btnEdit = this.lookupReference("btnEdit"),
            loggedInUser = model.get("loggedInUser"),
            status = record.get("statusCode");
        //if reroute is there
        if (btnReassign) {
            //cannot reroute a request that is already received/archived
            if ([5, 6, 7, 16].includes(status)) {
                btnReassign.enable();
            } else {
                btnReassign.disable();
            }
        }

        //allow edit if the bch is the bch for the request or bao is the bao for the request or fco is the fco for the request
        //and the request is saved, prepared, submitted, reviewed, approved, ordered
        if (btnEdit) {
            if (
                ((record.get("bankcardHolderId") && record.get("bankcardHolderId") == loggedInUser.peopleId) ||
                    (record.get("bankcardApprovingOfficialId") &&
                        record.get("bankcardApprovingOfficialId") == loggedInUser.peopleId) ||
                        (record.get("fundsCertifyingOfficialId") &&
                        record.get("fundsCertifyingOfficialId") == loggedInUser.peopleId) ||
                    bcp.util.CommonUtil.isUserInRole(["App Admin"])) &&
                [1, 3, 5, 6, 7, 8, 12].includes(status)
            ) {
                btnEdit.enable();
            } else {
                btnEdit.disable();
            }
        }

        this.getReferences().historyview.getController().loadHistoryById(record.get("requestId"));
    },

    onRouteHistoryLoaded: function (panel) {
        //seems not needed after app container layout change
        /*
        var model=this.getViewModel(),
        refs=this.getReferences();
        refs.requestList.getView().focusRow(model.get('record'));*/
    },

    onUpdateReqData: function (button, e, eOpts) {
        //no action if user didn't select a item in the menu to update certain data of the request
    },

    //BANK-567
    //many combos in the toolbars need to limit data set and set default values
    setupCombos: function () {
        var model = this.getViewModel(),
            loggedInUser = model.get("loggedInUser"),
            ouStore = this.getStore("ous"),
            divStore = this.getStore("divisions"),
            grpStore = this.getStore("groups"),
            comboEmp = this.lookupReference("comboEmp"),
            ouEmpStore = this.getStore("ouEmployees"),
            //divEmpStore = this.getStore('divEmployees'),
            ouId = 0,
            divId = 0,
            grpId = 0;

        //detailee mode value setup
        if (loggedInUser.detaileeMode == true) {
            var detailee = Ext.getStore("Detailees").first();
            ouId = detailee.get("ouId");
            divId = detailee.get("divisionId");
            grpId = detailee.get("groupId");
        } else {
            ouId = loggedInUser.ouId;
            divId = loggedInUser.divisionId;
            grpId = loggedInUser.groupId;
        }

        //---------------------- limit org data combo to one OU------------------
        if (!bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
            ouStore.addFilter({
                operator: "==",
                property: "ouId",
                value: ouId
            });

            divStore.addFilter({
                operator: "==",
                property: "ouId",
                value: ouId
            });

            grpStore.addFilter({
                operator: "==",
                property: "ouId",
                value: ouId
            });
        }
        //--------------------------------------------------------------------------

        //default ou to user's ou
        this.lookupReference("comboOu").setValue(ouId);

        //----------------------check user roles and limit data to one division if applies---------------
        //(check LkUserRoles store for roles defined in NIST Org)
        if (
            !bcp.util.CommonUtil.isUserInRole([
                //these roles can access ou level data
                "Director",
                "Deputy Director",
                "Administrative Officer",
                "Administrative Office Assistant", //MB-367 moved up from a lower level
                "Administrative Specialist",
                "Senior Management Advisor",
                "Funds Certifying Official",
                "Laboratory Office Manager",
                "Division Office Manager",
                "Group Office Manager",
                "Property Custodian",
                "ITSO",
                "DITSO",
                "Bankcard Approving Official"
            ])
        ) {
            //limit to user's division only
            divStore.addFilter({
                operator: "==",
                property: "divisionId",
                value: divId
            });

            grpStore.addFilter({
                operator: "==",
                property: "divisionId",
                value: divId
            });

            //this is the only place that we use divEmp store
            //since we have org data in the ouallstaff store, we will use it as the base
            //for ouEmp store and then use ouEmp to create a chainstore for divEmp
            //this way, we don't need to get data from the backend for 3 separate stores
            var divEmpChainedStore = Ext.create("Ext.data.ChainedStore", {
                source: ouEmpStore,
                filters: [
                    {
                        property: "divisionId",
                        value: divId
                    }
                ]
            });

            //limit to division employee only using the chained store
            comboEmp.bindStore(divEmpChainedStore);

            //limit to division employee only
            //comboEmp.bindStore(divEmpStore);

            //if user doesn't have ou access, default div to user's div
            this.lookupReference("comboDiv").setValue(divId);
        } else {
            //user have ou level access, make all OU employee available
            comboEmp.bindStore(ouEmpStore);
        }

        //detailee mode is based on group(select a group from the top menu group combo), if on, set default value for division and group combo
        if (loggedInUser.detaileeMode == true) {
            this.lookupReference("comboDiv").setValue(divId);
            this.lookupReference("comboGrp").setValue(grpId);
        }
    },

    onViewAdded: function (component, container, pos, eOpts) {
        var currentYear = new Date().getFullYear(), //yr in 4 digits
            currentFy = Number(currentYear.toString().substr(-2)), //yr in 2 digits
            createNextFyEndDate = new Date(new Date().getFullYear() + "-10-01T00:00:00-04:00");

        //load item statuses for search
        Ext.getStore("LkItemStatus").load();

        //if today is 10/1 or later, FY is in next FY
        if (new Date() >= createNextFyEndDate) {
            currentFy++;
        }

        //MB-464
        this.lookupReference("comboFy").setValue(currentFy);

        //BANK-567
        this.setupCombos();

        //added bch MB-299
        if (
            !bcp.util.CommonUtil.isUserInRole([
                "Bankcard Approving Official",
                "Administrative Officer",
                "Funds Certifying Official",
                "Bankcard Holder",
                "App Admin"
            ])
        ) {
            this.lookupReference("btnReassign").destroy();
        }

        //BAOs and BHs can edit actuals and update item status, note, received date
        if (
            !bcp.util.CommonUtil.isUserInRole([
                "Bankcard Holder",
                "Bankcard Approving Official",
                "Funds Certifying Official",
                "App Admin"
            ])
        ) {
            this.lookupReference("btnComment").destroy();
            this.lookupReference("btnEdit").destroy();
            this.lookupReference("btnDescription").destroy();
        }

        //handle hash url that contains the request Id
        if (component.requestId && component.requestId !== 0) {
            this.lookupReference("reqIdTf").setValue(component.requestId);
            this.onSearch();
        }
    },

    //MB-381
    onOUChange: function (ele, newValue, oldValue) {
        var divStore = this.getStore("divisions");
        var grpStore = this.getStore("groups");

        if (newValue != undefined && newValue != null) {
            divStore.addFilter({
                operator: "==",
                property: "ouId",
                value: newValue
            });

            grpStore.addFilter({
                operator: "==",
                property: "ouId",
                value: newValue
            });
        } else {
            divStore.clearFilter();
            grpStore.clearFilter();
        }
    },

    onDivChange: function (ele, newValue, oldValue) {
        var grpStore = this.getStore("groups");

        if (newValue != undefined && newValue != null) {
            grpStore.addFilter({
                operator: "==",
                property: "divisionId",
                value: newValue
            });
        } else {
            grpStore.clearFilter();
        }
    }
});
