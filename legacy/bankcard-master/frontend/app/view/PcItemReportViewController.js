/*
 * File: app/view/PcItemReportViewController.js
 *
 */

Ext.define("bcp.view.PcItemReportViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.pcitemreport",

    onExport: function (button, e, eOpts) {
        var grid = this.lookupReference("list");
        grid.saveDocumentAs({
            type: "xlsx",
            title: "PropertyCustodianExport",
            fileName: "PropertyCustodianSearchItemResult.xlsx"
        });
    },

    onSearch: function (button, e, eOpts) {
        var store = this.getStore("pcItems"),
            fileStore = Ext.getStore("RequestFiles"),
            refs = this.getReferences(),
            list = refs.list,
            fy = refs.comboFy.value,
            ouId = refs.comboOu.value,
            divisionId = refs.comboDiv.value,
            groupId = refs.comboGrp.value,
            fromDate = Ext.Date.format(this.lookupReference("dateFrom").value, "Y-m-d"),
            toDate = Ext.Date.format(this.lookupReference("dateTo").value, "Y-m-d");

        //clean previous result and selection if any
        list.setSelection(null);
        fileStore.removeAll();
        store.proxy.url = "/empbc/v1/items/pcItems?";

        //build query
        if (fy) {
            store.proxy.url += "fy=" + fy + "&";
        }
        if (ouId) {
            store.proxy.url += "ouId=" + ouId + "&";
        }
        if (divisionId) {
            store.proxy.url += "divisionId=" + divisionId + "&";
        }
        if (groupId) {
            store.proxy.url += "groupId=" + groupId + "&";
        }

        if (fromDate && fromDate !== "") {
            store.proxy.url += "fromDate=" + fromDate + "&";
        }
        if (toDate && toDate !== "") {
            store.proxy.url += "toDate=" + toDate + "&";
        }

        store.load();
    },

    onReset: function (button, e, eOpts) {
        Ext.suspendLayouts();
        var model = this.getViewModel(),
            refs = this.getReferences(),
            tb1 = refs.filterTb1,
            tb2 = refs.filterTb2,
            fields = tb1.items.items,
            f,
            fLen = fields.length;

        refs.comboFy.setValue(model.get("currentFy"));
        refs.comboGrp.reset();

        fields = tb2.items.items;
        fLen = fields.length;

        for (f = 0; f < fLen; f++) {
            if (fields[f].xtype !== "button") {
                fields[f].reset();
            }
        }

        Ext.resumeLayouts(true);
    },

    onSelect: function (rowmodel, record, index, eOpts) {
        var reqId = record.get("requestId"),
            fileStore = Ext.getStore("RequestFiles");

        fileStore.proxy.url = "/empbc/v1/requests/" + reqId + "/attachments";
        fileStore.load();
    },
    onViewAdded: function (component, container, pos, eOpts) {
        var model = this.getViewModel(),
            refs = this.getReferences(),
            loggedInUser = model.get("loggedInUser"),
            currentYear = new Date().getFullYear(), //yr in 4 digits
            currentFy = Number(currentYear.toString().substr(-2)),
            ouStore = this.getStore("ous"),
            divStore = this.getStore("divisions"),
            grpStore = this.getStore("groups"),
            ouId = 0,
            divId = 0;
        //grpId = 0;

        model.set("currentFy", currentFy);

        //detailee mode value setup
        if (loggedInUser.detaileeMode == true) {
            var detailee = Ext.getStore("Detailees").first();
            ouId = detailee.get("ouId");
            divId = detailee.get("divisionId");
            // grpId = detailee.get("groupId");
        } else {
            ouId = loggedInUser.ouId;
            divId = loggedInUser.divisionId;
            //grpId = loggedInUser.groupId;
        }

        refs.comboFy.setValue(currentFy);
        refs.comboOu.setValue(ouId);
        refs.comboDiv.setValue(divId);

        //first, limit org filter to user's ou only
        ouStore.addFilter({operator: "==", property: "ouId", value: ouId});

        divStore.addFilter({operator: "==", property: "ouId", value: ouId});

        grpStore.addFilter({operator: "==", property: "ouId", value: ouId});

        //property custodian is a division role
        divStore.addFilter({
            operator: "==",
            property: "divisionId",
            value: divId
        });

        grpStore.addFilter({
            operator: "==",
            property: "divisionId",
            value: divId
        });
    }
});
