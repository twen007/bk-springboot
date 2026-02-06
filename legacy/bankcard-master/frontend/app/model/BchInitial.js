Ext.define('bcp.model.BchInitial', {
    extend: 'Ext.data.Model',
    alias: 'model.bchinitial',

    requires: ['Ext.data.field.Number', 'Ext.data.field.String'],

    idProperty: 'id',

    fields: [
        {name: 'id', type: 'int'},
        {name: 'divId', type: 'int'},
        {name: 'peopleId', type: 'int'},
        {name: 'initials', type: 'string'}
    ]
});
