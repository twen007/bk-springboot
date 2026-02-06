/**
 * for Property Custodian report
 */


Ext.define('bcp.model.PcItem', {
    extend: 'Ext.data.Model',
    alias: 'model.pcitem',

    requires: [
        'Ext.data.field.String',
        'Ext.data.field.Number',
        'Ext.data.field.Boolean',
        'Ext.data.field.Date'
    ],

    idProperty: 'itemId',

    fields: [
        { type: 'int', name: 'requestId' },
        { type: 'int', name: 'fy' },
        { type: 'date', name: 'createdDate' },
        { type: 'string', name: 'requisitionNumber' },
        { type: 'int', name: 'ouId' },
        { type: 'int', name: 'divId' },
        { type: 'int', name: 'grpId' },
        { type: 'string', name: 'ou' },
        { type: 'string', name: 'division' },
        { type: 'string', name: 'group' },
        { type: 'string', name: 'vendor' },
        { type: 'int', name: 'itemId', },
        { type: 'string', name: 'catelogNumber' },
        { type: 'string', name: 'itemName' },
        { type: 'string', name: 'itemDescription' },
        { type: 'float', name: 'price' },
        { type: 'int', name: 'quantity' },
        { type: 'string', name: 'purpose'},
        { type: 'string', name: 'isChemical' },
        { type: 'int', name: 'shoppingCartFileId' },
        { type: 'string', name: 'itemStatus' },
        { type: 'int', name: 'itemStatusId' },
        { type: 'string', name: 'projectTask' },
        { type: 'string', name: 'objectClass' },
        { type: 'string', name: 'isTaggableEquipment' },
        { type: 'float', name: 'priceOrdered' },
        { type: 'int', name: 'quantityOrdered' },
        { type: 'string', name: 'itemNotes' },
        { type: 'date', name: 'dateReceived' },
        { type: 'string', name: 'transactionNumber' },
        { type: 'date', name: 'statementDate' },
        { type: 'string', name: 'unitIssue' },
        { type: 'int', name: 'purchaseTypeId' }
    ]
});
