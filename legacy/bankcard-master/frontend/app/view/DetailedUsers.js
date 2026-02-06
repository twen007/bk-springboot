Ext.define("bcp.view.DetailedUsers", {
    extend: "Ext.panel.Panel",
    alias: "widget.detailedusers",

    requires: [
        "bcp.view.DetailedUsersViewModel",
        "bcp.view.DetailedUsersViewController",
        "Ext.toolbar.Toolbar",
        "Ext.form.Label",
        "Ext.button.Button",
        "Ext.grid.Panel",
        "Ext.grid.column.Number",
        "Ext.form.field.ComboBox",
        "Ext.grid.column.Boolean",
        "Ext.form.field.Checkbox",
        "Ext.grid.column.Date",
        "Ext.grid.plugin.RowEditing",
        "Ext.form.field.Number"
    ],

    controller: "detailedusers",
    viewModel: {type: "detailedusers"},
    layout: "fit", // Simplified layout
    dockedItems: [
        {
            xtype: "toolbar",
            baseCls: "x-panel-header",
            dock: "top",
            height: 44,
            style: "background-color:	#184ed1;",
            defaultButtonUI: "default",
            enableOverflow: true,
            overflowHandler: "menu",
            items: [
                {
                    xtype: "label",
                    cls: "x-panel-header-title-default",
                    padding: "5 15 5 15",
                    text: "Detailees (Double Click to Edit)"
                },
                {
                    xtype: "button",
                    text: "Add",
                    iconCls: "fas fa-plus",
                    listeners: {click: "add"}
                },
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    text: "Remove",
                    iconCls: "fas fa-trash",
                    bind: {disabled: "{!list.selection}", hidden: "{!record}"},
                    listeners: {click: "remove"}
                },
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "fas fa-sync-alt",
                    text: "Refresh",
                    listeners: {click: "onReload"}
                }
            ]
        }
    ],

    items: [
        {
            xtype: "gridpanel",
            reference: "list",
            resizable: false,
            title: "",
            forceFit: true,
            bind: {store: "{detailedUsers}"},
            dockedItems: [
                {
                    xtype: "toolbar",
                    dock: "top",
                    defaultButtonUI: "default",
                    items: [
                        {
                            xtype: "textfield",
                            reference: "tfFilter",
                            minWidth: 350,
                            emptyText: "filter results by typing staff id here",
                            listeners: {
                                change: {
                                    fn: "onStaffTfFilterChange",
                                    delay: 500
                                }
                            }
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
            columns: [
                {text: "ID", dataIndex: "id", hidden: true}, //Hidden ID
                {
                    xtype: "gridcolumn",
                    minWidth: 200,
                    cellWrap: true,
                    dataIndex: "peopleId",
                    text: "Employee Id",
                    /*editor: {
                        xtype: "textfield",
                        allowBlank: false,
                        allowOnlyWhitespace: false
                    }*/
                    editor: {
                        xtype: "combobox",
                        reference: "comboEmp",
                        emptyText: "Employee Name",
                        //name: "peopleId",
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        displayField: "displayName",
                        forceSelection: true,
                        minChars: 2,
                        minWidth: 200,
                        queryParam: "filter",
                        store: "NistEmployees",
                        typeAhead: true,
                        valueField: "peopleId"
                    }
                },
                {
                    xtype: "gridcolumn",
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var rec = Ext.getStore("Ous").findRecord("ouId", value);
                        return rec ? rec.get("code") : "";
                    },
                    minWidth: 80,
                    dataIndex: "ouOrgId",
                    text: "OU",
                    editor: {
                        xtype: "combobox",
                        reference: "comboOu",
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        displayField: "code",
                        forceSelection: true,
                        queryMode: "local",
                        bind: {store: "{ous}"},
                        valueField: "ouId",
                        listeners: {change: "onOUChange"}
                    }
                },
                {
                    xtype: "gridcolumn",
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var rec = Ext.getStore("Divisions").findRecord("divisionId", value);
                        return rec ? rec.get("code") : "";
                    },
                    minWidth: 80,
                    dataIndex: "divOrgId",
                    text: "Division",
                    editor: {
                        xtype: "combobox",
                        reference: "comboDiv",
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        displayField: "code",
                        forceSelection: true,
                        queryMode: "local",
                        bind: {store: "{divisions}"},
                        valueField: "divisionId",
                        listeners: {change: "onDivChange"}
                    }
                },
                {
                    xtype: "gridcolumn",
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var rec = Ext.getStore("Groups").findRecord("groupId", value);
                        return rec ? rec.get("shortName") : "";
                    },
                    minWidth: 100,
                    dataIndex: "grpOrgId",
                    text: "Group",
                    editor: {
                        xtype: "combobox",
                        reference: "comboGroup",
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        displayField: "shortName",
                        forceSelection: true,
                        queryMode: "local",
                        valueField: "groupId",
                        bind: {store: "{groups}"}
                    }
                },
                {
                    text: "Access Group",
                    dataIndex: "accessGroup",
                    editor: {
                        xtype: "combobox",
                        displayField: "value",
                        valueField: "key",
                        queryMode: "local",
                        forceSelection: true,
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        bind: {store: "{yesnostore}"}
                    }
                },
                {
                    text: "Access Div",
                    dataIndex: "accessDiv",
                    editor: {
                        xtype: "combobox",
                        displayField: "value",
                        valueField: "key",
                        queryMode: "local",
                        forceSelection: true,
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        bind: {store: "{yesnostore}"}
                    }
                },
                {
                    text: "Access OU",
                    dataIndex: "accessOu",
                    editor: {
                        xtype: "combobox",
                        displayField: "value",
                        valueField: "key",
                        queryMode: "local",
                        forceSelection: true,
                        allowBlank: false,
                        allowOnlyWhitespace: false,
                        bind: {store: "{yesnostore}"}
                    }
                },
                {
                    xtype: "datecolumn",
                    stateId: "submitteddate",
                    width: 120,
                    cellWrap: true,
                    dataIndex: "validUntilDate",
                    text: "Valid Until",
                    format: "m/d/Y",
                    editor: {
                        xtype: "datefield",
                        format: "m/d/Y",
                        allowBlank: false,
                        allowOnlyWhitespace: false
                    }
                }
            ],
            plugins: [
                {
                    ptype: "rowediting",
                    pluginId: "rowEditPluginForDetailee",
                    listeners: {
                        canceledit: "onRowEditingCanceledit",
                        edit: "onRowEditingEdit",
                        beforeedit: "onRowEditingBeforeEdit"
                    }
                }
            ]
        }
    ]
   // ,listeners: {added: "onViewAdded"}
});
