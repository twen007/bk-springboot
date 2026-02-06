Ext.define('bcp.store.WsCallFailedRecordStore', {
    extend: 'Ext.data.Store',
    alias: 'store.wscallfailedrecordstore',

    requires: [
        'bcp.model.WsCallFailedRecord',
        'Ext.data.proxy.Rest',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'WsCallFailedRecordStore',
                    model: 'bcp.model.WsCallFailedRecord',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/records/ibbr',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
