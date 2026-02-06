/*
 * File: app/view/ItemCreditWindow.js
 *
 * Author: ppg
 * Create Date: 01/2021
 * Objective: Allow BCH to add/edit credit for purchases
 */

Ext.define('bcp.view.ItemCreditWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.itemcreditwindow',

    requires: [
        'Ext.form.Panel',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button',
        'Ext.form.field.Number',
        'Ext.form.field.Hidden'
    ],

    modal: true,
    width: 336,
    defaultFocus: 'noButton',
    closable: false,
    defaultButton: 'noBtn',
    title: 'Add/Edit Credit for the Purchase',
    defaultListenerScope: true,

    items: [
        {
            xtype: 'form',
            flex: 1,
            defaultFocus: '#unitPrice',
            bodyPadding: 10,
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    defaultButtonUI: 'default',
                    layout: {type: 'hbox', pack: 'center'},
                    items: [
                        {
                            xtype: 'button',
                            reference: 'cBtn',
                            itemId: 'noButton',
                            width: 80,
                            text: 'Cancel',
                            listeners: {click: 'onCBtnClick'}
                        },
                        {
                            xtype: 'button',
                            formBind: true,
                            width: 80,
                            text: 'Save',
                            listeners: {click: 'onApplyBtnClick'}
                        }
                    ]
                }
            ],
            items: [
                {
                    xtype: 'textfield',
                    anchor: '100%',
                    fieldLabel: 'Item Name',
                    labelAlign: 'top',
                    name: 'itemName',
                    submitValue: false,
                    value: 'Credit',
                    readOnly: true,
                    allowBlank: false,
                    allowOnlyWhitespace: false
                },
                {
                    xtype: 'numberfield',
                    anchor: '100%',
                    itemId: 'unitPrice',
                    width: '',
                    fieldLabel: 'Credit Amount',
                    labelAlign: 'top',
                    name: 'unitPrice',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    allowExponential: false,
                    minValue: 0.01,
                    bind: {maxValue: '{purchaseLimit}'}
                },
                {
                    xtype: 'hiddenfield',
                    anchor: '100%',
                    itemId: 'itemId',
                    fieldLabel: 'Label',
                    name: 'itemId'
                },
                {
                    xtype: 'hiddenfield',
                    anchor: '100%',
                    itemId: 'projTask',
                    fieldLabel: 'Label',
                    name: 'projTask'
                }
            ]
        }
    ],

    onCBtnClick: function (button, e, eOpts) {
        this.fireEvent('cancelC');
    },

    onApplyBtnClick: function (button, e, eOpts) {
        this.fireEvent('saveC', this.query('form')[0].getValues());
    }
});
