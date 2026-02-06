CREATE OR REPLACE FUNCTION BCPMS_OWNER.get_request_total_cost (p_request_id in INTEGER)
return NUMBER
IS
total_cost NUMBER(10,2);
begin
   SELECT 
 sum(price*quantity) into total_cost
FROM BCPMS_OWNER.ITEM
where request_id= p_request_id;
return total_cost;
end;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.GET_REQUEST_TOTAL_COST FOR BCPMS_OWNER.GET_REQUEST_TOTAL_COST;


GRANT EXECUTE ON BCPMS_OWNER.GET_REQUEST_TOTAL_COST TO BCPMS_APP;