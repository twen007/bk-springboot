/**
 * used as remote store to get matched employee by name
 */
Ext.define('bcp.store.NistEmployees', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.BcUser',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'NistEmployees',
                    model: 'bcp.model.BcUser',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/users/nistEmployeesAll',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
