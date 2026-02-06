/*
 * File: app/view/EditMissionCriticalWindow.js
 *
 * This file allows users to edit the mission-critical category and justification.
 */

Ext.define('bcp.view.EditMissionCriticalWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.editmissioncriticalwindow',

    requires: [
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.form.field.TextArea',
        'Ext.button.Button'
    ],

    config: {
        store: null // Add a config option for the store
    },

    modal: true,
    height: 350,
    width: 600,
    layout: 'fit',
    closable: false,
    title: 'Edit Mission Critical Information',
    defaultListenerScope: true,

    items: [{
        xtype: 'form',
        itemId: 'mcEditForm',
        bodyPadding: 10,
        defaults: {
            labelWidth: 200,
            labelAlign: 'top',
            anchor: '100%'
        },
        items: [{
            xtype: 'combobox',
            fieldLabel: 'Mission Critical Category',
            itemId: 'mcCategoryCombo',
            forceSelection: true,
            allowOnlyWhitespace: false,
            allowBlank: false,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            name: 'missionCriticalCategoryId'
        }, {
            xtype: 'textareafield',
            fieldLabel: 'Mission Critical Justification',
            itemId: 'mcJustificationTextArea',
            allowOnlyWhitespace: false,
            allowBlank: false,
            minHeight: 120,
            maxLength: 2000,
            name: 'missionCriticalJustification'
        }],
        dockedItems: [{ // Use dockedItems instead of buttons
            xtype: 'container',
            dock: 'bottom',
            defaultButtonUI: 'default',
            layout: {
                type: 'hbox',
                pack: 'center' // Center the buttons horizontally
            },
            padding: '10 0 10 0', // Add some padding around the buttons
            items: [{
                xtype: 'button',
                text: 'Save',
                width: 80,
                itemId: 'saveButton',
                formBind: true,
                handler: 'onSaveButtonClick'
            }, {
                xtype: 'button',
                text: 'Cancel',
                width: 80,
                itemId: 'cancelButton',
                margin: '0 0 0 10',
                handler: 'onCancelButtonClick'
            }]
        }]
    }],
    listeners: {
        added: 'onWindowAdded' // listener to run when window is added
    },

    onSaveButtonClick: function (button) {
        var form = this.down('#mcEditForm').getForm();
        if (form.isValid()) {
            var values = form.getValues();
            this.fireEvent('save', values); // Fire the 'save' event
        }
    },

    onCancelButtonClick: function (button) {
        this.fireEvent('cancel'); // Fire the 'cancel' event
    },

    onWindowAdded: function (component, container, pos, eOpts) {
        this.down('#mcCategoryCombo').bindStore(this.store); // bind the store
    }
});