/*
 * File: app/view/PcItemReportViewModel.js
 *
 */


Ext.define("bcp.view.PcItemReportViewModel", {
    extend: "Ext.app.ViewModel",
    alias: "viewmodel.pcitemreport",
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
        pcItems: {
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
        }
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
