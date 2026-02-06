/*
 * File: app/view/RequestGrid.js
 *
 * a shared grid component used by many views
 */

Ext.define('bcp.view.RequestGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.requestgrid',

    requires: [
        'Ext.view.Table',
        'Ext.grid.column.Number',
        'Ext.grid.column.Date',
        'Ext.grid.column.Boolean',
        'Ext.grid.plugin.Exporter'
    ],

    config: {listStateId: 'rg', showContainsIcon: false},

    stateId: 'requestlist',
    itemId: 'reqGrid',

    bind: {store: '{store}'},
    viewConfig: {enableTextSelection: true},
    columns: [
        {
            xtype: 'numbercolumn',
            stateId: 'fy',
            width: 60,
            dataIndex: 'fy',
            text: 'FY',
            format: '00'
        },
        {
            xtype: 'numbercolumn',
            stateId: 'reqId',
            width: 90,
            dataIndex: 'requestId',
            text: 'Request Id',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'reqNum',
            width: 200,
            dataIndex: 'requisitionNumber',
            text: 'Requisition<br>Number'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'contains',
            width: 160,
            dataIndex: 'purchaseTypeId',
            text: 'Contains',
            renderer: function (value) {
                var chemIcon =
                        '<img src="resources/images/chem.svg" style="width: 40px; height: 40px;">',
                    bioIcon =
                        '<img src="resources/images/bio.svg" style="width: 40px; height: 40px;">';
                if (value === 3) {
                    return chemIcon;
                } else if (value === 4) {
                    return bioIcon;
                } else if (value === 5) {
                    return chemIcon + bioIcon;
                } else {
                    return '';
                }
            }
        },
        {
            xtype: 'numbercolumn',
            stateEvents: 'requesterId',
            hidden: true,
            dataIndex: 'requesterId',
            text: 'Requester Id',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colRequesterName',
            stateId: 'reqPerson',
            width: 120,
            cellWrap: true,
            dataIndex: 'requesterName',
            text: 'Requester<br>Name'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colReviewerName',
            stateId: 'reqReviewer',
            width: 120,
            cellWrap: true,
            dataIndex: 'reviewerName',
            text: 'Reviewer<br>Name'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colBchName',
            stateId: 'reqBch',
            width: 130,
            cellWrap: true,
            dataIndex: 'bhName',
            text: 'Bankcard Holder<br>Name'
        },
        {
            xtype: 'datecolumn',
            stateId: 'submitteddate',
            width: 120,
            cellWrap: true,
            dataIndex: 'submittedDate',
            exportStyle: {
                alignment: {horizontal: 'Right'},
                format: 'Short Date'
            },
            text: 'Submitted On',
            format: 'm/d/Y'
        },
        {
            xtype: 'datecolumn',
            stateId: 'neededbydate',
            width: 120,
            cellWrap: true,
            dataIndex: 'neededByDate',
            exportStyle: {
                alignment: {horizontal: 'Right'},
                format: 'Short Date'
            },
            text: 'Needed By',
            format: 'm/d/Y'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colVendors',
            stateId: 'reqVednor',
            width: 160,
            cellWrap: true,
            dataIndex: 'vendors',
            text: 'Vendor'
        },
        {
            xtype: 'gridcolumn',
            exportRenderer: function (
                value,
                metaData,
                record,
                rowIndex,
                colIndex,
                store,
                view
            ) {
                if (value) {
                    return value.replace(/<br>/g, ', ');
                } else {
                    return value;
                }
            },
            reference: 'colItems',
            stateId: 'reqItems',
            width: 200,
            cellWrap: true,
            dataIndex: 'items',
            text: 'Item(s)'
        },
        {
            xtype: 'gridcolumn',
            renderer: function (
                value,
                metaData,
                record,
                rowIndex,
                colIndex,
                store,
                view
            ) {
                return bcp.util.CommonUtil.moneyRenderer(value);
            },
            stateEvents: 'reqEstimatedCost',
            width: 100,
            align: 'end',
            dataIndex: 'totalCost',
            text: 'Estimated<br>Total Cost'
        },
        {
            xtype: 'gridcolumn',
            renderer: function (
                value,
                metaData,
                record,
                rowIndex,
                colIndex,
                store,
                view
            ) {
                return bcp.util.CommonUtil.moneyRenderer(value);
            },
            stateEvents: 'reqActualCost',
            width: 100,
            align: 'end',
            dataIndex: 'actualTotalCost',
            text: 'Actual<br>Total Cost'
        },
        {
            xtype: 'gridcolumn',
            renderer: function (
                value,
                metaData,
                record,
                rowIndex,
                colIndex,
                store,
                view
            ) {
                return bcp.util.CommonUtil.moneyRenderer(value);
            },
            stateEvents: 'reqApprovalAmount',
            width: 100,
            align: 'end',
            dataIndex: 'approvalAmount',
            text: 'Approval<br>Amount'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'delivAddr',
            hidden: true,
            width: 160,
            cellWrap: true,
            dataIndex: 'delivAddr',
            text: 'Delivery<br>Address'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colRouteFrom',
            stateId: 'reqRouteFrom',
            width: 120,
            cellWrap: true,
            dataIndex: 'routeFromName',
            text: 'Route From'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colPendingAction',
            stateId: 'reqRouteTo',
            width: 120,
            cellWrap: true,
            dataIndex: 'routeToName',
            text: 'Pending Action'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colRouteNote',
            stateId: 'reqRouteNote',
            minWidth: 160,
            cellWrap: true,
            dataIndex: 'approverNote',
            text: 'Route Note'
        },
        {
            xtype: 'gridcolumn',
            reference: 'colStatusText',
            stateId: 'reqStatusText',
            minWidth: 160,
            cellWrap: true,
            dataIndex: 'statusText',
            text: 'Route Status'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'reqComments',
            hidden: true,
            width: 160,
            cellWrap: true,
            dataIndex: 'comments',
            text: 'Comments'
        },
        {
            xtype: 'booleancolumn',
            hidden: true,
            dataIndex: 'isShoppingCart',
            text: 'Is Shopping<br>Cart'
        },
        {
            xtype: 'datecolumn',
            stateId: 'reqdate',
            width: 120,
            cellWrap: true,
            dataIndex: 'reqDate',
            exportStyle: {
                alignment: {horizontal: 'Right'},
                format: 'Short Date'
            },
            text: 'Created On',
            format: 'm/d/Y'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'reqCreatedBy',
            hidden: true,
            dataIndex: 'creatorName',
            text: 'Created By'
        },
        {
            xtype: 'gridcolumn',
            stateEvents: 'requestedForName',
            hidden: true,
            dataIndex: 'requestedForName',
            text: 'Used By'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'bchComments',
            //hidden: true,
            width: 160,
            cellWrap: true,
            dataIndex: 'bchComments',
            text: 'BCH Comments'
        },
        {
            xtype: 'gridcolumn',
            stateId: 'description',
            //hidden: true,
            width: 160,
            cellWrap: true,
            dataIndex: 'description',
            text: 'Description'
        }
    ],
    plugins: [{ptype: 'gridexporter'}],

    initConfig: function (instanceConfig) {
        var me = this,
            config = {};
        me.processRequestGrid(config);
        if (instanceConfig) {
            me.self.getConfigurator().merge(me, config, instanceConfig);
        }
        return me.callParent([config]);
    },

    initComponent: function () {
        if (this.getShowContainsIcon()) {
            this.columns[3].hidden = false;
        } else {
            this.columns[3].hidden = true;
        }

        this.callParent(arguments);
    },

    processRequestGrid: function (config) {
        //config.stateId='requestlist'+this.initialConfig.listStateId;
        return config;
    }
});
