Ext.define('bcp.view.DivPreferencesViewModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.divpreferences',

    requires: [
        'Ext.data.ChainedStore',
        'Ext.data.Store',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.util.Filter'
    ],
    stores: {
        aodivisions: {
            source: 'SupportedDivisions',
            filters: {
                filterFn: function (div) {
                    //remove the first record "all divisions"
                    return div.code != '0';
                }
            }
        },
        divisions: {source: 'Divisions'},
        bchdivisions: {source: 'Divisions'},
        bchInitials: {source: 'BchInitials'}
    }
});
