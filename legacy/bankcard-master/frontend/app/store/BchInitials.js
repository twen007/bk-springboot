/**
 * stores custom bankcard holder initials setup in the division preference view
 */
Ext.define('bcp.store.BchInitials', {
    extend: 'Ext.data.Store',

    requires: [
        'bcp.model.BchInitial',
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
                    storeId: 'BchInitials',
                    model: 'bcp.model.BchInitial',
                    proxy: {
                        type: 'ajax',
                        noCache: false,
                        limitParam: false,
                        pageParam: false,
                        startParam: false,
                        url: '/empbc/v1/users/nistOrgs/getBchInitPrefs/',
                        reader: {type: 'json', rootProperty: 'data'}
                    },
                    sorters: {property: 'initials'}
                },
                cfg
            )
        ]);
    }
});
