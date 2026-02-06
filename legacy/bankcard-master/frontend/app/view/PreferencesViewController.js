/*
 * File: app/view/PreferencesViewController.js
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences.
 */

Ext.define('bcp.view.PreferencesViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.preferences',

    onLoadPrefs: function (component, container, pos, eOpts) {
        Ext.Ajax.request({
            url: bcp.config.Runtime.getServerBaseUrl() + 'users/getprefs',
            method: 'GET',
            scope: this,
            success: function (response, opts) {
                var prefValue = response.responseText;
                if (prefValue.length > 5)
                    Ext.getCmp('cbWeekdayToSendEmails').setRawValue(prefValue);
            },
            failure: function (err) {
                Ext.MessageBox.alert(
                    'Failure',
                    'Unexpected error occurred, the preference is not loaded.'
                );
            }
        });

        if (
            bcp.util.CommonUtil.isUserInRole([
                'Bankcard Holder',
                'Administrative Officer'
                //"Bankcard Approving Official",
            ])
        ) {
            Ext.Ajax.request({
                url:
                    bcp.config.Runtime.getServerBaseUrl() +
                    'users/getNapCbsDiffPref',
                method: 'GET',
                scope: this,
                success: function (response, opts) {
                    var prefValue = response.responseText;
                    if (prefValue.length >= 5 && prefValue.length <= 10) {
                        Ext.getCmp('cbnegativediff').setRawValue(prefValue);
                    } else {
                        Ext.getCmp('cbnegativediff').setRawValue('never');
                    }
                },
                failure: function (err) {
                    Ext.MessageBox.alert(
                        'Failure',
                        'Unexpected error occurred, the preference is not loaded.'
                    );
                }
            });
        } else {
            this.lookupReference('fsEmailNotification2').hide();
        }
    },

    onSave: function (button, e, eOpts) {
        var prefValue = Ext.getCmp('cbWeekdayToSendEmails').getRawValue();
        if (prefValue == null || prefValue.length < 6) return;

        Ext.Ajax.request({
            url:
                bcp.config.Runtime.getServerBaseUrl() +
                'users/setprefs/' +
                prefValue,
            method: 'POST',
            scope: this,
            params: {prefValue: prefValue},
            success: function (response, opts) {
                Ext.Msg.alert('Success', 'Reminder Email Preference saved.');
            },
            failure: function (err) {
                Ext.MessageBox.alert(
                    'Failure',
                    'Unexpected error occurred, the preference is not saved.'
                );
            }
        });
    },

    onSave2: function (button, e, eOpts) {
        var prefValue = Ext.getCmp('cbnegativediff').getRawValue();
        if (prefValue == null) {
            return;
        }

        Ext.Ajax.request({
            url:
                bcp.config.Runtime.getServerBaseUrl() +
                'users/setNapCbsDiffPref/' +
                prefValue,
            method: 'POST',
            scope: this,
            params: {prefValue: prefValue},
            success: function (response, opts) {
                Ext.Msg.alert(
                    'Success',
                    'Bankcard total less than CBS total Notification Preference saved.'
                );
            },
            failure: function (err) {
                Ext.MessageBox.alert(
                    'Failure',
                    'Unexpected error occurred, the preference is not saved.'
                );
            }
        });
    }
});
