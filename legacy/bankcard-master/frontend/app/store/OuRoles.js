/*
 *Stores staffs with ou level roles for the login user's ou.
 *It could contain just the role name but with no staff (no one is assigned this role for a OU in NIST Org)
 */

Ext.define('bcp.store.OuRoles', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.OuRoleUser',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],
    
    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'OuRoles',
                    model: 'bcp.model.OuRoleUser',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/users/profile/ouRoles',
                        reader: {type: 'json', rootProperty: 'data'}
                    },
                    //only roles with people are useful so use this filter out roles with no people
                    //e.g. {"roleName":"Chief of Staff","fullName":null,"peopleId":null,"active":null}
                    filters: {operator: '>', property: 'peopleId', value: 0}
                },
                cfg
            )
        ]);
    }
});
