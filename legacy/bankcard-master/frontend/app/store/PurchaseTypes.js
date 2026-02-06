Ext.define('bcp.store.PurchaseTypes', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.LookUpData',
        'Ext.data.proxy.Memory',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'PurchaseTypes',
                    autoLoad: true,
                    model: 'bcp.model.LookUpData',
                    data: [
                        {id: 1, name: 'General'},
                        {id: 2, name: 'IT'},
                        {id: 3, name: 'Chemical'},
                        {id: 4, name: 'Bio'},
                        {id: 5, name: 'Chemical & Bio'}
                    ],
                    proxy: {type: 'memory', reader: {type: 'json'}}
                },
                cfg
            )
        ]);
    }
});
