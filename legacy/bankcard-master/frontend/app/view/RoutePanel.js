Ext.define("bcp.view.RoutePanel", {
    extend: "Ext.panel.Panel",
    alias: "widget.routepanel",

    requires: [
        "bcp.view.RoutePanelViewModel",
        "bcp.view.RoutePanelViewController",
        "bcp.view.RoutingHistory",
        "Ext.form.field.Display",
        "Ext.form.Panel",
        "Ext.form.FieldSet",
        "Ext.form.field.Number",
        "Ext.toolbar.Toolbar",
        "Ext.button.Button",
        "Ext.form.field.Hidden",
        "Ext.form.field.TextArea",
        "Ext.form.FieldContainer"
    ],

    controller: "routepanel",
    viewModel: {type: "routepanel"},
    reference: "routePanel",
    iconCls: "fas fa-paper-plane",
    title: "Routes",
    minWidth: 600,
    maxWidth: 1200,
    scrollable: true,
    padding: 10,
    layout: {
        type: "table",
        // The total column count must be specified here
        columns: 2
    },

    dockedItems: [
        {
            xtype: "displayfield",
            cls: "bcp_notice",
            dock: "top",
            fieldLabel: "Note",
            labelWidth: 60,
            value: "<b>The routing is pre-populated with users who are assigned roles in NIST Org. If for some reason that one or more steps in the approving process do not have a designate person, please contact your AO or Office Manager. </b>"
        },
        {
            xtype: "routinghistory",
            dock: "right",
            padding: 10,
            collapseDirection: "right",
            collapsible: true
        }
    ],
    items: [
        {
            xtype: "form",
            reference: "form",
            minWidth: 500,
            maxWidth: 650,
            //scrollable: true,
            defaults: {labelWidth: 130},
            bodyPadding: 10,
            dockedItems: [
                {
                    xtype: "toolbar",
                    reference: "routeTb",
                    dock: "top",
                    defaultButtonUI: "default",
                    items: [
                        {
                            xtype: "button",
                            id: "rpSubmitBtn",
                            text: "Submit",
                            bind: {
                                disabled: "{!routeRule.submit}",
                                hidden: "{!routeRule.submit}"
                            },
                            listeners: {click: "onSubmit"}
                        },
                        {
                            xtype: "button",
                            reference: "btnAppr",
                            text: "Approve",
                            bind: {
                                disabled: "{!routeRule.approve}",
                                hidden: "{!routeRule.approve}"
                            },
                            listeners: {click: "onApprove"}
                        },
                        {
                            xtype: "button",
                            reference: "actBtn",
                            text: "Route to Requester",
                            bind: {
                                disabled: "{routeRule.submit || routeRule.approve}",
                                hidden: "{routeRule.submit || routeRule.approve}"
                            },
                            listeners: {click: "onAction"}
                        },
                        {
                            xtype: "button",
                            reference: "rerouteBtn",
                            text: "Reroute",
                            listeners: {click: "onReroute"}
                        },
                        {
                            xtype: "button",
                            reference: "reassignBtn",
                            text: "Re-Assign",
                            tooltip:
                                "Use this function if you want to re-assign the request to someone who has the same role as you. For example, you are a Bankcard Holder and you want to make the request a different Bankcard Holder's responsibility.",
                            listeners: {click: "onReassign"}
                        },
                        {
                            xtype: "button",
                            reference: "btnApprAddRoute",
                            text: "Approve & Add a Route",
                            listeners: {click: "onApproveAndAddRoute"}
                        },
                        {
                            xtype: "button",
                            text: "Return for Info",
                            tooltip: "return the request to the requester to get additional information",
                            bind: {
                                disabled: "{!routeRule.reject}",
                                hidden: "{!routeRule.reject}"
                            },
                            listeners: {click: "onReturnForInfo"}
                        },
                        {
                            xtype: "button",
                            text: "Reject",
                            tooltip:
                                "use this function when the request cannot be approved due to policy or lack of fund. For missed information/justification or change request content, use the [Return for Info] instead.",
                            bind: {
                                disabled: "{!routeRule.reject}",
                                hidden: "{!routeRule.reject}"
                            },
                            listeners: {click: "onReject"}
                        }
                    ]
                }
            ],
            items: [
                {xtype: "hiddenfield", colspan: 2, name: "hostId"},
                {xtype: "hiddenfield", colspan: 2, name: "reviewerId"},
                {
                    xtype: "hiddenfield",
                    colspan: 2,
                    name: "bankcardApprovingOfficialId"
                },
                {xtype: "hiddenfield", colspan: 2, name: "bankcardHolderId"},
                {
                    xtype: "fieldset",
                    reference: "fsUpTo",
                    colspan: 2,
                    title: "Request Approved up to $",
                    items: [
                        {
                            xtype: "displayfield",
                            //anchor: "100%",
                            bind: {
                                //changed from record to generalInfo
                                html: "The [Total Cost] is <b>${(generalInfo.actualTotalCost>generalInfo.totalCost)?generalInfo.actualTotalCost:generalInfo.totalCost}</b>. The approval amount should be set to equal or greater than the total cost."
                            }
                            //value: '<b>By default, approval amount is set to equal to the estimated cost. However, you can optionally approve a [up to] amount that is greater than estimated cost.</b>'
                        },
                        {
                            xtype: "numberfield",
                            fieldLabel: "$",
                            labelWidth: 15,
                            reference: "nfApprAmt",
                            name: "approvalAmount",
                            id: "rpnumberfield_nfApprAmt",
                            bind: {
                                //changed from record to generalInfo
                                minValue: "{generalInfo.totalCost}",
                                maxValue: "{purchaseLimit}"
                            },
                            listeners: {blur: "onApprovalAmountBlur"}
                        }
                    ]
                },
                {
                    xtype: "fieldset",
                    title: "Additional Comments or Instructions",
                    margin: 5,
                    minHeight: 120,
                    colspan: 2,
                    minWidth: 500,
                    maxWidth: 600,
                    items: [
                        {
                            xtype: "textareafield",
                            reference: "commentsTa",
                            id: "rptextareafield_commentsTa",
                            listeners: {
                                blur: {
                                    fn: "onCommentsBlur",
                                    scope: "controller"
                                }
                            },
                            padding: "0 20 0 0",
                            minHeight: 100,
                            minWidth: 500,
                            anchor: "50%",
                            maxLength: 1000,
                            name: "comments",
                            emptyText:
                                "If you have any additional information or special instruction about the request that you want the reviewer or approver to know, please enter it here."
                        }
                    ]
                },
                {
                    xtype: "fieldset",
                    title: "Route Stops In Ascending Order",
                    margin: 5,
                    colspan: 2,
                    minHeight: 240,
                    minWidth: 500,
                    maxWidth: 600,
                    items: [
                        {
                            xtype: "textfield",
                            anchor: "100%",
                            maxWidth: 500,
                            fieldLabel: "Thanks for preparing this request for",
                            labelAlign: "top",
                            name: "requesterName",
                            id: "rptextfield_requesterName",
                            readOnly: true,
                            allowBlank: false,
                            allowOnlyWhitespace: false,
                            bind: {
                                disabled: "{generalInfo.statusCode!==12}",
                                hidden: "{generalInfo.statusCode!==12}"
                            }
                        },
                        {
                            xtype: "displayfield",
                            anchor: "100%",
                            maxWidth: 550,
                            value: "<b>When you are ready, click the [Route to Requester] button above. The requester will review and submit the request.</b>",
                            bind: {hidden: "{generalInfo.statusCode!==12}"}
                        },
                        {
                            xtype: "fieldcontainer",
                            reference: "ctnApprovers",
                            layout: {type: "vbox", align: "stretch"},
                            items: [
                                {
                                    xtype: "fieldcontainer",
                                    fieldLabel: "Reviewer (Bona Fide Need Certifier)",
                                    labelAlign: "top",
                                    layout: {type: "hbox", align: "stretch"},
                                    items: [
                                        {
                                            xtype: "textfield",
                                            minWidth: 300,
                                            padding: "0 5 0 0",
                                            name: "reviewerName",
                                            id: "rptextfield_reviewerName",
                                            readOnly: true,
                                            allowBlank: false,
                                            allowOnlyWhitespace: false
                                        },
                                        {
                                            xtype: "button",
                                            reference: "btnChangeReviewer",
                                            text: "Change",
                                            listeners: {
                                                click: "onChangeReviewerClick"
                                            }
                                        }
                                    ]
                                },
                                {
                                    xtype: "fieldcontainer",
                                    fieldLabel: "Mission Critical Approver",
                                    reference: "fcDc",
                                    labelAlign: "top",
                                    layout: {type: "hbox", align: "stretch"},
                                    items: [
                                        {
                                            xtype: "textfield",
                                            minWidth: 300,
                                            padding: "0 5 0 0",
                                            name: "dcName",
                                            reference: "tfDc",
                                            id: "rptextfield_tfDc",
                                            readOnly: true
                                        },
                                        {
                                            xtype: "button",
                                            reference: "btnChangeDc",
                                            text: "Update",
                                            listeners: {
                                                click: "onChangeDcClick"
                                            }
                                        }
                                    ]
                                },

                                {
                                    xtype: "textfield",
                                    fieldLabel: "OU Director",
                                    labelAlign: "top",
                                    readOnly: true,
                                    anchor: "100%",
                                    maxWidth: 500,
                                    reference: "fcDr",
                                    padding: "0 5 0 0",
                                    value: "This request [Mission Critical Category: Other] requires OU Director approval."
                                },

                                {
                                    xtype: "fieldcontainer",
                                    fieldLabel: "Funds Certifying Official",
                                    reference: "fcFco",
                                    labelAlign: "top",
                                    layout: {type: "hbox", align: "stretch"},
                                    items: [
                                        {
                                            xtype: "textfield",
                                            minWidth: 300,
                                            padding: "0 5 0 0",
                                            name: "fcoName",
                                            reference: "tfFco",
                                            id: "rptextfield_tfFco",
                                            readOnly: true
                                        },
                                        {
                                            xtype: "button",
                                            reference: "btnChangeFco",
                                            text: "Change",
                                            //hidden:true,
                                            //disabled:true,
                                            listeners: {
                                                click: "onChangeFcoClick"
                                            }
                                        }
                                    ]
                                },
                                {
                                    xtype: "fieldcontainer",
                                    reference: "fcBao",
                                    fieldLabel: "Bankcard Approving Official and Funds Certifying Official",
                                    labelAlign: "top",
                                    layout: {type: "hbox", align: "stretch"},
                                    items: [
                                        {
                                            xtype: "textfield",
                                            minWidth: 300,
                                            padding: "0 5 0 0",
                                            name: "baoName",
                                            id: "rptextfield_baoName",
                                            readOnly: true,
                                            allowBlank: false,
                                            allowOnlyWhitespace: false
                                        },
                                        {
                                            xtype: "button",
                                            reference: "btnChangeBao",
                                            text: "Change",
                                            listeners: {
                                                click: "onChangeBaoClick"
                                            }
                                        }
                                    ]
                                },
                                {
                                    xtype: "fieldcontainer",
                                    fieldLabel: "Bankcard Holder",
                                    labelAlign: "top",
                                    layout: {type: "hbox", align: "stretch"},
                                    items: [
                                        {
                                            xtype: "textfield",
                                            minWidth: 300,
                                            padding: "0 5 0 0",
                                            name: "bhName",
                                            id: "rptextfield_bhName",
                                            readOnly: true,
                                            allowBlank: false,
                                            allowOnlyWhitespace: false
                                        },
                                        {
                                            xtype: "button",
                                            reference: "btnChangeBh",
                                            text: "Change",
                                            listeners: {
                                                click: "onChangeBhClick"
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],
    listeners: {added: "onViewAdded", beforeshow: "onViewBeforeShow"}
});
