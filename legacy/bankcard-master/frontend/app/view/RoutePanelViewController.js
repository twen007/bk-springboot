Ext.define("bcp.view.RoutePanelViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.routepanel",

    //for double approval and show approval amount field
    isBaoReviewing: function (rec) {
        var status = rec.get("statusCode");
        var lastRouteIsDynamic = rec.get("isDynamic");
        //since FCO step is in between reviewer and BAO, can't do double approval
        //var isSameReviewer = rec.get("reviewerId") === rec.get("bankcardApprovingOfficialId");
        return !lastRouteIsDynamic && status === 6; //|| (status === 5 && isSameReviewer));
    },

    //there's a need to switch reviewer in case the requester is senior management
    changeApprover: function (type) {
        var model = this.getViewModel(),
            rec = model.get("generalInfo"),
            reqId = rec.get("requestId"),
            store = {},
            refs = this.getReferences(),
            form = refs.form,
            requesterRoles = Ext.getStore("RequesterUserRoles"),
            me = this;
        if (type === "reviewer") {
            if (
                (rec.data.creatorId === rec.data.requesterId &&
                    bcp.util.CommonUtil.isUserInRole(["Division Chief", "Deputy Director", "Director"])) ||
                (rec.data.creatorId != rec.data.requesterId &&
                    bcp.util.CommonUtil.isUserInRole(
                        ["Division Chief", "Deputy Director", "Director"],
                        requesterRoles.data.items
                    ))
            ) {
                //ouRoles uses different model so we need to create a new store and add records to it for the combo
                var bcUserStore = Ext.create("Ext.data.Store", {
                    model: "bcp.model.BcUser"
                });
                var ouRoles = this.getStore("ouRoles");
                ouRoles.each(function (ouRole) {
                    bcUserStore.add({
                        peopleId: ouRole.get("peopleId"),
                        fullName: ouRole.get("fullName")
                    });
                });
                store = bcUserStore;
                //store = this.getStore('ouRoles');
            } else {
                store = this.getStore("reviewers");
            }
        } else if (type === "bao") {
            store = Ext.getStore("Baos");
        } else if (type === "bh") {
            store = Ext.getStore("BankcardHolders");
        } else if (type === "fco") {
            store = Ext.getStore("AllFcos");
        } else if (type === "dc") {
            //don't need this for now since 1. only one division chief, 2. no one says DC cannot be mission critical approver for the DC self
            //3. the DivisionChiefs stores current user's DC, not requester's DC so if the requester is not the current user, cannot use this store
            store = Ext.getStore("DivisionChiefs");
        }

        var crwindow = Ext.create("widget.changeroutewindow", {
            reference: "crwindow",
            store: store
        });
        //set event listeners
        crwindow.on({
            cancelChange: {
                fn: function (data) {
                    crwindow.destroy();
                },
                scope: this,
                single: true
            }
        });

        crwindow.on({
            applyChange: {
                fn: function (data) {
                    //set value
                    if (type === "reviewer") {
                        //BANK-539
                        if (rec.data.requesterId == data.peopleId) {
                            bcp.util.CommonUtil.showWarning(
                                "You cannot be the Reviewer (Bona Fide Need Certifier) for your own request"
                            );
                            crwindow.destroy();
                            return;
                        }

                        //possible that XO or SMA is the direct report of these roles
                        if (!bcp.util.CommonUtil.isUserInRole(["Division Chief", "Deputy Director", "Director"])) {
                            if (rec.data.requesterId == data.bossId) {
                                bcp.util.CommonUtil.showWarning(
                                    "You cannot select your direct report to be the Reviewer (Bona Fide Need Certifier) for your own request"
                                );
                                crwindow.destroy();
                                return;
                            }
                        }
                        rec.set("reviewerId", data.peopleId);
                    } else if (type === "dc") {
                        rec.set("divisionChiefId", data.peopleId);
                    } else if (type === "bao") {
                        rec.set("bankcardApprovingOfficialId", data.peopleId);
                    } else if (type === "bh") {
                        rec.set("bankcardHolderId", data.peopleId);
                    } else if (type === "fco") {
                        rec.set("fundsCertifyingOfficialId", data.peopleId);
                    }

                    //update request record
                    //TODO: can use single field update API (dc and fco are available), need to take care return better though
                    //so if failed for some reason, handle it properly
                    Ext.Ajax.request({
                        url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId,
                        method: "PUT",
                        scope: this,
                        jsonData: Ext.encode(rec.data),
                        success: function (response) {
                            bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                rec = Ext.create("bcp.model.BcpRequest", result.data);
                                model.set("generalInfo", rec);
                                form.loadRecord(rec);
                                //if the user updated the BAO to self
                                if (me.isBaoReviewing(rec)) {
                                    refs.fsUpTo.show();
                                    refs.nfApprAmt.enable();
                                } else {
                                    refs.fsUpTo.hide();
                                    refs.nfApprAmt.disable();
                                }
                                crwindow.destroy();
                            });
                        }
                    });
                },
                scope: this,
                single: true
            }
        });

        //add to the parent view
        this.view.add(crwindow);

        // Show the window
        crwindow.show();
    },

    //called by all route action methods (not anymore)
    popupApprovalWindow: function (newRecord, routeDataArray, itsoRecord, addTwoRoutes, isUpdate) {
        //isDynamic) {
        var model = this.getViewModel(),
            req = model.get("generalInfo");
        var formWindow;
        var isDynamic = newRecord.get("isDynamic");
        //there's a case when DRed from a fixed route, approved and back to the fixed route where the planned route has isDynamic = 0
        //so we allow the fixed route approver to access special UIs for fixed route. However, in case of approve the route, if we are doing
        //a update (route_date), even the next route is to the fixed route approver, we don't want to display any special UIs in the popup
        //so we used " || isUpdate" here to always use the dynamicroutewindow for exec a planned route
        if (isDynamic || isUpdate) {
            formWindow = Ext.create("widget.dynamicroutewindow");
        } else {
            formWindow = Ext.create("widget.routewindow");
            if (newRecord.data.routeTo !== 0) {
                formWindow.lookupController().removeRouteToCombo();
            }
        }

        //if we are trying to execute a planned route
        formWindow.lookupController().getViewModel().set("isUpdate", isUpdate);

        if (model.get("itsoStep")) {
            //if we are trying to execute a planned route as a result of a ITSO approval
            formWindow.lookupController().getViewModel().set("itsoStep", true);
        }

        //set original request record
        formWindow.lookupController().getViewModel().set("req", req);

        //set record for form bindings
        if (routeDataArray == null) {
            //call made from places where no Expedite Routings possible
            formWindow.lookupController().getViewModel().set("record", newRecord);
            //load record
            formWindow.lookupController().lookupReference("form").getForm().loadRecord(newRecord);
        } else {
            //if there are Expedite Routings, it should have at least one route
            //in the array
            if (Array.isArray(routeDataArray)) {
                let finalRoute = routeDataArray[routeDataArray.length - 1];
                //use the last one in the array to set record
                //so the window will display proper message
                formWindow.lookupController().getViewModel().set("record", finalRoute);
                //load record
                formWindow.lookupController().lookupReference("form").getForm().loadRecord(finalRoute);
                formWindow.lookupController().getViewModel().set("routeDataArray", routeDataArray);
            } else {
                //call made from place where extraRecord is added (approve & add a route)
                formWindow.lookupController().getViewModel().set("extraRecord", routeDataArray);
                //load record
                formWindow.lookupController().lookupReference("form").getForm().loadRecord(newRecord);
            }
        }

        //added for insert two routes if reviewer and BAO is the same person
        //not applicable after so many routing changes
        //formWindow.lookupController().getViewModel().set("addTwoRoutes", addTwoRoutes);

        //if (addTwoRoutes && addTwoRoutes.doubleApprove) {
        //    formWindow.lookupController().getViewModel().set("extraRecord", extraRecord);
        //}

        //set itso record in model if exists
        if (itsoRecord) {
            formWindow.lookupController().getViewModel().set("itsoRecord", itsoRecord);
        }

        //this will be used to determine if next step is BAO/FCO or only BAO
        //TODO: by now, 04/2025, all orgs should switched to have separate FCO and BAO steps
        //so at some point, I need to clean code related to this
        formWindow.lookupController().getViewModel().set("needFcoRoute", model.get("needFcoRoute"));

        //set event listeners
        this.onApprovalWindow(formWindow);
        //move it up 200 pixels
        var p = formWindow.getPosition();
        p[1] = p[1] - 200;
        formWindow.setXY(p);
    },

    onApprovalAmountBlur: function (component, event, eOpts) {
        var model = this.getViewModel(),
            approvalAmount = component.value,
            req = model.get("generalInfo");

        req.set("approvalAmount", approvalAmount);
    },

    //reroute to the dynamic approver
    onReroute: function (button, e, eOpts) {
        var model = this.getViewModel(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            currentStatus = req.get("statusCode"),
            rerouteStack = req.get("rerouteStack"),
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                //reroute to the dynamic approver, step doesn't change
                statusId: currentStatus
            });

        //type id stay the same
        newRecord.set("typeId", req.get("routeTypeId"));

        //set route type to "reroute"
        newRecord.set("isDynamicReroute", 1);

        //indicate it's a dynamic route
        newRecord.set("isDynamic", 1);

        //set current route step
        newRecord.set("routeStep", req.get("routeStep"));

        //set dynamic type to DR(dynamic reroute)
        newRecord.set("dynamicType", "DR");

        //increase reroute stack TODO: after changed to use route step, this is not needed
        newRecord.set("rerouteStack", rerouteStack + 1);

        //popup approval window
        this.popupApprovalWindow(newRecord, null, null, false, false);
    },

    //approve current step and add a route to a dynamic approver before it reaches the next fixed route
    onApproveAndAddRoute: function (button, e, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form, // this.view,
            record = form.getRecord(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            currentRouteId = req.get("routeId"),
            history = Ext.getStore("RoutingHistory"),
            currentStatus = req.get("statusCode"),
            limit = bcp.config.Runtime.getPurchaseLimit(),
            //find the current route record
            currentRouteRec = history.findRecord("routeId", currentRouteId),
            currentRouteStep = currentRouteRec.get("routeStep"),
            //if there's a planned route next, get it; otherwise, no planned route exists; the next route should a fixed route
            nextRouteRec = history.findRecord("routeStep", currentRouteStep + 1),
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                //reroute to the dynamic approver, step doesn't change
                statusId: currentStatus
            }),
            extraRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId
            }),
            addTwoRoutes = {};
        //make this a object so it can be passed by reference with the setupNextFixedRoute method
        addTwoRoutes.doubleApprove = false;

        //for BAO that use this function, add this so approval amount is updated.
        //if current route is DR, approver is not the BAO, don't validate. only validate if user is the BAO
        if (currentStatus === 6 && currentRouteRec.get("dynamicType") != "DR") {
            //check for approval amount and update it in the req
            if (!this.updateReq(reqId, record, req, model, form, limit)) {
                //validation fails, no need to continue
                return;
            }
        }

        //if there's a planned route next, set the routeToName for display the name in the msg in the popup window
        if (nextRouteRec) {
            newRecord.set("routeToName", nextRouteRec.get("routeToName"));
        } else {
            //no next route found, so the next route is a fixed route. need to call this method to figure out
            //next fixed route's routeToName for display the msg in the popup window
            this.setupNextFixedRoute(currentStatus, record, newRecord, extraRecord, addTwoRoutes);
        }

        //since AA is approve and add a dynamic route, if the previous route is a fixed one, the approve
        //should move the request to the next fixed route
        //in this case, the reviewer and BAO is the same person, so the approval amount is entered, the request
        //should move to bao step and we also need to update the approval amount in the request

        /*now we have MC and FCO steps in between reveiwer and BAO, this doubleApprove case will not be possible
        if (addTwoRoutes.doubleApprove == true && currentStatus === 5) {
            if (!this.updateReq(reqId, record, req, model, form, limit)) {
                //validation fails, no need to continue
                return;
            }
        }*/

        //TODO: check why type stay same! AA is approve and add a route so the approve advanced the type if prev route is fixed
        //type id stay the same
        //newRecord.set("typeId", req.get("routeTypeId"));

        //set route type to "reroute"
        newRecord.set("isDynamicReroute", 0);

        //indicate it's a dynamic route
        newRecord.set("isDynamic", 1);

        //set dynamic type to AA(approve and add a route)
        newRecord.set("dynamicType", "AA");

        //popup approval window
        //it's possible to have double approval when AA, so we need to pass extraRecord & addTwoRoutes
        this.popupApprovalWindow(newRecord, extraRecord, null, addTwoRoutes, false);
    },

    //create a Director route record
    //deprecated; switched to use commonutil's prepareDirectorRecord
    /*prepareDirectorRecord: function () {
        var model = this.getViewModel(),
            currentUserOuId = model.get("loggedInUser").ouId,
            ors = this.getStore("ouRoles"),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            currentStatus = req.get("statusCode"),
            me=this;

        //TODO: becuase the call is async, call this on viewAdded and save dr data in model, then try to get it from model in here
        //if found set it, otherwise use OU DR

        //issue 679 OU can assign the deputy director to the "mission critical director approver" role so the Deputy DR will
        //be notified and approve requests instead of DR
        //if the OU setup "mission critical director approver" role in nist org, use it; otherwise, use the director role of that OU    
        bcp.util.CommonFunctions.prepareDrRecord(req, currentUserOuId).then(function (newRecord) {
            if (newRecord) {
                return newRecord;
            } else {
                newRecord = Ext.create("bcp.model.RequestRoute", {
                    requestId: reqId
                });
                if (ors.getCount() < 1) {
                    return null; // or handle the case as needed
                } else {
                    var dir = ors.findRecord("roleName", "Director");
                    if (!dir) return null;
                    //route to director
                    var dirPId = dir.get("peopleId");
                    //set comment
                    newRecord.set("notes", "Routing for mission critical Director approval.");
                    me.setRouteValues(newRecord, 15, 18, dirPId, dir.get("fullName"));
                    return newRecord;
                }
            }
        });
    },*/

    //create a ITSO route record
    prepareItsoRecord: function () {
        var model = this.getViewModel(),
            itsos = Ext.getStore("ItsoUsers"),
            //they could setup multiple OU ITSO in Nist Org but route can only be send to one staff
            //so we pick the first one find, the rest staffs in the store will be treated as deputies
            ouitso = itsos.findRecord("ditso", true),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            currentStatus = req.get("statusCode"),
            //find the current route record
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                //reroute to the dynamic approver, step doesn't change
                statusId: currentStatus
            });

        //type id stay the same
        newRecord.set("typeId", req.get("routeTypeId"));

        //set route type to "reroute"
        newRecord.set("isDynamicReroute", 1);

        //indicate it's a dynamic route
        newRecord.set("isDynamic", 1);

        //set dynamic type to ITSO(IT Security Officer)
        newRecord.set("dynamicType", "ITSO");

        //if a OU ITSO was found
        if (ouitso) {
            var ouitsoPId = ouitso.get("peopleId");
            newRecord.set("routeTo", ouitsoPId);
            try {
                if (itsos) {
                    var peopleIds = [];
                    itsos.each(function (rec) {
                        //ditso (the OU ITSO) is what we will email, the rest are backup ITSOs and we need to
                        //format a email list string and set it to alsoNotify so the SP can notify them also

                        if (rec.get("peopleId") != ouitsoPId) {
                            /*var fullName = rec.data.fullName,
                                idx = fullName.indexOf(", "),
                                fname = fullName.substr(idx + 2),
                                lname = fullName.substring(0, idx),
                                //in case the first name NIST ORG returns has middle init such as "Andrew K."
                                idx2 = fname.indexOf(" "),
                                email = "";

                            if (idx2 == -1) {
                                //firstName.lastName@nist.gov from fullName (in "last, first" format)
                                email = fname + "." + lname + "@nist.gov;";
                            } else {
                                //get only the firstname without middle initial
                                email = fname.substring(0, idx2) + "." + lname + "@nist.gov;";
                            }

                            ccList = ccList + email;*/
                            peopleIds.push(rec.get("peopleId"));
                        }
                    });
                    newRecord.set("alsoNotify", peopleIds.join(","));
                }
            } catch (ex) {
                //in case it causes error, we need to take a look
                newRecord.set("alsoNotify", "23826");
            }
        }

        return newRecord;
    },

    //create a FCO approval route record. If no FCO set for the division, return null
    prepareFcoRecord: function () {
        var model = this.getViewModel(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            //find the current route record
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                statusId: 16, //new status used to show that FCO is reviewing the request
                typeId: 16 //new type id created when switched to fixed route
            });

        //for divs not switched to use explicit fco yet, the code won't come here
        //if it's here, it means the div did the switched and we should have the fcoId set
        //in the onViewAdded.
        if (req.get("fundsCertifyingOfficialId") === 0) {
            return null; //no FCO set for the division, return null and do not create the route record
        } else {
            newRecord.set("routeTo", req.get("fundsCertifyingOfficialId"));
            newRecord.set("routeToName", req.get("fcoName"));
        }
        return newRecord;
    },

    //in compliance with execute order, NIST requires all  bankcard requests with mission critical category [Other] be approved by the OU Director
    //before routed to other approvers in the normal workflow
    //add Director approval
    addDirectorRoute: function () {
        var model = this.getViewModel(),
            me = this,
            //newRecord = this.prepareDirectorRecord();
            newRecord = model.get("drRecord");
        //if a DR record is created successfully
        if (newRecord) {
            var dcMsg =
                "Guidance effective immediately March 2nd, 2025, all mission critical credit card purchases with category <b>[Other]</b> must be reviewed and approved by the OU Director." +
                "<br><br>Do you want to route this request to <b>" +
                newRecord.data.routeToName +
                "</b> for approval?";
            // Ask user to confirm this action
            Ext.Msg.confirm("Route to OU Director Confirmation", dcMsg, function (result) {
                // User confirmed yes
                if (result == "yes") {
                    me.makeRoutingAjaxRequest({
                        me: me,
                        jsonData: Ext.encode(newRecord.data),
                        successMsg: "The request routed to the OU Director successfully.",
                        failMsg: "OU Director Route Failed"
                    });
                }
            });
        } else {
            //no OU Director seup in NIST Org
            var msg =
                "To comply with the new executive order, all mission critical credit card purchases with category [Other] must be routed to the OU Director for approval." +
                " However, the OU Director role was not setup in the NIST Org system for your division. Please contact your office manager to set it up.";
            bcp.util.CommonUtil.showWarning(msg);
        }
    },

    //issue 606, all OUs use the same route rule for ITSO approval so no need for defaultITSORoutingWay
    //add ITSO approval
    addItsoRoute: function () {
        var me = this,
            itsos = Ext.getStore("ItsoUsers"),
            ouitso = itsos.findRecord("ditso", true),
            newRecord = this.prepareItsoRecord();
        //if a OU ITSO was found
        if (ouitso) {
            var ItsoMsg =
                "The request is a <b>IT Purchase</b>, so it requires a <b>ITSO</b> pre-screen and approval. " +
                "The System will route the request to the ITSO [<b>" +
                ouitso.get("fullName") +
                "</b>] now.  After the request is approved, it will be routed back to you. " +
                "<br><br>Do you want to route this request to <b>" +
                ouitso.get("fullName") +
                "</b> for approval?";
            // Ask user to confirm this action
            Ext.Msg.confirm("Re-route to ITSO Confirmation", ItsoMsg, function (result) {
                // User confirmed yes
                if (result == "yes") {
                    me.makeRoutingAjaxRequest({
                        me: me,
                        jsonData: Ext.encode(newRecord.data),
                        successMsg: "The request routed to the ITSO successfully.",
                        failMsg: "ITSO Route Failed"
                    });
                }
            });
        } else {
            //no OU ITSO seup in NIST Org
            var msg =
                "This request is a IT Purchase, so it must be routed to the ITSO for approval." +
                " However, your OU ITSO was not setup in the NIST Org system. Please contact your office manager to set it up.";
            bcp.util.CommonUtil.showWarning(msg);
        }
    },

    //MB-461
    onReassign: function (button, e, eOpts) {
        var model = this.getViewModel(),
            record = model.get("generalInfo"),
            status = record.get("statusCode"),
            store = {},
            formWindow = null,
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: record.get("requestId")
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
                "Funds Certifying Official"
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
                            this.redirectTo("#dashboard");
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
                bcp.util.CommonUtil.showWarning("Only requests currently in the approval process can be reassigned.");
            }
        }
    },

    //the requester submits the request
    onSubmit: function (button, e, eOpts) {
        var model = this.getViewModel(),
            hasMissingPtcOrOc = false,
            hasItChecklist = false,
            req = model.get("generalInfo"),
            isItPurchase = model.get("isItPurchase"),
            items = Ext.getStore("RequestItems"),
            files = Ext.getStore("RequestFiles"),
            vendor = Ext.getStore("RequestVendors").first(),
            newRoute = Ext.create("bcp.model.RequestRoute", {
                requestId: req.get("requestId"),
                statusId: 5,
                typeId: 1,
                routeTo: req.get("reviewerId"),
                routeToName: req.get("reviewerName"),
                divisionId: req.get("divisionId") //need this to check for custom bch initials
            });

        var validationErrorMsg = this.commonValidationHandler();

        if (req.get("reviewerId") === 0) {
            validationErrorMsg += "Please select a reviewer before submitting the request.<br>";
        }
        //BANK-565 add this chheck before ITSO record would prevent submssion to the same person as requester for review with the auto ITSO route
        if (req.get("reviewerId") === req.get("requesterId")) {
            validationErrorMsg +=
                "The [Requester] and the [Reviewer] cannot be the same person. Please select a different reviewer before submitting the request.<br>";
        }

        //BANK-488
        var pref = bcp.util.CommonFunctions.getDivisionPreferences(req.get("divisionId"));

        if (pref.get("financePrefVal") == "Y") {
            if (items) {
                items.each(function (record) {
                    if (record.get("projTask") === "" || record.get("objectClass") === "") {
                        hasMissingPtcOrOc = true;
                    }
                });
            }
            if (hasMissingPtcOrOc) {
                validationErrorMsg += "Project Task Code(s) and/or Object Class(es) are missing.<br>";
            }
        }

        if (pref.get("justPrefVal") == "Y" && !model.get("hasVendor")) {
            validationErrorMsg += "Please add a vendor before submitting the request.<br>";
        }

        //issue 608
        if (isItPurchase) {
            //if IT purchase
            if (files) {
                //check if IT Checklist is attached before submission
                files.each(function (record) {
                    if (record.get("fileCategoryId") == 11) {
                        hasItChecklist = true;
                        return false; // This will end the loop
                    }
                });
                if (!hasItChecklist) {
                    validationErrorMsg +=
                        "Please fill and attach the appropriate IT Compliance Checklist to the request.<br>";
                }
            } else {
                bcp.util.CommonUtil.showWarning("Error retriveing file attachments information");
            }
        } else {
            //issue 616, if not using IT purchaseType but select IT Buying Service as vendor, prompt the user to fix it
            if (vendor && vendor.get("refVendorId") == -99) {
                validationErrorMsg +=
                    "You Selected [IT Buying Service] as the vendor but your Purchase Type is not [IT].";
            }
        }

        if (validationErrorMsg != "") {
            bcp.util.CommonUtil.showAlert("Validation Error", validationErrorMsg);
            return;
        }

        //this is the normal way before issue 618 where we prep a route to reviewer and also check whether
        //we need to route to ITSO first
        //only requester can submit and the after submitted, the status is "5 - reviewer reviews"
        this.handleITSORoute(newRoute);
    },

    //this method should be call in the handler method of the button that triggers request submission
    handleITSORoute: function (newRoute) {
        var model = this.getViewModel(),
            req = model.get("generalInfo"),
            isItPurchase = model.get("isItPurchase"),
            needItsoApproval = model.get("needItsoApproval");

        // 2/6/25 all IT Purchases need ITSO approval first, so we need to update the ITSO routes to be the same as
        //NCNR and EL used now
        //IT Buying Service do not need ITSO approval for OUs except NCNR and EL
        //but it should have the prefilled IT Checklist attached, which we are thinking to make an auto attach or auto show
        //feature (issue 610, 608)
        if (isItPurchase) {
            if (needItsoApproval) {
                var itsoRec = this.prepareItsoRecord();
                //do this to mimic the steps: route to reviewer and then reroute to ITSO
                itsoRec.set("statusId", 6);
                //do this so after ITSO approval, it will go to reviewer, not back to requester
                itsoRec.set("routeBy", req.get("reviewerId"));
                //all IT purchases to go to ITSO after submission
                this.popupApprovalWindow(newRoute, null, itsoRec, false, false);
            } else {
                //for IT Purchases that do not need ITSO approval
                this.popupApprovalWindow(newRoute, null, null, false, false);
            }
        } else {
            //for non IT Purchases
            //since not dynamic ITSO route, it's possible to do Expedite Routing when approvers are the same person
            let routeDataArray = [];
            routeDataArray.push(newRoute);
            //will add DC route or DC and DR routes to the array if applies
            this.addExpediteRoutings(routeDataArray, req);

            this.popupApprovalWindow(newRoute, routeDataArray, null, false, false);
        }
    },

    commonValidationHandler: function () {
        var model = this.getViewModel(),
            validationError = "",
            limit = bcp.config.Runtime.getPurchaseLimit(),
            comments = this.lookupReference("commentsTa").value;
        if (comments && comments.length > 1000) {
            validationError += "The maximum length for the Comments is 1000 characters.<br>";
        }

        if (!model.get("hasItem")) {
            validationError +=
                "There are no items added for the request. Please add at least one item before submitting the request.<br>";
        }

        if (!model.get("hasShipping")) {
            validationError +=
                "There is no shipping & handling cost added for the request. Please add the estimated cost for shipping & handling in the [Items] panel before submitting the request.<br>";
        }

        if (model.get("overPurchaseLimit")) {
            validationError +=
                "The Total amount of the request exceeds the " +
                Ext.util.Format.number(limit, "$0,000") +
                " limit. Please reduce it before submitting the request.<br>";
        }
        return validationError;
    },

    returnRoutesHandler: function (typeId, statusId) {
        //common function for reject or return
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form, //this.view,
            req = model.get("generalInfo"),
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: req.get("requestId")
            });

        //update value in record
        form.getForm().updateRecord();
        newRecord.set("typeId", typeId);
        newRecord.set("statusId", statusId);

        //for return
        //issue# 583
        if (typeId === 13) {
            //for prepared requests
            if (req.data.creatorId != req.data.requesterId) {
                newRecord.set("routeTo", req.get("creatorId"));
                newRecord.set("routeToName", req.get("creatorName"));
                //update status to 15; this is a new status added in 8/2024 to show the return is to preparer
                newRecord.set("statusId", 15);
                //14 is to shwo the return is to requester
                newRecord.backToPreparer = true;
            } else {
                //BANK-572
                newRecord.set("routeTo", req.get("requesterId"));
                newRecord.set("routeToName", req.get("requesterName"));
            }
        } else {
            //for reject
            newRecord.set("routeTo", req.get("requesterId"));
            newRecord.set("routeToName", req.get("requesterName"));
        }

        this.popupApprovalWindow(newRecord, null, null, false, false);
    },

    onReject: function (button, e, eOpts) {
        this.returnRoutesHandler(5, 11);
    },

    onReturnForInfo: function (button, e, eOpts) {
        this.returnRoutesHandler(13, 14);
    },

    onCommentsBlur: function (component, event, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form, //this.view,
            comments = component.value,
            req = model.get("generalInfo"),
            reqId = req.get("requestId");

        //validations before sent to requester
        var validationErrorMsg = this.commonValidationHandler();

        if (validationErrorMsg != "") {
            bcp.util.CommonUtil.showAlert("Validation Error", validationErrorMsg);
            return;
        }

        //update comments if changed
        if (comments != req.get("comments")) {
            req.set("comments", comments);
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId,
                method: "PUT",
                scope: this,
                jsonData: Ext.encode(req.data),
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                        req = Ext.create("bcp.model.BcpRequest", result.data);
                        model.set("generalInfo", req);
                        form.loadRecord(req);
                    });
                }
            });
        }
    },

    onAction: function (button, e, eOpts) {
        //the action can be either a Fed prepared the request and send it to another Fed (the requester) OR
        //an associate prepared the request and send it to another Fed or the host who is also a Fed
        var model = this.getViewModel(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            hasItChecklist = false,
            isItPurchase = model.get("isItPurchase"),
            items = Ext.getStore("RequestItems"),
            files = Ext.getStore("RequestFiles"),
            vendor = Ext.getStore("RequestVendors").first(),
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                statusId: 3
            });

        //validations before sent to requester
        var validationErrorMsg = this.commonValidationHandler();

        //issue 608
        if (isItPurchase) {
            //if IT purchase
            if (files) {
                //check if IT Checklist is attached before submission
                files.each(function (record) {
                    if (record.get("fileCategoryId") == 11) {
                        hasItChecklist = true;
                        return false; // This will end the loop
                    }
                });
                if (!hasItChecklist) {
                    validationErrorMsg +=
                        "Please fill and attach the appropriate IT Compliance Checklist to the request.<br>";
                }
            } else {
                bcp.util.CommonUtil.showWarning("Error retriveing file attachments information");
            }
        } else {
            //issue 616, if not using IT purchaseType but select IT Buying Service as vendor, prompt the user to fix it
            if (vendor && vendor.get("refVendorId") == -99) {
                validationErrorMsg +=
                    "You Selected [IT Buying Service] as the vendor but your Purchase Type is not [IT].";
            }
        }

        if (validationErrorMsg != "") {
            bcp.util.CommonUtil.showAlert("Validation Error", validationErrorMsg);
            return;
        }

        newRecord.set("typeId", 12);
        newRecord.set("routeTo", req.get("requesterId"));
        newRecord.set("routeToName", req.get("requesterName"));
        this.popupApprovalWindow(newRecord, null, null, false, false);
    },

    onApprove: function (button, e, eOpts) {
        this.approve();
    },

    //new method to replace the old approve method after switched to use route step and planned routes approach
    approve: function () {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            form = refs.form, // this.view,
            record = form.getRecord(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            history = Ext.getStore("RoutingHistory"),
            currentRouteId = req.get("routeId"),
            //find the current route record
            currentRouteRec = history.findRecord("routeId", currentRouteId),
            currentRouteStep = req.get("routeStep"),
            items = Ext.getStore("RequestItems"),
            hasMissingPtcOrOc = false,
            limit = bcp.config.Runtime.getPurchaseLimit(),
            currentStatus = req.get("statusCode"),
            fcoRec = null,
            newRecord = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId
            }),
            me = this;

        let routeDataArray = [];

        const checkBaoFcoSamePersonDate = new Date("2025-03-27");
        let submittedDate = req.get("submittedDate");

        if (!history.isLoaded()) {
            history.load();
            bcp.util.CommonUtil.showWarning(
                "Error loading necessary data for the request. Please try to approve again."
            );
            return;
        }

        //if there's a planned/dynamic route next, get it; otherwise, no planned route exists; the next route should a fixed route
        let nextRouteRec = history.findRecord("routeStep", currentRouteStep + 1);

        if (nextRouteRec) {
            //find a planned route, let's execute it by update its route date
            this.popupApprovalWindow(nextRouteRec, null, null, false, true);
            return;
        }

        //if code continues here, do fixed route

        //Reviewer clicked the approve button
        //issue 618, submission goes to DC after reviewer approves it, if mission critial category is other, it also need Director approval
        //so typeId 14 will be the new submisison type and status of it is 17
        if (currentStatus == 5 && req.get("dynamicType") != "ITSO") {
            //for requests (submitted on or after 3/27/2025) of divisions that have not setup FCO route yet
            //do it here because I'd rather stop here than popup the message when DC or DR tries to approve
            if (submittedDate && submittedDate >= checkBaoFcoSamePersonDate && !model.get("needFcoRoute")) {
                bcp.util.CommonUtil.showWarning(
                    "Guidance effective March 27th, 2025, Funds Certifying Official and Bankcard Approving Official can no longer be the same individual. " +
                        "Please <b><a href=\"Javascript: window.open('https://nistgov.atlassian.net/wiki/spaces/MMLAdminPortal/pages/212140034/Separate+Fund+Certifying+Official+and+Bankcard+Approving+Official+in+the+Bankcard+System')\";>add a Funds Certifying Official</a></b> role for your division before proceeding."
                );
                return;
            }

            if (req.get("divisonChiefId") == 0) {
                //no Division Chief seup in NIST Org
                var msg =
                    "To comply with the new executive order, all bankcard requests must be routed to the Division Chief for approval." +
                    " However, the Division Chief role was not setup in the NIST Org system for your division. Please contact your office manager to set it up.";
                bcp.util.CommonUtil.showWarning(msg);
                return;
            }

            //next step is DC
            let dcRouteData = Ext.create("bcp.model.RequestRoute", {
                requestId: reqId,
                statusId: 17, // Example: Status ID for 'Pending DC Approval'
                typeId: 14, // Example: Type ID for 'Sent to DC'
                routeTo: req.get("divisionChiefId"),
                routeToName: req.get("dcName"),
                notes: "Routing for mission critical approval."
            });
            routeDataArray.push(dcRouteData);
            //will add DR route to the array if applies
            this.addExpediteRoutings(routeDataArray, req);
            this.popupApprovalWindow(dcRouteData, routeDataArray, null, false, false);
            return;
        }

        //DC clicked the approve button
        if (currentStatus == 17) {
            //check if Director approval is needed
            //if not, prep a route to reviewer and also check if ITSO route is needed
            if (record.get("missionCriticalCategoryId") == 4) {
                //category is "Other"
                this.addDirectorRoute(newRecord, req);
                return;
            }
        }

        //update form value in record such as additional comments or up to amount
        form.getForm().updateRecord();

        //fco, bao or bch
        this.setupNextFixedRoute(currentStatus, record, newRecord);

        //issue585 division preference set Funds Certifying Official as a separate route
        if (model.get("needFcoRoute") && (currentStatus == 17 || currentStatus == 18)) {
            //route to FCO
            model.set("needFcoApproval", true);
            fcoRec = this.prepareFcoRecord();
            if (fcoRec == null) {
                //alert the user that we cannot find the fco
                bcp.util.CommonUtil.showWarning(
                    'Please contact the AO or Office Manager to setup [Funds Certifying Official] role for your division in the <a href="https://mmlweb.nist.gov/org" target="_blank" style="color: blue; text-decoration: underline;">NIST Org</a> system.'
                );
                return;
            } else {
                //it could be either DC or DR route to the FCO so get the current route's routeTo is the best bet
                fcoRec.set("routeBy", currentRouteRec.get("routeTo"));

                //issue 637 check if bao and fco is the same person
                if (submittedDate && submittedDate >= checkBaoFcoSamePersonDate) {
                    if (
                        //if there are more than one fco, routeTo is 0, user need to pick a FCO in the popupdown to route
                        fcoRec.get("routeTo") === req.get("bankcardApprovingOfficialId") ||
                        req.get("fundsCertifyingOfficialId") === req.get("bankcardApprovingOfficialId")
                    ) {
                        bcp.util.CommonUtil.showWarning(
                            "Guidance effective March 27th, 2025, Funds Certifying Official and Bankcard Approving Official can no longer be the same individual. Please change the Funds Certifying Official before proceeding."
                        );
                        return;
                    }
                }
            }
            //route to FCO
            this.popupApprovalWindow(fcoRec, null, null, false, false);
        } else {
            //no need for separate FCO approval. this code block should go away once all divs switched to explicit FCO route(it is required by policy)
            model.set("needFcoApproval", false);

            //conditional validation for BAO and BCH approval
            if (items) {
                items.each(function (record) {
                    if (record.get("projTask") === "" || record.get("objectClass") === "") {
                        hasMissingPtcOrOc = true;
                    }
                });
            }

            if (currentStatus == 6) {
                //at BAO step, do this
                //check for approval amount and update it in the req
                if (!this.updateReq(reqId, record, req, model, form, limit)) {
                    //validation fails, no need to continue
                    return;
                }
            }

            //current route could be 17 or 18 but no need for separate FCO approval
            //since setupNextFixedRoute does nothing for these two status, we need to create a route to BAO
            if (!model.get("needFcoRoute") && (currentStatus == 17 || currentStatus == 18)) {
                this.setRouteValues(newRecord, 2, 6, record.get("bankcardApprovingOfficialId"), record.get("baoName"));
            }

            //validate for some routes
            if ((newRecord.get("statusId") === 7 || newRecord.get("statusId") === 6) && hasMissingPtcOrOc) {
                var me = this;
                Ext.Msg.confirm(
                    "Confirm Missing Data",
                    "One or more items do not have project tasks or object classes. " +
                        "Are you sure you want to approve this request?",
                    function (result) {
                        // User confirmed yes
                        if (result == "yes") {
                            me.popupApprovalWindow(newRecord, null, null, false, false);
                        }
                    }
                );
            } else if (newRecord.get("statusId") === 8 && hasMissingPtcOrOc) {
                //for BCH, PTC and OC are required and must be filled before approval
                bcp.util.CommonUtil.showWarning(
                    "Please fill out missing Project Tasks and/or Object Classes in the " +
                        "[Finance Data] tab before make purchase."
                );
                return;
            } else {
                //route via normal window
                this.popupApprovalWindow(newRecord, null, null, false, false);
            }
        }
    },

    //to prepare multiple routes when reviewer = DC or reviewer = DC = DR
    addExpediteRoutings: function (routeDataArray, req) {
        var currentStatus = req.get("statusCode"),
            reqId = req.get("requestId"),
            model = this.getViewModel();

        if (currentStatus == 1) {
            //at submit stage, reviewRoute should be added in routeDataArray by the onSubmit already
            //and we need to check if reviewer and DC is the same person
            if (req.get("reviewerId") == req.get("divisionChiefId")) {
                let dcRouteData = Ext.create("bcp.model.RequestRoute", {
                    requestId: reqId,
                    statusId: 17, // Example: Status ID for 'Pending DC Approval'
                    typeId: 14, // Example: Type ID for 'Sent to DC'
                    routeBy: req.get("reviewerId"),
                    routeTo: req.get("divisionChiefId"),
                    routeToName: req.get("dcName"),
                    notes: "Routing for mission critical approval."
                });
                routeDataArray.push(dcRouteData);
                //check if missionCriticalCatego ==4. if true, after the double routes, next one is to DR, if false, next one is FCO
                if (req.get("missionCriticalCategoryId") == 4) {
                    //get DR and see if DR == DC, if yes, add DR
                    //let drRouteData = this.prepareDirectorRecord();
                    let drRouteData = model.get("drRecord");
                    if (drRouteData && req.get("divisionChiefId") == drRouteData.get("routeTo")) {
                        drRouteData.set("routeBy", req.get("divisionChiefId"));
                        routeDataArray.push(drRouteData);
                    }
                }
            }
        } else if (currentStatus == 5 && req.get("dynamicType") != "ITSO") {
            //at reviewer stage, dcRoute should be added in routeDataArray by approve laready
            //check DR or FCO for possible expedite routing
            if (req.get("missionCriticalCategoryId") == 4) {
                //get DR and see if DR == DC, if yes, add DR
                //let drRouteData = this.prepareDirectorRecord();
                let drRouteData = model.get("drRecord");
                if (drRouteData && req.get("divisionChiefId") == drRouteData.get("routeTo")) {
                    drRouteData.set("routeBy", req.get("divisionChiefId"));
                    routeDataArray.push(drRouteData);
                }
            }
        } else {
            //if DC or DR stage, FCO need more logic and also need to check the certifying checkbox, so no multi-approvals
            //if FCO stage, since FCO and BAO cannot be the same person, so no multi-approvals
            //if BAO stage, BAO need to process the request and make it a purchase so no multi-approvals
        }
    },

    //currentStatus is the status of the request before it is routed to the next step
    //record is what we got from the route tab form data
    //newRecord is a route record we will insert for the next step
    //this method only handles steps at FCO step or after. Previous steps were handled in onSubmit and approve
    setupNextFixedRoute: function (currentStatus, record, newRecord) {
        //NOTE: we need this info to display next route_to in a message if we do AA(approve and add a route)
        //if it turns out to be a dynamic route, the data set here are not used anyway in dynamic route SPs
        if (currentStatus === 16) {
            //issue 649 after FCO approved, it should go to BAO
            this.setRouteValues(newRecord, 2, 6, record.get("bankcardApprovingOfficialId"), record.get("baoName"));
        } else if (currentStatus === 6) {
            //after BAO, it should go to BCH
            this.setRouteValues(newRecord, 3, 7, record.get("bankcardHolderId"), record.get("bhName"));
        } else if (currentStatus === 7) {
            //add a check to see if the totalCost <= approvalAmount
            //because once we allow BHs to edit request during BH review step
            //the BHs can change item price or quantity and the new totalCost
            //could be greater than what the BAO approved. When that happens,
            //the request should be routed back to review and approve again
            if (record.get("totalCost") > record.get("approvalAmount")) {
                this.setRouteValues(newRecord, 2, 6, record.get("bankcardApprovingOfficialId"), record.get("baoName"));
                newRecord.set(
                    "notes",
                    "The total cost of the request is greater than what the BAO approved. Route back to the BAO for re-approval."
                );
            } else {
                this.setRouteValues(newRecord, 4, 8, record.get("bankcardHolderId"), record.get("bhName"));
            }
        }
    },

    setRouteValues: function (record, typeId, statusId, routeTo, routeToName) {
        record.set("typeId", typeId);
        record.set("statusId", statusId);
        record.set("routeTo", routeTo);
        record.set("routeToName", routeToName);
    },

    onChangeReviewerClick: function (button, e, eOpts) {
        this.changeApprover("reviewer");
    },

    onChangeBaoClick: function (button, e, eOpts) {
        this.changeApprover("bao");
    },

    onChangeBhClick: function (button, e, eOpts) {
        this.changeApprover("bh");
    },

    onChangeFcoClick: function (button, e, eOpts) {
        this.changeApprover("fco");
    },
    //since one division only have one DC, we only need to update DC when a request hasn't reach the DC but DC was changed
    onChangeDcClick: function (button, e, eOpts) {
        let model = this.getViewModel(),
            refs = this.getReferences(),
            tfDc = refs.tfDc,
            req = model.get("generalInfo"),
            currentUserDivId = model.get("loggedInUser").divId,
            me = this;
        Ext.Msg.confirm(
            "Confirm Updating Division Chief",
            "Based on the latest role assignment in the NIST Org system, we will update the Mission Critical Approver to the Division Chief of the [Official Requester]. " +
                "Do you want to proceed?",
            function (result) {
                // User confirmed yes
                if (result == "yes") {
                    bcp.util.CommonFunctions.prepareDcRecord(req, currentUserDivId).then(function (newRecord) {
                        if (newRecord) {
                            //update req
                            req.set("divisionChiefId", newRecord.get("routeTo"));
                            req.set("dcName", newRecord.get("routeToName"));
                            //update DC approver field
                            tfDc.setValue(newRecord.get("routeToName"));
                            //update DB data
                            Ext.Ajax.request({
                                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + req.get("requestId"),
                                method: "PUT",
                                scope: this,
                                jsonData: Ext.encode(req.data),
                                failure: function (response) {
                                    // Alert the user about the failure
                                    bcp.util.CommonUtil.showError(
                                        "Failed to process the request. Please try again later."
                                    );
                                }
                            });
                        }
                    });
                }
            }
        );
    },

    onViewAdded: function (component, container, pos, eOpts) {
        var model = this.getViewModel(),
            req = model.get("generalInfo"),
            reqId = req.get("requestId"),
            ors = this.getStore("ouRoles"),
            currentUserDivId = model.get("loggedInUser").divId,
            currentUserOuId = model.get("loggedInUser").ouId,
            commentsField = this.lookupReference("commentsTa"),
            refs = this.getReferences(),
            needFcoRoute = false,
            me = this;

        if (container.xtype == "readonlyrequest" || component.readOnly) {
            commentsField.disable();
        } else {
            commentsField.enable();
        }

        //check if DC is set, if not, try to set it
        if (req.get("divisionChiefId") === 0) {
            bcp.util.CommonFunctions.prepareDcRecord(req, currentUserDivId).then(function (newRecord) {
                if (newRecord) {
                    //edge case when the DC is the requester, we need to set DR as the MCA
                    if (newRecord.get("routeTo") === req.get("requesterId")) {
                        bcp.util.CommonFunctions.prepareDrRecord(req, currentUserOuId).then(function (drRecord) {
                            req.set("divisionChiefId", drRecord.get("routeTo"));
                            req.set("dcName", drRecord.get("routeToName"));
                            Ext.Ajax.request({
                                url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + req.get("requestId"),
                                method: "PUT",
                                scope: this,
                                jsonData: Ext.encode(req.data),
                                failure: function (response) {
                                    // Alert the user about the failure
                                    bcp.util.CommonUtil.showError(
                                        "Failed to set proper approver for the request. Please try again later or contact the app support team."
                                    );
                                }
                            });
                        });
                    } else {
                        req.set("divisionChiefId", newRecord.get("routeTo"));
                        req.set("dcName", newRecord.get("routeToName"));
                        Ext.Ajax.request({
                            url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + req.get("requestId"),
                            method: "PUT",
                            scope: this,
                            jsonData: Ext.encode(req.data),
                            failure: function (response) {
                                // Alert the user about the failure
                                bcp.util.CommonUtil.showError("Failed to set proper approver for the request. Please try again later or contact the app support team.");
                            }
                        });
                    }
                }
            });
        }

        //prepare a DR route record
        bcp.util.CommonFunctions.prepareDrRecord(req, currentUserOuId).then(function (newRecord) {
            if (newRecord) {
                //return newRecord;
                model.set("drRecord", newRecord);
            } else {
                newRecord = Ext.create("bcp.model.RequestRoute", {
                    requestId: reqId
                });
                if (ors.getCount() < 1) {
                    return null; // or handle the case as needed
                } else {
                    let dir = ors.findRecord("roleName", "Director");
                    if (!dir) return null;
                    //route to director
                    let dirPId = dir.get("peopleId");
                    //set comment
                    newRecord.set("notes", "Routing for mission critical Director approval.");
                    me.setRouteValues(newRecord, 15, 18, dirPId, dir.get("fullName"));
                    //return newRecord;
                    model.set("drRecord", newRecord);
                }
            }
        });

        //set default rule to the view model
        model.set("routeRule", Ext.create("bcp.model.RouteRule"));
        //these two lines where moved from onViewBeforeShow to here
        //var record = req;
        //model.set("record", record);
        var pref = bcp.util.CommonFunctions.getDivisionPreferences(req.get("divisionId"));
        if (pref.get("addFcoRoutePrefVal") == "Y") {
            needFcoRoute = true;

            var fcos = Ext.getStore("AllFcos");
            var fcoName = "";
            var focId = 0;

            if (req.get("fcoName")) {
                fcoName = req.get("fcoName");
            } else {
                try {
                    fcoName = fcos.getAt(0).get("displayName");
                    focId = fcos.getAt(0).get("peopleId");
                } catch (ex) {
                    fcoName = "";
                    focId = 0;
                }
                //if first created, the request has no fco yet, try assign one
                req.set("fcoName", fcoName);
                req.set("fundsCertifyingOfficialId", focId);
                Ext.Ajax.request({
                    url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + req.get("requestId"),
                    method: "PUT",
                    scope: this,
                    jsonData: Ext.encode(req.data),
                    failure: function (response) {
                        // Alert the user about the failure
                        bcp.util.CommonUtil.showError("Failed to process the request. Please try again later.");
                    }
                });
            }

            //refs.tfFco.setValue(fcoName);
        } else {
            refs.fcFco.destroy();
        }

        //set this to be used by other methods in the controller
        model.set("needFcoRoute", needFcoRoute);
    },

    //check whether a request need ITSO approval
    requireItsoApproval: function (req) {
        var model = this.getViewModel(),
            //itsoLimit = bcp.config.Runtime.getItsoLimit(),
            isItPurchase = model.get("isItPurchase"),
            isNotBuildInVendor = false,
            isOverItsoLimit = false,
            needItsoApproval = false,
            vendor = Ext.getStore("RequestVendors").first();

        //if (req.get("totalCost") > itsoLimit) {
        //  isOverItsoLimit = true;
        //}
        //any amount of IT Purchase need ITSO approval
        isOverItsoLimit = true;

        if (vendor && vendor.get("refVendorId") != -99) {
            isNotBuildInVendor = true;
            model.set("isItBuying", false);
        } else {
            model.set("isItBuying", true);
        }

        var ouRec = Ext.getStore("Ous").findRecord("ouId", req.get("ouId"));
        var ou = ouRec ? ouRec.get("acronym") : "";
        var defaultITSORoutingWay = !["NCNR", "EL", "ITL"].includes(ou); //issue 623, ITL wants all route to ITSO too

        if (defaultITSORoutingWay) {
            if (isItPurchase && isOverItsoLimit && isNotBuildInVendor) {
                needItsoApproval = true;
            }
        } else {
            //EL & NCNR sends all IT requests to ITSO first, even when they are using the built-in vendor (IT Buying Service)
            if (isItPurchase && isOverItsoLimit) {
                needItsoApproval = true;
            }
        }
        return needItsoApproval;
    },

    onViewBeforeShow: function (component, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            req = model.get("generalInfo"),
            status = req.get("statusCode"),
            dynamicType = req.get("dynamicType"),
            itsoApproved = req.get("itsoApproved"),
            //record = model.get("record"),
            //these two values will be used to check if the logged in user is the
            //person who currently has the route. Craig reported a defect that
            //an approver could use a past email link to access a request that is
            //already routed to someone else. It can happen if the approver is a AO
            //or BCH who had the email in previous step but the request was routed to
            //another BCH. It could also happen if a request is routed to a dynamic
            //approver but the request remain in the same approval step, which allows the
            //fixed approver to have access to it via email link pointing to the pending
            //request view. To prevent past approvers to perform a route when the request
            //is not at their hands, we need to check who has the request currently.
            currentRouteToId = req.get("routeTo"),
            // currentRouteToName = req.get("routeToName"),
            user = model.get("loggedInUser"),
            userId = user.peopleId,
            reqId = req.data.requestId,
            routings = Ext.getStore("RoutingHistory"),
            lastRouteIsDynamic = req.get("isDynamic"),
            rule = this.getStore("routeRules").first(),
            actBtn = this.lookupReference("actBtn"),
            form = refs.form;

        //check purchaseTypeId instead of isItPurchase and set this  to be used by other methods in the controller
        model.set("isItPurchase", req.get("purchaseTypeId") == 2);

        //this.view.getForm();

        //the form.reset sometimes creates a exception if user moves to a different tab and come back
        //not sure why. unless there's a case where we need to reset the value of the form before showing
        //the view, we really don't need to reset it every time it is showed
        /*try {
      form.reset();
    } catch (ex) {
      console.log("caught a exception during form reset.");
      
    }*/

        //issue 585
        if (model.get("needFcoRoute")) {
            refs.fcBao.setFieldLabel("Bankcard Approving Official");
        } else {
            refs.fcBao.setFieldLabel("Bankcard Approving Official and Fund Certifying Official");
        }
        //only check for ITSO if the request is not already approved by ITSO, not currently in ITSO dynamic route
        // and it's in requester(1), reviewer(5), bao(6) and bch(7,8,9) stages
        if (0 == itsoApproved && "ITSO" != dynamicType && [1, 3, 5, 6, 7, 8, 9].includes(status)) {
            //issue 606
            //built-in vendor is for enforce justification or not
            //IT Buying Service (vendorId=-99) is for IT Purchase and this is the one we need to check, not isNotBuildInVendor
            var needItsoApproval = this.requireItsoApproval(req);

            if (needItsoApproval) {
                model.set("needItsoApproval", true);
                //if the user has not submitted the request yet, once submitted, the code that handles submission will route the request to ITSO
                if (![1, 3].includes(status)) {
                    //for any other stage, we determined the request is not ITSO approved but need one (vendor changed, total changed that is greater than limit, ...)
                    refs.routeTb.hide();
                    this.addItsoRoute();
                }
            } else {
                model.set("needItsoApproval", false);
            }
        }

        //changed from record to generalInfo
        form.loadRecord(req);
        model.set("routeRule", rule);
        //somehow the isDynamic value in req is changed if [cancel], so set it in model instead
        model.set("lastRouteIsDynamic", lastRouteIsDynamic);

        //load routing history data
        routings.proxy.url = "/empbc/v1/routes/" + reqId + "/routeHistory";
        routings.load();

        //if in prepare or submit stage, do not show dynamic routing buttons or reassign
        if (rule.data.submit || rule.data.accept) {
            refs.btnApprAddRoute.hide();
            refs.rerouteBtn.hide();
            refs.reassignBtn.hide();
        } else {
            refs.btnApprAddRoute.show();
            refs.rerouteBtn.show();
            refs.reassignBtn.show();
        }

        if (req.data.creatorId !== req.data.requesterId) {
            actBtn.setText("Route to Requester");
        }

        //if a dynamic approver is reviewing, we shouldn't allow the user
        //to change fixed route approvers because reroutes always route the
        //request back to who init the reroute and if the approver, e.g. a
        //reviewer init a reroute and a dynamic approver changes the reviewer
        //to a different person, the logic will still send the req back to
        //the original reviewer and cause problems
        if (lastRouteIsDynamic) {
            //issue 585
            refs.btnChangeReviewer.disable();
            refs.btnChangeDc.disable();
            refs.btnChangeBao.disable();
            refs.btnChangeBh.disable();
            if (refs.btnChangeFco) refs.btnChangeFco.disable();
            //cannot do reassign when in dynamic route
            refs.reassignBtn.hide();
        } else {
            //MB-434 now we allow preparers to change approvers for the requester, or returned/rejected request
            if ([11, 12, 14, 15].includes(status)) {
                refs.btnChangeReviewer.enable();
                refs.btnChangeDc.enable();
                if (refs.btnChangeFco) refs.btnChangeFco.enable();
                refs.btnChangeBao.enable();
                refs.btnChangeBh.enable();
            } else {
                //same step approval update should use the re-assign button
                if ([1, 2, 3, 4].includes(status)) {
                    refs.btnChangeReviewer.enable();
                } else {
                    refs.btnChangeReviewer.disable();
                }
                //when to disable change DC
                if ([1, 2, 3, 4, 5].includes(status)) {
                    refs.btnChangeDc.enable();
                } else {
                    refs.btnChangeDc.disable();
                }

                if ([1, 2, 3, 4, 5, 17, 18].includes(status)) {
                    if (refs.btnChangeFco) refs.btnChangeFco.enable();
                } else {
                    if (refs.btnChangeFco) refs.btnChangeFco.disable();
                }

                if ([1, 2, 3, 4, 5, 16, 17, 18].includes(status)) {
                    refs.btnChangeBao.enable();
                } else {
                    refs.btnChangeBao.disable();
                }
                if ([1, 2, 3, 4, 5, 6, 16, 17, 18].includes(status)) {
                    refs.btnChangeBh.enable();
                } else {
                    refs.btnChangeBh.disable();
                }
            }
        }

        //current user is the requester and the request is not an unsaved one
        if (userId == req.data.requesterId && req.data.requestId !== 0) {
            this.fireViewEvent("showSubmitBtn");
        } else {
            this.fireViewEvent("hideSubmitBtn");
        }

        //show the approved up to field only when BAO is reviewing
        //or if reviewer and BAO is the same person and the reviewer is reviewing(double approvals)
        if (this.isBaoReviewing(req)) {
            refs.fsUpTo.show();
            refs.nfApprAmt.enable();
        } else {
            refs.fsUpTo.hide();
            refs.nfApprAmt.disable();
        }

        //MB-215
        //add dynamic check because dynamic approver cannot make purchase
        if (status == 7 && !lastRouteIsDynamic) {
            refs.btnAppr.setText("Purchased");
            //cannot do approve and add a route when BCH reviews the req
            //because once approved by BCH, it's a purchase and exits from fixed
            //route approving chain.
            refs.btnApprAddRoute.hide();
        } else {
            refs.btnAppr.setText("Approve");
        }

        //if request is received/archived, no buttons at the top of the form
        if (status == 13) {
            refs.routeTb.hide();
        }

        //issue# 583 if request is returned to the preparer, the preparer would use the edit & resend button, which will set
        //the status to 12. The only time the status is 15 in this panel is when the user click the view detail button
        //and go to the route tab. in this case, we don't want the preparer to do anything here
        if (status == 15) {
            refs.routeTb.hide();
        }

        //in ITSO approval stage, no other dynamic routings allowed; ITSO can either approve it or
        // return/reject it.
        if ("ITSO" == dynamicType) {
            refs.btnApprAddRoute.hide();
            refs.rerouteBtn.hide();
            refs.reassignBtn.hide();
            model.set("itsoStep", true);
        } else {
            model.set("itsoStep", false);
        }

        //if an approver accessed the request that is in someone else's hand, no buttons
        //at the top of the form
        if (userId != currentRouteToId) {
            //users with ITSO role can backup each other
            if ("ITSO" == dynamicType) {
                //since emails were sent to mulitple ITSOs, one may try to approve it by click the email link
                //but the request is already approved by another ITSO
                if (itsoApproved) {
                    bcp.util.CommonUtil.showAlert("Info", "The request is already ITSO approved.");
                    refs.routeTb.hide();
                    return;
                }

                if (Ext.getStore("ItsoUsers").findRecord("peopleId", userId)) {
                    //if the current user is not who has the route but has a ITSO role, then allow the "approve" function
                    refs.routeTb.show();
                } else {
                    refs.routeTb.hide();
                }
            } else {
                refs.routeTb.hide();
                refs.btnChangeReviewer.disable();
                refs.btnChangeBao.disable();
                refs.btnChangeBh.disable();
                if (refs.btnChangeFco) refs.btnChangeFco.disable();
            }
        }

        //do not expect the division chief or director to reroute, reassign or approve and add a route
        //issue 618
        if (status == 17 || status == 18) {
            refs.btnApprAddRoute.hide();
            refs.rerouteBtn.hide();
            refs.reassignBtn.hide();
        }
        //issue 677
        if (req && req.data.missionCriticalCategoryId == 4 && [1, 12, 3].includes(status)) {
            refs.fcDr.show();
        } else {
            refs.fcDr.hide();
        }
    },

    updateReq: function (reqId, record, req, model, form, limit) {
        if (record.get("approvalAmount") < record.get("totalCost")) {
            bcp.util.CommonUtil.showWarning(
                "The Approval amount cannot be less than the estimated cost: $" + record.get("totalCost")
            );
            return false;
        } else if (record.get("approvalAmount") < record.get("actualTotalCost")) {
            //add this check in case a request is send back for re-approval when actual cost exceeds approved amount
            bcp.util.CommonUtil.showWarning(
                "The Approval amount cannot be less than the actual cost: $" + record.get("actualTotalCost")
            );
            return false;
        } else if (record.get("approvalAmount") > limit) {
            bcp.util.CommonUtil.showWarning(
                "The Approval amount cannot be greater than the purchase limit: " +
                    Ext.util.Format.number(limit, "$0,000")
            );
            return false;
        } else {
            //approval amount by default is null before approval
            //so when it is either populated with estimated total or the BAO
            //increased it, do a update now
            if (record.get("approvalAmount") >= record.get("totalCost")) {
                Ext.Ajax.request({
                    url: bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId,
                    method: "PUT",
                    scope: this,
                    jsonData: Ext.encode(record.data),
                    success: function (response) {
                        bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                            req = Ext.create("bcp.model.BcpRequest", result.data);
                            model.set("generalInfo", req);
                            form.loadRecord(req);
                        });
                    }
                });
            }
            return true;
        }
    },

    routeInSameStep: function (currentStatus, newRecord, record) {
        if (currentStatus === 5) {
            this.setRouteValues(newRecord, 1, 5, record.get("reviewerId"), record.get("reviewerName"));
        } else if (currentStatus === 6) {
            this.setRouteValues(newRecord, 2, 6, record.get("bankcardApprovingOfficialId"), record.get("baoName"));
        } else if (currentStatus === 7) {
            this.setRouteValues(newRecord, 3, 7, record.get("bankcardHolderId"), record.get("bhName"));
        }
    },

    onApprovalWindow: function (formWindow) {
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
                    var model = this.getViewModel(),
                        returnTo = model.get("returnToView");
                    if (returnTo) {
                        this.redirectTo(returnTo);
                    } else {
                        //send user to home view
                        this.redirectTo("#dashboard");
                    }
                },
                scope: this,
                single: true
            }
        });
        //add window to view
        this.view.add(formWindow);
        //show window
        formWindow.show();
    },

    /**
     * function to make AJAX requests with custom success/failure handling for Routing.
     *
     * @param {Object} config - Configuration object for the AJAX request.
     * @param {Object} config.me - The scope (e.g., 'this' in the controller).
     * @param {string} config.urlParam - The URL endpoint for the request. For all routing, url is fixed
     * @param {string} config.method - The HTTP method (e.g., 'POST', 'PUT'). For all routing, method is POST
     * @param {Object} config.jsonData - The data to send (will be JSON encoded).
     * @param {string} config.successMsg - The success message to display.
     * @param {string} [config.redirectTo] - The route to redirect to after success (optional).
     * @param {string} config.failMsg - The base failure message to display.
     *
     */
    makeRoutingAjaxRequest: function (config) {
        var me = config.me;
        bcp.util.CommonUtil.makeAjaxRequest({
            //url: config.urlParam,
            //method: config.method,
            url: bcp.config.Runtime.getServerBaseUrl() + "routes",
            method: "POST",
            jsonData: config.jsonData,
            scope: me
        })
            .then(function (response) {
                bcp.util.CommonUtil.showAlert("Success", config.successMsg);
                if (config.redirectTo) {
                    me.redirectTo(config.redirectTo, true);
                } else {
                    me.redirectTo("dashboard", true); //default redirect to dashboard
                }
            })
            .catch(function (response) {
                bcp.util.CommonUtil.showAlert(config.failMsg, response.statusText || "An unknown error occurred.");
            });
    }
});
