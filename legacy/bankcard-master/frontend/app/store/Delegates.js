Ext.define('bcp.store.Delegates', {
    extend: 'Ext.data.Store',

    requires: ['bcp.model.DelegateProfile', 'Ext.data.proxy.SessionStorage'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'Delegates',
                    model: 'bcp.model.DelegateProfile',
                    proxy: {type: 'sessionstorage', id: 'bcpSessionStore'}
                },
                cfg
            )
        ]);
    }
});
