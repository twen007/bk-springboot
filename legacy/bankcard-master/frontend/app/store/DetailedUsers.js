Ext.define('bcp.store.DetailedUsers', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.Vendor',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'DetailedUsers',
                    model: 'bcp.model.DetailedUser',
                    proxy: {
                        type: 'ajax',
                        noCache: false,
                        limitParam: false,
                        pageParam: false,
                        startParam: false,
                        url: '/empbc/v1/detailedusers',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});