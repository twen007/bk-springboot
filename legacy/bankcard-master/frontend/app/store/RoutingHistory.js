/*
 * File: app/store/RoutingHistory.js

 */

Ext.define('bcp.store.RoutingHistory', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.RequestRoute',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'RoutingHistory',
                    model: 'bcp.model.RequestRoute',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/routes/',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
