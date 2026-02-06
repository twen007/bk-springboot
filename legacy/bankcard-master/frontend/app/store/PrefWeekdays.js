/*
 * File: app/store/PrefWeekdays.js
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences, originally this is the only preference.
 */

Ext.define('bcp.store.PrefWeekdays', {
    extend: 'Ext.data.Store',

    requires: ['Ext.data.proxy.Memory', 'Ext.data.field.Field'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'PrefWeekdays',
                    autoLoad: true,
                    data: [
                        {id: 1, text: 'Monday'},
                        {id: 2, text: 'Tuesday'},
                        {id: 3, text: 'Wednesday'},
                        {id: 4, text: 'Thursday'},
                        {id: 5, text: 'Friday'}
                    ],
                    proxy: {type: 'memory'},
                    fields: [{name: 'id'}, {name: 'text'}]
                },
                cfg
            )
        ]);
    }
});
