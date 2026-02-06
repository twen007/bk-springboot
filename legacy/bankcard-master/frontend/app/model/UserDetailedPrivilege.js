/*
 * File: app/model/UserDetailedPrivilege.js
 *
 * stores data on what org other than the user's current org that the user have job functions to
 * so we can use the data to determine whether the user can create requests for other orgs
 */

Ext.define('bcp.model.UserDetailedPrivilege', {
    extend: 'Ext.data.Model',
    alias: 'model.userDetailedPrivilege',

    requires: ['Ext.data.field.Integer', 'Ext.data.field.Boolean'],

    idProperty: 'id',

    fields: [
        {type: 'int', name: 'id'},
        {type: 'int', name: 'peopleId'},
        {type: 'boolean', name: 'accessOu'},
        {type: 'boolean', name: 'accessDiv'},
        {type: 'boolean', name: 'accessGroup'},
        {type: 'int', name: 'ouId'},
        {type: 'int', name: 'divisionId'},
        {type: 'int', name: 'groupId'}
    ]
});
