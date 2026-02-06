/*
 * File: app/view/CreateRequestWindow.js
 *
 */

Ext.define("bcp.view.CreateRequestWindow", {
    extend: "Ext.window.Window",
    alias: "widget.createrequest",
    requires: [
        "bcp.view.CreateRequestWindowViewModel",
        "bcp.view.CreateRequestWindowViewController",
        "Ext.form.Panel",
        "Ext.form.field.Hidden",
        "Ext.form.field.Display",
        "Ext.form.RadioGroup",
        "Ext.form.field.Radio",
        "Ext.XTemplate",
        "Ext.form.field.ComboBox",
        "Ext.button.Button"
    ],

    controller: "createrequestwindow",
    viewModel: {type: "createrequestwindow"},
    minWidth: 700,
    width: 700,
    closable: false,
    title: "Create a Request",

    items: [
        {
            xtype: "form",
            id: "crwForm",
            reference: "form",
            anchor: "100%",
            flex: 1,
            scrollable: true,
            defaults: {labelWidth: 230, labelAlign: "left", labelSeparator: "", submitEmptyText: false, anchor: "100%"},
            bodyPadding: 10,
            layout: {type: "table", columns: 2},
            items: [
                {
                    xtype: "hiddenfield",
                    reference: "requestIdHf",
                    fieldLabel: "Label",
                    name: "requestId",
                    bind: {
                        value: "{record.requestId}"
                    }
                },
                {
                    xtype: "hiddenfield",
                    name: "creatorId"
                },
                {
                    xtype: "hiddenfield",
                    reference: "requesterIdHf",
                    name: "requesterId",
                    bind: {value: "{record.requesterId}"}
                },
                {
                    xtype: "hiddenfield",
                    name: "statusCode"
                },
                {
                    xtype: "hiddenfield",
                    name: "requesterName"
                },
                {
                    xtype: "hiddenfield",
                    reference: "reviewerIdHf",
                    name: "reviewerId",
                    bind: {value: "{record.reviewerId}"}
                },
                {
                    xtype: "hiddenfield",
                    reference: "bankcardHolderIdHf",
                    name: "bankcardHolderId",
                    bind: {value: "{record.bankcardHolderId}"}
                },
                {
                    xtype: "hiddenfield",
                    reference: "bankcardApprovingOfficialIdHf",
                    name: "bankcardApprovingOfficialId",
                    bind: {value: "{record.bankcardApprovingOfficialId}"}
                },
                {
                    xtype: "displayfield",
                    colspan: 2,
                    reference: "rNum",
                    margin: 0,
                    fieldLabel: "Requisition Number",
                    name: "requisitionNumber",
                    value: "Not Generated Yet",
                    bind: {hidden: '{record.requisitionNumber==""}'}
                },
                {
                    xtype: "combobox",
                    colspan: 2,
                    fieldLabel: "FY",
                    reference: "comboFy",
                    minWidth: 100,
                    forceSelection: true,
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    queryMode: "local",
                    displayField: "fy",
                    valueField: "fy",
                    bind: {store: "{fys}"},
                    name: "fy"
                },
                {
                    xtype: "hiddenfield",
                    reference: "hfOuId",
                    name: "ouId",
                    bind: {value: "{record.ouId}"}
                },
                {
                    xtype: "hiddenfield",
                    reference: "hfDivisionId",
                    name: "divisionId",
                    bind: {value: "{record.divisionId}"}
                },
                {
                    xtype: "combobox",
                    reference: "comboGrp",
                    minWidth: 60,
                    fieldLabel: "Group",
                    emptyText: "Group",
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    displayField: "shortName",
                    forceSelection: true,
                    queryMode: "local",
                    valueField: "groupId",
                    colspan: 2,
                    readOnly: true,
                    name: "groupId",
                    bind: {value: "{record.groupId}"},
                    listeners: {change: "onGroupComboChange"}
                },
                {
                    xtype: "radiogroup",
                    labelWidth: 90,
                    labelAlign: "top",
                    colspan: 2,
                    reference: "requesterRg",
                    margin: 0,
                    maxWidth: 650,
                    padding: "0 0 10 0",
                    afterLabelTextTpl: ['<span style="color:red">&nbsp;*</span>'],
                    fieldLabel: "Who is the Official Requester (must be a Federal Employee)",
                    submitValue: false,
                    allowBlank: false,
                    layout: {type: "vbox", align: "stretch"},
                    items: [
                        {
                            xtype: "radiofield",
                            id: "rfself",
                            reference: "selfRb",
                            margin: 0,
                            name: "whoVal",
                            checked: true,
                            inputValue: "self",
                            bind: {boxLabel: '{loggedInUser.lastName +", " +loggedInUser.firstName}'}
                        },
                        {
                            xtype: "container", // Add a container
                            layout: "hbox", // Use hbox layout
                            align: "stretch",
                            minWidth: 500,
                            flex: 1,
                            items: [
                                {
                                    xtype: "radiofield",
                                    id: "rfother",
                                    reference: "notmeRb",
                                    margin: 0,
                                    name: "whoVal",
                                    boxLabel: "Other NIST Federal Employee",
                                    inputValue: "notme"
                                },
                                {
                                    xtype: "combobox",
                                    flex: 1,
                                    reference: "onBehalfCombo",
                                    padding: "0 0 0 20",
                                    maxWidth: 400,
                                    minWidth: 200,
                                    name: "requesterId",
                                    allowBlank: false,
                                    allowOnlyWhitespace: false,
                                    emptyText: "type staff name here",
                                    anyMatch: true,
                                    displayField: "displayName",
                                    forceSelection: true,
                                    minChars: 2,
                                    queryMode: "local",
                                    queryParam: "filter",
                                    //typeAhead: true,
                                    valueField: "peopleId",
                                    bind: {disabled: "{forMe}", hidden: "{forMe}", store: "{bcEmployees}"},
                                    listeners: {change: "onReqForComboChange"}
                                }
                            ]
                        }
                    ],
                    listeners: {
                        change: "onReqForChange"
                    }
                },
                {
                    xtype: "combobox",
                    id: "crwComboPurchaseType",
                    colspan: 2,
                    fieldLabel: "Purchase Type",
                    afterLabelTextTpl: [
                        '<i class="fa fa-info-circle fa-lg" aria-hidden="true" data-qtip="Click to view IT Purchase Type Description" style="cursor:pointer"  id="purchaseTypeInfoBtn"></i>'
                    ],
                    reference: "comboType",
                    minWidth: 160,
                    forceSelection: true,
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    queryMode: "local",
                    displayField: "name",
                    valueField: "id",
                    bind: {store: "{purchaseTypes}"},
                    listeners: {
                        change: "onTypeChange",
                        afterrender: "onPurchaseTypeAfterRender"
                    },
                    name: "purchaseTypeId"
                },
                {
                    xtype: "combobox",
                    id: "crwComboMcCategory",
                    colspan: 2,
                    flex: 1,
                    fieldLabel: "Mission Critical Category",
                    reference: "comboMcCategory",
                    afterLabelTextTpl: [
                        '<i class="fa fa-info-circle fa-lg" aria-hidden="true" data-qtip="Click to view Mission Critical Category Description" style="cursor:pointer"  id="mcCategoryInfoBtn"></i>'
                    ],
                    minWidth: 600,
                    forceSelection: true,
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    queryMode: "local",
                    displayField: "name",
                    valueField: "id",
                    bind: {store: "{mcCategories}"},
                    listeners: {
                        change: "onMcCategoryChange",
                        afterrender: "onMcCategoryAfterRender"
                    },
                    name: "missionCriticalCategoryId"
                },
                {
                    xtype: "combobox",
                    colspan: 2,
                    hidden: true,
                    maxWidth: 400,
                    fieldLabel:
                        "Will the purchase received by a NIST Associate? <br> (If yes,  use the drop down below to select a  NIST Associate)",
                    name: "requestedForId",
                    submitValue: false,
                    anyMatch: true,
                    displayField: "displayName",
                    forceSelection: true,
                    minChars: 2,
                    queryMode: "local",
                    queryParam: "filter",
                    //typeAhead: true,
                    valueField: "peopleId",
                    bind: {store: "{bcAssociates}"}
                },
                {
                    xtype: "label",
                    colspan: 2,
                    html: '<b>Mission Critical Justification</b><span style="color:red">&nbsp;*</span>'
                },
                {
                    xtype: "textareafield",
                    colspan: 2,
                    reference: "mcJustification",
                    allowOnlyWhitespace: false,
                    allowBlank: false,
                    margin: "0 0 10 0",
                    minHeight: 120,
                    minWidth: 600,
                    maxLength: 2000,
                    name: "missionCriticalJustification",
                    bind: {
                        value: "{record.missionCriticalJustification}"
                    }
                },
                {
                    xtype: "container",
                    layout: "hbox",
                    items: [
                        {
                            xtype: "button",
                            margin: "0 10 0 0",
                            text: "Create",
                            id: "btnCreateReq",
                            formBind: true,
                            listeners: {click: "onSave"}
                        },
                        {
                            xtype: "button",
                            text: "Cancel",
                            listeners: {click: "onCancel"}
                        }
                    ]
                }
            ],
            dockedItems: [
                {
                    xtype: "displayfield",
                    cls: "bcp_notice",
                    dock: "top",
                    padding: "0 0 0 10",
                    fieldLabel: "Disclaimer",
                    value: "<b>If any of the purchased items would be used to process classified information or if they are telecommunications/surveillance/bluetooth IT purchase, the purchase cannot be completed through the bankcard system.</br>"
                }
            ]
        }
    ],
    listeners: {added: "onWindowAdded"}
});
