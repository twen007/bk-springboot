Ext.define('bcp.view.EditReqViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.editreq',

    onTabpanelBeforeTabChange: function (tabPanel, newCard, oldCard, eOpts) {
        //debugger;
        //MB-382 related change
        //allow users with edit privilege to update general info of the selected request
        //this wasn't allowed before but since we separate the change requester function
        //from the generalinfo and we now have the need to update deliver to home checkbox
        //in case the requester forgot to select it, it is editable now.
        var refs = this.getReferences(),
            form = refs.generalPanelEditable,
            model = this.getViewModel(),
            orgRec = model.get('generalInfo'),
            rec = form.getRecord(),
            method = 'PUT',
            url = bcp.config.Runtime.getServerBaseUrl() + 'requests/';

        if (oldCard.reference == 'generalPanelEditable') {
            //update the request since some data may change in the generalPanel form
            form.updateRecord();
            //BANK-493 form logic would update the requester id to login user's id; since we are not supposed to update requester when BAO edit the request
            //we need to change the requester back
            rec.set('requesterId', orgRec.get('requesterId'));
            Ext.Ajax.request({
                url: url + rec.data.requestId,
                method: method,
                scope: this,
                jsonData: Ext.encode(rec.data),
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(
                        response,
                        function (result) {
                            if (!result.data) {
                                Ext.Msg.alert('Failure','The request was not updated.');
                                return false;
                            }
                        }
                    );
                }
            });
        } else if (oldCard.reference == 'justificationPanelEditable') {
            var jcontroller = oldCard.lookupController(),
                jform = jcontroller.lookupReference('form'),
                jSaveBtn = jcontroller.lookupReference('btnSave');

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
                    }
                }
            }
        }
    }
});