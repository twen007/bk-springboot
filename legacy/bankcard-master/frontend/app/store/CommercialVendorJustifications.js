Ext.define('bcp.store.CommercialVendorJustifications', {
    extend: 'Ext.data.Store',

    requires: [
        'Ext.data.proxy.Memory',
        'Ext.data.reader.Json',
        'Ext.data.field.Field'
    ],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'CommercialVendorJustifications',
                    data: [
                        {
                            text: 'The Vendor is compliant with Section 889B as self-certified in Sam.gov (FAR 52.204-26 Reps and Certs).'
                        },
                        {text: 'Other'}
                    ],
                    proxy: {type: 'memory', reader: {type: 'json'}},
                    fields: [{name: 'text'}]
                },
                cfg
            )
        ]);
    }
});
