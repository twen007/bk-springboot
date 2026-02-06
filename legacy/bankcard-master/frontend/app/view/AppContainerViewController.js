Ext.define("bcp.view.AppContainerViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.appcontainer",

    requires: ["Ext.route.Route"],

    listen: {controller: {"#": {unmatchedroute: "onUnmatchedRoute"}}},

    routes: {
        //Okta change
        "*": {before: "onBeforeRoute"},

        createrequest: {
            before: function (action) {
                var store = Ext.getStore("Groups");
                //make sure groups were loaded because we need to use it onWindowAdded
                if (!store.loading) {
                    //if already loaded, show the create req window
                    if (action.resume) action.resume();
                } else {
                    store.load(function (records, operation, success) {
                        //if it is the current view and an action triggers a redirect to the same view
                        //the resume function is null so do a check before
                        if (action.resume) action.resume();
                    });
                }
            },
            action: "onCreateRequest"
        },
        changerequester: "onChangeRequester",
        auditreport: "onAuditReport",
        pcitemreport: {
            before: function (action) {
                this.checkPrivilege(action, ["Property Custodian", "App Admin"]);
            },
            action: "onPCItemReport"
        },
        itemsreport: {
            before: function (action) {
                this.checkPrivilege(action, ["Administrative Officer","Senior Management Advisor","Executive Officer", "ITSO","DITSO", "Administrative Specialist",
                    "Funds Certifying Official","Bankcard Approving Official","Bankcard Holder","App Admin"]);
            },
            action: "onItemsReport"
        },

        eareport: "onEAReport",
        "newrequest/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onSavedRequestResume"
        },
        pendingrequests: {
            before: function (action) {
                var store = Ext.getStore("PendingRequests");
                store.load(function (records, operation, success) {
                    //if it is the current view and an action triggers a redirect to the same view
                    //the resume function is null so do a check before
                    if (action.resume) action.resume();
                });
            },
            action: "onPendingRequests"
        },
        "pendingrequests/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onPendingRequestResume"
        },
        historicalrequests: {
            before: function (action) {
                var store = Ext.getStore("HistoricalRequests");
                store.load(function (records, operation, success) {
                    if (action.resume) action.resume();
                });
            },
            action: "onArchivedRequests"
        },
        "historicalrequests/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onArchivedRequestResume"
        },
        requesttracking: {
            before: function (action) {
                var store = Ext.getStore("ActiveRequests");
                store.load(function (records, operation, success) {
                    if (action.resume) action.resume();
                });
            },
            action: "onActiveRequests"
        },
        "requesttracking/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onActiveRequestResume"
        },
        preparedrequests: {
            before: function (action) {
                var store = Ext.getStore("PreparedRequests");
                store.load(function (records, operation, success) {
                    if (action.resume) action.resume();
                });
            },
            action: "onPreparedRequests"
        },
        "preparedrequests/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onPreparedRequestResume"
        },
        savedrequests: {
            before: function (action) {
                var store = Ext.getStore("SavedRequests");
                store.load(function (records, operation, success) {
                    if (action.resume) action.resume();
                });
            },
            action: "onSavedRequests"
        },
        purchases: {
            before: function (action) {
                var store = Ext.getStore("ProcessedRequests");
                store.proxy.url = "/empbc/v1/requests/processed/" + bcp.util.CommonUtil.currentFiscalYear();
                var storedMissingStmtDt = localStorage.getItem("purchaseViewChkboxMissingStmtDtValue");
                //preserve the user selection in the purchase view if any
                store.getProxy().setExtraParam("showPurchaseWithMissingStmtDt", storedMissingStmtDt);
                store.load(function (records, operation, success) {
                    if (action.resume) action.resume();
                });
            },
            action: "onPurchaseHistory"
        },
        "purchases/:id": {
            before: function (id, action) {
                this.prepareViewData(id, action);
            },
            conditions: {":id": "([0-9]+)"},
            action: "onPurchasesResume"
        },
        page401: "onPage401",
        dashboard: "onHome",
        vendors: "onVendorManagement",
        ibbr: {
            before: function (action) {
                this.checkPrivilege(action, ["App Admin"]);
            },
            action: "onIbbr"
        },
        detailedusers: {
            before: function (action) {
                this.checkPrivilege(action, ["App Admin"]);
            },
            action: "onDetailedusers"
        },
        preferences: "onPreferences",
        //divpreferences: "onDivPreferences",
        divpreferences: {
            before: function (action) {
                //get AO supported divisions
                var store = Ext.getStore("SupportedDivisions"),
                    me = this;
                store.load(function (records, operation, success) {
                    //check AO privilege
                    me.checkPrivilege(action, [
                        "Administrative Officer",
                        "Senior Management Advisor",
                        "Executive Officer"
                    ]);
                });
            },
            action: "onDivPreferences"
        },
        requestsearching: {
            before: function (action) {
                var model = this.getViewModel();
                //start refresh
                this.initRequest(model);
                action.resume();
            },
            action: "onRequestSearching"
        },
        "requestsearching/:id": {
            before: function (id, action) {
                //NOTE: the before action of requestsearching cannot use the prepareViewData method
                //because the result depends on user selected search criteria of the list view.
                //Other request tracking stores have fixed query string
                var model = this.getViewModel();
                //start refresh
                this.initRequest(model);
                action.resume();
            },
            conditions: {":id": "([0-9]+)"},
            action: "onRequestSearchingResume"
        }
    },
    onPage401: function () {
        this.setCurrentView("page401");
    },
    onIbbr: function () {
        this.setCurrentView("wscallfailedrecord");
    },
    onDetailedusers: function () {
        this.setCurrentView("detailedusers");
    },
    onCreateRequest: function () {
        this.setCurrentView("createrequest");
    },

    onChangeRequester: function () {
        this.setCurrentView("changerequester");
    },

    onAuditReport: function () {
        this.setCurrentView("auditreport");
    },

    onPCItemReport: function () {
        this.setCurrentView("pcitemreport");
    },
    onItemsReport: function () {
        this.setCurrentView("itemsreport");
    },

    onEAReport: function () {
        this.setCurrentView("eareport");
    },

    onSavedRequestResume: function (id) {
        this.setCurrentView("newrequest", id);
    },

    onPendingRequests: function () {
        this.setCurrentView("pendingrequests");
    },

    onPendingRequestResume: function (id) {
        this.setCurrentView("pendingrequests", id);
    },

    onArchivedRequests: function () {
        this.setCurrentView("historicalrequests");
    },

    onArchivedRequestResume: function (id) {
        this.setCurrentView("historicalrequests", id);
    },

    onActiveRequests: function () {
        this.setCurrentView("requesttracking");
    },

    onActiveRequestResume: function (id) {
        this.setCurrentView("requesttracking", id);
    },

    onPreparedRequests: function () {
        this.setCurrentView("preparedrequests");
    },

    onPreparedRequestResume: function (id) {
        this.setCurrentView("preparedrequests", id);
    },

    onSavedRequests: function () {
        this.setCurrentView("savedrequests");
    },

    onPurchaseHistory: function () {
        this.setCurrentView("purchases");
    },

    onPurchasesResume: function (id) {
        this.setCurrentView("purchases", id);
    },

    onHome: function () {
        this.setCurrentView("dashboard");
    },

    onVendorManagement: function () {
        this.setCurrentView("vendors");
    },

    onPreferences: function () {
        this.setCurrentView("preferences");
    },

    onDivPreferences: function () {
        this.setCurrentView("divpreferences");
    },

    onRequestSearching: function () {
        this.setCurrentView("requestsearching");
    },

    onRequestSearchingResume: function (id) {
        this.setCurrentView("requestsearching", id);
    },

    initRequest: function (model) {
        /*
        init a request record, store it in the model, and
        set init values of the request that controls different views and
        functions in the app
        */
        model.set("currentRequest", Ext.create("bcp.model.BcpRequest", {requestId: 0}));

        //when actions within the view or its subview finishes, it should return to. default o dashboard
        model.set("returnToView", "dashboard");
        //data related to Items and price
        model.set("hasShipping", false);
        model.set("overPurchaseLimit", false);
        model.set("isShoppingCart", false);
        model.set("hasItem", false);
        //data related to file
        model.set("fileCount", "");
        //data related to vendor
        model.set("hasVendor", false);
        //enable BH function in purchase view
        model.set("isPurchase", false);

        //when statusCode is 8, 9, 13, the request is purchased by BCH
        model.set("requestPurchased", false);
        //when statusCode is 13, the request is archived by BCH means no edit at all!
        model.set("requestFinal", false);
    },

    checkPrivilege: function (action, allowedRoles) {
        //use this method when we only need to check for access control
        if (bcp.util.CommonUtil.isUserInRole(allowedRoles)) {
            action.resume();
        } else {
            //user not authorized
            this.redirectTo("page401");
        }
    },

    prepareViewData: function (reqId, action) {
        /*
        This method is called by the before action method in routing and used to get the request
        record and other records(items, vendors, justifications) associated with the request. Once
        the stores are loaded, the action will resume to create the views that use these store.
        Also, some records need to be set in viewmodel which is also used by the view. In some case, the
        request was created without approving chain data. we also need to do a check here and if
        we found that data is missing, we can update the request record with the approving chain data
        in the default route
        */
        var model = this.getViewModel(),
            userId = model.get("loggedInUser").peopleId,
            currentReqStore = Ext.getStore("CurrentRequest"),
            url = bcp.config.Runtime.getServerBaseUrl() + "requests/" + reqId,
            fileStore = Ext.getStore("RequestFiles"),
            itemStore = Ext.getStore("RequestItems"),
            vendorStore = Ext.getStore("RequestVendors"),
            JustificationStore = Ext.getStore("RequestJustifications"),
            routeStore = Ext.getStore("DefaultRoutes"),
            defaultRouteModel = routeStore.first(),
            defaultRoute = null,
            needUpdt = false;

        if (defaultRouteModel && defaultRouteModel.data) {
            defaultRoute = defaultRouteModel.data;
        }

        //start refresh
        this.initRequest(model);
        //load the request record
        currentReqStore.proxy.url = url;
        currentReqStore.load({
            scope: this,
            callback: function (records, operation, success) {
                //TODO: if a user use the link in email notification, the request can be find if the user
                //is in the same division as the request even when the user currently doesn't have the request in the view (e.g. pending or purchase)
                //that is requested. this should be restricted

                //since request id is used, it should return only one record or
                //return nothing if record not found or the user doesn't have privilege to access
                //the record
                var rec = currentReqStore.first();
                if (rec && rec.get("requestId") > 0) {
                    //if the logged in user is the requester of the request
                    if (userId === rec.data.requesterId) {
                        //update NIST Org route info in the request if route data is missing
                        //if NIST Org return nothing, defaultRoute will be {} and no need to update
                        if (defaultRoute && Object.getOwnPropertyNames(defaultRoute).length > 0) {
                            if (rec.get("reviewerId") === 0 && defaultRoute.reviewer) {
                                rec.set("reviewerId", defaultRoute.reviewer.personId);
                                rec.set("reviewerName", defaultRoute.reviewer.name);
                                needUpdt = true;
                            }
                            if (rec.get("bankcardHolderId") === 0 && defaultRoute.bankcardHolder) {
                                rec.set("bankcardHolderId", defaultRoute.bankcardHolder.personId);
                                rec.set("bhName", defaultRoute.bankcardHolder.name);
                                needUpdt = true;
                            }
                            if (
                                rec.get("bankcardApprovingOfficialId") === 0 &&
                                defaultRoute.bankcardApprovingOfficial
                            ) {
                                rec.set("bankcardApprovingOfficialId", defaultRoute.bankcardApprovingOfficial.personId);
                                rec.set("baoName", defaultRoute.bankcardApprovingOfficial.name);
                                needUpdt = true;
                            }
                        }

                        if (needUpdt) {
                            Ext.Ajax.request({
                                url: url,
                                method: "PUT",
                                scope: this,
                                jsonData: Ext.encode(rec.data),
                                success: function (response) {
                                    bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {});
                                }
                            });
                        }
                    }

                    //save the loaded request in viewmodel
                    model.set("currentRequest", rec);

                    //check if creator and requester is the same person, if not
                    //load requester's user roles
                    if (rec.data.creatorId != rec.data.requesterId) {
                        Ext.Ajax.request({
                            url: bcp.config.Runtime.getServerBaseUrl() + "users/roles/" + rec.data.requesterId,
                            method: "GET",
                            success: function (response) {
                                bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                    var roles = result.data;
                                    Ext.getStore("RequesterUserRoles").loadRawData(roles);
                                });
                            }
                        });
                    }

                    //load request vendor
                    vendorStore.proxy.url = "/empbc/v1/vendors/" + rec.data.requestId;
                    vendorStore.load();

                    //load request justification
                    JustificationStore.proxy.url = "/empbc/v1/vendors/justification/" + rec.data.requestId;
                    JustificationStore.load();

                    //load request items
                    itemStore.proxy.url = "/empbc/v1/requests/" + rec.data.requestId + "/items";
                    itemStore.load();

                    //the request could have files attached
                    fileStore.proxy.url = "/empbc/v1/requests/" + rec.data.requestId + "/attachments";
                    fileStore.load();

                    //continue the action
                    if (action) action.resume();
                } else {
                    if (action) action.stop(true);
                    bcp.util.CommonUtil.showError("No request record was found with id: " + reqId);
                    this.redirectTo("dashboard", true);
                    //me.redirectTo("#page404");
                }
            }
        });
    },

    setCurrentView: function (hashTag, id) {
        //debugger;
        hashTag = (hashTag || "").toLowerCase();

        if (hashTag == "dashboard") {
            hashTag = "bcpdashboard";
        }
        var me = this,
            refs = me.getReferences(),
            mainCard = refs.mainCardPanel,
            mainLayout = mainCard.getLayout(),
            navigationList = refs.navigationTreeList,
            store = navigationList.getStore(),
            node = store.findNode("routeId", hashTag),
            //store.findNode('viewType', hashTag),
            //view = (node && node.get('viewType')) || 'page404',

            //TODO: if use hashTag directly, need to add privilege check here to prevent
            //non authorized user to init a protected view; e.g. a non BCH put #purchases
            //in the url to access the purchase view
            view = hashTag,
            lastView = me.lastView,
            existingItem = mainCard.child("component[routeId=" + hashTag + "]"),
            newView;

        // debugger;
        // Kill any previously routed window
        //if (lastView && lastView.isWindow) {
        //changed to always kill the last view so when switch from view to view
        //subcomponents won't exists in multiple views and cause duplicate ids error
        if (lastView) {
            lastView.destroy();
        }

        lastView = mainLayout.getActiveItem();

        try {
            if (!existingItem) {
                newView = Ext.create({
                    xtype: view,
                    routeId: hashTag, // for existingItem search later
                    hideMode: "offsets",
                    requestId: id
                });
            } else {
                //if(existingItem.routeId!=='faqs'){
                existingItem.destroy();
                existingItem = Ext.create({
                    xtype: view,
                    routeId: hashTag, // for existingItem search later
                    hideMode: "offsets",
                    requestId: id
                });
                //}
            }
        } catch (ex) {
            newView = existingItem = Ext.create({
                xtype: "page404",
                routeId: "page404",
                hideMode: "offsets",
                requestId: id
            });
        }

        if (!newView || !newView.isWindow) {
            // !newView means we have an existing view, but if the newView isWindow
            // we don't add it to the card layout.
            if (existingItem) {
                // We don't have a newView, so activate the existing view.
                if (existingItem !== lastView) {
                    mainLayout.setActiveItem(existingItem);
                }
                newView = existingItem;
            } else {
                // newView is set (did not exist already), so add it and make it the
                // activeItem.
                Ext.suspendLayouts();
                mainLayout.setActiveItem(mainCard.add(newView));
                Ext.resumeLayouts(true);
            }
        } else {
            //if a window, add it to the viewport
            this.getView().add(newView);
        }

        //if an id is included in the url, it should go to the detail view of an request
        //and we do not want to change selection in the menu because it triggers an action
        //that will send users to the list view instead of the detail view
        if (id) {
            navigationList.setSelection(null);
        } else if (!node) {
            navigationList.setSelection(null);
        } else {
            navigationList.setSelection(node);
        }

        if (newView.isFocusable(true)) {
            newView.focus();
        }

        if (newView.isWindow) {
            newView.show();
        }

        me.lastView = newView;
    },

    onUnmatchedRoute: function () {
        this.setCurrentView("page404");
    },

    //Okta change
    //if we rely on the client to take whatever url the okta valve is redirect to
    //the redirect url will not contain any fragment(#something)
    //if change links to use querystring style #?subview=requesttracking/28394 instead of #requesttracking/28394
    //we will then get ?subview=requesttracking/28394 back but extjs don't know how to route it
    //the method below would try to convert it back to fragment style and render the correct view

    //note 1: if we  do the convert in a filter on the server side, we would use the sendRedirect method, which
    //always trigger a full page load meaning the app would reload again and didn't find a way to prevent it

    //note 2: if we url /app/#?subview=requesttracking/28394 instead of /app/?subview=requesttracking/28394
    //the logic in the "if" will run and send the user to the proper view without a full page reload
    // but the whole "#?subview=requesttracking/28394" thing would be considered as a hash and ignored by the server code
    //if we use /app/?subview=requesttracking/28394, it uses logic in the "else if" and would cause a full page reload
    //because when you pass a parameter in the url, the browser always render a new page.
    onBeforeRoute: function (action) {
        //debugger;
        var hash = window.location.hash,
            queryString = window.location.search,
            params = new URLSearchParams(queryString),
            subviewParamValue = params.get("subview"),
            value = "";
        if (hash.indexOf("?subview=") !== -1) {
            value = hash.split("=")[1];
            value = value.replace(/&sessionstate.*?(?=&|$)/g, "");
            window.location.hash = value;
            //this.setCurrentView(value);
        } else if (subviewParamValue) {
            //window.location.href = window.location.pathname +  '#' + value;
            //window.location.search="";
            //window.location.hash =subviewParamValue;
            //this.redirectTo(subviewParamValue, true);
            //action.resume();
            window.history.replaceState(null, null, window.location.pathname + "#" + subviewParamValue);

            var parts = subviewParamValue.split("/");
            var viewName = parts[0];
            var recordId = parts.length > 1 ? parts[1] : null;
            if (recordId) {
                this.setCurrentView(viewName, recordId);
            } else {
                this.setCurrentView(viewName);
            }
        } else {
            action.resume();
        }
    },

    onNavBtnClick: function (button, e, eOpts) {
        var me = this,
            refs = me.getReferences(),
            treelist = refs.navigationTreeList,
            ct = refs.mainContainerWrap,
            collapsing = !treelist.getMicro(),
            //change to use a logo png file, set min width to 88 from 64
            new_width = collapsing ? 70 : 250;

        button.setIconCls(collapsing ? "far fa-caret-square-right" : "far fa-caret-square-left");

        treelist.setMicro(collapsing);
        //change to use a logo png file, no need to re-position the button
        //refs.bankcardLogo.setWidth(new_width);
        ct.setWidth(new_width);

        // IE8 has an odd bug with handling font icons in pseudo elements;
        // it will render the icon once and not update it when something
        // like text color is changed via style addition or removal.
        // We have to force icon repaint by adding a style with forced empty
        // pseudo element content, (x-sync-repaint) and removing it back to work
        // around this issue.
        // See this: https://github.com/FortAwesome/Font-Awesome/issues/954
        // and this: https://github.com/twbs/bootstrap/issues/13863
        if (Ext.isIE8) {
            this.repaintList(treelist, collapsing);
        }
    },

    onFaqClick: function (button, e, eOpts) {
        window.open("https://nistgov.atlassian.net/wiki/spaces/MMLAdminPortal/pages/14517308/MML+Bankcard+FAQs+Help");
        return;
    },

    onHelpClick: function (button, e, eOpts) {
        window.open("mailto:MML.SystemsHelp@nist.gov?subject=Question About Bankcard Purchase Application " + "&body=");
    },

    onSearchClick: function (button, e, eOpts) {
        this.redirectTo("requestsearching", true);
    },

    onHomeClick: function (button, e, eOpts) {
        this.redirectTo("dashboard", true);
    },

    onLogoutClick: function (button, e, eOpts) {
        var logoutUrl = bcp.config.Runtime.getServerBaseUrl() + "users/logout";
        Ext.MessageBox.confirm(
            "Confirm",
            "Are you sure you want to log out?",
            function (btn, text) {
                if (btn === "yes") {
                    Ext.Ajax.request({
                        url: logoutUrl,
                        method: "GET",
                        scope: this,
                        success: function (response) {
                            //remove the browser's default popup
                            window.onbeforeunload = null;
                            window.location.href = "/empbc/login.html";
                        },
                        failure: function (form, response) {
                            Ext.Msg.alert("Log out Failed", response.result.statusText);
                        }
                    });
                }
            },
            this
        );
    },

    onTreeListItemClick: function (sender, info, eOpts) {
        //if a user click the same menu item, it won't trigger the selection change
        //so added this method to handle it and force a same route (view refresh)
        var record = info.node,
            to = record && (record.get("routeId") || record.get("viewType"));

        if (to) {
            //use the true flag to force a route even if it's the same as current route
            this.redirectTo(to, true);
        }
    },

    onContainerAdded: function (component, container, pos, eOpts) {
        //set the user and other userful info in the main viewmodel
        //so all child viewmodel can access them
        //also set listeners on revelvant stores' onload event
        var model = this.getViewModel(),
            auStore = Ext.getStore("AuthUser"),
            user = auStore.first(),
            reqVendorStore = Ext.getStore("RequestVendors"),
            vendorStore = Ext.getStore("SharedVendors"),
            detailedUsersStore = Ext.getStore("DetailedUsers"),
            JustificationStore = Ext.getStore("RequestJustifications"),
            fileStore = Ext.getStore("RequestFiles"),
            itemStore = Ext.getStore("RequestItems"),
            auStore = Ext.getStore("AuthUser"),
            delegateStore = Ext.getStore("Delegates"),
            detaileeStore = Ext.getStore("Detailees"),
            dtlStore = Ext.getStore("UserDetailedPrivileges"),
            grpStore = Ext.getStore("Groups"),
            user = auStore.first(),
            me = this,
            refs = this.getReferences();

        //issue 665 use css to add scrolling text notes 25secs * 3 times, then clear the text so it won't irritate the users
        setTimeout(() => {
            refs.sysNotes.setValue(""); // Clear the text after 75 secs
        }, 75000);

        model.set("loggedInUser", user.data);
        model.set("purchaseLimit", bcp.config.Runtime.getPurchaseLimit());
        // Set delegating property for delegation button label
        model.set("delegating", !!user.data.delegating);

        //if in delegating mode
        if (auStore.first().data.delegating == true) {
            //detailee mode cannot be used when delegating
            refs.detaileeBtn.hide();
            var profile = delegateStore.first();
            refs.delegationMenu.add({
                text: "You (" + profile.data.lastName + ", " + profile.data.firstName + ")",
                username: profile.data.username,
                firstName: profile.data.firstName,
                lastName: profile.data.lastName,
                clearDelegation: true
            });
        } else {
            if (delegateStore.getCount() > 0) {
                delegateStore.each(function (record) {
                    refs.delegationMenu.add({
                        text: record.data.lastName + ", " + record.data.firstName,
                        username: record.data.username,
                        firstName: record.data.firstName,
                        lastName: record.data.lastName,
                        clearDelegation: false
                    });
                }, me);
            } else {
                refs.delegationBtn.hide();
            }
        }

        //if detailee mode, show the detailee group when clicked the menu
        if (user.data.detaileeMode == true) {
            //used for change detailee button icon
            model.set("detaileeMode", true);
            //cannot use delegating function while in detailee mode
            refs.delegationBtn.hide();
            profile = detaileeStore.first();
            refs.detaileeMenu.add({
                text:
                    "Exit Detailee Mode (" +
                    profile.data.groupCode.substring(0, 3) +
                    "." +
                    profile.data.groupCode.substring(3) +
                    ")",
                clearDetailee: true
            });
        } else {
            //show detailee button when the user has detailed privilege
            //check current mode; if not detailee mode, load detailee groups according to the detaillee data set for the user
            //since we need group data first, put logic in the group store onload to make sure the group data
            //is available when the logic runs
            if (dtlStore.getCount() > 0 || bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
                grpStore.load(function (records, operation, success) {
                    model.set("detaileeMode", false);

                    grpStore.clearFilter();

                    if (bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
                        Ext.each(grpStore.getRange(), function (record) {
                            refs.detaileeMenu.add({
                                text: record.data.shortName,
                                groupName: record.data.shortName,
                                groupId: record.data.groupId,
                                clearDetailee: false
                            });
                        });
                    } else {
                        //MB-545 check user profile to see if the user has detail privilege for a different org
                        //if yes, show groups that the user is allowed to create requests to
                        for (var i = 0; i < dtlStore.totalCount; i++) {
                            //get the detailed privilege
                            var ud = dtlStore.getAt(i);
                            //remove any filter

                            //depending on accese level, decide what filter to use
                            if (!bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
                                if (ud.data.accessOu) {
                                    grpStore.addFilter({operator: "==", property: "ouId", value: ud.data.ouId});
                                } else if (ud.data.accessDiv) {
                                    grpStore.addFilter({
                                        operator: "==",
                                        property: "divisionId",
                                        value: ud.data.divisionId
                                    });
                                } else if (ud.data.accessGroup) {
                                    grpStore.addFilter({operator: "==", property: "groupId", value: ud.data.groupId});
                                }
                            }

                            //generate menu items
                            Ext.each(grpStore.getRange(), function (record) {
                                refs.detaileeMenu.add({
                                    text: record.data.shortName,
                                    groupName: record.data.shortName,
                                    groupId: record.data.groupId,
                                    clearDetailee: false
                                });
                            });

                            //remove filter afterwards
                            grpStore.clearFilter();
                        }
                    }
                });
            } else {
                refs.detaileeBtn.hide();
            }
        }

        reqVendorStore.on("load", function (operation, records, success) {
            model.set("hasVendor", records.length > 0);
        });

        JustificationStore.on("load", function (operation, records, success) {
            model.set("hasJustification", records.length > 0);
        });

        //all users can access and use shared vendors
        vendorStore.load();

        //only admin can manage detailees
        if (bcp.util.CommonUtil.isUserInRole(["App Admin"])) {
            detailedUsersStore.load();
        }

        fileStore.on("load", function (operation, records, success) {
            var count = records.length;
            model.set("fileCount", count > 0 ? count : "");
            //issue 610
            var req = Ext.getStore("CurrentRequest").first();
            if (req && req.data.requestId > 0) {
                //get latest req data first, then check if we need to add the checklist
                bcp.util.CommonFunctions.loadBcpRequest(req.data.requestId).then(function (req) {
                    if (req) {
                        if (req.get("vendors") == "IT Buying Service" && req.get("fy") >= 25) {
                            var store = Ext.getStore("RequestFiles");
                            //if already has it or user uploaded a IT Checklist before, do not added it
                            if (
                                store.findRecord("fileId", -999) == null &&
                                store.findRecord("fileCategoryId", 11) == null
                            ) {
                                //add a prefilled IT Checklist record if IT Buying Sevice is the vendor
                                //with -999 as id so users don't have to upload duplicate files for IT Buying Sevice requests
                                store.add(
                                    Ext.create("bcp.model.BcpFile", {
                                        requestId: req.get("requestId"),
                                        fileId: -999,
                                        fileName: "Prefilled IT Compliance in Acquisition Checklist.pdf",
                                        fileCategoryId: 11,
                                        fileType: "application/pdf",
                                        fileSize: 400000,
                                        categoryName: "IT Compliance Checklist"
                                    })
                                );
                                count++;
                                model.set("fileCount", count);
                            }
                        }
                    }
                });
            }
        });

        itemStore.on("load", function (operation, records, success) {
            var hasShipping = false,
                isShoppingCart = false,
                hasItem = false,
                limit = bcp.config.Runtime.getPurchaseLimit(),
                req = null,
                approvedAmount = 0,
                actualTotal = 0;

            for (var i = 0; i < records.length; i++) {
                if (records[i].get("isShippingCost")) {
                    hasShipping = true;
                } else if (records[i].get("shoppingCartFileId") > 0) {
                    isShoppingCart = true;
                    hasItem = true;
                } else {
                    hasItem = true;
                }
            }
            //float point calculation could produce a number with many decimal points so we need to round it
            //before comparision. toFixed(2) convert it to a string first, then parseFloat it back to number and
            //compare them

            //add check for actual
            actualTotal = this.sum("actualAmount");
            if (actualTotal > 0) {
                actualTotal = actualTotal.toFixed(2);
            }

            req = Ext.getStore("CurrentRequest").first();

            if (req && req.data.requestId > 0) {
                approvedAmount = req.get("approvalAmount");
                if (approvedAmount > 0) {
                    approvedAmount = approvedAmount.toFixed(2);
                }
                var status = req.get("statusCode");

                //only check after BAO set a real approved amount(steps before that use estimated total as approved amount)
                // and approved the request
                if (
                    [7, 8, 9, 13].includes(status) &&
                    parseFloat(approvedAmount) > 0 &&
                    parseFloat(approvedAmount) < parseFloat(actualTotal)
                ) {
                    //MB-428 auto trigger this popup when items loads and condition met
                    var newRoute = Ext.create("bcp.model.RequestRoute", {
                        requestId: req.get("requestId"),
                        statusId: 6
                    });
                    newRoute.set("typeId", 2);
                    newRoute.set("routeTo", req.get("bankcardApprovingOfficialId"));
                    newRoute.set("routeToName", req.get("baoName"));
                    newRoute.set("routeToName", req.get("baoName"));
                    newRoute.set(
                        "notes",
                        "The total cost: $" +
                            actualTotal +
                            ", exceeds the approved amount: $" +
                            approvedAmount +
                            ". Route back to the Bankcard Approving Offcial for Re-approval"
                    );
                    // Ask user to confirm this action
                    Ext.Msg.confirm(
                        "Re-approval Confirmation",
                        "The total cost of the request is $" +
                            actualTotal +
                            ", which is more than the approved amount of $" +
                            approvedAmount +
                            ". Do you want to route this request to the Bankcard Approving Official for re-approval?",
                        function (result) {
                            // User confirmed yes
                            if (result == "yes") {
                                Ext.Ajax.request({
                                    url: bcp.config.Runtime.getServerBaseUrl() + "routes",
                                    method: "POST",
                                    jsonData: Ext.encode(newRoute.data),
                                    scope: this,
                                    success: function (response) {
                                        bcp.util.CommonUtil.ajaxSuccessHandler(response, function (result) {
                                            Ext.Msg.alert("Success", "Your request was routed successfully.");
                                            me.redirectTo("purchases", true);
                                        });
                                    },
                                    failure: function (response) {
                                        Ext.Msg.alert("Failed", response.result.statusText);
                                    }
                                });
                            }
                        }
                    );
                }
            }

            model.set("actualTotal", parseFloat(actualTotal));
            model.set("overPurchaseLimit", this.sum("amount") > limit);
            model.set("isShoppingCart", isShoppingCart);
            model.set("hasShipping", hasShipping);
            model.set("hasItem", hasItem);
        });

        this.initRequest(model);
    }
});
