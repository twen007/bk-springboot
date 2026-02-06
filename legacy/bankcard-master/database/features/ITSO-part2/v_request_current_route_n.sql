CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST_CURRENT_ROUTE_N
--added route step and used the new current_route column in request
(
    REQUEST_ID,
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
    ROUTE_TO,
    IS_DYNAMIC,
    IS_DYNAMIC_REROUTE,
    REROUTE_STACK,
    ROUTE_STEP,
    DYNAMIC_TYPE
)
BEQUEATH DEFINER
AS
    SELECT a.REQUEST_ID,
           a.ROUTE_ID,
           a.ROUTE_TYPE_ID,
           (SELECT ROUTE_TYPE_NAME
              FROM LKUP_ROUTE_TYPE
             WHERE ROUTE_type_ID = a.ROUTE_TYPE_ID)
               AS ROUTE_TYPE_NAME,
           a.ROUTE_NOTES,
           get_user_name (a.ROUTE_BY)
               AS route_by_name,
           a.ROUTE_BY,
           a.ROUTE_DATE,
           a.ROUTE_STATUS_ID,
           (SELECT ROUTE_STATUS_NAME
              FROM LKUP_ROUTE_STATUS
             WHERE ROUTE_STATUS_ID = a.ROUTE_STATUS_ID)
               AS ROUTE_STATUS_NAME,
           get_user_name (a.ROUTE_TO)
               AS route_to_name,
           a.ROUTE_TO,
           a.IS_DYNAMIC,
           a.IS_DYNAMIC_REROUTE,
           a.REROUTE_STACK,
           a.ROUTE_STEP,
           a.DYNAMIC_TYPE
      FROM BCPMS_OWNER.ROUTE a, request r
     WHERE a.request_id = r.request_id AND a.route_id = r.CURRENT_ROUTE;