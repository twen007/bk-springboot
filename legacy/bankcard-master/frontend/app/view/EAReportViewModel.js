/*
 * File: app/view/EAReportViewModel.js
 * Author: ppg
 * Create Date: 01/2021
 * Objective: for the EA Report
 */

Ext.define('bcp.view.EAReportViewModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.eareport',

    requires: [
        'Ext.data.ChainedStore',
        'Ext.data.Store',
        'Ext.data.proxy.Ajax',
        'Ext.data.reader.Json',
        'Ext.util.Filter',
        'Ext.app.bind.Formula'
    ],

    data: {viewState: 'list'},

    stores: {
        ous: {source: 'Ous'},
        divisions: {source: 'Divisions'},
        groups: {source: 'Groups'},
        searchedItems: {
            model: 'bcp.model.EaItem',
            proxy: {
                type: 'ajax',
                url: '/empbc/v1/items/search',
                reader: {type: 'json', rootProperty: 'data'}
            }
        },
        /*statuses: {
            source: 'LkItemStatus',
        },
        bcEmployees: {
            source: 'OuEmployees'
        },*/
        reqFiles: {
            model: 'bcp.model.BcpFile',
            proxy: {
                type: 'ajax',
                url: '/empbc/v1/items/',
                reader: {type: 'json', rootProperty: 'data'}
            }
        }
    },
    formulas: {
        isDetailView: {
            get: function (data) {
                return data === 'detail';
            },
            bind: {bindTo: '{viewState}'}
        }
    }
});
