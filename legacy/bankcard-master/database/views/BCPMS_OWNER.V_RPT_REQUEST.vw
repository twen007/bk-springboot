/* Formatted on 9/6/2018 2:14:06 PM (QP5 v5.318) */
CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_RPT_REQUEST
(
    REQUEST_ID,
    NOTES,
    REQUESTER_ID,
    REQUESTER_NAME,
    CREATED_FOR,
    REQUEST_CREATED_FOR_NAME,
    CREATED_BY,
    REQUEST_CREATED_BY_NAME,
    REQUEST_CREATED_DATE,
    REQUEST_UPDATED_BY,
    REQUEST_UPDATED_BY_NAME,
    REQUEST_UPDATED_DATE,
    IS_SHOPPING_CART,
    REFERENCE_ID,
    DELIVER_ADDRESS,
    REQUISITION_NUMBER,
    ROUTE_ID,
    LATEST_ROUTE_TYPE_ID,
    LATEST_ROUTE_TYPE_NAME,
    LATEST_ROUTE_DATE,
    LATEST_ROUTE_BY_ID,
    LATEST_ROUTE_BY_NAME,
    LATEST_ROUTE_TO_ID,
    LATEST_ROUT_TO_NAME,
    VENDOR_ID,
    VENDOR_NAME,
    VENDOR_ADDRESS,
    ACCOUNT_NUMBER,
    ITEM_NAME,
    PRICE,
    PRICE_ORDERED,
    QUANTITY,
    QUANTITY_ORDERED,
    IS_SHIPPING
)
BEQUEATH DEFINER
AS
    SELECT                                                     -- from request
           r.request_id,
           r.notes,
           r.requester_id,
           get_user_name (r.requester_id)
               AS requester_name,
           r.created_for,
           get_user_name (r.created_for)
               AS request_created_for_name,
           r.created_by,
           get_user_name (r.created_by)
               AS request_created_by_name,
           r.created_date
               request_created_date,
           r.updated_by
               request_updated_by,
           get_user_name (r.updated_by)
               AS request_updated_by_name,
           r.updated_date
               request_updated_date,
           r.is_shopping_cart,
           r.reference_id,
           r.deliver_address,
           r.requisition_number,
           -- from route
           rt.route_id,
           rt.route_type_id
               AS latest_route_type_id,
           lprt.route_type_name
               AS latest_route_type_name,
           rt.route_date
               AS latest_route_date,
           rt.route_by
               AS latest_route_by_id,
           get_user_name (rt.route_by)
               AS latest_route_by_name,
           rt.route_to
               AS latest_route_to_id,
           get_user_name (rt.route_to)
               AS latest_rout_to_name,
           -- from vendor
           v.vendor_id,
           v.VENDOR_NAME,
           v.street || ', ' || v.city || ', ' || v.state || ', ' || v.zip
               AS vendor_address,
           v.account_number,
           -- from item
           i.ITEM_NAME,
           i.price,
           i.price_ordered,
           i.quantity,
           i.quantity_ordered,
           i.is_shipping
      /*
          ,
          ist.item_status_id as last_item_status_id,
          lpist.item_status_type_name as last_item_status_name
      */
      FROM request  r
           INNER JOIN route rt
               ON     r.request_id = rt.request_id
                  AND rt.route_id = (SELECT MAX (route_id)
                                       FROM route
                                      WHERE request_id = r.request_id)
           INNER JOIN lkup_route_type lprt
               ON rt.route_type_id = lprt.route_type_id
           LEFT OUTER JOIN request_vendor rv ON r.request_id = rv.request_id
           LEFT OUTER JOIN vendor v ON rv.vendor_id = v.vendor_id
           LEFT OUTER JOIN item i ON r.request_id = i.request_id
           LEFT OUTER JOIN (  SELECT item_id, MAX (item_status_id)
                                FROM item_status
                            GROUP BY item_id) ist
               ON i.item_id = ist.item_id
--left outer join item_status istt on ist.item_status_id = istt.item_status_id and
--left outer join lkup_item_status_type lpist on is.item_status_type_id = lpist.item_status_type_id
;


CREATE OR REPLACE SYNONYM BCPMS_APP.V_RPT_REQUEST FOR BCPMS_OWNER.V_RPT_REQUEST;


AUDIT RENAME ON BCPMS_OWNER.V_RPT_REQUEST BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.V_RPT_REQUEST BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT SELECT ON BCPMS_OWNER.V_RPT_REQUEST TO BCPMS_APP;