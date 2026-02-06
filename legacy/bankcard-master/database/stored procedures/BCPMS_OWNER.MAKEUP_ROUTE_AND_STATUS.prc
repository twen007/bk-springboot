CREATE OR REPLACE PROCEDURE BCPMS_OWNER.MAKEUP_route_and_status (
   p_created_by    INT)
IS
   CURSOR v_request_cursor
   IS
      SELECT request_id, requester_id
        FROM request
       WHERE request_id NOT IN (SELECT request_id FROM route);

   v_request_row     v_request_cursor%ROWTYPE;

   CURSOR v_item_cursor
   IS
      SELECT item_id, requester_id
        FROM request r, item i
       WHERE     r.request_id = i.request_id
             AND i.item_id NOT IN (SELECT item_id FROM item_status);

   v_item_row        v_item_cursor%ROWTYPE;

   v_error_code      NVARCHAR2 (20);
   v_error_message   NVARCHAR2 (2000);
BEGIN
   --request table
   OPEN v_request_cursor;

   LOOP
      FETCH v_request_cursor INTO v_request_row;

      EXIT WHEN v_request_cursor%NOTFOUND;

      --insert a saved route into route table if request has no route due to copy function
      INSERT INTO ROUTE (REQUEST_ID,
                         ROUTE_TYPE_ID,
                         ROUTE_BY,
                         ROUTE_DATE,
                         ROUTE_STATUS_ID,
                         ROUTE_TO)
           VALUES (v_request_row.request_id,
                   0,
                   v_request_row.requester_id,
                   SYSDATE,
                   1,
                   v_request_row.requester_id);
   END LOOP;

   CLOSE v_request_cursor;


   --item table
   OPEN v_item_cursor;

   LOOP
      FETCH v_item_cursor INTO v_item_row;

      EXIT WHEN v_item_cursor%NOTFOUND;

      INSERT INTO ITEM_STATUS (ITEM_ID,
                               ITEM_STATUS_TYPE_ID,
                               CREATED_BY,
                               CREATED_DATE)
           VALUES (v_item_row.item_id,
                   1,
                   p_created_by,
                  -- v_item_row.requester_id,
                   SYSDATE);
   END LOOP;

   CLOSE v_item_cursor;

   COMMIT;
EXCEPTION
   WHEN OTHERS
   THEN
      ROLLBACK;
      v_error_code := SQLCODE;
      v_error_message := SUBSTR (SQLERRM, 1, 2000);

      INSERT INTO db_error (source,
                            user_id,
                            code,
                            MESSAGE)
           VALUES ('makeup_route_and_status',
                   p_created_by,
                   v_error_code,
                   v_error_message);
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.MAKEUP_ROUTE_AND_STATUS FOR BCPMS_OWNER.MAKEUP_ROUTE_AND_STATUS;


AUDIT RENAME ON BCPMS_OWNER.MAKEUP_ROUTE_AND_STATUS BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.MAKEUP_ROUTE_AND_STATUS BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.MAKEUP_ROUTE_AND_STATUS TO BCPMS_APP;