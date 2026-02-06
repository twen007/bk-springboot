Ext.define('bcp.store.UserDetailedPrivileges', {
    extend: 'Ext.data.Store',

    requires: ['bcp.model.UserDetailedPrivilege', 'Ext.data.proxy.Memory'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'UserDetailedPrivileges',
                    model: 'bcp.model.UserDetailedPrivilege',
                    proxy: {type: 'memory'}
                },
                cfg
            )
        ]);
    }
});
