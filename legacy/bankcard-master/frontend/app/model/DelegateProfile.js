Ext.define('bcp.model.DelegateProfile', {
    extend: 'Ext.data.Model',
    alias: 'model.delegateprofile',

    requires: [
        'Ext.data.field.String',
        'Ext.data.field.Number',
        'Ext.data.field.Boolean',
        'Ext.data.field.Date'
    ],

    idProperty: 'username',

    fields: [
        {type: 'string', name: 'username'},
        {type: 'string', name: 'lastName'},
        {type: 'string', name: 'firstName'},
        {type: 'string', name: 'middleName'}
    ]
});
