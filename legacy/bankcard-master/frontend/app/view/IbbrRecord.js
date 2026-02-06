Ext.define('bcp.view.IbbrRecord', {
    extend: 'Ext.window.Window',
    alias: 'widget.ibbrrecord',

    requires: [
        'bcp.view.IbbrRecordViewModel',
        'bcp.view.IbbrRecordViewController',
        'Ext.form.Panel',
        'Ext.form.field.Display',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    controller: 'ibbrrecord',
    viewModel: {type: 'ibbrrecord'},
    modal: true,
    reference: 'details',
    height: 500,
    itemId: 'detailPanel',
    minWidth: 600,
    layout: 'fit',
    bodyPadding: 10,
    title: 'IBBR Chemical Item',

    items: [
        {
            xtype: 'form',
            defaults: {
                width: 400,
                labelWidth: 200,
                labelStyle: 'font-weight:bold;'
            },
            bodyPadding: 10,
            layout: {type: 'table', columns: 2},
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Item Name',
                    bind: {value: '{ibbrRec.name}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Cost',
                    bind: {value: '{ibbrRec.cost}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Supplier',
                    bind: {value: '{ibbrRec.supplier}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Catalog',
                    bind: {value: '{ibbrRec.catalog}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Quantity',
                    bind: {value: '{ibbrRec.quantity}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Amount and Unit Per Container',
                    bind: {value: '{ibbrRec.amount}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Owner First Name',
                    bind: {value: '{ibbrRec.owner_given}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Owner Last Name',
                    bind: {value: '{ibbrRec.owner_sn}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Email',
                    bind: {value: '{ibbrRec.owner_email}'}
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Room',
                    bind: {value: '{ibbrRec.room}'}
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    layout: {type: 'hbox', align: 'bottom'},
                    items: [
                        {xtype: 'button', handler: 'onOK', text: 'OK'},
                        {
                            xtype: 'button',
                            itemId: 'ibbrResubmit',
                            text: 'Resubmit',
                            listeners: {click: 'onIbbrResubmitClick'}
                        }
                    ]
                }
            ]
        }
    ]
});
