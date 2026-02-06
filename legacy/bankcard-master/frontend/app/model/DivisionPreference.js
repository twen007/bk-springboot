Ext.define('bcp.model.DivisionPreference', {
    extend: 'Ext.data.Model',

    requires: ['Ext.data.field.Boolean', 'Ext.data.field.Integer'],

    idProperty: 'divId',

    fields: [
        {type: 'int', name: 'divId'},
        {type: 'string', name: 'justPrefVal'},
        {type: 'string', name: 'financePrefVal'},
        {type: 'string', name: 'shippingCostPrefVal'},
        {type: 'float', name: 'shippingCostPrefValDetail'},
        {type: 'string', name: 'upToPrefVal'},
        {type: 'float', name: 'upToPrefValDetail'},
        {type: 'string', name: 'addFcoRoutePrefVal', defaultValue: 'Y'}
    ]
});
