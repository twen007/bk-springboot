Ext.define('bcp.view.WsCallFailedRecordViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.wscallfailedrecord',

    onDetail: function (view, rowIndex, colIndex, item, e, record, row) {
        var window = Ext.create('bcp.view.IbbrRecord', {});
        this.view.add(window);

        this.getViewModel().set('ibbrRec', record.get('ibbrRecord'));
        this.getViewModel().set('wsCallRec', record.data);
        window.show();
    },

    onResubmit: function (button, e, eOpts) {
        var selectedRecords = this.view.getSelection();
        var selected = [];
        Ext.each(selectedRecords, function (item) {
            console.log(item.data);
            selected.push(item.data);
        });

        var url = bcp.config.Runtime.getServerBaseUrl() + 'records/ibbr';
        var message = 'All succeeded except the following:';

        try {
            Ext.MessageBox.show({
                title: 'Resyncing',
                progressText: 'Resyncing IBBR records...please wait',
                width: 400,
                progress: true,
                closable: false
            });

            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: Ext.encode(selected),
                scope: this,
                success: function (response) {
                    Ext.MessageBox.hide();

                    var jsonRsp = Ext.decode(response.responseText);
                    var myData = jsonRsp.data;
                    var convertRsp = [];
                    var foundFailure = false;
                    for (var i = 0; i < myData.length; i++) {
                        convertRsp.push(myData[i]);
                        if (myData[i].statusCode != -1) {
                            message += '<li>' + myData[i].description + '</li>';
                            foundFailure = true;
                        }
                    }
                    var myStore = Ext.getStore('WsCallFailedRecordStore');
                    myStore.proxy.url = url;
                    myStore.load();
                    if (foundFailure) {
                        Ext.Msg.alert('Resubmission Result', message);
                    } else {
                        Ext.Msg.alert(
                            'Resubmission Successful',
                            'IBBR record(s) are resynced'
                        );
                    }
                }
            });
        } catch (ex) {
            Ext.MessageBox.hide();
            var subject = '?subject=EMPBC Error',
                body = '&body=' + Ext.htmlEncode(ex.stack),
                mailto = 'mailto:MML.SystemsHelp@nist.gov' + subject + body;
            Ext.Msg.alert(
                'Error',
                'Unexpected error happened while resubmitting the IRRB records.<br>' +
                    ex.message +
                    '<br>' +
                    '<a href="' +
                    mailto +
                    '" ' +
                    'target="_blank">Please click here to report the error.</a>'
            );
        }
    },

    onWsCallRecordRowSelect: function (rowmodel, record, index, eOpts) {
        //Ext.Msg.alert('', "You've clicked " + index.toString());
    },

    onViewAdded: function (component, container, pos, eOpts) {
        Ext.getStore('WsCallFailedRecordStore').load();
    }
});
