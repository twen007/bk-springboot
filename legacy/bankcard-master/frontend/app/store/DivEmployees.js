/**
 * Deprecated 03/2025, after we include org data in ouallstaff store
 * ouEmp and divEmp can all be chainedStore of the ouallstaff store
 */
Ext.define('bcp.store.DivEmployees', {
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
                    storeId: 'DivEmployees',
                    model: 'bcp.model.BcUser',
                    proxy: {
                        type: 'ajax',
                        noCache: false,
                        limitParam: false,
                        pageParam: false,
                        startParam: false,
                        // @sw-cache {handler: "fastest", options: { cache: { name: 'divEmps', maxAgeSeconds: 86400} } }
                        url: '/empbc/v1/users/divEmployees',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
