/*
handles dynamic routings [reroute] or [approve & add a route]
 */
Ext.define("bcp.view.DynamicRouteWindowViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.dynamicroutewindow",

    cancel: function (button, e, eOpts) {
        this.fireViewEvent("cancelRoute");
    },

    //additional route
    proceed: function (button, e, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form.getForm(),
            record = form.getRecord(),
            req = model.get("req"),
            method = "POST",
            url = bcp.config.Runtime.getServerBaseUrl() + "routes",
            me = this;
        // Update associated record with values
        form.updateRecord();

        if (refs.certifyCb.isVisible() && !refs.certifyCb.getValue() == true) {
            bcp.util.CommonUtil.showAlert(
                "Validation Error",
                "Please certify the funds by checking the checkbox before Approving."
            );
            return;
        }

        //one more check here in case there are multiple FCOs and the user changed the FCO here to the same person
        const checkBaoFcoSamePersonDate = new Date("2025-03-27");
        var submittedDate = req.get("submittedDate");
        if (submittedDate && submittedDate >= checkBaoFcoSamePersonDate && record.get("statusId")==16 && record.get("routeBy")==record.get("routeTo")){
            bcp.util.CommonUtil.showWarning(
                "Guidance effective March 27th, 2025, Funds Certifying Official and Bankcard Approving Official can no longer be the same individual. Please change the Funds Certifying Official before proceeding."
            );
            return;
        }

        if (model.get("isUpdate") === true) {
            method = "PUT";
            url = bcp.config.Runtime.getServerBaseUrl() + "routes/planned/" + record.get("routeId");
            //check if this update is a result of a ITSO approval
            if (model.get("itsoStep")) {
                record.data.isItsoApproval = true;
            }
        }

        //if reviewer and BAO is the same person (only AA would have a case like this)
        if (model.get("addTwoRoutes") && model.get("addTwoRoutes").doubleApprove) {
            //for double approves, this extra record is to add the reviewed route
            var extraRecord = model.get("extraRecord");

            Ext.Ajax.request({
                url: url,
                method: "POST",
                jsonData: Ext.encode(extraRecord.data),
                scope: this,
                success: function (response) {
                    //after the reviewed route is inserted, the record is the AA record
                    Ext.Ajax.request({
                        url: url,
                        method: "POST",
                        jsonData: Ext.encode(record.data),
                        scope: this,
                        success: function (response) {
                            bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                bcp.util.CommonUtil.showAlert("Success", "Your request routed successfully.");
                                me.fireViewEvent("routed", this.result);
                            });
                        }
                    });
                }
            });
        } else {
            Ext.Ajax.request({
                url: url,
                method: method,
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
    },

    onWindowBeforeShow: function (component, eOpts) {
        var refs = this.getReferences(),
            form = refs.form.getForm(),
            model = this.getViewModel(),
            record = form.getRecord();

        //for BAO do AA, need to show certifyCb checkbox
        //next route could be to a dynamic approver, then once approved, go to BCH
        //issue 585 note: if the division use explicit FCO approval, then the req will go to BAO, then to FCO
        //so we should not show the certifying checkbox for the
        //TODO: FCO won't be here after using fixed route. the order is also changed to FCO, then BAO
        //so debug here to see what changes is needed here
        if ("AA" == record.data.dynamicType && record.data.typeId == 2 && !model.get("needFcoRoute")) {
            refs.approveBtn1.disable();
            refs.certifyCb.enable();
            refs.certifyCb.show();
        } else {
            refs.approveBtn1.enable();
            refs.certifyCb.disable();
            refs.certifyCb.hide();
        }
    },

    onApproveCheckboxChange: function (field, newValue, oldValue, eOpts) {
        var refs = this.getReferences(),
            form = refs.form.getForm();
        form.updateRecord();
        /*var btn = this.lookupReference("approveBtn1");
        if (newValue && newValue === true) {
            btn.enable();
        } else {
            btn.disable();
        }*/
    },

    onWindowAdded: function (component, container, pos, eOpts) {
        var refs = this.getReferences(),
            form = refs.form.getForm(),
            record = form.getRecord();
        //make sure the combobox is unselected
        refs.comboAddiRouteEmp.reset();

        //bind employee store to the combo
        //NOTE: cannot do bind: {store: '{bcEmployees}'} directly in the combo definition
        //in the view. if do that, combo.bindStore(newstore) changes the data but the UI
        //still show old store's data somehow
        refs.comboAddiRouteEmp.bindStore(this.getStore("bcEmployees"));

        //show different msg for [reroute] or [approve & add a route]
        if (record.data.dynamicType === "DR") {
            refs.routeMsg.setValue(
                "Please select an employee below " +
                    "and reroute this request to the employee for an additional approval. <br><br>" +
                    "If approved, the request will be routed <b>back to you</b> for approval."
            );
        } else if (record.data.dynamicType === "AA") {
            refs.routeMsg.setValue(
                "Please select an employee below. <br><br>" +
                    "After your approval, this request will be routed to <b>this employee</b> " +
                    "for additional approval first before it reaches  " +
                    "<b>" +
                    record.data.routeToName +
                    "</b> for approval."
            );
        } else if (record.data.dynamicType === "ITSO") {
            //ITSO will be routed in routepanel directly since we know who is the ITSO
            //if in the future, we need to add an option for user to select a different ITSO from the possible holders returned from NIST Org
            //the code below maybe useful. So let's keep it here for now.
            var itsos = Ext.getStore("ItsoUsers");
            //change dropdown to use a list of ITSOs
            refs.comboAddiRouteEmp.bindStore(itsos);

            //find division ITSO and set the staff in the combo
            var ditso = itsos.findRecord("ditso", true);
            if (ditso) {
                record.data.routeToName = ditso.get("fullName");
                record.data.routeTo = ditso.get("peopeleId");
                //refs.comboAddiRouteEmp.setValue(ditso.get('peopeleId'));
                refs.routeMsg.setValue(
                    "This request is a IT Purchase, so it will be routed to the ITSO <b>" +
                        "<b>" +
                        record.data.routeToName +
                        "</b> for approval. Optionally, you can select a Backup ITSO from the list below if your division ITSO is not avaiable"
                );
            } else {
                var msg =
                    "This request is a IT Purchase, so it will be routed to the ITSO for approval." +
                    "However, your division ITSO was not setup in the NIST Org. Please contact your office manager to set it up.";
                if (itsos.count() > 0) {
                    msg += "<br>  Optionally, you can select a staff with the ITSO role from the list below.";
                }
                refs.routeMsg.setValue(msg);
            }

            //refs.comboAddiRouteEmp.hide();
            //refs.comboAddiRouteEmp.disable();
        } else {
            //must be executing a planned route
            refs.comboAddiRouteEmp.hide();
            refs.comboAddiRouteEmp.disable();
            refs.routeMsg.setValue(
                "This request will be routed to <b>" + record.data.routeToName + "</b> for approval."
            );
        }
    }
});
