Ext.define('bcp.view.PendingRequestViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.pendingrequests',

    showView: function (view) {
        var model = this.getViewModel(),
            layout = this.getView().getLayout(),
            refs = this.getReferences(),
            list = refs.list,
            detail = refs.detail;

        if (view === 'list') {
            layout.setActiveItem(list);
        } else {
            layout.setActiveItem(detail);
        }

        //TODO: do we still need this?
        model.set('viewState', view);
    },

    onBackToList: function (button, e, eOpts) {
        this.redirectTo('pendingrequests', true);
    },

    onViewDetail: function (button, e, eOpts) {
        var refs = this.getReferences(),
            list = refs.requestList,
            req = list.selection;

        if (req) {
            var reqId = req.get('requestId');
            this.redirectTo('#pendingrequests/' + reqId);
        }
    },

    onEditResubmit: function (button, e, eOpts) {
        var model = this.getViewModel(),
            rec = model.get('record'),
            requestId = rec.data.requestId,
            newRecord = Ext.create('bcp.model.RequestRoute', {
                requestId: requestId,
                statusId: 1
            }),
            me = this;

        //you can only resubmit if the request is rejected and routed back to you
        if (rec.data.statusCode === 11 || rec.data.statusCode === 14) {
            //check if the request is prepared by someone; if yes, since the returned request is going to the requester, not
            //the preparer, the type should be 12 - prepared and status should be 3. This change would allow the request to appear
            //in the pending view, which fix the issue of lossing the request if the requester go to other view after click "Edit & Re-submit"
            //and cannot access the request from any view after that. However, it doesn't fix another issue
            //if the purpose of reject or return is for the requester to fix some data, it won't work because the requester doesn't have edit
            //right, only review and approve/reject. The code would allow the requester to reject it back to preparer who can edit though

            //BANK-564, after discussion with Megan, decided to route "return for info" for prepared requests to preparer
            //"reject" requests will still route to requester. So in this case, requesters won't get rejected prepared requests
            //and the condition below is not necessary. However, just keep the code for now in case the requirement need more change in the future
            if (rec.data.creatorId != rec.data.requesterId) {
                newRecord.set('typeId', 12);
                newRecord.set('statusId', 3);
            } else {
                newRecord.set('typeId', 0);
            }

            newRecord.set('routeTo', rec.get('requesterId'));
            newRecord.set('routeToName', rec.get('requesterName'));
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + 'routes',
                method: 'POST',
                jsonData: Ext.encode(newRecord.data),
                scope: this,
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(
                        response,
                        function (result) {
                            me.redirectTo('#newrequest/' + requestId);
                        }
                    );
                },
                failure: function (form, response) {
                    Ext.Msg.alert('Route Failed', response.result.statusText);
                }
            });
        } else {
            Ext.Msg.alert(
                'Warning',
                'Someone is working on the request and you cannot edit it.'
            );
        }
    },

    onEditResend: function (button, e, eOpts) {
        var rec = this.getViewModel().get('record'),
            requestId = rec.data.requestId,
            newRecord = Ext.create('bcp.model.RequestRoute', {
                requestId: requestId,
                statusId: 12
            }),
            me = this;
        //issue# 583 you can only resend if the request is returned to the preparer
        if (rec.data.statusCode === 15) {
            newRecord.set('typeId', 0);
            newRecord.set('routeTo', rec.get('creatorId'));
            newRecord.set('routeToName', rec.get('creatorName'));
            Ext.Ajax.request({
                url: bcp.config.Runtime.getServerBaseUrl() + 'routes',
                method: 'POST',
                jsonData: Ext.encode(newRecord.data),
                scope: this,
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(
                        response,
                        function (result) {
                            me.redirectTo('#newrequest/' + requestId);
                        }
                    );
                },
                failure: function (form, response) {
                    Ext.Msg.alert('Route Failed', response.result.statusText);
                }
            });
        } else {
            Ext.Msg.alert(
                'Warning',
                'Someone is working on the request and you cannot edit it.'
            );
        }
    },

    onReload: function (button, e, eOpts) {
        Ext.getStore('PendingRequests').load();
    },

    onSelect: function (rowmodel, record, index, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            userId = model.get('loggedInUser').peopleId,
            requesterId = record.data.requesterId,
            status = record.data.statusCode;

        //binding doesn't work for these ui controls, so use code control it here
        if (status === 11 || status == 14) {
            if (userId === requesterId) {
                //current user is the requester, enable the resubmit
                refs.btnResubmit.enable();
            } else {
                //current user is the creator, enable the resend
                refs.btnResend.enable();
            }
        } else if (status == 15) {
            refs.btnResend.enable();
        } else {
            refs.btnResubmit.disable();
            refs.btnResend.disable();
        }

        //reload history data with the selected record
        this.getReferences()
            .historyview.getController()
            .loadHistoryById(record.get('requestId'));
    },

    onViewAdded: function (component, container, pos, eOpts) {
        var refs = this.getReferences(),
            detail = refs.detail,
            model = this.getViewModel(),
            //userId = model.get('loggedInUser').peopleId,
            rec = null;

        //when actions within the view or its subview finishes, it should return to
        model.set('returnToView', this.view.routeId);

        //check if id is passed in
        if (component.requestId && component.requestId !== 0) {
            //the request record should be prepared by the before action already
            rec = model.get('currentRequest');

            if (rec) {
                model.set('generalInfo', rec);
                model.set('record', rec);

                //added code for decide whether to show actual total & approval amount
                var status = rec.get('statusCode');
                var type = rec.get('routeTypeId');
                var divPrefs = Ext.getStore('DivisionPreferences');
                var pref = divPrefs.findRecord('divId', rec.get('divisionId'));

                if (status == 8 || status == 9 || status == 13) {
                    model.set('showActual', true);
                } else {
                    model.set('showActual', false);
                }

                if (status == 7 || status == 8 || status == 9 || status == 13) {
                    model.set('showApprovalAmount', true);
                } else {
                    model.set('showApprovalAmount', false);
                }

                if (detail.items.length === 0) {
                    Ext.suspendLayouts();

                    detail.add({
                        xtype: 'generalinfopanel',
                        readOnly: true,
                        title: 'Request Summary'
                    });

                    detail.add({
                        xtype: 'vendorpanel',
                        title: 'Vendor',
                        readOnly: false
                    });

                    //BANK-488
                    //requesters access prepared ones in this view so make sure if requesters use this view
                    //the div preference is applied. for other approvers in this view, always show it
                    if (
                        (type == 12 &&
                            (!pref ||
                                pref.get('justPrefVal') == 'O' ||
                                pref.get('justPrefVal') == 'Y')) ||
                        type != 12
                    ) {
                        detail.add({
                            xtype: 'justificationpanel',
                            readOnly: false
                        });
                    }

                    detail.add({
                        xtype: 'itempanel',
                        title: 'Items',
                        readOnly: true
                    });

                    detail.add({
                        xtype: 'fileattachmentgrid',
                        title: 'File Attachments',
                        readOnly: false,
                        tabConfig: {bind: {badgeText: '{fileCount}'}}
                    });

                    //BANK-488
                    if (
                        (type == 12 &&
                            (!pref ||
                                pref.get('financePrefVal') == 'O' ||
                                pref.get('financePrefVal') == 'Y')) ||
                        type != 12
                    ) {
                        detail.add({
                            xtype: 'financepanel',
                            title: 'Finance Data',
                            readOnly: false
                        });
                    }

                    var routeTab = Ext.create('bcp.view.RoutePanel', {
                        title: 'Routes',
                        readOnly: true
                    });

                    detail.add(routeTab);

                    //BANK-488
                    var routeController = routeTab.lookupController();
                    //if BAO reviews the request, apply auto up to amount if set in div prefs
                    if (routeController.isBaoReviewing(rec)) {
                        if (
                            pref.get('upToPrefVal') == 'Y' &&
                            rec.get('approvalAmount') == 0
                        ) {
                            rec.set(
                                'approvalAmount',
                                pref.get('upToPrefValDetail') +
                                    rec.get('totalCost')
                            );
                        }
                    }

                    Ext.resumeLayouts();
                }

                /*if (status === 11 || status == 14) {
                    if (userId === rec.data.requesterId) {
                        //current user is the requester, enable the resubmit
                        refs.btnResubmit.enable();
                    } else {
                        //current user is the creator, enable the resend
                        refs.btnResend.enable();
                    }

                } else {
                    refs.btnResubmit.disable();
                    refs.btnResend.disable();
                }*/

                if (status === 11 || status == 14) {
                    refs.btnResubmit.enable();
                } else if (status == 15) {
                    refs.btnResend.enable();
                } else {
                    refs.btnResubmit.disable();
                    refs.btnResend.disable();
                }

                this.showView('detail');
            }
        } else {
            this.showView('list');
        }
    },

    //add this handler because when BCHs are in the pending view, they can
    //modify justifications and we need this to trigger the save justification action in case they tab away
    onBeforeTabChange: function (tabPanel, newCard, oldCard, eOpts) {
        if (oldCard && oldCard.reference == 'justificationPanel') {
            var jcontroller = oldCard.lookupController(),
                jform = jcontroller.lookupReference('form'),
                jSaveBtn = jcontroller.lookupReference('btnSave');
            //only check form valid if user started filling the form(select a value for the first question isCv);
            //otherwise, let it go through without showing the validation error. This is because when switching tab, sometimes
            //the hasShipping prompt could cause the user to stay in a empty justification tab with a popup error msg asking
            //the user to fix justificaiton data in the form
            //previously used the way of checking justification record to solve this problem but it would create a problem of
            //not saving justification data when user switch tabs
            if (jform.isVisible() && jSaveBtn && jSaveBtn.isVisible()) {
                if (jform.isValid()) {
                    //update the vendor since some data may change in the vendor form
                    jcontroller.save();
                } else {
                    var vals = jform.getForm().getValues();
                    if (vals.isCv !== undefined) {
                        Ext.Msg.alert(
                            'Validation Error',
                            'Please fix the invalid data in the Justification Form'
                        );
                        return false;
                    } else {
                        //user hasn't fill anything yet, let it go through
                        return true;
                    }
                }
            }
        }
    }
});
