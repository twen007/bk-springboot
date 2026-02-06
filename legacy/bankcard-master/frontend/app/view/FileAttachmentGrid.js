/*
 * File: app/view/FileAttachmentGrid.js
 * This is the file attachment tab
 */

Ext.define("bcp.view.FileAttachmentGrid", {
    extend: "Ext.grid.Panel",
    alias: "widget.fileattachmentgrid",

    requires: [
        "bcp.view.FileAttachmentGridViewModel",
        "bcp.view.FileAttachmentGridViewController",
        "Ext.toolbar.Toolbar",
        "Ext.form.Label",
        "Ext.toolbar.Fill",
        "Ext.button.Button",
        "Ext.grid.column.Number",
        "Ext.grid.column.Date"
    ],

    controller: "fileattachmentgrid",
    viewModel: {type: "fileattachmentgrid"},
    reference: "fileList",
    flex: 1,
    forceFit: true,
    store: "RequestFiles",
    selModel: {
        selType: 'checkboxmodel',
        mode: 'MULTI'
    },

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
                    text: "File Attachments"
                },
                {xtype: "tbfill"},
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "fas fa-upload",
                    text: "Upload",
                    reference: "btnUpload",
                    listeners: {click: "onUploadFile"}
                },
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "fas fa-download",
                    text: "Download",
                    reference: "btnDownload",
                    hidden: true,
                    listeners: {click: "onDownloadFile"}
                },
                {
                    //issue 495 nice to have, works okay with most files except excel
                    //users may enter chars in excel that throw error when convert to pdf
                    //converted excel is hard to read (not showing grid and columns maybe too wide to fit)
                    //docx with pictures in it would not show pictures in the pdf
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "fas fa-archive",
                    text: "Download All as PDF",
                    reference: "btnDownloadAllPdf",
                    cls: "x-badge",
                    tooltip: "Experimental", //this is for showing the badge text
                    hidden: true, //highly experimental feature with things that didn't work well so hide it for now
                    listeners: {click: "onDownloadFileAllPdf"}
                },
                {
                    xtype: "button",
                    margin: "0 5 0 5",
                    iconCls: "far fa-file-archive",
                    text: "Download Selected as Zip",
                    reference: "btnDownloadSelectedZip",
                    //hidden: true,
                    listeners: {click: "onDownloadFileSelectedZip"}
                },
                {
                    xtype: "button",
                    text: "Remove",
                    reference: "btnRemove",
                    hidden: true,
                    listeners: {click: "remove"}
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
    listeners: {selectionchange: "onGridSelectionChange", added: "onViewAdded"}
});
