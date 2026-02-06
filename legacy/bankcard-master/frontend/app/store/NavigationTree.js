Ext.define('bcp.store.NavigationTree', {
    extend: 'Ext.data.TreeStore',

    requires: ['Ext.data.field.Field'],

    constructor: function (cfg) {
        var me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: 'NavigationTree',
                    root: {
                        expanded: true,
                        children: [
                            {
                                text: 'Dashboard',
                                iconCls: 'fas fa-home',
                                viewType: 'bcpdashboard',
                                routeId: 'dashboard',
                                // routeId defaults to viewType
                                leaf: true
                            },
                            {
                                text: 'Preferences',
                                iconCls: 'fas fa-th-list',
                                expanded: true,
                                selectable: false,
                                children: [
                                    {
                                        text: 'User',
                                        iconCls: 'fas fa-tools',
                                        viewType: 'preferences',
                                        routeId: 'preferences',
                                        leaf: true
                                    },
                                    {
                                        text: 'Division',
                                        iconCls: 'fas fa-toolbox',
                                        viewType: 'divpreferences',
                                        routeId: 'divpreferences',
                                        leaf: true
                                    }
                                ]
                            },
                            {
                                text: 'My Requests',
                                iconCls: 'fas fa-money-check-alt',
                                expanded: true,
                                selectable: false,
                                children: [
                                    {
                                        text: 'New',
                                        iconCls: 'fas fa-plus',
                                        viewType: 'createrequest',
                                        routeId: 'createrequest',
                                        leaf: true
                                    },
                                    {
                                        text: 'Saved',
                                        iconCls: 'fas fa-save',
                                        viewType: 'savedrequests',
                                        routeId: 'savedrequests',
                                        leaf: true
                                    },
                                    {
                                        text: 'Prepared',
                                        iconCls: 'fas fa-thumbs-up',
                                        viewType: 'preparedrequests',
                                        routeId: 'preparedrequests',
                                        leaf: true
                                    },
                                    {
                                        text: 'Submitted',
                                        iconCls: 'fas fa-shopping-cart',
                                        viewType: 'requesttracking',
                                        routeId: 'requesttracking',
                                        leaf: true
                                    },
                                    {
                                        text: 'Received',
                                        iconCls: 'fas fa-archive',
                                        viewType: 'historicalrequests',
                                        routeId: 'historicalrequests',
                                        leaf: true
                                    }
                                ]
                            },
                            {
                                text: 'Search',
                                iconCls: 'fas fa-search',
                                viewType: 'requestsearching',
                                routeId: 'requestsearching',
                                leaf: true
                            },
                            {
                                text: 'Pending Actions',
                                iconCls: 'fas fa-certificate',
                                viewType: 'pendingrequests',
                                routeId: 'pendingrequests',
                                leaf: true
                            },
                            {
                                text: 'Purchases',
                                iconCls: 'fas fa-money-bill',
                                viewType: 'purchases',
                                routeId: 'purchases',
                                leaf: true
                            },
                            {
                                text: 'Vendors',
                                iconCls: 'fas fa-building',
                                viewType: 'vendors',
                                routeId: 'vendors',
                                leaf: true
                            },
                            {
                                text: 'Reports',
                                iconCls: 'fas fa-th-list',
                                expanded: true,
                                selectable: false,
                                children: [
                                    {
                                        text: 'Audit Report',
                                        iconCls: 'fas fa-history',
                                        viewType: 'auditreport',
                                        routeId: 'auditreport',
                                        leaf: true
                                    },
                                    {
                                        text: 'PC Report',
                                        tooltip:'Property Custodian Report',
                                        iconCls: 'fas fa-truck',
                                        viewType: 'pcitemreport',
                                        routeId: 'pcitemreport',
                                        leaf: true
                                    },
                                     {
                                        text: 'Items Report',
                                        tooltip:'Items Report',
                                        iconCls: 'fas fa-book',
                                        viewType: 'itemsreport',
                                        routeId: 'itemsreport',
                                        leaf: true
                                    },
                                    {
                                        text: 'EA Report',
                                        iconCls: 'fas fa-file-signature',
                                        viewType: 'eareport',
                                        routeId: 'eareport',
                                        leaf: true
                                    }
                                ]
                            },
                            {
                                text: 'Admin',
                                iconCls: 'fa fa-lock',
                                expanded: true,
                                selectable: false,
                                children: [
                                    {
                                        text: 'IBBR',
                                        viewType: 'wscallfailedrecord',
                                        routeId: 'ibbr',
                                        leaf: true
                                    },
                                    {
                                        text: 'Detailees',
                                        viewType: 'detailedusers',
                                        routeId: 'detailedusers',
                                        leaf: true
                                    }
                                ]
                            }
                        ]
                    },
                    fields: [{name: 'text'}]
                },
                cfg
            )
        ]);
    }
});
