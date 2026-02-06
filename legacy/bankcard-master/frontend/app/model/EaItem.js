Ext.define('bcp.model.EaItem', {
    extend: 'bcp.model.BcpItem',
    alias: 'model.eaitem',

    requires: [
        'Ext.data.field.String',
        'Ext.data.field.Number',
        'Ext.data.field.Boolean',
        'Ext.data.field.Date'
    ],

    idProperty: 'itemId',

    fields: [
        {type: 'string', name: 'bch'},
        {type: 'string', name: 'division'},
        {type: 'string', name: 'ou'}
    ]
});
