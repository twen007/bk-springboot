Ext.define('bcp.model.IbbrRecord', {
    extend: 'Ext.data.Model',
    alias: 'model.ibbrrecord',

    requires: ['Ext.data.field.String', 'Ext.data.field.Integer'],

    fields: [
        {type: 'string', name: 'amount'},
        {type: 'string', name: 'room'},
        {type: 'string', name: 'catalog'},
        {type: 'string', name: 'cost'},
        {type: 'string', name: 'name'},
        {type: 'string', name: 'owner_sn'},
        {type: 'string', name: 'owner_given'},
        {type: 'string', name: 'supplier'},
        {type: 'string', name: 'email'},
        {type: 'int', name: 'quantity'}
    ]
});
