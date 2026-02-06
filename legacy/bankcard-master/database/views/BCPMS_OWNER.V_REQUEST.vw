CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST
(
    REQUEST_ID,
    FY,
    NOTES,
    REQUESTER_ID,
    REQUESTER_NAME,
    CREATED_BY,
    CREATED_BY_NAME,
    CREATED_FOR,
    CREATED_FOR_NAME,
    CREATED_DATE,
    IS_SHOPPING_CART,
    REFERENCE_ID,
    UPDATED_BY,
    UPDATED_DATE,
    DELIVER_ADDRESS,
    DELIVER_TO_HOME,
    NEEDED_BY_DATE,
    REVIEWER_ID,
    REVIEWER_NAME,
    BANKCARD_APPROVING_OFFICIAL_ID,
    BAO_NAME,
    BANKCARD_HOLDER_ID,
    BH_NAME,
    VENDORS,
    ITEMS,
    TOTAL_COST,
    ACTUAL_TOTAL_COST,
    REQUISITION_NUMBER,
    ESTIMATED_TIME_OF_ARRIVAL,
    ORDER_NUMBER,
    GSA_SESSION_NUMBER,
    PURCHASE_ORDER_NUMBER,
    SUBMITTED_DATE,
    BCH_COMMENTS,
    APPROVAL_AMOUNT,
    ROUTE_ID,
    ROUTE_NOTES,
    ROUTE_BY,
    ROUTE_BY_NAME,
    ROUTE_TO,
    ROUTE_TO_NAME,
    ROUTE_DATE,
    ROUTE_TYPE_ID,
    ROUTE_TYPE_NAME,
    ROUTE_STATUS_ID,
    ROUTE_STATUS_NAME,
    OU_ORG_ID,
    DIV_ORG_ID,
    GRP_ORG_ID
)
BEQUEATH DEFINER
AS
      SELECT req.request_id,
             req.fy,
             req.notes,
             req.requester_id,
             u.last_name || ', ' || u.first_name
                 AS requester_name,
             req.created_by,
             get_user_name (req.created_by)
                 AS created_by_name,
             req.created_for,
             get_user_name (req.created_for)
                 AS created_for_name,
             req.created_date,
             req.is_shopping_cart,
             req.reference_id,
             req.updated_by,
             req.updated_date,
             req.deliver_address,
             req.deliver_to_home,
             REQ.NEEDED_BY_DATE,
             REQ.REVIEWER_ID,
             get_user_name (req.REVIEWER_ID)
                 AS REVIEWER_Name,
             REQ.BANKCARD_APPROVING_OFFICIAL_ID,
             get_user_name (req.BANKCARD_APPROVING_OFFICIAL_ID)
                 AS BAO_NAME,
             REQ.BANKCARD_HOLDER_ID,
             get_user_name (req.BANKCARD_HOLDER_ID)
                 AS BH_NAME,
             vrv.vendors,
             vri.items,
             get_request_total_cost (req.request_id)
                 AS total_cost,
             get_request_actual_total (req.request_id)
                 AS actual_total_cost,
             req.requisition_number,
             req.estimated_time_of_arrival,
             req.order_number,
             req.gsa_session_number,
             req.purchase_order_number,
             req.submitted_date,
             req.bch_comments,
             req.approval_amount,
             r.route_id,
             r.route_notes,
             r.route_by,
             get_user_name (r.route_by)
                 AS route_by_name,
             r.route_to,
             get_user_name (r.route_to)
                 AS route_to_name,
             r.route_date,
             r.route_type_id,
             lrt.route_type_name,
             r.route_status_id,
             lrs.route_status_name,
             U.OU_ORG_ID,
             U.DIV_ORG_ID,
             U.GRP_ORG_ID
        FROM request          req,
             route            r,
             lkup_route_status lrs,
             lkup_route_type  lrt,
             v_request_vendors vrv,
             v_request_items  vri,
             nist_user        u
       WHERE     r.route_status_id = lrs.route_status_id
             AND r.route_type_id = lrt.route_type_id
             AND req.request_id = r.request_id
             AND req.request_id = vrv.request_id
             AND req.request_id = vri.request_id
             AND req.requester_id = u.people_id
             --AND req.created_by = 23826
             --AND r.route_type_id = 0
             AND r.route_date = (SELECT MAX (route_date)
                                   FROM route
                                  WHERE request_id = req.request_id)
    ORDER BY req.created_date;

CREATE OR REPLACE SYNONYM BCPMS_APP.V_REQUEST FOR BCPMS_OWNER.V_REQUEST;


AUDIT RENAME ON BCPMS_OWNER.V_REQUEST BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.V_REQUEST BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT SELECT ON BCPMS_OWNER.V_REQUEST TO BCPMS_APP;