/*
 * File: app/view/windows/RouteBack.js
 *
 * It allow a AO, SMA, BAO or BCH to route back a request from current stage to a previous stage to be processed/re-approved again.
 * This function is created to preserve route history and it replaces the pullback function, which destroys previous route. BANK-494
 */

Ext.define('bcp.view.windows.RouteBack', {
    extend: 'Ext.window.Window',
    alias: 'widget.windows.routeback',

    requires: [
        'bcp.view.windows.RouteBackViewModel',
        'bcp.view.windows.RouteBackViewController',
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Radio',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    controller: 'windows.routeback',
    viewModel: {type: 'windows.routeback'},
    height: 300,
    width: 550,
    layout: 'fit',
    title: 'Route Back',

    items: [
        {
            xtype: 'form',
            reference: 'form',
            bodyPadding: 10,
            items: [
                {
                    xtype: 'radiogroup',
                    fieldLabel:
                        'Please select an approver to route this request back to',
                    reference: 'rgApprovers',
                    labelAlign: 'top',
                    allowBlank: false,
                    simpleValue: true,
                    blankText: 'Please select an approver',
                    layout: {type: 'vbox', align: 'stretch'},
                    items: [
                        {
                            xtype: 'radiofield',
                            reference: 'rbReviewer',
                            name: 'routeTo'
                        },
                        {
                            xtype: 'radiofield',
                            reference: 'rbFco',
                            name: 'routeTo'
                        },
                        {
                            xtype: 'radiofield',
                            reference: 'rbBao',
                            name: 'routeTo'
                        },
                        {
                            xtype: 'radiofield',
                            reference: 'rbBch',
                            name: 'routeTo'
                        }
                    ]
                },
                {
                    xtype: 'toolbar',
                    defaultButtonUI: 'default',
                    layout: {type: 'hbox', pack: 'center'},
                    items: [
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            listeners: {click: 'onCancel'}
                        },
                        {
                            xtype: 'button',
                            formBind: true,
                            text: 'Route',
                            listeners: {click: 'onRoute'}
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {beforeshow: 'onWindowBeforeShow', added: 'onWindowAdded'}
});
