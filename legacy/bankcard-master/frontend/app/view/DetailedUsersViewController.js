Ext.define("bcp.view.DetailedUsersViewController", {
    extend: "Ext.app.ViewController",
    alias: "controller.detailedusers",

    add: function () {
        var list = this.lookupReference("list"),
            rowEditing = list.getPlugin("rowEditPluginForDetailee"),
            store = list.store;

        rowEditing.cancelEdit();

        const today = new Date();
        // Create a new date object and add one year
        const nextYear = new Date(today);
        nextYear.setFullYear(today.getFullYear() + 1);

        // Create a model instance with default value
        var r = Ext.create("model.detaileduser", {
            id: 0,
            accessOu: "N",
            accessDiv: "Y", //default to division access
            accessGroup: "N",
            validUntilDate: nextYear //default to one year from today
        });

        //filter will prevent the new row to show
        this.onReset();

        //make sure inserted row is at the top
        store.sort("peopleId", "ASC");

        store.insert(0, r);
        rowEditing.startEdit(0, 0);
    },

    remove: function () {
        var list = this.lookupReference("list"),
            rowEditing = list.getPlugin("rowEditPluginForDetailee"),
            store = Ext.getStore("DetailedUsers"),
            rec = list.selModel.lastSelected;

        rowEditing.cancelEdit();

        if (rec) {
            var id = rec.get("id");

            if (id === 0) {
                store.remove(rec); 
                store.sync();
                store.load();
            } else {
                // Ask user to confirm this action
                Ext.Msg.confirm(
                    "Confirm Delete",
                    "Are you sure you want to delete this detailee privilege?",
                    function (result) {
                        // User confirmed yes
                        if (result == "yes") {
                            Ext.Ajax.request({
                                url: "/empbc/v1/detailedusers/" + id,
                                method: "DELETE",
                                success: function (response) {
                                    Ext.getStore("DetailedUsers").reload();
                                }
                            });
                        }
                    }
                );
            }
        }
    },

    onReload: function (button, e, eOpts) {
        var store = Ext.getStore("DetailedUsers");
        store.load();
    },

    onStaffTfFilterChange: function (field, newValue, oldValue, eOpts) {
        this.lookupReference("list").store.filter([
            {
                filterFn: function (record) {
                    if (newValue !== null && newValue.length > 0) {
                        var val = newValue.toString();
                        return record.get("peopleId").toString().indexOf(val) > -1;
                    } else {
                        return true;
                    }
                },
                id: "staffIdtextflt"
            }
        ]);
    },

    onReset: function (button, e, eOpts) {
        this.lookupReference("list").store.clearFilter();
        this.lookupReference("tfFilter").reset();
    },

    onRowEditingCanceledit: function (editor, context, eOpts) {
        // Canceling editing of a locally added, unsaved record: remove it
        if (context.record && context.record.id === 0) {
            var store =  this.lookupReference("list").store;
            store.remove(context.record);
        }
    },

    onRowEditingEdit: function (editor, context, eOpts) {
        //only update if users changed something
        if (context.record.dirty) {
            Ext.Ajax.request({
                url: "/empbc/v1/detailedusers",
                jsonData: Ext.encode(context.record.data),
                method: context.record.id === 0 ? "POST" : "PUT",
                scope: this,
                success: function (response) {
                    this.onReload();
                }
            });
        }
    },

    onRowEditingBeforeEdit: function (editor, context, eOpts) {
        //NOTE: more privilege checks maybe needed here
        /*var recOuId = context.record.get('ouId'),
            model = this.getViewModel(),
            loggedInUser = model.get('loggedInUser'),
            userOuId = loggedInUser.ouId;

        //edit vendor shared by other OUs not allowed
        if (userOuId != recOuId) {
            Ext.Msg.alert(
                'Privilege Error',
                'You cannot edit vendors shared by other OUs.'
            );
            return false;
        }*/
    },

    onOUChange: function (ele, newValue, oldValue) {
        var divStore = this.getStore("divisions");
        var grpStore = this.getStore("groups");

        if (newValue != undefined && newValue != null) {
            divStore.addFilter({
                operator: "==",
                property: "ouId",
                value: newValue
            });

            grpStore.addFilter({
                operator: "==",
                property: "ouId",
                value: newValue
            });
        } else {
            divStore.clearFilter();
            grpStore.clearFilter();
        }
    },

    onDivChange: function (ele, newValue, oldValue) {
        var grpStore = this.getStore("groups");

        if (newValue != undefined && newValue != null) {
            grpStore.addFilter({
                operator: "==",
                property: "divisionId",
                value: newValue
            });
        } else {
            grpStore.clearFilter();
        }
    }
});
