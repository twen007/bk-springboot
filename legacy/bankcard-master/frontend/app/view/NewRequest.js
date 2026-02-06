Ext.define('bcp.view.NewRequest', {
    extend: 'Ext.tab.Panel',
    alias: 'widget.newrequest',

    requires: [
        'bcp.view.NewRequestViewModel',
        'bcp.view.NewRequestViewController',
        'bcp.view.GeneralInfoPanel',
        'Ext.toolbar.Toolbar',
        'Ext.tab.Tab',
        'Ext.form.Panel'
    ],

    controller: 'newrequest',
    viewModel: {type: 'newrequest'},
    anchor: '80%',
    cls: 'wizardnew',
    flex: 1,
    scrollable: true,
    title: 'New Purchase Request',
    activeTab: 0,

    dockedItems: [
        {
            xtype: 'toolbar',
            fixed: true,
            reference: 'bbar',
            cls: ['wizardbbar', 'allow-overflow'],
            dock: 'bottom',
            defaults: {height: 30},
            defaultButtonUI: 'default',
            enableOverflow: true,
            overflowHandler: 'menu',
            layout: {type: 'hbox', pack: 'center'},
            items: [
                {
                    xtype: 'button',
                    reference: 'prevBtn',
                    disabled: true,
                    style: 'background-color: #039be5;',
                    iconCls: 'fas fa-arrow-left',
                    text: 'Previous',
                    listeners: {click: 'onPrev'}
                },
                {
                    xtype: 'button',
                    iconCls: 'fas fa-trash',
                    text: 'Discard',
                    listeners: {click: 'onDiscard'}
                },
                {
                    xtype: 'button',
                    cls: 'blue-badge',
                    iconCls: 'fas fa-paperclip',
                    text: 'File Attachments',
                    bind: {badgeText: '{fileCount}'},
                    listeners: {click: 'onFileAttachment'}
                },
                {
                    xtype: 'button',
                    iconCls: 'fas fa-envelope',
                    text: 'Request Help',
                    tooltip:
                        "email the Support Staff to fill out the request form (this function does not work if you don't have a native email client setup)",
                    listeners: {click: 'onQuickRoute'}
                },
                {
                    xtype: 'button',
                    reference: 'submitBtn',
                    cls: '',
                    hidden: true,
                    style: 'background-color: #039be5;',
                    iconCls: 'fas fa-cloud-upload-alt',
                    text: 'Submit',
                    bind: {
                        disabled:
                            '{generalInfo.creatorId!==generalInfo.requesterId || generalInfo.requestId===0}'
                    },
                    listeners: {click: 'onSubmit'}
                },
                {
                    xtype: 'button',
                    reference: 'nextBtn',
                    cls: '',
                    style: 'background-color: #039be5;',
                    iconCls: 'fas fa-arrow-right',
                    text: 'Save & Continue',
                    listeners: {click: 'onNext'}
                }
            ]
        }
    ],
    items: [{xtype: 'generalinfopanel'}],
    listeners: {
        tabchange: 'onTabChange',
        added: {fn: 'onViewAdded', delay: 250},
        beforetabchange: 'onBeforeTabChange'
    }
});
