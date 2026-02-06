/*
 *store user with OU level roles
 */

Ext.define('bcp.model.OuRoleUser', {
    extend: 'Ext.data.Model',

    requires: ['Ext.data.field.String'],

    //idProperty: 'peopleId', //if use peopleId, user with multiple ou roles would end up with only one record in the store
    idProperty: 'roleName', //need to use this because ou roles are unique but a user can have multiple ou roles
    fields: [
        {name: 'peopleId'},
        {type: 'string', name: 'fullName'},
        {type: 'string', name: 'roleName'},
        {name: 'active'},
    ]
});
