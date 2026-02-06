/*
 * File: app/view/EAReport.js
 * Author: ppg
 * Create Date: 01/2021
 * Objective: for the EA Report
 */

Ext.define('bcp.view.EAReport', {
    extend: 'Ext.form.Panel',
    alias: 'widget.eareport',

    requires: [
        'bcp.view.EAReportViewModel',
        'bcp.view.EAReportViewController',
        'bcp.view.RequestGrid',
        'Ext.toolbar.Toolbar',
        'Ext.form.Label',
        'Ext.toolbar.Fill',
        'Ext.button.Button',
        'Ext.form.field.Display',
        'Ext.form.field.ComboBox',
        'Ext.form.field.Date',
        'Ext.grid.Panel',
        'Ext.resizer.Splitter'
    ],

    controller: 'eareport',
    viewModel: {type: 'eareport'},
    scrollable: true,

    layout: {type: 'vbox', align: 'stretch'},
    dockedItems: [
        {
            xtype: 'toolbar',
            baseCls: 'x-panel-header',
            dock: 'top',
            height: 44,
            style: 'background-color:	#184ed1;',
            defaultButtonUI: 'default',
            enableOverflow: true,
            overflowHandler: 'menu',
            items: [
                {
                    xtype: 'label',
                    cls: 'x-panel-header-title-default',
                    padding: '5 15 5 15',
                    text: 'EA Report'
                },
                {xtype: 'tbfill'},
                {
                    xtype: 'button',
                    id: 'eaReportExport',
                    margin: '0 5 0 5',
                    iconCls: 'fas fa-download',
                    text: 'Export Report',
                    //hidden: true,
                    disabled: true,
                    bind: {
                        //hidden: '{list.data.length < 1}',
                        hidden: '{isDetailView}'
                    },
                    listeners: {click: 'onExport'}
                }
            ]
        },
        {
            xtype: 'toolbar',
            reference: 'filterTb1',
            dock: 'top',
            defaultButtonUI: 'default',
            enableOverflow: true,
            overflowHandler: 'menu',
            items: [
                /*{
                    xtype: 'label',
                    reference: 'tOU',
                    width: 120,
                    html: '<b></b>'
                },*/
                /*{
                    xtype: 'label',
                    reference: 'tDiv',
                    width: 180,
                    hidden: true,
                    html: '<b></b>'
                },*/
                /*{
                    xtype: 'label',
                    reference: 'tSupDiv',
                    html: '<b>Divisions:</b>'
                },*/
                /* {
                     xtype: 'combobox',
                     reference: 'comboSupDiv',
                     width: 400,
                     emptyText: 'Supported Divisions',
                     displayField: 'name',
                     forceSelection: true,
                     queryMode: 'local',
                     //valueField: 'divisionId',
                     valueField: 'code',
                     editable: false,
                     store: 'SupportedDivisions'
                 },*/
                {
                    xtype: 'tagfield',
                    reference: 'comboSupDiv',
                    width: 600,
                    fieldLabel: 'Divisions',
                    emptyText: 'Select one or more Divisions',
                    displayField: 'shortName',
                    required: true,
                    allowBlank: false,
                    forceSelection: false,
                    queryMode: 'local',
                    valueField: 'code',
                    bind: {store: '{divisions}'}
                }
                /*{
                    xtype: 'combobox',
                    reference: 'comboGrp',
                    width: 100,
                    emptyText: 'Group',
                    displayField: 'shortName',
                    forceSelection: true,
                    queryMode: 'local',
                    valueField: 'groupId',
                    bind: {
                        store: '{groups}'
                    }
                },
                {
                    xtype: 'combobox',
                    reference: 'comboEmp',
                    allowOnlyWhitespace: false,
                    emptyText: 'Requester Name',
                    anyMatch: true,
                    displayField: 'displayName',
                    forceSelection: true,
                    minChars: 2,
                    queryMode: 'local',
                    queryParam: 'filter',
                    typeAhead: true,
                    valueField: 'peopleId',
                    bind: {
                        store: '{bcEmployees}'
                    }
                },
                {
                    xtype: 'textfield',
                    reference: 'reqNumTf',
                    emptyText: 'Requisition#',
                    maxLength: 8
                }*/
            ]
        },
        {
            xtype: 'toolbar',
            reference: 'filterTb2',
            dock: 'top',
            defaultButtonUI: 'default',
            enableOverflow: true,
            overflowHandler: 'menu',
            items: [
                {
                    xtype: 'datefield',
                    reference: 'dateFrom',
                    emptyText: 'From',
                    format: 'Y-m-d'
                },
                {
                    xtype: 'datefield',
                    reference: 'dateTo',
                    emptyText: 'To',
                    format: 'Y-m-d'
                },
                /*{
                    xtype: 'combobox',
                    reference: 'comboItemStatus',
                    value: 6,
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    emptyText: 'Item Status',
                    forceSelection: true,
                    queryMode: 'local',
                    valueField: 'id',
                    bind: {
                        store: '{LkItemStatus}'
                    }
                },*/
                {
                    xtype: 'button',
                    iconCls: 'fas fa-search',
                    text: 'Search',
                    formBind: true,
                    listeners: {click: 'onSearch'}
                }
                /*{
                    xtype: 'button',
                    iconCls: 'fas fa-eraser',
                    text: 'Reset Filters',
                    listeners: {
                        click: 'onReset'
                    }
                }*/
            ]
        }
    ],
    items: [
        {
            xtype: 'itemgrid',
            listStateId: 'searchGrid',
            reference: 'list',
            flex: 1,
            bind: {store: '{searchedItems}'}
        }
    ],
    listeners: {added: 'onViewAdded'}
});
