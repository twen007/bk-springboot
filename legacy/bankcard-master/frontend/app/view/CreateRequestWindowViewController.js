/*
 * File: app/view/CreateRequestWindowViewController.js
 *
 */

Ext.define("bcp.view.CreateRequestWindowViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.createrequestwindow",

    onReqForChange: function (field, newValue, oldValue, eOpts) {
        var record = this.getViewModel().get("record"),
            requesterIdHf = this.lookupReference("requesterIdHf");

        if (newValue) {
            if (newValue.whoVal === "notme") {
                record.set("requesterId", -1);
                requesterIdHf.disable();
            } else {
                //make it the same as creator's id
                record.set("requesterId", record.get("creatorId"));
                requesterIdHf.enable();
            }
        }
    },

    onReqForComboChange: function (field, newValue, oldValue, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record");
        if (field.selection) {
            var sel = field.selection.data;

            //set requester and requestFor;
            record.set("requesterId", sel.peopleId);
            record.set("requesterName", sel.displayName);
        }
    },

    onTypeChange: function (field, newValue, oldValue, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record");
        if (field.selection) {
            var sel = field.selection.data;
            //set requester and requestFor;
            record.set("purchaseTypeId", sel.id);
            if (sel.id === 2) {
                record.set("isItPurchase", "Y");
            } else {
                record.set("isItPurchase", "N");
            }
        }
    },
    onPurchaseTypeAfterRender: function (field, eOpts) {
        // get the info icon element
        var infoIconEl = Ext.get("purchaseTypeInfoBtn");

        // add click listener to the info icon
        if (infoIconEl) {
            infoIconEl.on("click", function () {
                Ext.Ajax.request({
                    url: "resources/htmls/purchaseTypeDefinition.html", // Path to your HTML file
                    method: "GET",
                    success: function (response) {
                        var htmlContent = response.responseText;
                        // Create and open a new window with the HTML content
                        var popupWindow = window.open(
                            "",
                            "PurchaseTypeDefinition",
                            "width=600,height=250,resizable=yes,scrollbars=yes"
                        );
                        //popupWindow.document.write(htmlContent);
                        //popupWindow.document.close();
                        setTimeout(function() {
                            popupWindow.document.body.innerHTML = htmlContent;
                        }, 600);
                    },
                    failure: function (response) {
                        bcp.util.CommonUtil.showError("Failed to load Purchase Type Definition.");
                    }
                });
            });
        }
    },

    onMcCategoryChange: function (field, newValue, oldValue, eOpts) {},

    onMcCategoryAfterRender: function (field, eOpts) {
        // get the info icon element
        var infoIconEl = Ext.get("mcCategoryInfoBtn");

        // add click listener to the info icon
        if (infoIconEl) {
            infoIconEl.on("click", function () {
                Ext.Ajax.request({
                    url: "resources/htmls/missionCriticalDefinitions.html", // Path to your HTML file
                    method: "GET",
                    success: function (response) {
                        var htmlContent = response.responseText;
                        // Create and open a new window with the HTML content
                        var popupWindow = window.open(
                            "",
                            "MissionCriticalDefinitions",
                            "width=600,height=520,resizable=yes,scrollbars=yes"
                        );
                        //popupWindow.document.write(htmlContent);
                        //popupWindow.document.close();
                        setTimeout(function() {
                            popupWindow.document.body.innerHTML = htmlContent;
                        }, 600);
                    },
                    failure: function (response) {
                        bcp.util.CommonUtil.showError("Failed to load Purchase Type Definition.");
                    }
                });
            });
        }
    },

    onGroupComboChange: function (field, newValue, oldValue, eOpts) {
        var model = this.getViewModel(),
            record = model.get("record");
        if (field.selection) {
            var sel = field.selection.data;

            //set org data;
            record.set("ouId", sel.ouId);
            record.set("divisionId", sel.divisionId);
        }
    },

    /*  Test CORS
        onTest: function () {
        var url = 'https://mmltest.nist.gov/org/api/v1/employees/23826/roles';
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            scope: this,
            success: function (response) {
               

            },
            failure: function (response) {
                
            }
        });
    },*/

    onSave: function (button, e, eOpts) {
        var refs = this.getReferences(),
            model = this.getViewModel(),
            form = refs.form,
            user = model.get("loggedInUser"),
            userId = user.peopleId,
            formData = {},
            rec = {},
            method = "POST",
            url = "",
            routeStore = Ext.getStore("DefaultRoutes"),
            ouRoles = Ext.getStore("OuRoles"),
            defaultRouteModel = routeStore.first(),
            defaultRoute = null,
            me = this;

        if (defaultRouteModel && defaultRouteModel.data) {
            defaultRoute = defaultRouteModel.data;
        }

        // check form Valid only for generalpanel which is a form, other tabs are not forms
        if (form.isValid()) {
            form.updateRecord();
            formData = form.getValues();
            rec = form.getRecord();

            formData.creatorId = userId;

            //create for myself
            if (formData.whoVal === "self") {
                //if a NIST Associate
                if (user.staffType !== "NIST Employee") {
                    if (!defaultRoute || !defaultRoute.host || defaultRoute.host.personId === 0) {
                        Ext.Msg.alert(
                            "Warning",
                            "The system cannot perform this operation because the NIST Host is not defined. " +
                                "Please contact your AO or Office Manager. They can define who is the NIST Host for you " +
                                "using the NIST Org application"
                        );
                        this.redirectTo("#dashboard");
                        return;
                    } else {
                        rec.set("requesterId", defaultRoute.host.personId);
                        rec.set("requesterName", defaultRoute.host.name);
                    }
                } else {
                    //user is an employee
                    rec.set("requesterId", userId);
                }
            }

            //the number is generated after submission only now (req change 10/2019)
            rec.data.generateRequisitionNumber = false;

            //add indicator if the user creates a request for a different group than the user's group (detailee, temp assignment...)
            if (rec.data.groupId != user.groupId) {
                rec.data.isDetailReq = true;
            }

            //insert url
            url = bcp.config.Runtime.getServerBaseUrl() + "requests";

            //set approvers first, then save the request
            this.setApprovers(rec, defaultRoute, ouRoles, user)
                .then(this.saveRequest(me, url, method, rec))
                .catch((error) => Ext.Msg.alert(error.name, error.message));
        } else {
            Ext.Msg.alert("Form Validation Error", "One or more fields in the form are invalid");
            return;
        }
    },

    onCancel: function (button, e, eOpts) {
        this.redirectTo("#dashboard");
    },

    onWindowAdded: function (component, container, pos, eOpts) {
        var model = this.getViewModel(),
            user = model.get("loggedInUser"),
            dtlStore = Ext.getStore("UserDetailedPrivileges"),
            grpStore = Ext.getStore("Groups"),
            detaileeStore = Ext.getStore("Detailees"),
            userId = user.peopleId,
            refs = this.getReferences(),
            form = refs.form.getForm(),
            currentYear = new Date().getFullYear(), //yr in 4 digits
            currentFy = Number(currentYear.toString().substr(-2)), //yr in 2 digits
            createNextFyEndDate = new Date(new Date().getFullYear() + "-10-01T00:00:00-04:00"),
            fys = this.getStore("fys"),
            newRecord = {};

        //if today is 10/1 or later, FY is in next FY
        if (new Date() >= createNextFyEndDate) {
            currentFy++;
        }

        fys.addFilter([{operator: ">=", property: "fy", value: currentFy}]);

        var allowedGroups = Ext.create("Ext.data.Store", {
            model: grpStore.model
        });
        grpStore.clearFilter();
        //always add user's current group
        var userGrpRec = grpStore.findRecord("groupId", user.groupId);
        var newUserGrpRec = Ext.clone(userGrpRec.copy().data);
        allowedGroups.add(newUserGrpRec);

        if (!bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
            //MB-545 check user profile to see if the user has detail privilege for a different org
            //if yes, show groups that the user is allowed to create requests to
            if (dtlStore.totalCount > 0) {
                for (var i = 0; i < dtlStore.totalCount; i++) {
                    //get the detailed privilege
                    var ud = dtlStore.getAt(i);
                    //remove any filter

                    //depending on accese level, decide what filter to use
                    if (ud.data.accessOu) {
                        grpStore.addFilter({operator: "==", property: "ouId", value: ud.data.ouId});
                    } else if (ud.data.accessDiv) {
                        grpStore.addFilter({operator: "==", property: "divisionId", value: ud.data.divisionId});
                    } else if (ud.data.accessGroup) {
                        grpStore.addFilter({operator: "==", property: "groupId", value: ud.data.groupId});
                    }

                    //copy groups to new store
                    Ext.each(grpStore.getRange(), function (record) {
                        var newRecordData = Ext.clone(record.copy().data);
                        //var model = new grpStore.model(newRecordData, newRecordData.id);
                        allowedGroups.add(newRecordData);
                    });
                }

                refs.comboGrp.bindStore(allowedGroups);
            } else {
                refs.comboGrp.hide();
                refs.comboGrp.disable();
            }
        } else {
            refs.comboGrp.bindStore(grpStore);
        }

        if (user.staffType === "NIST Associate") {
            newRecord = Ext.create("bcp.model.BcpRequest", {
                requestId: 0,
                creatorId: userId,
                requestedForId: userId,
                requesterId: user.bossId,
                groupId: user.groupId
            });
            refs.requesterIdHf.disable();
            refs.notmeRb.setBoxLabel("NIST Federal Employee");
        } else {
            newRecord = Ext.create("bcp.model.BcpRequest", {
                requestId: 0,
                creatorId: userId,
                requestedForId: -1,
                requesterId: userId,
                groupId: user.groupId
            });
        }

        //if only one FY record is in the store, select it so users don't have to
        if (fys.getRange().length == 1) {
            newRecord.set("fy", currentFy);
        }

        model.set("record", newRecord);

        //detailee mode
        if (user.detaileeMode == true) {
            var profile = detaileeStore.first();
            newRecord.set("groupId", profile.data.groupId);
        }

        // Clear form
        form.reset();
        // Set record
        form.loadRecord(newRecord);

        //if associates try to do it for someone else, that someone must be a fed
        if (user.staffType === "NIST Associate") {
            refs.requesterRg.setValue({whoVal: "notme"});
            refs.selfRb.hide();
            refs.selfRb.disable();
            refs.onBehalfCombo.setValue(newRecord.get("requesterId"));
        } else {
            refs.selfRb.show();
            refs.selfRb.enable();
            refs.requesterRg.setValue({whoVal: "self"});
        }
    },

    saveRequest: async function (me, url, method, rec) {
        try {
            const response = await Ext.Ajax.request({
                url: url,
                method: method,
                scope: this,
                jsonData: Ext.encode(rec.data)
            });
            const ret = Ext.decode(response.responseText);
            if (ret.data) {
                rec = Ext.create("bcp.model.BcpRequest", ret.data);
                var requestId = rec.data.requestId;
                me.redirectTo("#newrequest/" + requestId);
                return;
            }
        } catch (error) {
            Ext.Msg.alert("Failure", "Unexpected error occurred. The request was not created.");
            me.redirectTo("#dashboard");
            return;
        }
    },

    //use this method to determine approvers for a request and set values in the record before save it to DB
    setApprovers: async function (rec, defaultRoute, ouRoles, user) {
        //if submit for self, set approvers get from NIST Org
        //if create the req for someone else, we don't know the approvers info
        //for that person from NIST Org yet, so don't insert the values until
        //that person gets the request
        //MB-434 to make this issue to work, the server code will get defaultRoute using the requesterId
        if (defaultRoute && defaultRoute.bankcardHolder) {
            rec.set("bankcardHolderId", defaultRoute.bankcardHolder.personId);
        }
        if (defaultRoute && defaultRoute.bankcardApprovingOfficial) {
            rec.set("bankcardApprovingOfficialId", defaultRoute.bankcardApprovingOfficial.personId);
        }
        //set reviewer is a little tricky
        //BANK-539
        //NIST ORG's default reviewer is the GL but there are cases that it doesn't work
        //Special Case #1 GL's default reviewer is the DC
        //Special Case #2 DC's default reviewer is the XO
        //Special Case #3 (may never happen) OU DR's default reviewer is the SMA (cannot ask NIST DR to approve )
        //For other normal cases,
        //add a check for default reviewer to make sure the reviewer is not the same person as the requester
        //if it is, make it blank so the user would need to pick a different reviewer in the routes view
        var reviewer = null;
        if (rec.data.creatorId === rec.data.requesterId) {
            //save NIST Org route info in the new request initially
            if (defaultRoute && defaultRoute.reviewer) {
                if (bcp.util.CommonUtil.isUserInRole(["Deputy Director", "Director"])) {
                    reviewer = ouRoles.findRecord("roleName", "Senior Management Advisor");
                    if (reviewer && reviewer.data.peopleId) {
                        rec.set("reviewerId", reviewer.data.peopleId);
                    }
                } else if (bcp.util.CommonUtil.isUserInRole(["Division Chief"])) {
                    reviewer = ouRoles.findRecord("roleName", "Executive Officer");
                    if (reviewer && reviewer.data.peopleId) {
                        rec.set("reviewerId", reviewer.data.peopleId);
                    }
                } else if (bcp.util.CommonUtil.isUserInRole(["Group Leader"])) {
                    rec.set("reviewerId", user.bossId);
                } else if (rec.data.requesterId !== defaultRoute.reviewer.personId) {
                    rec.set("reviewerId", defaultRoute.reviewer.personId);
                } else {
                    defaultRoute.reviewer = null;
                }
            }
        } else {
            //someone prepares a request, we need to get roles of the requester and determine the reviewer
            try {
                const response = await Ext.Ajax.request({
                    url: bcp.config.Runtime.getServerBaseUrl() + "users/roles/" + rec.data.requesterId,
                    disableCaching: false,
                    method: "GET"
                });
                const ret = Ext.decode(response.responseText);
                var roles = ret.data;
                //also store it in a store so the info can be used in other tabs that need to use
                //requester's role to perform logic
                Ext.getStore("RequesterUserRoles").loadRawData(roles);
                if (bcp.util.CommonUtil.isUserInRole(["Deputy Director", "Director"], roles)) {
                    reviewer = ouRoles.findRecord("roleName", "Senior Management Advisor");
                    if (reviewer && reviewer.data.peopleId) {
                        rec.set("reviewerId", reviewer.data.peopleId);
                    }
                } else if (bcp.util.CommonUtil.isUserInRole(["Division Chief"], roles)) {
                    reviewer = ouRoles.findRecord("roleName", "Executive Officer");
                    if (reviewer && reviewer.data.peopleId) {
                        rec.set("reviewerId", reviewer.data.peopleId);
                    }
                } else if (bcp.util.CommonUtil.isUserInRole(["Group Leader"], roles)) {
                    //since we don't have the profile of group leader, we don't know who is the boss for the GL
                    //so set it to null and the prepare can pick a name from the review dropdown
                    rec.set("reviewerId", null);
                    defaultRoute.reviewer = null;
                }
            } catch (error) {
                throw new Error("Error getting user roles for the requester.");
            }
        }
        return rec;
    }
});
