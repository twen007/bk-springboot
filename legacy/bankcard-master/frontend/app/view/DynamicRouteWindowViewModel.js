/*

 */

Ext.define('bcp.view.DynamicRouteWindowViewModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.dynamicroutewindow',
    requires: ['Ext.data.ChainedStore', 'Ext.util.Filter'],

    stores: {
        bcEmployees: {
            source: 'OuAllStaffs',
            filters: {
                operator: '==',
                property: 'staffType',
                value: 'NIST Employee'
            }
        }
    }
});
