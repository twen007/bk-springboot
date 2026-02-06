/*
 * File: app/view/ChangeRequesterWindow.js
 *
 * change the requester of a request. The request's org info is based on
 * the requester's org info via DB trigger right now.
 *
 *
 */

Ext.define('bcp.view.ChangeRequesterWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.changerequester',

    requires: [
        'bcp.view.ChangeRequesterWindowViewModel',
        'bcp.view.ChangeRequesterWindowViewController',
        'Ext.form.Panel',
        'Ext.form.field.Hidden',
        'Ext.form.field.Display',
        'Ext.form.RadioGroup',
        'Ext.form.field.Radio',
        'Ext.XTemplate',
        'Ext.form.field.ComboBox',
        'Ext.button.Button'
    ],

    controller: 'changerequesterwindow',
    viewModel: {type: 'changerequesterwindow'},
    minWidth: 600,
    width: 600,
    closable: false,
    title: 'Change Requester',

    items: [
        {
            xtype: 'form',
            reference: 'form',
            anchor: '100%',
            flex: 1,
            scrollable: true,
            defaults: {
                labelWidth: 90,
                labelAlign: 'top',
                labelSeparator: '',
                submitEmptyText: false,
                anchor: '100%'
            },
            bodyPadding: 10,
            layout: {type: 'table', columns: 2},
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
                            margin: '0 10 0 0',
                            text: 'Save',
                            listeners: {click: 'onSave'}
                        },
                        {
                            xtype: 'button',
                            text: 'Cancel',
                            listeners: {click: 'onCancel'}
                        }
                    ]
                }
            ],
            items: [
                {
                    xtype: 'hiddenfield',
                    reference: 'requestIdHf',
                    fieldLabel: 'Label',
                    name: 'requestId',
                    bind: {value: '{record.requestId}'}
                },
                {xtype: 'hiddenfield', name: 'creatorId'},
                {
                    xtype: 'hiddenfield',
                    reference: 'requesterIdHf',
                    name: 'requesterId',
                    bind: {value: '{record.requesterId}'}
                },
                {xtype: 'hiddenfield', name: 'statusCode'},
                {xtype: 'hiddenfield', name: 'requesterName'},
                {
                    xtype: 'hiddenfield',
                    reference: 'reviewerIdHf',
                    name: 'reviewerId',
                    bind: {value: '{record.reviewerId}'}
                },
                {
                    xtype: 'hiddenfield',
                    reference: 'bankcardHolderIdHf',
                    name: 'bankcardHolderId',
                    bind: {value: '{record.bankcardHolderId}'}
                },
                {
                    xtype: 'hiddenfield',
                    reference: 'bankcardApprovingOfficialIdHf',
                    name: 'bankcardApprovingOfficialId',
                    bind: {value: '{record.bankcardApprovingOfficialId}'}
                },
                {
                    xtype: 'displayfield',
                    colspan: 2,
                    reference: 'rNum',
                    margin: 0,
                    fieldLabel: 'Requisition Number',
                    name: 'requisitionNumber',
                    value: 'Not Generated Yet',
                    bind: {hidden: '{record.requisitionNumber==""}'}
                },
                {
                    xtype: 'radiogroup',
                    colspan: 2,
                    reference: 'requesterRg',
                    margin: 0,
                    maxWidth: 400,
                    afterLabelTextTpl: [
                        '<span style="color:red">&nbsp;*</span>'
                    ],
                    fieldLabel:
                        'Who is the Official Requester (must be a Federal Employee)',
                    submitValue: false,
                    allowBlank: false,
                    layout: {type: 'vbox', align: 'stretch'},
                    items: [
                        {
                            xtype: 'radiofield',
                            reference: 'selfRb',
                            margin: 0,
                            name: 'whoVal',
                            inputValue: 'self',
                            bind: {
                                boxLabel:
                                    '{loggedInUser.lastName +", " +loggedInUser.firstName}'
                            }
                        },
                        {
                            xtype: 'radiofield',
                            reference: 'notmeRb',
                            margin: 0,
                            name: 'whoVal',
                            boxLabel:
                                'Other NIST Federal Employee <br>(drop down appeared if selected)',
                            inputValue: 'notme'
                        },
                        {
                            xtype: 'combobox',
                            flex: 1,
                            reference: 'onBehalfCombo',
                            maxWidth: 400,
                            name: 'requesterId',
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
                            valueField: 'peopleId',
                            bind: {
                                disabled: '{forMe}',
                                hidden: '{forMe}',
                                store: '{bcEmployees}'
                            },
                            listeners: {change: 'onReqForComboChange'}
                        }
                    ],
                    listeners: {change: 'onReqForChange'}
                },
                {
                    xtype: 'combobox',
                    colspan: 2,
                    hidden: true,
                    maxWidth: 400,
                    minHeight: 20,
                    fieldLabel:
                        'Will the purchase received by a NIST Associate? <br> (If yes,  use the drop down below to select a  NIST Associate)',
                    name: 'requestedForId',
                    submitValue: false,
                    anyMatch: true,
                    displayField: 'displayName',
                    forceSelection: true,
                    minChars: 2,
                    queryMode: 'local',
                    queryParam: 'filter',
                    //typeAhead: true,
                    valueField: 'peopleId',
                    bind: {store: '{bcAssociates}'}
                }
                //{
                //    xtype: 'container',
                //    layout: 'hbox',
                //    margin: '20 0 0 0',
            ]
        }
    ],
    listeners: {added: 'onWindowAdded'}
});
