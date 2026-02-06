/*
 * File: app/view/ItemsReport.js
 *
 */
Ext.define("bcp.view.ItemsReport", {
    extend: "Ext.panel.Panel",
    alias: "widget.itemsreport",

    requires: [
        "bcp.view.ItemsReportViewModel",
        "bcp.view.ItemsReportViewController",
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

    controller: "itemsreport",
    viewModel: {type: "itemsreport"},
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
                    text: "Items Report"
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
            xtype: "form",
            header: {
                baseCls: "x-panel-header",
                style: "background-color:rgba(20, 104, 153, 0.37);",
                title: "Search Criteria",
                height: 30,
                padding: "5 0 5 15"
            },
            collapsible: true,
            collapsed: false, // Start expanded
            dock: "top",
            items: [
                {
                    xtype: "toolbar",
                    reference: "searchTb1",
                    // dock: "top", // Removed: No longer directly docked
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
                        },
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
                            text: "Reset Search Criteria",
                            listeners: {click: "onResetSearch"}
                        }
                    ]
                }
            ]
        },
        {
            xtype: "form",
            header: {
                baseCls: "x-panel-header",
                style: "background-color:rgba(20, 104, 153, 0.37);",
                title: "Filters",
                height: 30,
                padding: "5 0 5 15"
            },
            collapsible: true,
            collapsed: false, // Start expanded
            dock: "top",
            items: [
                {
                    xtype: "toolbar",
                    reference: "filterTb1",
                    // dock: "top", // Removed: No longer directly docked
                    defaultButtonUI: "default",
                    enableOverflow: true,
                    overflowHandler: "menu",
                    items: [
                        {
                            xtype: "textfield",
                            reference: "tfItemName",
                            allowOnlyWhitespace: false,
                            emptyText: "Item Name",
                            minWidth: 208,
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "combobox",
                            reference: "comboPtc",
                            emptyText: "Project Task",
                            anyMatch: true,
                            displayField: "code",
                            minChars: 2,
                            minWidth: 208,
                            queryMode: "local",
                            valueField: "code",
                            bind: {store: "{projectTasksSearch}"},
                            listeners: {
                                select: {fn: "onFilterFieldChange", scope: "controller"},
                                clear: {fn: "onFilterFieldChange", scope: "controller"}
                            }
                        },
                        {
                            xtype: "textfield",
                            reference: "tfObjcls",
                            emptyText: "Object Class",
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "textfield",
                            reference: "reqNumTf",
                            width: 170,
                            emptyText: "Requisition#",
                            maxLength: 25,
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "combobox",
                            emptyText: "Purchase Type",
                            reference: "comboPurchaseType",
                            width: 160,
                            forceSelection: true,
                            queryMode: "local",
                            displayField: "name",
                            valueField: "id",
                            bind: {store: "{purchaseTypes}"},
                            listeners: {
                                select: {fn: "onFilterFieldChange", scope: "controller"},
                                clear: {fn: "onFilterFieldChange", scope: "controller"}
                            }
                        }
                    ]
                },
                {
                    xtype: "toolbar",
                    reference: "filterTb2",
                    // dock: "top", // Removed: No longer directly docked
                    defaultButtonUI: "default",
                    enableOverflow: true,
                    overflowHandler: "menu",
                    items: [
                        {
                            xtype: "tagfield",
                            reference: "tagItemStatus",
                            displayField: "name",
                            bind: {store: "{itemStatuses}"},
                            valueField: "id",
                            forceSelection: false,
                            queryMode: "local",
                            emptyText: "Item Statuses",
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "textfield",
                            reference: "tfCatalogNum",
                            emptyText: "Catalog Number",
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "textfield",
                            reference: "tfTransactionNum",
                            emptyText: "Transaction#",
                            listeners: {
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "datefield",
                            reference: "dfStatementDate",
                            emptyText: "Statement Date",
                            listeners: {
                                select: {fn: "onFilterFieldChange", scope: "controller"},
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "datefield",
                            reference: "dfReceivedDate",
                            emptyText: "Received Date",
                            listeners: {
                                select: {fn: "onFilterFieldChange", scope: "controller"},
                                change: {fn: "onFilterFieldChange", scope: "controller", buffer: 300}
                            }
                        },
                        {
                            xtype: "button",
                            iconCls: "fas fa-eraser",
                            text: "Reset Filters",
                            listeners: {click: "onResetFilter"}
                        }
                    ]
                }
            ]
        }
    ],
    items: [
        {
            xtype: "gridpanel",
            reference: "list",
            flex: 2,
            bind: {store: "{items}"},
            listeners: {select: "onSelect"},
            columns: [
                {text: "Request ID", dataIndex: "requestId", width: 100, hidden: true},
                {text: "FY", dataIndex: "fy", width: 50, format: "00"},
                {
                    text: "Statement Date",
                    dataIndex: "statementDate",
                    xtype: "datecolumn",
                    width: 120,
                    cellWrap: true,
                    format: "m/d/Y",
                    exportStyle: {
                        alignment: {horizontal: "Right"},
                        format: "Short Date"
                    }
                },
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
                    formatter: 'date("Y-m-d")',
                    hidden: true
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
                {text: "Catalog Number", dataIndex: "catelogNumber", width: 80},
                {text: "Transaction#", dataIndex: "transactionNumber", width: 120},
                {text: "Purpose", dataIndex: "purpose", hidden: true},
                {
                    text: "Is<br>Chemical",
                    dataIndex: "isChemical",
                    width: 80,
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        return bcp.util.CommonUtil.ynRenderer(value);
                    }
                },
                {text: "Item Status", dataIndex: "itemStatus", width: 100},
                {text: "Project Task", dataIndex: "projectTask", width: 120},
                {text: "Object Class", dataIndex: "objectClass", width: 100},
                {
                    text: "Is Taggable<br>Equipment",
                    dataIndex: "isTaggableEquipment",
                    width: 100,
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        return bcp.util.CommonUtil.ynRenderer(value);
                    }
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
            xtype: "fileattachmentgrid",
            flex: 1,
            readOnly: true,
            tabConfig: {bind: {badgeText: "{fileCount}"}}
        }
    ],
    listeners: {added: "onViewAdded"}
});
