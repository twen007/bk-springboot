/*
 * File: app/store/PrefNotificationFrequency.js
 * Author: xinweiw
 * Purpose: a memory store for email notification frequency that users can set in user preference
 */
Ext.define('bcp.store.PrefNotificationFrequency', {
    extend: 'Ext.data.Store',

    requires: ['Ext.data.proxy.Memory', 'Ext.data.field.Field'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'PrefNotificationFrequency',
                    autoLoad: true,
                    data: [
                        {id: 1, text: 'weekly'},
                        {id: 2, text: 'bi-weekly'},
                        {id: 3, text: 'monthly'},
                        {id: 4, text: 'never'}
                    ],
                    proxy: {type: 'memory'},
                    fields: [{name: 'id'}, {name: 'text'}]
                },
                cfg
            )
        ]);
    }
});
