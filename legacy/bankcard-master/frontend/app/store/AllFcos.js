/**
 * the div FCO API returns all users assigned to the div including users belong to outside OUs.
 * the OU FCO API returns all users with FCO role in that OU only regardless if assigned to a div
 * the Fco combo originally used ouFcos, which would not show outside OU users
 * combining the data from these two stores will give a complete list of FCOs; issue 676
 */
Ext.define("bcp.store.AllFcos", {
    extend: "Ext.data.Store",

    requires: ["bcp.model.BcUser", "Ext.data.proxy.Ajax", "Ext.data.reader.Json", "Ext.util.Sorter"],

    constructor: function (cfg) {
        let me = this;
        cfg = cfg || {};
        me.callParent([
            Ext.apply(
                {
                    storeId: "AllFcos",
                    model: "bcp.model.BcUser",
                    sorters: {property: "fullName"}
                },
                cfg
            )
        ]);
    }
});
