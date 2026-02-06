Ext.define('bcp.view.EditReq', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.editreq',

    requires: [
        'bcp.view.GeneralInfoPanel',
        'bcp.view.VendorPanel',
        'bcp.view.JustificationPanel',
        'bcp.view.ItemPanel',
        'bcp.view.FileAttachmentGrid',
        'bcp.view.FinancePanel',
        'Ext.form.Panel',
        'Ext.tab.Tab',
        'Ext.grid.Panel'
    ],

    controller: 'editreq',
    viewModel: {type: 'editreq'},
    activeTab: 0,

    items: [
        {
            xtype: 'generalinfopanel',
            reference: 'generalPanelEditable',
            title: 'Request Summary'
        },
        {xtype: 'vendorpanel', reference: 'vendorPanelEditable'},
        {xtype: 'justificationpanel', reference: 'justificationPanelEditable'},
        {xtype: 'itempanel', reference: 'itemPanelEditable', title: 'Items'},
        {
            xtype: 'fileattachmentgrid',
            tabConfig: {bind: {badgeText: '{fileCount}'}},
            reference: 'fileListEditable',
            title: 'File Attachments'
        },
        {
            xtype: 'financepanel',
            reference: 'financePanelEditable',
            title: 'Finance Data'
        }
    ],
    listeners: {beforetabchange: 'onTabpanelBeforeTabChange'}
});
