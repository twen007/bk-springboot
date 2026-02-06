/*
 * File: app/view/ChangeRouteWindow.js
 *
 * allow a user to select someone with the role (reviewers, baos, or bhs)
 * and update the request with that person
 */

Ext.define('bcp.view.ChangeRouteWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.changeroutewindow',

    requires: [
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    config: {store: {}},

    modal: true,
    height: 214,
    width: 400,
    defaultFocus: 'noButton',
    layout: 'card',
    closable: false,
    defaultButton: 'noBtn',
    title: 'Change Route',
    defaultListenerScope: true,

    items: [
        {
            xtype: 'form',
            flex: 1,
            bodyPadding: 10,
            items: [
                {
                    xtype: 'combobox',
                    anchor: '100%',
                    reference: 'comboApprovers',
                    fieldLabel: 'Select an Employee',
                    labelAlign: 'top',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    anyMatch: true,
                    displayField: 'displayName',
                    forceSelection: true,
                    minChars: 2,
                    queryMode: 'local',
                    queryParam: 'filter',
                    store: 'Reviewers',
                    valueField: 'peopleId'
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
                            text: 'Apply',
                            listeners: {click: 'onApplyButtonClick'}
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {added: 'onWindowAdded'},

    onNoButtonClick: function (button, e, eOpts) {
        this.fireEvent('cancelChange');
    },

    onApplyButtonClick: function (button, e, eOpts) {
        this.fireEvent('applyChange', this.query('combobox')[0].selection.data);
    },

    onWindowAdded: function (component, container, pos, eOpts) {
        this.query('combobox')[0].bindStore(this.store);
    }
});
