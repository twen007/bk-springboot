Ext.define('bcp.store.ItsoUsers', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.ItsoUser',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'ItsoUsers',
                    model: 'bcp.model.ItsoUser',
                    proxy: {
                        type: 'ajax',
                        noCache: false,
                        limitParam: false,
                        pageParam: false,
                        startParam: false,
                        // @sw-cache {handler: "fastest", options: { cache: { name: 'bchs', maxAgeSeconds: 86400} } }
                        url: '/empbc/v1/users/profile/itsos',
                        reader: {type: 'json', rootProperty: 'data'}
                    },
                    sorters: {property: 'fullName'}
                },
                cfg
            )
        ]);
    }
});
