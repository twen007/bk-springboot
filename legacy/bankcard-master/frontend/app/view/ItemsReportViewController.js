/*
 * File: app/view/ItemsReportViewController.js
 *
 */

Ext.define("bcp.view.ItemsReportViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.itemsreport",

    onExport: function (button, e, eOpts) {
        var grid = this.lookupReference("list");
        grid.saveDocumentAs({
            type: "xlsx",
            title: "ItemsExport",
            fileName: "SearchItemsResult.xlsx"
        });
    },

    onSearch: function (button, e, eOpts) {
        var store = this.getStore("items"),
            fileStore = Ext.getStore("RequestFiles"),
            refs = this.getReferences(),
            list = refs.list,
            fy = refs.comboFy.value,
            ouId = refs.comboOu.value,
            divisionId = refs.comboDiv.value,
            groupId = refs.comboGrp.value,
            fromDate = Ext.Date.format(refs.dateFrom.value, "Y-m-d"),
            toDate = Ext.Date.format(refs.dateTo.value, "Y-m-d"),
            baseUrl = "/empbc/v1/items/pcItems",
            queryParams = [];

        //clean previous result and selection if any
        list.setSelection(null);
        fileStore.removeAll();
        // Clear any existing client-side filters before loading new data
        store.clearFilter(true); // true to suppress event, load will fire one

        //build query
        if (fy) queryParams.push("fy=" + encodeURIComponent(fy));
        if (ouId) queryParams.push("ouId=" + encodeURIComponent(ouId));
        if (divisionId) queryParams.push("divisionId=" + encodeURIComponent(divisionId));
        if (groupId) queryParams.push("groupId=" + encodeURIComponent(groupId));
        if (fromDate && fromDate !== "") queryParams.push("fromDate=" + encodeURIComponent(fromDate));
        if (toDate && toDate !== "") queryParams.push("toDate=" + encodeURIComponent(toDate));

        if (queryParams.length > 0) {
            store.proxy.url = baseUrl + "?" + queryParams.join("&");
        } else {
            store.proxy.url = baseUrl;
        }

        store.load();
    },

    onResetSearch: function (button, e, eOpts) {
        Ext.suspendLayouts();
        let model = this.getViewModel(), // Keep model if used for other default values
            refs = this.getReferences();

        refs.comboFy.setValue(model.get("currentFy"));
        refs.comboDiv.reset();
        refs.comboGrp.reset();
        refs.dateFrom.reset();
        refs.dateTo.reset();

        Ext.resumeLayouts(true);

        // Reset client-side filters as well
        this.onResetFilter();
    },

    onFilterFieldChange: function () {
        this.applyGridFilters();
    },

    applyGridFilters: function () {
        var refs = this.getReferences(),
            store = this.getStore("items"),
            filters = [];

        var addFilter = function (property, value, options) {
            options = options || {};
            if (value !== null && value !== undefined && value !== "" && !(Ext.isArray(value) && value.length === 0)) {
                var filterConfig = {property: property, value: value};
                if (options.anyMatch) {
                    filterConfig.anyMatch = true;
                    filterConfig.caseSensitive = options.caseSensitive !== undefined ? options.caseSensitive : false;
                } else {
                    // exactMatch is default for non-anyMatch
                    filterConfig.exactMatch = true;
                }
                filters.push(new Ext.util.Filter(filterConfig));
            }
        };

        var addDateFilter = function (property, dateValue) {
            if (dateValue && Ext.isDate(dateValue)) {
                filters.push(
                    new Ext.util.Filter({
                        filterFn: function (record) {
                            var recordDate = record.get(property);
                            if (recordDate && Ext.isDate(recordDate)) {
                                return (
                                    Ext.Date.clearTime(new Date(recordDate), true).getTime() ===
                                    Ext.Date.clearTime(new Date(dateValue), true).getTime()
                                );
                            }
                            return false;
                        }
                    })
                );
            }
        };

        var addTagFilter = function (property, valueArray) {
            if (valueArray && valueArray.length > 0) {
                filters.push(
                    new Ext.util.Filter({
                        filterFn: function (item) {
                            return Ext.Array.contains(valueArray, item.get(property));
                        }
                    })
                );
            }
        };

        // FilterTb1 fields - Assuming dataIndex in store matches or is derived
        addFilter("itemName", refs.tfItemName.getValue(), {anyMatch: true});
        addFilter("projectTask", refs.comboPtc.getValue()); // comboPtc valueField is 'code', dataIndex 'projectTask'
        addFilter("objectClass", refs.tfObjcls.getValue(), {anyMatch: true});
        addFilter("requisitionNumber", refs.reqNumTf.getValue(), {anyMatch: true});
        addFilter("purchaseTypeId", refs.comboPurchaseType.getValue()); // Assuming 'purchaseTypeId' in store for comboPurchaseType.valueField 'id'

        // FilterTb2 fields
        addTagFilter("itemStatusId", refs.tagItemStatus.getValue()); // Assuming 'itemStatusId' in store for tagItemStatus.valueField 'id'
        addFilter("transactionNumber", refs.tfTransactionNum.getValue(), {anyMatch: true});
        addFilter("catelogNumber", refs.tfCatalogNum.getValue(), {anyMatch: true}); 

        addDateFilter("statementDate", refs.dfStatementDate.getValue());
        addDateFilter("dateReceived", refs.dfReceivedDate.getValue());

        store.getFilters().replaceAll(filters); // Replaces all existing filters with the new set
        if (filters.length === 0 && store.isFiltered()) {
            // Ensure grid updates if all filters are cleared
            store.clearFilter();
        }
    },

    onResetFilter: function (button, e, eOpts) {
        Ext.suspendLayouts();
        let refs = this.getReferences(),
            filterToolbars = [refs.filterTb1, refs.filterTb2];

        filterToolbars.forEach(function (toolbar) {
            if (toolbar) {
                toolbar.items.each(function (item) {
                    if (item.isFormField && typeof item.reset === "function") {
                        item.reset();
                    }
                });
            }
        });

        Ext.resumeLayouts(true);

        // Clear the filters from the store
        let store = this.getStore('items');
        if (store) {
            store.clearFilter(); // This will remove client-side filters and refresh the grid
        }
    },

    onSelect: function (rowmodel, record, index, eOpts) {
        let reqId = record.get("requestId"),
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

        //load item statuses for search
        Ext.getStore("LkItemStatus").load();

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
    }
});
