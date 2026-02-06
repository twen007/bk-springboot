Ext.define('bcp.store.Fys', {
    extend: 'Ext.data.Store',

    requires: ['Ext.data.proxy.Ajax', 'Ext.data.reader.Array'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'Fys',
                    proxy: {type: 'ajax', reader: {type: 'array'}}
                },
                cfg
            )
        ]);
    }
});
