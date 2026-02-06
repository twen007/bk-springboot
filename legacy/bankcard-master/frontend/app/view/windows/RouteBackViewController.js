Ext.define('bcp.view.windows.RouteBackViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.windows.routeback',

    onCancel: function (button, e, eOpts) {
        this.view.close();
    },

    //AO, SMA, XO, BAO or BCH can perform this action (restricted in server code)
    onRoute: function (button, e, eOpts) {
        var refs = this.getReferences(),
            view = this.view,
            model = this.getViewModel(),
            loggedInUser = model.get('loggedInUser'),
            rec = refs.rgApprovers.getValue(),
            reqId = rec.data.requestId,
            method = 'POST',
            url = bcp.config.Runtime.getServerBaseUrl() + 'routes',
            errorMsg = 'Route back failed.',
            successMsg = 'Your request routed successfully.',
            me = this;

        //set a note so the route history will show who did the route back.
        //NOTE: the late part of the msg is also used to in a condition to display route history
        rec.set(
            'notes',
            loggedInUser.lastName +
                ', ' +
                loggedInUser.firstName +
                ' routed back the request to a previous stage.'
        );

        /* 
           bcp.util.CommonUtil.ajax(method, url, Ext.encode(rec.data),
            successMsg, errorMsg, 'toast', model)
            .then(success => view.close())
            .catch(error => {
				console.log(error);
				bcp.util.CommonUtil.hideLoader();
			});
            */

        //normal route
        Ext.Ajax.request({
            url: url,
            method: method,
            jsonData: Ext.encode(rec.data),
            scope: this,
            success: function (response) {
                bcp.util.CommonUtil.ajaxSuccessHandler(
                    response,
                    function (result) {
                        Ext.Msg.alert('Success', successMsg);
                        me.redirectTo('requestsearching/' + reqId, true);
                        view.close();
                        //me.fireViewEvent("routed", this.result);
                    }
                );
            },
            failure: function (form, response) {
                Ext.Msg.alert(errorMsg, response.result.statusText);
            }
        });
    },

    onWindowBeforeShow: function (component, eOpts) {},

    onWindowAdded: function (component, container, pos, eOpts) {
        var model = this.getViewModel(),
            req = model.get('generalInfo'),
            reqId = req.get('requestId'),
            refs = this.getReferences(),
            currentStatus = req.get('statusCode'),
            pref = bcp.util.CommonFunctions.getDivisionPreferences(
                req.get('divisionId')
            );

        //set display info and route vo for each radio
        refs.rbReviewer.setBoxLabel(
            'Reviewer (Bona Fide Need Certifier) - ' + req.get('reviewerName')
        );
        refs.rbReviewer.inputValue = Ext.create('bcp.model.RequestRoute', {
            requestId: reqId,
            statusId: 5,
            typeId: 1,
            routeTo: req.get('reviewerId'),
            routeToName: req.get('reviewerName')
        });

        if (pref.get('addFcoRoutePrefVal') == 'Y') {
            refs.rbBao.setBoxLabel(
                'Bankcard Approving Official - ' + req.get('baoName')
            );
            refs.rbFco.setBoxLabel(
                'Funds Certifying Official - ' + req.get('fcoName')
            );
        } else {
            refs.rbBao.setBoxLabel(
                'Bankcard Approving Official and Funds Certifying Official - ' +
                    req.get('baoName')
            );
        }

        refs.rbFco.inputValue = Ext.create('bcp.model.RequestRoute', {
            requestId: reqId,
            statusId: 16,
            typeId: 16,
            routeTo: req.get('fundsCertifyingOfficialId'),
            routeToName: req.get('fcoName')
        });

        refs.rbBao.inputValue = Ext.create('bcp.model.RequestRoute', {
            requestId: reqId,
            statusId: 6,
            typeId: 2,
            routeTo: req.get('bankcardApprovingOfficialId'),
            routeToName: req.get('baoName')
        });

        refs.rbBch.setBoxLabel('Bankcard Holder - ' + req.get('bhName'));
        refs.rbBch.inputValue = Ext.create('bcp.model.RequestRoute', {
            requestId: reqId,
            statusId: 7,
            typeId: 3,
            routeTo: req.get('bankcardHolderId'),
            routeToName: req.get('bhName')
        });

        //cannot let users route back to a stage that was never approved before
        if ([17, 18].includes(currentStatus)){
            //DC or DR step
            refs.rbFco.destroy();
            refs.rbBao.destroy();
            refs.rbBch.destroy();
        } else if (currentStatus === 7) {
            //BCH step
            refs.rbBch.destroy();
        } else if (currentStatus === 16) {
            //FCO step
            refs.rbBao.destroy();
            refs.rbBch.destroy();
        }else if (currentStatus === 6) {
            //BAO step
            refs.rbBch.destroy();
        }
    }
});
