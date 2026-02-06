Ext.define('bcp.store.Detailees', {
    extend: 'Ext.data.Store',

    requires: ['bcp.model.DetaileeUser', 'Ext.data.proxy.SessionStorage'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'Detailees',
                    model: 'bcp.model.DetaileeUser',
                    proxy: {
                        type: 'sessionstorage',
                        id: 'bcpDetaileeSessionStore'
                    }
                },
                cfg
            )
        ]);
    }
});
