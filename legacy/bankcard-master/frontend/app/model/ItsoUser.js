Ext.define('bcp.model.ItsoUser', {
    extend: 'Ext.data.Model',

    requires: ['Ext.data.field.String'],

    idProperty: 'id',

    fields: [
        {name: 'id'},
        {name: 'peopleId'},
        {type: 'string', name: 'fullName'},
        {name: 'active'},
        {name: 'ditso'}
    ]
});
