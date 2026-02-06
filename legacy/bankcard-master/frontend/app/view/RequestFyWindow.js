Ext.define('bcp.view.RequestFyWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.requestfywindow',

    requires: [
        'Ext.form.Panel',
        'Ext.form.field.ComboBox',
        'Ext.toolbar.Toolbar',
        'Ext.button.Button'
    ],

    modal: true,
    height: 150,
    width: 300,
    defaultFocus: 'noButton',
    layout: 'card',
    closable: false,
    defaultButton: 'noBtn',
    title: 'Change FY',
    defaultListenerScope: true,

    items: [
        {
            xtype: 'form',
            flex: 1,
            bodyPadding: 10,
            items: [
                {
                    xtype: 'combobox',
                    fieldLabel: 'FY',
                    width: 200,
                    forceSelection: true,
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    queryMode: 'local',
                    displayField: 'fy',
                    valueField: 'fy',
                    name: 'fy'
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

    onWindowAdded: function (component, container, pos, eOpts) {
        var currentYear = new Date().getFullYear(), //yr in 4 digits
            currentFy = Number(currentYear.toString().substr(-2)), //yr in 2 digits
            createNextFyEndDate = new Date(
                new Date().getFullYear() + '-10-01T00:00:00-04:00'
            ),
            fys = Ext.create('Ext.data.ChainedStore', {
                storeId: 'fys',
                source: 'Fys'
            });

        if (new Date() >= createNextFyEndDate) {
            currentFy++;
        }

        fys.addFilter([{operator: '>=', property: 'fy', value: currentFy - 1}]);
        this.query('combobox')[0].store = fys;
    },

    onNoButtonClick: function (button, e, eOpts) {
        this.fireEvent('cancelFyChange');
    },

    onApplyButtonClick: function (button, e, eOpts) {
        var val = this.query('combobox')[0].value;
        this.fireEvent('applyFyChange', val);
    }
});
