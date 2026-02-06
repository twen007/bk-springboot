/*
 * File: app/view/Preferences.js
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences.
 */

Ext.define('bcp.view.Preferences', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.preferences',

    requires: [
        'bcp.view.PreferencesViewModel',
        'bcp.view.PreferencesViewController',
        'Ext.toolbar.Toolbar',
        'Ext.form.Label',
        'Ext.toolbar.Fill',
        'Ext.button.Button',
        'Ext.form.field.ComboBox',
        'Ext.grid.Panel',
        'Ext.resizer.Splitter',
        'Ext.tab.Panel'
    ],

    controller: 'preferences',
    viewModel: {type: 'preferences'},
    layout: 'fit',

    dockedItems: [
        {
            xtype: 'toolbar',
            baseCls: 'x-panel-header',
            dock: 'top',
            height: 44,
            style: 'background-color:	#184ed1;',
            defaultButtonUI: 'default',
            enableOverflow: true,
            overflowHandler: 'menu',
            items: [
                {
                    xtype: 'label',
                    cls: 'x-panel-header-title-default',
                    padding: '5 15 5 15',
                    text: 'User Preferences'
                }
            ]
        }
    ],
    items: [
        {
            xtype: 'form',
            reference: 'list',
            items: [
                {
                    xtype: 'fieldset',
                    title: 'What weekday would you like to receive reminder emails about pending requests that need your approval',
                    reference: 'fsEmailNotification',
                    layout: {type: 'hbox', align: 'stretch'},
                    defaultButtonUI: 'default',
                    items: [
                        {
                            xtype: 'combobox',
                            id: 'cbWeekdayToSendEmails',
                            padding: '0 10 0 0',
                            emptyText: 'Select a weekday',
                            forceSelection: true,
                            queryMode: 'local',
                            valueField: 'id',
                            bind: {store: '{weekdays}'}
                        },
                        {
                            xtype: 'button',
                            iconCls: 'fas fa-save',
                            text: 'Save',
                            listeners: {click: 'onSave'}
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'How often would you like to receive notification email about requests that have purchase total less than the CBS total?',
                    reference: 'fsEmailNotification2',
                    layout: {type: 'hbox', align: 'stretch'},
                    defaultButtonUI: 'default',
                    items: [
                        {
                            xtype: 'combobox',
                            id: 'cbnegativediff',
                            padding: '0 10 0 0',
                            emptyText: 'Select a frequency',
                            forceSelection: true,
                            queryMode: 'local',
                            valueField: 'text',
                            bind: {store: '{frequencies}'}
                        },
                        {
                            xtype: 'button',
                            iconCls: 'fas fa-save',
                            text: 'Save',
                            listeners: {click: 'onSave2'}
                        },
                        {}
                    ]
                }
            ]
        }
    ],
    listeners: {added: 'onLoadPrefs'}
});
