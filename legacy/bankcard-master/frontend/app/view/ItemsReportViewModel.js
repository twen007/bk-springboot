/*
 * File: app/view/ItemsReportViewModel.js
 *
 */

Ext.define("bcp.view.ItemsReportViewModel", {
    extend: "Ext.app.ViewModel",
    alias: "viewmodel.itemsreport",
    requires: [
        "Ext.data.ChainedStore",
        "Ext.data.Store",
        "Ext.data.proxy.Ajax",
        "Ext.data.reader.Json",
        "Ext.util.Filter",
        "Ext.app.bind.Formula"
    ],
    data: {viewState: "list"},
    stores: {
        fys: {source: "Fys"},
        ous: {source: "Ous"},
        divisions: {source: "Divisions"},
        groups: {source: "Groups"},
        items: {
            model: "bcp.model.PcItem",
            proxy: {
                type: "ajax",
                url: "/empbc/v1/items/pcItems/",
                reader: {type: "json", rootProperty: "data"}
            }
        },
        reqFiles: {
            model: "bcp.model.BcpFile",
            proxy: {
                type: "ajax",
                url: "/empbc/v1/requests/",
                reader: {type: "json", rootProperty: "data"}
            }
        },
        projectTasksSearch: {source: "ProjectTaskCodesSearch"},
        itemStatuses: {source: "LkItemStatus"},
        purchaseTypes: {source: "PurchaseTypes"}
    },
    formulas: {
        isDetailView: {
            get: function (data) {
                return data === "detail";
            },
            bind: {bindTo: "{viewState}"}
        }
    }
});
