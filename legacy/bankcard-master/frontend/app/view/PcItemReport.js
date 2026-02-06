/*
 * File: app/view/PcItemReport.js
 *
 */
Ext.define("bcp.view.PcItemReport", {
    extend: "Ext.panel.Panel",
    alias: "widget.pcitemreport",

    requires: [
        "bcp.view.PcItemReportViewModel",
        "bcp.view.PcItemReportViewController",
        "Ext.grid.Panel",
        "Ext.toolbar.Toolbar",
        "Ext.form.Label",
        "Ext.toolbar.Fill",
        "Ext.button.Button",
        "Ext.view.Table",
        "Ext.grid.column.Number",
        "Ext.grid.column.Date",
        "Ext.grid.column.Boolean",
        "Ext.grid.plugin.Exporter"
    ],

    controller: "pcitemreport",
    viewModel: {type: "pcitemreport"},
    scrollable: true,

    layout: {type: "vbox", align: "stretch"},
    dockedItems: [
        {
            xtype: "toolbar",
            baseCls: "x-panel-header",
            dock: "top",
            height: 44,
            style: "background-color: #184ed1;",
            defaultButtonUI: "default",
            enableOverflow: true,
            overflowHandler: "menu",
            items: [
                {
                    xtype: "label",
                    cls: "x-panel-header-title-default",
                    padding: "5 15 5 15",
                    text: "Property Custodian Report"
                },
                {xtype: "tbfill"},
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "fas fa-file-excel",
                    text: "Export",
                    listeners: {click: "onExport"}
                }
            ]
        },
        {
            xtype: "toolbar",
            reference: "filterTb1",
            dock: "top",
            defaultButtonUI: "default",
            enableOverflow: true,
            overflowHandler: "menu",
            items: [
                {
                    xtype: "combobox",
                    reference: "comboFy",
                    width: 85,
                    emptyText: "FY",
                    forceSelection: true,
                    queryMode: "local",
                    displayField: "display",
                    valueField: "fy",
                    bind: {store: "{fys}"},
                    name: "fy"
                },
                {
                    xtype: "combobox",
                    reference: "comboOu",
                    width: 110,
                    emptyText: "OU",
                    displayField: "shortName",
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "ouId",
                    bind: {store: "{ous}"}
                },
                {
                    xtype: "combobox",
                    reference: "comboDiv",
                    width: 120,
                    emptyText: "Division",
                    displayField: "shortName",
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "divisionId",
                    bind: {store: "{divisions}"}
                },
                {
                    xtype: "combobox",
                    reference: "comboGrp",
                    width: 100,
                    emptyText: "Group",
                    displayField: "shortName",
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "groupId",
                    bind: {store: "{groups}"}
                }
            ]
        },
        {
            xtype: "toolbar",
            reference: "filterTb2",
            dock: "top",
            defaultButtonUI: "default",
            enableOverflow: true,
            overflowHandler: "menu",
            items: [
                {
                    xtype: "datefield",
                    reference: "dateFrom",
                    emptyText: "From",
                    format: "Y-m-d"
                },
                {
                    xtype: "datefield",
                    reference: "dateTo",
                    emptyText: "To",
                    format: "Y-m-d"
                },
                {
                    xtype: "button",
                    iconCls: "fas fa-search",
                    text: "Search",
                    listeners: {click: "onSearch"}
                },
                {
                    xtype: "button",
                    iconCls: "fas fa-eraser",
                    text: "Reset Filters",
                    listeners: {click: "onReset"}
                }
            ]
        }
    ],
    items: [
        {
            xtype: "gridpanel",
            reference: "list",
            flex: 2,
            bind: {store: "{pcItems}"},
            listeners: {select: "onSelect"},
            columns: [
                {text: "Request ID", dataIndex: "requestId", width: 100, hidden: true},
                {text: "FY", dataIndex: "fy", width: 50, format: "00"},
                {
                    text: "Created Date",
                    dataIndex: "createdDate",
                    xtype: "datecolumn",
                    width: 120,
                    cellWrap: true,
                    format: "m/d/Y",
                    exportStyle: {
                        alignment: {horizontal: "Right"},
                        format: "Short Date"
                    },
                    formatter: 'date("Y-m-d")'
                },
                {text: "Requisition<br>Number", dataIndex: "requisitionNumber", width: 200},
                {text: "OU", dataIndex: "ou", width: 50},
                {text: "Division", dataIndex: "division", width: 80},
                {text: "Group", dataIndex: "group", width: 80},
                {text: "Vendor", dataIndex: "vendor", width: 180, cellWrap: true},
                {text: "Item Name", dataIndex: "itemName", width: 200, cellWrap: true},
                {text: "Description", dataIndex: "itemDescription", width: 250, cellWrap: true},
                {
                    text: "Price",
                    dataIndex: "price",
                    width: 80,
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        return bcp.util.CommonUtil.moneyRenderer(value);
                    },
                    align: "end"
                },
                {text: "Quantity", dataIndex: "quantity", width: 80},
                {text: "Catalog Number", dataIndex: "catelogNumber", width: 80, hidden: true},
                {text: "Purpose", dataIndex: "purpose", hidden: true},
                {xtype: "booleancolumn", text: "Is<br>Chemical", dataIndex: "isChemical", width: 80},
                {text: "Item Status", dataIndex: "itemStatus", width: 100},
                {text: "Project Task", dataIndex: "projectTask", width: 120},
                {text: "Object Class", dataIndex: "objectClass", width: 100},
                {
                    xtype: "booleancolumn",
                    text: "Is Taggable<br>Equipment",
                    dataIndex: "isTaggableEquipment",
                    width: 100
                },
                {
                    text: "Price<br>Ordered",
                    dataIndex: "priceOrdered",
                    width: 130,
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        return bcp.util.CommonUtil.moneyRenderer(value);
                    },
                    align: "end"
                },
                {text: "Quantity<br>Ordered", dataIndex: "quantityOrdered", width: 120, align: "right"},
                {text: "Item Notes", dataIndex: "itemNotes", width: 120},
                {
                    text: "Date Received",
                    dataIndex: "dateReceived",
                    xtype: "datecolumn",
                    width: 120,
                    cellWrap: true,
                    format: "m/d/Y",
                    hidden: true,
                    exportStyle: {
                        alignment: {horizontal: "Right"},
                        format: "Short Date"
                    },
                    formatter: 'date("Y-m-d")'
                }
            ],
            plugins: [{ptype: "gridexporter"}]
        },
        {xtype: "splitter"},
        {
            xtype: 'fileattachmentgrid',
            flex:1,
            readOnly: true,
            tabConfig: { bind: { badgeText: '{fileCount}' } }
        },
    ],
    listeners: {added: "onViewAdded"}
});
