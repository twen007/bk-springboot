/*
 * a random non-supervisor fed user for a group, which is selected by a user
 * in the detailee mode menu
 */

Ext.define('bcp.model.DetaileeUser', {
    extend: 'Ext.data.Model',

    requires: ['Ext.data.field.Integer', 'Ext.data.field.String'],

    idProperty: 'peopleId',

    fields: [
        {type: 'int', name: 'peopleId'},
        {type: 'string', name: 'username'},
        {type: 'string', name: 'ouCode'},
        {type: 'string', name: 'divisionCode'},
        {type: 'string', name: 'groupCode'},
        {type: 'int', name: 'ouId'},
        {type: 'int', name: 'divisionId'},
        {type: 'int', name: 'groupId'}
    ]
});
