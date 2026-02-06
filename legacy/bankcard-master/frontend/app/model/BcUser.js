Ext.define("bcp.model.BcUser", {
	extend: "Ext.data.Model",

	requires: ["Ext.data.field.String"],

	idProperty: "peopleId",

	fields: [
		{ name: "peopleId" },
		{ type: "string", name: "fullName" },
		{ type: "string", name: "staffType" },
		{ type: "string", name: "empEmail" },
		{
			convert: function (v, rec) {
				if ("NIST Associate" === rec.data.staffType) {
					return rec.data.fullName + " (Assoc)";
				} else {
					/*else if(rec.data.staffType==='NIST Employee'){
                return rec.data.fullName + ' (Fed)';
                }*/
					//calling getnistemployee ws won't return stafftype
					if (rec.data.fullName.indexOf("(Fed)") >= 0) {
						return rec.data.fullName;
					} else {
						return rec.data.fullName + " (Fed)";
					}
				}
			},
			name: "displayName"
		},
		{ type: "string", mapping: "active", name: "isActive" },
		{ type: "int", name: "bossId" },
		{ type: "int", mapping: "divisionId", name: "divId" },
		{ type: "int", name: "groupId" },
		{ type: "int", name: "ouId" },
		{
			//only used for checking reviewer when division chief make a request
			name: "roleName"
		}
	]
});
