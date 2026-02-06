/**
 * all users with the FCO roles assigned to the division (current_holders)
 */
Ext.define('bcp.store.Fcos', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.BcUser',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.util.Sorter'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'Fcos',
                    model: 'bcp.model.BcUser',
                    proxy: {
                        type: 'ajax',
                        noCache: false,
                        limitParam: false,
                        pageParam: false,
                        startParam: false,
                        // @sw-cache {handler: "fastest", options: { cache: { name: 'fcos', maxAgeSeconds: 86400} } }
                        url: '/empbc/v1/users/profile/fundsCertifyingOfficials',
                        reader: {type: 'json', rootProperty: 'data'}
                    },
                    sorters: {property: 'fullName'}
                },
                cfg
            )
        ]);
    }
});
