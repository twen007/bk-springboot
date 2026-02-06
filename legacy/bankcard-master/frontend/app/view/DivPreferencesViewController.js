Ext.define('bcp.view.DivPreferencesViewController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.divpreferences',

    onDivChange: function (field, newValue, oldValue, eOpts) {
        var refs = this.getReferences(),
            bchInitStore = Ext.getStore('BchInitials'),
            bchDivStore = this.getStore('bchdivisions'),
            form = refs.form;

        var rec = bcp.util.CommonFunctions.getDivisionPreferences(newValue);
        form.loadRecord(rec);

        //load BCH initials data
        bchInitStore.proxy.url =
            '/empbc/v1/nistOrgs/getBchInitPrefs/' + newValue;
        bchInitStore.load();

        //filter comboBchDiv
        bchDivStore.clearFilter();
        bchDivStore.addFilter({
            operator: '==',
            property: 'divisionId',
            value: newValue
        });
    },

    onViewAdded: function (component, container, pos, eOpts) {
        var refs = this.getReferences(),
            model = this.getViewModel(),
            loggedInUser = model.get('loggedInUser'),
            form = refs.form,
            aoDivStore = this.getStore('aodivisions'),
            divStore = this.getStore('divisions'),
            bchInitStore = Ext.getStore('BchInitials');

        //BANK-506
        if (
            !bcp.util.CommonUtil.isUserInRole([
                'Senior Management Advisor',
                'Executive Officer'
            ])
        ) {
            //since a AO can work for multiple divisions and their default division is usually the director's office
            //we cannot assume the AOs try to save div preference for their default division. we need to use NIST Org API
            //to get the list of Divs the AO is assigned to and give them the option to choose which division's pref they want to change
            divStore.addFilter([
                {
                    filterFn: function (item) {
                        var aodivs = aoDivStore.data.items;
                        for (var i = 0; i < aodivs.length; i++) {
                            if (aodivs[i].get('code') == item.get('code')) {
                                return true;
                            }
                        }
                        return false;
                    },
                    id: 'aoDivsFlt'
                }
            ]);
        } else {
            //for SMA and XO, they should see all divisions from their OU.
            divStore.addFilter({
                operator: '==',
                property: 'ouId',
                value: loggedInUser.ouId
            });
        }

        var prefRec = divStore.first();
        if (!prefRec) {
            //this is to handle the case when the AO wasn't setup properly in NIST Org
            //NOTE: currently, there is an unresolved defect in NIST Org that if multiple AOs are assigned to a division, the
            //AOs won't be include in the supportedDivision API call
            Ext.Msg.alert(
                'No Supported Divisions found',
                'You have the AO role but your supported Division(s) are not specified in NIST Org.'
            );
            this.redirectTo('#dashboard');
            return;
        }

        //get division id for the first division record in the division combo
        var divId = prefRec.data.divisionId;
        refs.comboDiv.setValue(divId);
        //find a existing div preference record
        var rec = bcp.util.CommonFunctions.getDivisionPreferences(divId);
        form.loadRecord(rec);

        //load BCH initials data
        bchInitStore.proxy.url = '/empbc/v1/nistOrgs/getBchInitPrefs/' + divId;
        bchInitStore.load();
    },

    onSave: function (button, e, eOpts) {
        var refs = this.getReferences(),
            form = refs.form,
            store = Ext.getStore('DivisionPreferences'),
            formData = null;

        if (form.isValid()) {
            formData = form.getValues();

            Ext.Ajax.request({
                url:
                    bcp.config.Runtime.getServerBaseUrl() +
                    'nistOrgs/setDivPrefs',
                method: 'POST',
                jsonData: Ext.encode(formData),
                success: function (response) {
                    bcp.util.CommonUtil.ajaxSuccessHandler(
                        response,
                        function (result) {
                            Ext.Msg.alert('Success', 'Preferences saved.');
                            store.reload();
                        }
                    );
                }
            });
        } else {
            Ext.Msg.alert(
                'Form Validation Error',
                'Please fix the invalid form data.'
            );
        }
    },

    //add new bch inits
    add: function (button, e, eOpts) {
        var list = this.lookupReference('list'),
            rowEditing = list.getPlugin('rowEditPlugin'),
            store = Ext.getStore('BchInitials'),
            bchDivStore = this.getStore('bchdivisions');

        rowEditing.cancelEdit();

        // Create a model instance
        var r = Ext.create('model.bchinitial', {
            initials: '',
            id: 0,
            peopleId: 0,
            divId: bchDivStore.first().get('divisionId')
        });

        store.insert(0, r);
        //make sure inserted row is at the top
        store.sort('id', 'ASC');
        rowEditing.startEdit(0, 0);
    },

    remove: function (button, e, eOpts) {
        var list = this.lookupReference('list'),
            rowEditing = list.getPlugin('rowEditPlugin'),
            store = Ext.getStore('BchInitials'),
            rec = list.selModel.lastSelected,
            id = 0;

        rowEditing.cancelEdit();

        if (rec) {
            id = rec.get('id');

            if (id === 0) {
                store.remove(rec);
            } else {
                // Ask user to confirm this action
                Ext.Msg.confirm(
                    'Confirm Delete',
                    'Are you sure you want to delete this custom initials?',
                    function (result) {
                        // User confirmed yes
                        if (result == 'yes') {
                            Ext.Ajax.request({
                                url: '/empbc/v1/nistOrgs/bchInitPrefs/' + id,
                                method: 'DELETE',
                                success: function (response) {
                                    Ext.getStore('BchInitials').reload();
                                }
                            });
                        }
                    }
                );
            }
        }
    },

    onRowEditingCanceledit: function (editor, context, eOpts) {
        // Canceling editing of a locally added, unsaved record: remove it
        if (context.record.phantom) {
            var store = this.getStore('BchInitials');
            store.remove(context.record);
        }
    },

    onRowEditingEdit: function (editor, context, eOpts) {
        //only update if users changed something
        if (context.record.dirty) {
            var vname = context.record.get('initials').trim(),
                vpid = context.record.get('peopleId'),
                store = this.lookupReference('list').store,
                v = new RegExp('^' + vname + '$', 'i'),
                v2 = new RegExp('^' + vpid + '$', 'i'),
                hasDup = 0,
                hasBchInit = 0;

            //for create, do a check for duplicated initials
            if (context.record.id === 0) {
                hasDup = store.findBy(function (rec, id) {
                    //the record itself is already in the store, so we have to check id!=0
                    if (v.test(rec.data.initials.trim()) && rec.id !== 0)
                        return true;
                    else return false;
                });
            } else {
                //for update
                hasDup = store.findBy(function (rec, id) {
                    //the record itself is already in the store, so we have to check id is not the record itself's id
                    if (
                        v.test(rec.data.initials.trim()) &&
                        rec.id !== context.record.id
                    )
                        return true;
                    else return false;
                });
            }

            if (hasDup != -1) {
                context.record.reject();
                Ext.Msg.alert(
                    'Validation Error',
                    'An identical Custom Initials [' +
                        vname.toUpperCase() +
                        '] already exists.'
                );
                return;
            }

            //for create, do a check for duplicated bch
            if (context.record.id === 0) {
                hasBchInit = store.findBy(function (rec, id) {
                    //the record itself is already in the store, so we have to check id!=0
                    if (v2.test(rec.data.peopleId) && rec.id !== 0) return true;
                    else return false;
                });
            } else {
                //for update
                hasBchInit = store.findBy(function (rec, id) {
                    //the record itself is already in the store, so we have to check id!=0
                    if (
                        v2.test(rec.data.peopleId) &&
                        rec.id !== context.record.id
                    )
                        return true;
                    else return false;
                });
            }

            if (hasBchInit != -1) {
                context.record.reject();
                Ext.Msg.alert(
                    'Validation Error',
                    'This Bankcard Holder already setup a Custom Initials for this division.'
                );
                return;
            }

            //make it uppercase if applies
            context.record.set('initials', vname.toUpperCase());

            Ext.Ajax.request({
                url: '/empbc/v1/nistOrgs/setBchInitPrefs',
                jsonData: Ext.encode(context.record.data),
                method: 'POST',
                scope: this,
                success: function (response) {
                    Ext.getStore('BchInitials').reload();
                }
            });
        }
    },

    onRowEditingBeforeEdit: function (editor, context, eOpts) {
        var id = context.record.data.id;
        if (id !== 0) {
            this.getViewModel().set('isUpdate', true);
        } else {
            this.getViewModel().set('isUpdate', false);
        }
    }
});
