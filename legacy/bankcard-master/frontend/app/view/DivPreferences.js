/*
 * Author: xinweiw
 * Purpose: Allow AOs to set the division level preferences.
 */

Ext.define('bcp.view.DivPreferences', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.divpreferences',

    requires: [
        'bcp.view.DivPreferencesViewModel',
        'bcp.view.DivPreferencesViewController',
        'Ext.toolbar.Toolbar',
        'Ext.form.Label',
        'Ext.toolbar.Fill',
        'Ext.button.Button',
        'Ext.form.field.ComboBox',
        'Ext.grid.Panel',
        'Ext.resizer.Splitter',
        'Ext.tab.Panel',
        'Ext.layout.container.Accordion'
    ],

    controller: 'divpreferences',
    viewModel: {type: 'divpreferences'},
    layout: {type: 'accordion', multi: true},
    //layout: 'fit',
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
                    text: 'Division Preferences'
                },
                {
                    xtype: 'combobox',
                    reference: 'comboDiv',
                    //fieldLabel: 'Division',
                    margin: '0 0 5 20',
                    width: 300,
                    allowBlank: false,
                    //name: 'divId',
                    emptyText: 'Select a Division',
                    displayField: 'shortName',
                    forceSelection: true,
                    queryMode: 'local',
                    valueField: 'divisionId',
                    bind: {store: '{divisions}'},
                    listeners: {change: 'onDivChange'}
                }
            ]
        }
    ],
    items: [
        {
            xtype: 'form',
            header: {
                baseCls: 'x-panel-header',
                style: 'background-color:	#57a0cb5e;',
                title: 'Request & Approval'
            },
            reference: 'form',
            minHeight: 500,
            scrollable: true,
            dockedItems: [
                {
                    xtype: 'toolbar',
                    reference: 'filterTb1',
                    defaultButtonUI: 'default',
                    items: [
                        {
                            xtype: 'button',
                            margin: '0 0 0 15',
                            iconCls: 'fas fa-save',
                            text: 'Save Request & Approval Preference',
                            formBind: true,
                            listeners: {click: 'onSave'}
                        }
                    ]
                }
            ],
            items: [
                {xtype: 'hiddenfield', name: 'divId'},
                {
                    xtype: 'fieldset',
                    title: 'Justification Preference',
                    layout: {type: 'hbox', align: 'stretch', padding: ''},
                    items: [
                        {
                            xtype: 'radiogroup',
                            flex: 1,
                            fieldLabel:
                                'Do initiators/requesters need to provide Justification before Submission?',
                            labelAlign: 'top',
                            labelWidth: 400,
                            allowBlank: false,
                            simpleValue: true,
                            layout: {type: 'vbox', align: 'stretch'},
                            items: [
                                {
                                    xtype: 'radiofield',
                                    flex: 1,
                                    margin: '0 0 5 20',
                                    name: 'justPrefVal',
                                    boxLabel:
                                        'No, it is optional. Initiators/Requesters provide what they can before submission. Approvers will help to complete the rest.',
                                    //checked: true,
                                    inputValue: 'O' //Optional
                                },
                                {
                                    xtype: 'radiofield',
                                    flex: 1,
                                    margin: '0 0 5 20',
                                    name: 'justPrefVal',
                                    boxLabel:
                                        'No. Do not show the Justification form to the Initiators/Requesters. Approvers will provide the justification.',
                                    inputValue: 'N' //Not showing at all
                                },
                                {
                                    xtype: 'radiofield',
                                    margin: '0 0 5 20',
                                    name: 'justPrefVal',
                                    boxLabel:
                                        'Yes, it is required. Initiators/Requesters need to complete the Justification form before submission.',
                                    inputValue: 'Y' //Yes, require it
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'Finance Preference',
                    layout: {type: 'hbox', align: 'stretch', padding: ''},
                    items: [
                        {
                            xtype: 'radiogroup',
                            flex: 1,
                            fieldLabel:
                                'Do initiators/requesters need to provide Project Task codes and Object Classes before Submission?',
                            labelAlign: 'top',
                            labelWidth: 400,
                            allowBlank: false,
                            simpleValue: true,
                            layout: {type: 'vbox', align: 'stretch'},
                            items: [
                                {
                                    xtype: 'radiofield',
                                    flex: 1,
                                    margin: '0 0 5 20',
                                    name: 'financePrefVal',
                                    boxLabel:
                                        'No, it is optional. Initiators/Requesters provide what they can before submission. Approvers will help to complete the rest.',
                                    //checked: true,
                                    inputValue: 'O'
                                },
                                {
                                    xtype: 'radiofield',
                                    flex: 1,
                                    margin: '0 0 5 20',
                                    name: 'financePrefVal',
                                    boxLabel:
                                        'No. Do not show the Finance Data to the initiators/requesters. Approvers will provide them.',
                                    inputValue: 'N'
                                },
                                {
                                    xtype: 'radiofield',
                                    margin: '0 0 5 20',
                                    name: 'financePrefVal',
                                    boxLabel:
                                        'Yes, it is required. Initiators/Requesters need to complete the Finance data before submission.',
                                    inputValue: 'Y'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'Shipping & Handling Cost Preference',
                    layout: {type: 'hbox', align: 'stretch', padding: ''},
                    items: [
                        {
                            xtype: 'radiogroup',
                            flex: 1,
                            fieldLabel: 'Make the application to',
                            labelAlign: 'top',
                            labelWidth: 400,
                            allowBlank: false,
                            simpleValue: true,
                            layout: {type: 'vbox', align: 'stretch'},
                            items: [
                                {
                                    xtype: 'radiofield',
                                    flex: 1,
                                    margin: '0 0 5 20',
                                    name: 'shippingCostPrefVal',
                                    boxLabel:
                                        'remind the initiators/requesters if they did not add the Shipping & Handling line item before submission.',
                                    inputValue: 'R'
                                },
                                {
                                    xtype: 'container',
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'radiofield',
                                            margin: '0 0 5 20',
                                            boxLabel:
                                                'automatically add a Shipping & Handling line item with a specified amount ($0 ~ $200). $: ',
                                            name: 'shippingCostPrefVal',
                                            inputValue: 'A'
                                        },
                                        {
                                            xtype: 'numberfield',
                                            margin: '0 0 5 10',
                                            minValue: 0,
                                            maxValue: 200,
                                            minWidth: 100,
                                            value: 0,
                                            width: 80,
                                            name: 'shippingCostPrefValDetail'
                                        }
                                    ]
                                },
                                {
                                    xtype: 'displayfield',
                                    margin: '-10 0 5 45',
                                    value: '<b>Initiators/Requesters can change it if needed. (Note: Shipping cost is included in the solvency.)</b>'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'Up to Amount Preference',
                    layout: {type: 'hbox', align: 'stretch', padding: ''},
                    items: [
                        {
                            xtype: 'radiogroup',
                            flex: 1,
                            //height: 130,
                            fieldLabel:
                                'To avoid re-approvals due to actual total cost exceeds the approved amount, the Bankcard Approving Officials (BAO) need to provide a [up to amount] that is more than the estimated total cost. The application can automatically add a specified amount to the estimated total cost as the [up to amount] for each request.',
                            labelAlign: 'top',
                            labelWidth: 400,
                            allowBlank: false,
                            simpleValue: true,
                            layout: {type: 'vbox', align: 'stretch'},
                            items: [
                                {
                                    xtype: 'radiofield',
                                    margin: '0 0 5 20',
                                    name: 'upToPrefVal',
                                    boxLabel:
                                        'No thanks. The BAOs will determine and enter the up to amount for each request.',
                                    inputValue: 'N'
                                },
                                {
                                    xtype: 'container',
                                    layout: 'hbox',
                                    items: [
                                        {
                                            xtype: 'radiofield',
                                            margin: '0 0 5 20',
                                            boxLabel:
                                                'Yes, please automatically add this amount ($0 ~ $200)  on top of the estimated total. $: ',
                                            name: 'upToPrefVal',
                                            inputValue: 'Y'
                                        },
                                        {
                                            xtype: 'numberfield',
                                            margin: '0 0 5 10',
                                            minValue: 0,
                                            minWidth: 100,
                                            maxValue: 200,
                                            value: 0,
                                            width: 80,
                                            name: 'upToPrefValDetail'
                                        }
                                    ]
                                },
                                {
                                    xtype: 'displayfield',
                                    margin: '-10 0 5 45',
                                    value: '<b>BAOs can change the [up to amount] if needed. (Note: up to amount is not included in the solvency.)</b>'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    title: 'Route Preference',
                    layout: {type: 'hbox', align: 'stretch', padding: ''},
                    items: [
                        {
                            xtype: 'radiogroup',
                            flex: 1,
                            //height: 130,
                            fieldLabel:
                                'To require a separate Funds Certifying official (FCO) approval after the Bankcard Approving Officials (BAO) approved a request. This is for divisions whose [Funds Certifying official] and [Bankcard Approving Official] are different person',
                            labelAlign: 'top',
                            labelWidth: 400,
                            allowBlank: false,
                            simpleValue: true,
                            layout: {type: 'vbox', align: 'stretch'},
                            items: [
                                {
                                    xtype: 'radiofield',
                                    margin: '0 0 5 20',
                                    name: 'addFcoRoutePrefVal',
                                    boxLabel:
                                        'No thanks. The FCO and BAO is the same person so there is no need to add a seperate route.',
                                    inputValue: 'N'
                                },
                                {
                                    xtype: 'radiofield',
                                    margin: '0 0 5 20',
                                    name: 'addFcoRoutePrefVal',
                                    boxLabel:
                                        'Yes, please automatically route requests to the FCO for approval after the [BAO] approved them.',
                                    inputValue: 'Y'
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            xtype: 'panel',
            header: {
                baseCls: 'x-panel-header',
                style: 'background-color:	#57a0cb5e;',
                title: 'Bankcard Holder Initials <i class="fa fa-info-circle fa-lg" aria-hidden="true" data-qtip="A bankcard holder initial (2 characters; by default using first character of first name and last name) can be included in the requisition number. AOs can set it up in the NAP."></i>&nbsp;&nbsp;<b>(<a href="Javascript: window.open(\'https://emp.nist.gov/nap/portal/#divreqnum\')";>See Set Up Division Requisition Number Preferences in NAP</a>)</b>'
            },
            reference: 'initpanel',
            scrollable: true,
            height: 275,
            dockedItems: [
                {
                    xtype: 'toolbar',
                    reference: 'filterTb2',
                    defaultButtonUI: 'default',
                    items: [
                        {
                            xtype: 'label',
                            //cls: 'x-panel-header-title-default',
                            padding: '5 15 5 15',
                            text: 'Custom Initials (Double Click to Edit)'
                        },
                        {
                            xtype: 'button',
                            text: 'Add',
                            iconCls: 'fas fa-plus',
                            listeners: {click: 'add'}
                        },
                        {
                            xtype: 'button',
                            margin: '0 5 0 5',
                            text: 'Remove',
                            iconCls: 'fas fa-trash',
                            bind: {
                                disabled: '{!list.selection}',
                                hidden: '{!record}'
                            },
                            listeners: {click: 'remove'}
                        }
                    ]
                }
            ],
            items: [
                {
                    xtype: 'gridpanel',
                    reference: 'list',
                    title: '',
                    maxWidth: 600,
                    minHeight: 250,
                    bind: {store: '{bchInitials}'},
                    columns: [
                        {
                            xtype: 'numbercolumn',
                            hidden: true,
                            dataIndex: 'id',
                            text: 'Id',
                            format: '00'
                        },
                        {
                            xtype: 'gridcolumn',
                            renderer: function (
                                value,
                                metaData,
                                record,
                                rowIndex,
                                colIndex,
                                store,
                                view
                            ) {
                                var rec = Ext.getStore('Divisions').findRecord(
                                    'divisionId',
                                    value
                                );
                                return rec ? rec.get('code') : '';
                            },
                            minWidth: 120,
                            dataIndex: 'divId',
                            text: 'Division',
                            editor: {
                                xtype: 'combobox',
                                reference: 'comboBchDiv',
                                allowBlank: false,
                                allowOnlyWhitespace: false,
                                displayField: 'code',
                                forceSelection: true,
                                queryMode: 'local',
                                bind: {
                                    store: '{divisions}',
                                    disabled: '{isUpdate}'
                                },
                                valueField: 'divisionId'
                            }
                        },
                        {
                            xtype: 'gridcolumn',
                            renderer: function (
                                value,
                                metaData,
                                record,
                                rowIndex,
                                colIndex,
                                store,
                                view
                            ) {
                                var rec =
                                    Ext.getStore('BankcardHolders').getById(
                                        value
                                    );
                                if (rec) {
                                    return rec.get('fullName');
                                } else {
                                    return 'New Record';
                                }
                            },
                            minWidth: 200,
                            dataIndex: 'peopleId',
                            text: 'Bankcard Holder',
                            editor: {
                                xtype: 'combobox',
                                allowBlank: false,
                                allowOnlyWhitespace: false,
                                displayField: 'fullName',
                                forceSelection: true,
                                queryMode: 'local',
                                valueField: 'peopleId',
                                store: 'BankcardHolders',
                                bind: {disabled: '{isUpdate}'}
                            }
                        },
                        {
                            xtype: 'gridcolumn',
                            minWidth: 80,

                            dataIndex: 'initials',
                            text: 'Initials',
                            editor: {
                                xtype: 'textfield',
                                enforceMaxLength: true,
                                allowBlank: false,
                                allowOnlyWhitespace: false,
                                maxLength: 2,
                                regex: /^[a-zA-Z0-9]*$/
                            }
                        }
                    ],
                    plugins: [
                        {
                            ptype: 'rowediting',
                            pluginId: 'rowEditPlugin',
                            listeners: {
                                canceledit: 'onRowEditingCanceledit',
                                edit: 'onRowEditingEdit',
                                beforeedit: 'onRowEditingBeforeEdit'
                            }
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {added: 'onViewAdded'}
});
