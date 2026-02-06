Ext.define('bcp.view.windows.BcpAlert', {
    extend: 'Ext.window.Window',
    xtype: 'widget.windows.bcpalert',
    config: {
        title: 'Default Title',
        message: 'Default Message'
    },
    initComponent: function() {
        var me = this;
        // Create the alert window
        Ext.apply(me, {
            modal: true,
            layout: 'fit',
            width: 400,
            height: 200,
            items: [{
                xtype: 'panel',
                html: me.getMessage() // Display the message
            }],
            buttons: [{
                text: 'OK',
                id: 'bcpAlertOkBtn',
                handler: function() {
                    me.close(); // Close the alert window
                }
            }]
        });
        me.callParent(arguments);
    },
    // Method to show the alert
    showAlert: function(title, message) {
        this.setTitle(title);
        this.setMessage(message);
        this.show(); // Show the alert window
    }
});