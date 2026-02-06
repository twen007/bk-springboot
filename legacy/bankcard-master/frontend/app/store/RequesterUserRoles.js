/*
 The difference between this store and the UserRoles store is that the UserRoles store contains
 logged-in user's user roles but this store contains the current request's requester's user roles.
 It is used when a user prepare a request for someone else and we need to know that someone else's 
 roles to determine who's the reviewer (BANK-539). we may need use this for other future enhancement too
 */

Ext.define('bcp.store.RequesterUserRoles', {
    extend: 'Ext.data.Store',

    requires: ['bcp.model.UserRole', 'Ext.data.proxy.Memory'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'RequesterUserRoles',
                    model: 'bcp.model.UserRole',
                    proxy: {type: 'memory'}
                },
                cfg
            )
        ]);
    }
});
