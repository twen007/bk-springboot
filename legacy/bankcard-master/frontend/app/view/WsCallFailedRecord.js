Ext.define('bcp.view.WsCallFailedRecord', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.wscallfailedrecord',

    requires: [
        'bcp.view.WsCallFailedRecordViewModel',
        'bcp.view.WsCallFailedRecordViewController',
        'Ext.grid.Panel',
        'Ext.grid.column.Action',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button',
        'Ext.view.Table',
        'Ext.grid.column.Number',
        'Ext.grid.column.Date',
        'Ext.selection.CheckboxModel'
    ],

    controller: 'wscallfailedrecord',
    viewModel: {type: 'wscallfailedrecord'},
    itemId: 'ibbrFailedWsRecPanel',
    title: 'IBBR',
    reference: 'ibbrList',
    store: 'WsCallFailedRecordStore',
    columns: [
        {
            xtype: 'numbercolumn',
            width: 80,
            align: 'center',
            dataIndex: 'id',
            text: 'Id',
            format: '00',
            hidden: true
        },
        {
            xtype: 'actioncolumn',
            width: 100,
            maxWidth: 100,
            minWidth: 100,
            handler: 'onDetail',
            align: 'center',
            text: 'View',
            items: [{iconCls: 'fa fa-eye'}]
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
                return value.name;
            },
            width: 200,
            align: 'center',
            dataIndex: 'ibbrRecord',
            text: 'Item Name'
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
                return value.cost;
            },
            width: 100,
            align: 'right',
            dataIndex: 'ibbrRecord',
            text: 'Cost'
        },
        {
            xtype: 'numbercolumn',
            width: 100,
            align: 'center',
            dataIndex: 'statusCode',
            text: 'Status Code',
            format: '00'
        },
        {
            xtype: 'gridcolumn',
            width: 500,
            cellWrap: true,
            align: 'center',
            dataIndex: 'errorMessage',
            text: 'Error Message'
        },
        {
            xtype: 'gridcolumn',
            width: 200,
            align: 'center',
            dataIndex: 'dateCreated',
            text: 'Date Created'
        },
        {
            xtype: 'gridcolumn',
            width: 200,
            align: 'center',
            dataIndex: 'lastSubmitted',
            text: 'Last Submitted'
        }
    ],
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            defaultButtonUI: 'default',
            items: [
                {
                    xtype: 'button',
                    text: 'Resubmit',
                    listeners: {click: 'onResubmit'}
                }
            ]
        }
    ],
    selModel: {
        selType: 'checkboxmodel',
        mode: 'MULTI',
        checkboxSelect: true,
        rowNumbererHeaderWidth: 0
    },
    listeners: {select: 'onWsCallRecordRowSelect', added: {fn: 'onViewAdded'}}
});
