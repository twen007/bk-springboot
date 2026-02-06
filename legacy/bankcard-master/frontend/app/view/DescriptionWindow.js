/*
 * File: app/view/DescriptionWindow.js
 *
 * Author: ppg
 * Create Date: 01/2021
 * Objective: Allow BCH to add/edit description for requests
 */

Ext.define('bcp.view.DescriptionWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.descriptionwindow',

    requires: [
        'Ext.form.Panel',
        'Ext.form.field.TextArea',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    config: {store: 'store'},

    modal: true,
    height: 214,
    width: 400,
    defaultFocus: 'noButton',
    layout: 'card',
    closable: false,
    defaultButton: 'noBtn',
    title: 'Save Description',
    defaultListenerScope: true,

    items: [
        {
            xtype: 'form',
            flex: 1,
            bodyPadding: 10,
            items: [
                {
                    xtype: 'textareafield',
                    id: 'description',
                    anchor: '100%',
                    reference: 'description',
                    fieldLabel: 'Description',
                    labelAlign: 'top',
                    maxLength: 2000,
                    allowBlank: false,
                    allowOnlyWhitespace: false
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    defaultButtonUI: 'default',
                    layout: {type: 'hbox', pack: 'center'},
                    items: [
                        {
                            xtype: 'button',
                            reference: 'noBtn',
                            itemId: 'noButton',
                            width: 80,
                            text: 'Cancel',
                            listeners: {click: 'onNoButtonClick'}
                        },
                        {
                            xtype: 'button',
                            formBind: true,
                            width: 80,
                            text: 'Save',
                            listeners: {click: 'onSaveClick'}
                        }
                    ]
                }
            ]
        }
    ],

    onNoButtonClick: function (button, e, eOpts) {
        this.fireEvent('cancelDescription');
    },

    onSaveClick: function (button, e, eOpts) {
        this.fireEvent('saveDescription', this.query('textareafield')[0].value);
    }
});
