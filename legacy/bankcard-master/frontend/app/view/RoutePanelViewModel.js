Ext.define('bcp.view.RoutePanelViewModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.routepanel',

    requires: ['Ext.data.ChainedStore', 'Ext.util.Filter'],

    stores: {
        reviewers: {
            source: 'Reviewers',
            filters: {
                operator: '==',
                property: 'divId',
                value: '{generalInfo.divisionId}'
            }
        },
        ouRoles: {source: 'OuRoles'},
        baos: {source: 'Baos'},
        bankcardHolders: {source: 'BankcardHolders'},
        routeRules: {
            source: 'RouteRules',
            filters: {
                operator: '===',
                property: 'routeStatusId',
                value: '{generalInfo.statusCode}'
            }
        },
        routeHistory: {
            source: 'RoutingHistory',
            //only care about dynamic reroutes, which we use to figure out who to send the request
            //back to
            sorters: [{property: 'routeId', direction: 'DESC'}],
            filters: [{property: 'isDynamicReroute', value: 1}]
        }
    }
});
