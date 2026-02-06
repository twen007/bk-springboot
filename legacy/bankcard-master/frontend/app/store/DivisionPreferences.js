Ext.define('bcp.store.DivisionPreferences', {
    extend: 'Ext.data.Store',

    requires: [
        'Ext.data.proxy.Memory',
        'Ext.data.field.Field',
        'bcp.model.DivisionPreference'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'DivisionPreferences',
                    model: 'bcp.model.DivisionPreference',
                    proxy: {
                        type: 'ajax',
                        url: '/empbc/v1/nistOrgs/getDivPrefs/',
                        reader: {type: 'json', rootProperty: 'data'}
                    }
                },
                cfg
            )
        ]);
    }
});
