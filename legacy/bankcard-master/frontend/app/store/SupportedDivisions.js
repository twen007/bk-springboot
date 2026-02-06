/**
 * stores which divisions a AO is responsible for
 */
Ext.define('bcp.store.SupportedDivisions', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.OrgData',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'SupportedDivisions',
                    model: 'bcp.model.OrgData',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/items/supportedDivision',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
