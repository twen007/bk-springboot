/*
 * File: app/view/FileAttachments.js
 * this is the popup form to upload, download and remove file attachments
 */

Ext.define("bcp.view.FileAttachments", {
    extend: "Ext.window.Window",
    alias: "widget.fileattachments",

    requires: [
        "bcp.view.FileAttachmentsViewModel",
        "bcp.view.FileAttachmentsViewController",
        "Ext.grid.Panel",
        "Ext.toolbar.Toolbar",
        "Ext.button.Button",
        "Ext.grid.column.Number",
        "Ext.grid.column.Date",
        "Ext.form.Panel",
        "Ext.form.field.Display",
        "Ext.form.field.Hidden",
        "Ext.form.field.ComboBox",
        "Ext.form.field.Number",
        "Ext.form.field.File"
    ],

    controller: "fileattachments",
    viewModel: {type: "fileattachments"},
    modal: true,
    height: 400,
    shrinkWrap: 0,
    width: 600,
    layout: "card",
    closeAction: "hide",
    title: "File Attachments",
    config: { // This is where you set the config data
        bcpReq: null, // This is the config property for bcpReq
    },
    items: [
        {
            xtype: "gridpanel",
            reference: "list",
            title: "",
            forceFit: true,
            store: "RequestFiles",
            dockedItems: [
                {
                    xtype: "toolbar",
                    dock: "top",
                    defaultButtonUI: "default",
                    items: [
                        {
                            xtype: "button",
                            text: "Add",
                            listeners: {click: "add"}
                        },
                        {
                            xtype: "button",
                            text: "Remove",
                            bind: {
                                disabled: "{!list.selection}",
                                hidden: "{!record}"
                            },
                            listeners: {click: "remove"}
                        },
                        {
                            xtype: "button",
                            text: "Download",
                            bind: {disabled: "{!list.selection}"},
                            listeners: {click: "onDownload"}
                        }
                    ]
                }
            ],
            columns: [
                {
                    xtype: "numbercolumn",
                    hidden: true,
                    dataIndex: "fileId",
                    text: "File Id",
                    format: "00"
                },
                {
                    xtype: "gridcolumn",
                    width: 150,
                    cellWrap: true,
                    dataIndex: "categoryName",
                    text: "Category"
                },
                {
                    xtype: "gridcolumn",
                    width: 200,
                    cellWrap: true,
                    dataIndex: "fileName",
                    text: "File Name"
                },
                {
                    xtype: "gridcolumn",
                    width: 150,
                    cellWrap: true,
                    dataIndex: "fileType",
                    text: "File Type"
                },
                {
                    xtype: "gridcolumn",
                    renderer: function (value, metaData, record, rowIndex, colIndex, store, view) {
                        var v = Ext.util.Format.round(value / 1024, 2);
                        if (v < 1024) {
                            return v + "KB";
                        } else {
                            return Ext.util.Format.round(v / 1024, 2) + "MB";
                        }
                    },
                    width: 150,
                    dataIndex: "fileSize",
                    text: "File Size"
                },
                {
                    xtype: "gridcolumn",
                    //hidden: true,
                    cellWrap: true,
                    dataIndex: "createdByName",
                    text: "Uploaded By"
                },
                {
                    xtype: "datecolumn",
                    //hidden: true,
                    cellWrap: true,
                    dataIndex: "uploadedOn",
                    text: "Uploaded On",
                    format: "m/d/Y"
                }
            ],
            listeners: {select: "select"}
        },
        {
            xtype: "form",
            reference: "form",
            flex: 1,
            defaults: {labelWidth: 120},
            bodyPadding: 10,
            items: [
                {
                    xtype: "displayfield",
                    cls: "bcp_notice",
                    fieldLabel: "Note",
                    labelWidth: 60,
                    value: "Only the following file types are allowed: <b>.doc, .docx, .pdf, .jpg, .gif, .txt, .xls, .xlsx, and .csv</b>.  File size is limited to <b>20MB</b>."
                },
                {
                    xtype: "hiddenfield",
                    reference: "requestIdHf",
                    fieldLabel: "Label",
                    name: "requestId",
                    bind: {value: "{record.requestId}"}
                },
                {
                    xtype: "hiddenfield",
                    disabled: true,
                    maxWidth: 300,
                    minWidth: 200,
                    fieldLabel: "Quantity",
                    name: "quantity",
                    bind: {value: "{record.quantity}"}
                },
                {
                    xtype: "combobox",
                    reference: "fileCateCombo",
                    anchor: "100%",
                    fieldLabel: "File Category",
                    name: "fileCategoryId",
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    displayField: "name",
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "id",
                    bind: {store: "{fileCategories}"
                    },
                    listeners: {change: "onCategoryChange"}
                },
                {
                    xtype: "textfield",
                    anchor: "100%",
                    reference: "cateDesc",
                    hidden: true,
                    fieldLabel: "Category Description",
                    name: "fileDescription",
                    emptyText: "Optionally enter a description",
                    maxLength:100
                },
                {
                    xtype: "filefield",
                    reference: "fileLocation",
                    anchor: "100%",
                    fieldLabel: "File Location",
                    name: "file",
                    maxLength: 200,
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    listeners: {change: "onFilefieldChange"}
                }
            ],
            dockedItems: [
                {
                    xtype: "toolbar",
                    dock: "bottom",
                    defaultButtonUI: "default",
                    layout: {type: "hbox", pack: "center"},
                    items: [
                        {
                            xtype: "button",
                            text: "Cancel",
                            listeners: {click: "cancel"}
                        },
                        {
                            xtype: "button",
                            text: "Upload",
                            listeners: {click: "upload"}
                        }
                    ]
                }
            ],
            listeners: {show: "onFormShow"}
        }
    ],
    listeners: {close: "onWindowClose"}
});
