/*
 * File: app/view/ItemGrid.js
 * Author: ppg
 * Create Date: 01/2021
 * Objective: for the EA Report
 */

Ext.define('bcp.view.ItemGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.itemgrid',

    requires: [
        'Ext.view.Table',
        'Ext.grid.column.Number',
        'Ext.grid.column.Date',
        'Ext.grid.column.Boolean',
        'Ext.grid.plugin.Exporter',
        'Ext.grid.feature.Summary'
    ],

    config: {listStateId: 'ig'},

    stateId: 'itemlist',
    itemId: 'itemGrid',

    bind: {store: '{store}'},
    viewConfig: {enableTextSelection: true},
    columns: [
        {
            xtype: 'numbercolumn',
            stateEvents: 'reqId',
            width: 90,
            dataIndex: 'requestId',
            text: 'Request Id',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            width: 60,
            cellWrap: true,
            dataIndex: 'ou',
            text: 'OU'
        },
        {
            xtype: 'gridcolumn',
            width: 80,
            cellWrap: true,
            dataIndex: 'division',
            text: 'Division'
        },
        {
            xtype: 'gridcolumn',
            width: 150,
            cellWrap: true,
            dataIndex: 'bch',
            text: 'Bankcard Holder'
        },
        {
            xtype: 'gridcolumn',
            width: 140,
            cellWrap: true,
            dataIndex: 'requisitionNumber',
            text: 'Requisition Number'
        },
        {
            xtype: 'gridcolumn',
            width: 80,
            hidden: true,
            dataIndex: 'vendorId',
            text: 'Vendor Id'
        },
        {
            xtype: 'gridcolumn',
            width: 130,
            cellWrap: true,
            dataIndex: 'vendorName',
            text: 'Vendor Name'
        },
        {
            xtype: 'numbercolumn',
            stateEvents: 'itemId',
            width: 70,
            dataIndex: 'itemId',
            text: 'Item Id',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            minWidth: 180,
            cellWrap: true,
            dataIndex: 'itemName',
            text: 'Item Name'
        },
        {
            xtype: 'gridcolumn',
            minWidth: 180,
            cellWrap: true,
            dataIndex: 'description',
            text: 'Description'
        },
        {
            xtype: 'gridcolumn',
            width: 130,
            cellWrap: true,
            dataIndex: 'catalogNumber',
            text: 'Catalog Number'
        },
        {
            xtype: 'gridcolumn',
            hidden: true,
            cellWrap: true,
            dataIndex: 'purpose',
            text: 'Purpose'
        },
        {
            xtype: 'gridcolumn',
            minWidth: 80,
            width: 100,
            align: 'end',
            dataIndex: 'actualPrice',
            text: 'Unit Price',
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
            }
        },
        {
            xtype: 'numbercolumn',
            minWidth: 80,
            width: 50,
            align: 'center',
            dataIndex: 'actualQuantity',
            text: 'Quantity',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            summaryType: 'sum',
            minWidth: 180,
            align: 'end',
            dataIndex: 'actualAmount',
            exportSummaryRenderer: true,
            text: 'Amount',
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
            summaryRenderer: function (val, params, data, metaData) {
                return (
                    'Actual Total: ' + bcp.util.CommonUtil.moneyRenderer(val)
                );
            }
        },
        {
            xtype: 'gridcolumn',
            width: 150,
            cellWrap: true,
            dataIndex: 'projTask',
            text: 'Project Task'
        },
        {
            xtype: 'gridcolumn',
            width: 200,
            cellWrap: true,
            dataIndex: 'objectClass',
            text: 'Object Class'
        },
        {
            xtype: 'gridcolumn',
            width: 160,
            cellWrap: true,
            dataIndex: 'statementDate',
            text: 'Statement Date'
        },
        {
            xtype: 'booleancolumn',
            minWidth: 100,
            width: 110,
            dataIndex: 'isChemical',
            text: 'Is a Chemical',
            falseText: 'No',
            trueText: 'Yes'
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
                if (value !== 0) {
                    return 'Yes';
                } else {
                    return 'No';
                }
            },
            maxWidth: 160,
            minWidth: 120,
            cellWrap: true,
            dataIndex: 'shoppingCartFileId',
            text: 'Is Shopping Cart'
        }
    ],
    plugins: [{ptype: 'gridexporter'}],
    features: [{ftype: 'summary'}],
    initConfig: function (instanceConfig) {
        var me = this,
            config = {};
        me.processRequestGrid(config);
        if (instanceConfig) {
            me.self.getConfigurator().merge(me, config, instanceConfig);
        }
        return me.callParent([config]);
    },

    processRequestGrid: function (config) {
        //config.stateId='requestlist'+this.initialConfig.listStateId;
        return config;
    }
});
