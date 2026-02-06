Ext.define('bcp.model.WsCallFailedRecord', {
    extend: 'Ext.data.Model',
    alias: 'model.wscallfailedrecord',

    requires: ['Ext.data.field.Integer', 'Ext.data.field.String'],

    fields: [
        {type: 'int', name: 'id'},
        {type: 'int', name: 'wsCategory'},
        {type: 'string', name: 'wsMethod'},
        {type: 'int', name: 'statusCode'},
        {type: 'string', name: 'errorMessage'},
        {type: 'string', name: 'dateCreated'},
        {type: 'string', name: 'lastSubmitted'},
        {type: 'int', name: 'referenceId'},
        {name: 'ibbrRecord'}
    ]
});
