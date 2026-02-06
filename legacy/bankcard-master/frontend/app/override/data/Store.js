Ext.define('bcp.override.data.Store', {
    override: 'Ext.data.Store',
    listeners: {
        exception: function (proxy, options, response) {
            Ext.MessageBox.show({
                title: '',
                msg: Ext.decode(response.responseText).msg,
                buttons: Ext.Msg.OK,
                closable: false,
                icon: Ext.MessageBox.ERROR
            });
        }
    }
});
