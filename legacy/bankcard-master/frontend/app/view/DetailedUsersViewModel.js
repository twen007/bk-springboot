Ext.define("bcp.view.DetailedUsersViewModel", {
    extend: "Ext.app.ViewModel",
    alias: "viewmodel.detailedusers",
    requires: ["Ext.data.ChainedStore", "Ext.util.Filter"],
    stores: {
        detailedUsers: {source: 'DetailedUsers'},
        groups: {source: "Groups"},
        divisions: {source: "Divisions"},
        ous: {source: "Ous"},
        yesnostore: {
            // Define your yes/no store here
            autoLoad: true,
            fields: ["key", "value"], // Define the fields
            data: [
                {key: "Y", value: "Yes"},
                {key: "N", value: "No"}
            ],
            proxy: {
                type: "memory",
                reader: {
                    type: "json"
                }
            }
        }
    }
});
