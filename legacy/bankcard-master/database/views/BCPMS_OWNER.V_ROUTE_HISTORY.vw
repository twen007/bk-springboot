/* Formatted on 9/6/2018 2:14:06 PM (QP5 v5.318) */
CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_ROUTE_HISTORY
(
    REQUEST_ID,
    REQUESTER_NAME,
    REQUESTER_ID,
    ROUTE_ID,
    ROUTE_TYPE_ID,
    ROUTE_TYPE_NAME,
    ROUTE_NOTES,
    ROUTE_BY_NAME,
    ROUTE_BY,
    ROUTE_DATE,
    ROUTE_STATUS_ID,
    ROUTE_STATUS_NAME,
    ROUTE_TO_NAME,
    ROUTE_TO
)
BEQUEATH DEFINER
AS
      SELECT a.REQUEST_ID,
             get_user_name (b.requester_id) AS requester_name,
             b.requester_id               AS requester_id,
             a.ROUTE_ID,
             a.ROUTE_TYPE_ID,
             d.ROUTE_TYPE_NAME,
             a.ROUTE_NOTES,
             get_user_name (a.ROUTE_BY)   AS route_by_name,
             a.ROUTE_BY,
             a.ROUTE_DATE,
             a.ROUTE_STATUS_ID,
             c.ROUTE_STATUS_NAME,
             get_user_name (a.ROUTE_TO)   AS route_to_name,
             a.ROUTE_TO
        FROM BCPMS_OWNER.ROUTE a,
             request          b,
             lkup_route_status c,
             lkup_route_type  d
       WHERE     a.request_id = b.request_id
             AND a.ROUTE_STATUS_ID = c.ROUTE_STATUS_ID
             AND a.ROUTE_TYPE_ID = d.ROUTE_TYPE_ID
    ORDER BY a.request_id, a.route_id;


AUDIT RENAME ON BCPMS_OWNER.V_ROUTE_HISTORY BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.V_ROUTE_HISTORY BY ACCESS WHENEVER NOT SUCCESSFUL;