Ext.define('bcp.store.IbbrRecordStore', {
    extend: 'Ext.data.Store',
    alias: 'store.ibbrrecordstore',

    requires: ['bcp.model.IbbrRecord'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {storeId: 'IbbrRecordStore', model: 'bcp.model.IbbrRecord'},
                cfg
            )
        ]);
    }
});
