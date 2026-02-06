  CREATE OR REPLACE FORCE VIEW "BCPMS_OWNER"."V_RPT_PROPERTY_CUSTODIAN_ITEMS" ("REQUEST_ID", "FY", "CREATED_DATE", "REQUISITION_NUMBER", "OU_ID", "DIV_ID", "GRP_ID", "OU", "DIVISION", "Group", "Vendor", "ITEM_ID", "CATELOG_NUMBER", "ITEM_NAME", "ITEM_DESCRIPTION", "PRICE", "QUANTITY", "PURPOSE", "IS_CHEMICAL", "SHOPPING_CART_FILE_ID", "ITEM_STATUS", "PROJECT_TASK", "OBJECT_CLASS", "IS_TAGGABLE_EQUIPMENT", "PRICE_ORDERED", "QUANTITY_ORDERED", "ITEM_NOTES", "DATE_RECEIVED", "TRANSACTION_NUMBER", "STATEMENT_DATE", "UNIT_ISSUE") AS 
  SELECT r.REQUEST_ID,
       r.fy,
       r.created_date,
       r.REQUISITION_NUMBER,
       r.ou_id,
       r.div_id,
       r.grp_id,
       get_ou_code_by_org_id(r.OU_ID) as OU,
       get_div_code_by_org_id(r.DIV_ID) as Division,
       get_group_code_by_org_id(r.GRP_ID) as "Group",
       rv.vendor_name,
       i.ITEM_ID,
       i.CATELOG_NUMBER,
       i.ITEM_NAME,
       i.ITEM_DESCRIPTION,
       i.PRICE,
       i.QUANTITY,
       i.PURPOSE,
       i.CHEMICAL                    AS Is_Chemical,
       i.SHOPPING_CART_FILE_ID,
       lis.ITEM_STATUS_TYPE_NAME     AS Item_status,
       i.PROJECT_TASK,
       i.OBJECT_CLASS,
       IS_TAGGABLE_EQUIPMENT,
       i.PRICE_ORDERED,
       i.QUANTITY_ORDERED,
       i.ITEM_NOTES,
       i.DATE_RECEIVED,
       i.TRANSACTION_NUMBER,
       i.STATEMENT_DATE,
       i.UNIT_ISSUE
  FROM item                   i,
       request                r,
       Lkup_item_status_type  lis,
       v_item_current_status  vics,
       request_vendor_t         rv
 WHERE i.request_id = r.request_id
       AND i.item_id = vics.ITEM_ID
       AND vics.ITEM_STATUS_TYPE_ID = lis.ITEM_STATUS_TYPE_ID
       AND r.request_id = rv.request_id(+)
       --AND r.fy = 24
       --AND r.ou_id=13213
       AND get_route_type_by_route_id(r.CURRENT_ROUTE) in (4,6,7)
       AND i.IS_SHIPPING = 'N'
       
 ORDER BY "Group", r.request_id;

CREATE OR REPLACE SYNONYM BCPMS_APP.V_RPT_PROPERTY_CUSTODIAN_ITEMS FOR BCPMS_OWNER.V_RPT_PROPERTY_CUSTODIAN_ITEMS;

  GRANT SELECT ON "BCPMS_OWNER"."V_RPT_PROPERTY_CUSTODIAN_ITEMS" TO "BCPMS_APP";
  GRANT SELECT ON "BCPMS_OWNER"."V_RPT_PROPERTY_CUSTODIAN_ITEMS" TO "BCPMS_READ";