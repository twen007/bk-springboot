/*
 * File: app/view/EAReportViewController.js
 * Author: ppg
 * Create Date: 01/2021
 * Objective: for the EA Report
 */

Ext.define('bcp.view.EAReportViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.eareport',

    onExport: function (button, e, eOpts) {
        var grid = this.lookupReference('list');
        grid.saveDocumentAs({
            type: 'xlsx',
            title: 'Estimated Accrued Items Report',
            fileName: 'eareport.xlsx',
            showSummary: true
        });
    },

    onSearch: function (button, e, eOpts) {
        var store = this.getStore('searchedItems'),
            model = this.getViewModel(),
            //fileStore=Ext.getStore('RequestFiles'),
            refs = this.getReferences(),
            list = refs.list,
            loggedInUser = model.get('loggedInUser'),
            ouId = loggedInUser.ouId,
            //ouId = refs.comboOu.value,
            //divisionId = refs.comboDiv.value,
            divCode = refs.comboSupDiv.value,
            fromDate = Ext.Date.format(
                this.lookupReference('dateFrom').value,
                'Y-m-d'
            ),
            toDate = Ext.Date.format(
                this.lookupReference('dateTo').value,
                'Y-m-d'
            );
        store.proxy.url = '/empbc/v1/items?';
        //requestStatusId=10&ouId=13204&divisionId=13225&requesterId=23826
        //&fromDate=2017-05-30&toDate=2017-06-06&groupId=13316

        //clean previous result and selection if any
        list.setSelection(null);

        //build query
        if (ouId) {
            store.proxy.url += 'ouId=' + ouId + '&';
        }
        if (divCode) {
            store.proxy.url += 'divCode=' + divCode + '&';
        }
        if (fromDate && fromDate !== '') {
            store.proxy.url += 'fromDate=' + fromDate + '&';
        }
        if (toDate && toDate !== '') {
            store.proxy.url += 'toDate=' + toDate + '&';
        }

        store.load();
        store.on('load', function (operation, records, success) {
            Ext.getCmp('eaReportExport').disable();
            if (store.data.length > 0) Ext.getCmp('eaReportExport').enable();
        });
    },

    onReset: function (button, e, eOpts) {
        /*Ext.suspendLayouts();
        var refs=this.getReferences(),
            tb1=refs.filterTb1,
            tb2=refs.filterTb2,
            fields = tb1.items.items,
            f,
            fLen   = fields.length;

        for (f = 0; f < fLen; f++) {
            if(fields[f].xtype!=='button'){fields[f].reset();}
        }

        fields = tb2.items.items;
        fLen   = fields.length;

        for (f = 0; f < fLen; f++) {
            if(fields[f].xtype!=='button'){fields[f].reset();}
        }

        Ext.resumeLayouts(true);*/
    },

    onViewAdded: function (component, container, pos, eOpts) {
        var model = this.getViewModel(),
            loggedInUser = model.get('loggedInUser'),
            //refs = this.getReferences(),
            ouStore = this.getStore('ous'),
            divStore = this.getStore('divisions'),
            ouId = 0;
        //detailee mode
        if (loggedInUser.detaileeMode == true) {
            ouId = Ext.getStore('Detailees').first().get('ouId');
        } else {
            ouId = loggedInUser.ouId;
        }

        //first, limit org filter to user's ou only
        ouStore.addFilter({operator: '==', property: 'ouId', value: ouId});

        divStore.addFilter({operator: '==', property: 'ouId', value: ouId});

        //refs.comboOu.setValue(loggedInUser.ouId);
        //refs.comboDiv.setValue(loggedInUser.divId);

        //var recOu = ouStore.findRecord('ouId', loggedInUser.ouId);
        //var recDiv = divStore.findRecord('divisionId', loggedInUser.divId);

        //refs.tOU.setHtml('<b>OU: ' + recOu.data.shortName + '</b>');
        //refs.tDiv.setHtml('<b>Division: ' + recDiv.data.shortName + '</b>');
    }
});
