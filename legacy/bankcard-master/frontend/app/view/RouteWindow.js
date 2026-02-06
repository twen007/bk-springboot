Ext.define("bcp.view.RouteWindow", {
    extend: "Ext.window.Window",
    alias: "widget.routewindow",

    requires: [
        "bcp.view.RouteWindowViewModel",
        "bcp.view.RouteWindowViewController",
        "Ext.form.Panel",
        "Ext.form.field.Hidden",
        "Ext.form.field.Display",
        "Ext.form.field.ComboBox",
        "Ext.form.field.TextArea",
        "Ext.form.field.Checkbox",
        "Ext.toolbar.Toolbar",
        "Ext.button.Button"
    ],

    config: {store: {}},

    controller: "routewindow",
    viewModel: {type: "routewindow"},
    modal: true,
    width: 650,
    title: "Routing Confirmation",

    layout: {type: "vbox", align: "stretch"},
    items: [
        {
            xtype: "form",
            reference: "form",
            flex: 1,
            bodyPadding: 10,
            items: [
                {xtype: "hiddenfield", name: "requestId"},
                {xtype: "hiddenfield", name: "statusId"},
                {xtype: "hiddenfield", name: "typeId"},
                {xtype: "hiddenfield", name: "routeTo"},
                {xtype: "hiddenfield", name: "isDynamic"},
                {
                    xtype: "displayfield",
                    renderer: function (value, displayField) {
                        if (value) {
                            var routeRole = "",
                                routeStatus = value.data.statusId;
                            if (routeStatus === 4) {
                                routeRole = "NIST Host";
                            } else if (routeStatus === 5) {
                                routeRole = "Reviewer";
                            } else if (routeStatus === 6) {
                                routeRole = "Bankcard Approving Official";
                            } else if (routeStatus === 3 || routeStatus === 13 || routeStatus === 14) {
                                routeRole = "Official Requester";
                            } else if (routeStatus === 11) {
                                //reject, always go to requester
                                routeRole = "Official Requester";
                                /*
                            //BANK-564
                            if (value.data.typeId===13) { 
                                //normal return has typeId = 13 and statusId = 14
                                //but for parepared request returns, the statusId is set to 11 so preparer can do edit & resend
                                routeRole = "Preparer";
                            }else{
                                routeRole = "Official Requester";
                            }*/
                                //issue# 583
                            } else if (routeStatus === 15) {
                                //returned to preparer
                                routeRole = "Preparer";
                            } else if (routeStatus === 16) {
                                //returned to preparer
                                routeRole = "Funds Certifying Official";
                            } else if (routeStatus === 17) {
                                routeRole = "Division Chief";
                            } else if (routeStatus === 18) {
                                routeRole = "Director";
                            } else {
                                routeRole = "Bankcard Holder";
                            }
                            //for dynamic routing(create or approve), the route to user may not have
                            //any role in the system so we will just display [employee] in the msg.
                            //the only exception is when the user approves the reroute with rerouteStack=0,
                            //which means the req will be send back to the approver in the fixed route
                            if (
                                (value.data.isDynamic && value.data.rerouteStack > 0) ||
                                //added this one so we know if the original record's RerouteStack is 0; if it is
                                //the approver here should display 'Employee'
                                value.data.orgRerouteStack > 0
                            ) {
                                routeRole = "Employee";
                            }
                            //issue 591
                            var msg =
                                "This bankcard purchase request will to routed to the [<b>" +
                                routeRole +
                                "</b>], [<b>" +
                                value.data.routeToName +
                                "</b>], proceed?";
                            if (routeRole == "Bankcard Holder") {
                                msg =
                                    "<b>Cardholders shall ensure that subscriptions (such as magazines, journals, technical data, software)  do not automatically renew.</b><br><br>" +
                                    msg;
                            }
                            return msg;
                        }
                    },
                    anchor: "100%",
                    reference: "routeConfirmationMsg",
                    bind: {
                        disabled: "{record.routeTo===0}",
                        hidden: "{record.routeTo===0}",
                        value: "{record}"
                    }
                },
                {
                    //BANK-505
                    xtype: "tagfield",
                    reference: "comboShare",
                    width: 600,
                    fieldLabel: "Share",
                    emptyText: "optionally, select staff(s) here and notify them about this request via email",
                    displayField: "displayName",
                    required: false,
                    anyMatch: true,
                    minChars: 2,
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "peopleId",
                    bind: {
                        //only show this when users submit requests to reviewer/supervisor
                        disabled: "{record.typeId!==1}",
                        hidden: "{record.typeId!==1}",
                        store: "{bcEmployees}"
                    }
                },
                {
                    xtype: "container",
                    reference: "noRouteToCtn",
                    layout: {type: "vbox", align: "stretch"},
                    bind: {
                        disabled: "{record.routeTo!==0}",
                        hidden: "{record.routeTo!==0}"
                    },
                    items: [
                        {
                            xtype: "displayfield",
                            renderer: function (value, displayField) {
                                if (value) {
                                    //the user with the role that we are going to route to
                                    var routeRole = "",
                                        routeStatus = value.data.statusId;
                                    if (routeStatus === 5) {
                                        routeRole = "Reviewer";
                                    } else if (routeStatus === 6) {
                                        routeRole = "Bankcard Approving Official";
                                    } else if (routeStatus === 13) {
                                        routeRole = "Requester";
                                    } else if (routeStatus === 16) {
                                        routeRole = "Funds Certifying Official";
                                    } else if (routeStatus === 17) {
                                        routeRole = "Division Chief";
                                    } else if (routeStatus === 18) {
                                        routeRole = "Director";
                                    } else {
                                        routeRole = "Bankcard Holder";
                                    }
                                    return (
                                        "To route/reassign this bankcard purchase request, please select a " +
                                        routeRole +
                                        " below."
                                    );
                                }
                            },
                            reference: "routeInputRouteToMsg",
                            bind: {
                                disabled: "{record.routeTo!==0}",
                                hidden: "{record.routeTo!==0}",
                                value: "{record}"
                            }
                        },
                        {
                            xtype: "combobox",
                            reference: "routeToCombo",
                            fieldLabel: "Route to",
                            name: "routeTo",
                            allowBlank: false,
                            allowOnlyWhitespace: false,
                            anyMatch: true,
                            displayField: "displayName",
                            forceSelection: true,
                            queryMode: "local",
                            valueField: "peopleId",
                            bind: {disabled: "{record.routeTo!==0}"}
                        }
                    ]
                },
                {
                    xtype: "textareafield",
                    anchor: "100%",
                    reference: "notes",
                    maxLength: 1000,
                    fieldLabel: "Comments",
                    name: "notes"
                },
                {
                    xtype: "textareafield",
                    anchor: "100%",
                    reference: "rejectNotes",
                    fieldLabel: "Reason for Rejection",
                    name: "notes",
                    maxLength: 1000,
                    allowBlank: false,
                    allowOnlyWhitespace: false
                },
                {
                    xtype: "checkboxfield",
                    anchor: "100%",
                    reference: "certifyCb",
                    boxLabel:
                        "I certify that sufficient funds are available in the identified appropriation and suitable for the intended purpose.",
                    inputValue: "true",
                    uncheckedValue: "false",
                    listeners: {change: "onApproveCheckboxChange"}
                }
            ],
            dockedItems: [
                {
                    xtype: "toolbar",
                    dock: "bottom",
                    height: 48,
                    defaultButtonUI: "default",
                    layout: {type: "hbox", pack: "center"},
                    items: [
                        {
                            xtype: "button",
                            id: "routeWinCancelBtn",
                            text: "Cancel",
                            listeners: {click: "cancel"}
                        },
                        {
                            xtype: "button",
                            id: "routeWinProceedBtn",
                            formBind: true,
                            reference: "actionBtn",
                            text: "Proceed",
                            //bind: {hidden: '{record.typeId===2}'},//only show this when the approver is not FCO (FCO route to BAO)
                            listeners: {click: "proceed"}
                        },
                        {
                            xtype: "button",
                            id: "routeWinApproveBtn",
                            reference: "approveBtn",
                            disabled: true,
                            text: "Approve",
                            //bind: {hidden: '{record.typeId!==2}'},//this is for FCO only. the FCO need to check the certify checkbox to enable this btn
                            listeners: {click: "approve"}
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {beforeshow: "onWindowBeforeShow", added: "onWindowAdded"}
});
