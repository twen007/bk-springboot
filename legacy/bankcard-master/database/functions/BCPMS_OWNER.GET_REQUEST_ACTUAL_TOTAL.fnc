CREATE OR REPLACE FUNCTION BCPMS_OWNER.get_request_actual_total (
   p_request_id   IN INTEGER)
   RETURN NUMBER
IS
   actual_total_cost   NUMBER (10, 2);
BEGIN
   SELECT SUM (price_ordered * quantity_ordered)
     INTO actual_total_cost
     FROM BCPMS_OWNER.ITEM
    WHERE request_id = p_request_id;

   RETURN actual_total_cost;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.GET_REQUEST_ACTUAL_TOTAL FOR BCPMS_OWNER.GET_REQUEST_ACTUAL_TOTAL;


GRANT EXECUTE ON BCPMS_OWNER.GET_REQUEST_ACTUAL_TOTAL TO BCPMS_APP;