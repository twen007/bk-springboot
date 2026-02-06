/*
 * File: app/view/PreferencesViewModel.js
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences.
 */

Ext.define('bcp.view.PreferencesViewModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.preferences',

    requires: [
        'Ext.data.ChainedStore',
        'Ext.data.Store',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.app.bind.Formula'
    ],

    data: {viewState: 'list'},

    stores: {
        weekdays: {source: 'PrefWeekdays'},
        frequencies: {source: 'PrefNotificationFrequency'}
    }
});
