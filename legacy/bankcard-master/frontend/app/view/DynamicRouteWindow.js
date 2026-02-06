/*
popup approval window for dynamic routing & reroute
 */

Ext.define('bcp.view.DynamicRouteWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.dynamicroutewindow',

    requires: [
        'bcp.view.DynamicRouteWindowViewModel',
        'bcp.view.DynamicRouteWindowViewController',
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Radio',
        'Ext.form.field.Hidden',
        'Ext.form.field.Display',
        'Ext.form.field.ComboBox',
        'Ext.form.field.TextArea',
        'Ext.form.field.Checkbox',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    config: {store: {}},

    controller: 'dynamicroutewindow',
    viewModel: {type: 'dynamicroutewindow'},
    modal: true,
    constrain: true,
    width: 560,
    title: 'Dynamic Routing Confirmation',

    layout: {type: 'vbox', align: 'stretch'},
    listeners: {added: 'onWindowAdded', beforeshow: 'onWindowBeforeShow'},
    items: [
        {
            xtype: 'form',
            reference: 'form',
            flex: 1,
            bodyPadding: 10,
            items: [
                {
                    xtype: 'hiddenfield',
                    anchor: '100%',
                    fieldLabel: 'Label',
                    name: 'requestId'
                },
                {
                    xtype: 'hiddenfield',
                    anchor: '100%',
                    fieldLabel: 'Label',
                    name: 'statusId'
                },
                {xtype: 'hiddenfield', anchor: '100%', name: 'typeId'},
                {xtype: 'hiddenfield', anchor: '100%', name: 'routeStep'},
                {xtype: 'hiddenfield', anchor: '100%', name: 'dynamicType'},
                {xtype: 'displayfield', anchor: '100%', reference: 'routeMsg'},
                {
                    xtype: 'combobox',
                    anchor: '100%',
                    fieldLabel: 'Route To',
                    reference: 'comboAddiRouteEmp',
                    maxWidth: 400,
                    name: 'routeTo',
                    allowBlank: false,
                    allowOnlyWhitespace: false,
                    emptyText: 'type staff name here',
                    anyMatch: true,
                    displayField: 'displayName',
                    forceSelection: true,
                    minChars: 2,
                    queryMode: 'local',
                    queryParam: 'filter',
                    //typeAhead: true,
                    valueField: 'peopleId'
                    /*bind: {
                    store: '{bcEmployees}'
                }*/
                    /*,
                listeners: {
                    change: 'onComboAddiRouteEmpChange'
                }*/
                },
                {
                    xtype: 'textareafield',
                    anchor: '100%',
                    reference: 'notes2',
                    fieldLabel: 'Comments',
                    maxLength: 1000,
                    name: 'notes'
                },
                {
                    xtype: 'checkboxfield',
                    anchor: '100%',
                    reference: 'certifyCb',
                    allowBlank: false,
                    boxLabel:
                        'I certify that sufficient funds are available in the identified appropriation and suitable for the intended purpose.',
                    inputValue: 'true',
                    uncheckedValue: 'false',
                    beforeBoxLabelTextTpl: [
                        '<span style="color:red">&nbsp;*&nbsp;</span>'
                    ],
                    listeners: {change: 'onApproveCheckboxChange'}
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    height: 48,
                    defaultButtonUI: 'default',
                    layout: {type: 'hbox', pack: 'center'},
                    items: [
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            listeners: {click: 'cancel'}
                        },
                        {
                            xtype: 'button',
                            reference: 'approveBtn1',
                            formBind: true,
                            text: 'Approve',
                            listeners: {click: 'proceed'}
                        }
                    ]
                }
            ]
        }
    ]
});
