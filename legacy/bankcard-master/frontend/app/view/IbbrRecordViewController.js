Ext.define('bcp.view.IbbrRecordViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.ibbrrecord',

    onOK: function (button, e) {
        this.view.doClose();
    },

    onIbbrResubmitClick: function (button, e, eOpts) {
        var rec = this.getViewModel().get('wsCallRec');
        var toSend = [rec];
        var url = bcp.config.Runtime.getServerBaseUrl() + 'records/ibbr';
        console.log('url = ' + url);
        var message = '';

        try {
            Ext.MessageBox.show({
                title: 'Resyncing',
                progressText: 'Resyncing IBBR record...please wait',
                width: 300,
                progress: true,
                closable: false
            });

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: Ext.encode(toSend),
                scope: this,

                success: function (response) {
                    Ext.MessageBox.hide();
                    var jsonRsp = Ext.decode(response.responseText);
                    var myData = jsonRsp.data;
                    message = myData[0].description;

                    var myStore = Ext.getStore('WsCallFailedRecordStore');
                    myStore.proxy.url = url;
                    myStore.load();
                    Ext.Msg.alert('Resubmission Result', message);
                }
            });
        } catch (ex) {
            Ext.MessageBox.hide();
            var subject = '?subject=EMPBC Error',
                body = '&body=' + Ext.htmlEncode(ex.stack),
                mailto = 'mailto:MML.SystemsHelp@nist.gov' + subject + body;
            Ext.Msg.alert(
                'Error',
                'Unexpected error happened while resubmitting the IRRB record.<br>' +
                    ex.message +
                    '<br>' +
                    '<a href="' +
                    mailto +
                    '" ' +
                    'target="_blank">Please click here to report the error.</a>'
            );
        }
        this.view.doClose();
    }
});
