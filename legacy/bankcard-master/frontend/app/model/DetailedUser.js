Ext.define('bcp.model.DetailedUser', {
    extend: 'Ext.data.Model',
    alias: 'model.detaileduser',

    requires: [
        'Ext.data.field.Integer',
        'Ext.data.field.String',
        'Ext.data.field.Date'
    ],

    fields: [
        {type: 'int', name: 'id'},
        {type: 'int', name: 'peopleId'},
        {type: 'int', name: 'ouOrgId'},
        {type: 'int', name: 'divOrgId'},
        {type: 'int', name: 'grpOrgId'},
        {type: 'string', name: 'accessGroup'},
        {type: 'string', name: 'accessDiv'},
        {type: 'string', name: 'accessOu'},
        {type: 'date', name: 'validUntilDate'} // Matches Java format
    ]
});
