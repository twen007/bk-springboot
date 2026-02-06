Ext.define("bcp.view.RouteWindowViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.routewindow",

    onApproveCheckboxChange: function (field, newValue, oldValue, eOpts) {
        var btn = this.lookupReference("approveBtn");
        if (newValue && newValue === true) {
            btn.enable();
        } else {
            btn.disable();
        }
    },

    cancel: function (button, e, eOpts) {
        this.fireViewEvent("cancelRoute");
    },

    proceed: function (button, e, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form.getForm(),
            record = form.getRecord(),
            typeId = record.get("typeId"),
            routeDataArray = model.get("routeDataArray"),
            url = "",
            me = this;

        //add a progress window to prevent double clicks that will insert duplicate route records and may trigger duplicate IBBR API calls(if "Purchased")
        Ext.MessageBox.show({
            title: "Processing",
            progressText: "Processing...please wait",
            width: 300,
            progress: true,
            closable: false
        });

        // Update associated record with values
        //weird problem where routeTo value is somehow changed after form was shown
        //good thing is the record is not changed
        //form.updateRecord();
        //var data = record.getData();

        if (refs.notes) {
            record.set("notes", refs.notes.getValue());
        } else if (refs.rejectNotes) {
            record.set("notes", refs.rejectNotes.getValue());
        }

        if (refs.routeToCombo) {
            record.set("routeTo", refs.routeToCombo.getValue());
        }

        //after this approval, the req is back to an approver in the fixed route
        //so we don't want set it in dynamic anymore. if rerouteStack is not 0,
        //it means there are still dynamic approvals pending, so it is still dynamic
        if (record.data.isDynamic == 1 && record.data.rerouteStack == 0 && record.data.orgRerouteStack == 0) {
            record.set("isDynamic", 0);
        }

        //BANK-505
        var pids;
        if (refs.comboShare) {
            pids = refs.comboShare.getValue();
            if (pids && pids.length > 0) record.set("alsoNotify", pids.join(","));
        }

        //MB-364, 461
        if (typeId == 9) {
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + "routes/reassign",
                method: "POST",
                jsonData: Ext.encode(record.data),
                scope: this,
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        bcp.util.CommonUtil.showAlert("Success", "Your request reassigned successfully.");
                        me.fireViewEvent("routed", this.result);
                    });
                },
                failure: function (form, response) {
                    bcp.util.CommonUtil.showAlert("Re-Assign Failed", response.result.statusText);
                }
            });
        } else {
            let itsoRec = model.get("itsoRecord");
            if (itsoRec) {
                url = bcp.config.Runtime.getServerBaseUrl() + "routes";
                //don't notify the reviewer yet since code will act as the reviewer and dynamic route to the ITSO
                //and the ITSO need to approve the request first before the request can be routed back to the reviewer
                record.set("omitNotification", 1);
                Ext.Ajax.request({
                    url: url,
                    method: "POST",
                    jsonData: Ext.encode(record.data),
                    scope: this,
                    success: function (response) {
                        //BANK-573 use the ajaxSuccessHandler, which check for error code, display error message and stop executing the rest of the code;
                        //if not used, as long as the server side returns a 200, the next request will be posted even when the
                        //first post failed.
                        //NOTE: double approval code above was also updated with the use of ajaxSuccessHandler
                        bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                            //after the reviewed route is inserted, do ITSO route
                            Ext.Ajax.request({
                                url: url,
                                method: "POST",
                                jsonData: Ext.encode(itsoRec.data),
                                scope: this,
                                success: function (response) {
                                    //handle response
                                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                        bcp.util.CommonUtil.showAlert("Success", "Your request routed successfully.");
                                        me.fireViewEvent("routed", this.result);
                                    });
                                }
                            });
                        });
                    }
                });
            } else {
                //normal route
                if (routeDataArray) {//has multi routes to insert
                    bcp.util.CommonFunctions.makeRoutingAjaxRequest({
                        me: me, // Pass the controller scope
                        jsonDataArray: routeDataArray, // The array of route data
                        successMsg: "Request successfully routed.",
                        failMsg: "Routing sequence failed"
                    }).then(function () {
                        me.fireViewEvent("routed", this.result);
                    });
                } else {
                    //has single route to insert
                    Ext.Ajax.request({
                        url: bcp.config.Runtime.getServerBaseUrl() + "routes",
                        method: "POST",
                        jsonData: Ext.encode(record.data),
                        scope: this,
                        success: function (response) {
                            bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                bcp.util.CommonUtil.showAlert("Success", "Your request routed successfully.");
                                me.fireViewEvent("routed", this.result);
                            });
                        },
                        failure: function (form, response) {
                            bcp.util.CommonUtil.showAlert("Route Failed", response.result.statusText);
                        }
                    });
                }
            }
        }
    },

    approve: function (button, e, eOpts) {
        this.proceed();
    },

    onWindowBeforeShow: function (component, eOpts) {
        var refs = this.getReferences(),
            form = refs.form.getForm(),
            record = form.getRecord(),
            //current request
            req = this.getViewModel().get("req");
        //decide showing approval or reject note
        if (record.data.typeId === 5) {
            refs.notes.destroy();
        } else {
            refs.rejectNotes.destroy();
        }
        //issue 585: make sure the fund certify checkbox appear when FCO approves a request and the approval button is disabled unless the FCO checked the checkbox
        //for normal non BAO approval or a dynamic route approval, don't show certifyCb checkbox
        //also, FCO reassign should not show the checkbox
        //issue 720: if the request is rejected or returned by the FCO, don't show the certifyCb checkbox
        if (req.data.statusCode !== 16 || record.data.typeId === 9 || record.data.typeId === 5 || record.data.typeId === 13)  {
            //current status = 16 means the FCO is acting on the req
            refs.certifyCb.disable(); 
            refs.certifyCb.hide();
            refs.approveBtn.hide();
            refs.actionBtn.show();
            //check if original request was set in the route window's model (this is used to prevent some cases in the route view for dynamic routing, routings
            //happened in other views such as search view or purchase view may not set the original request
            if (req == null || (req && req.data.isDynamic == 1)) {
                //for dynamic approval, since certifyCb is not there, make sure the [approve]
                //button is enabled
                refs.approveBtn.enable();
            }
        } else {
            refs.certifyCb.enable();
            refs.certifyCb.show();
            refs.approveBtn.show();
            refs.actionBtn.hide();
        }
    },

    onWindowAdded: function (component, container, pos, eOpts) {
        var refs = this.getReferences(),
            form = refs.form.getForm(),
            record = form.getRecord();
        //for reroute, rebind the combobox with correct store
        if (record.data.typeId === 9) {
            this.lookupReference("routeToCombo").bindStore(this.view.store);
        }
    },

    removeRouteToCombo: function () {
        var refs = this.getReferences();
        refs.routeToCombo.destroy();
    }
});
