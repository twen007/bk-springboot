CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_insert_item_status_types(
        p_item_ids IN array_int,
        p_request_id IN INTEGER,
        p_created_by IN INTEGER,
        p_status_type_id IN INTEGER)
IS

    l_any_row_found INTEGER;

BEGIN

    FOR i IN 1 .. p_item_ids.COUNT LOOP

        SELECT
            COUNT(*) INTO l_any_row_found
        FROM
            item
        WHERE
            rownum = 1
                AND
            item_id = p_item_ids(i)
                AND
            request_id = p_request_id;

        IF l_any_row_found = 1 THEN
            INSERT INTO item_status
                (
                    item_id,
                    item_status_type_id,
                    created_by,
                    created_date
                )
            VALUES
                (
                    p_item_ids(i),
                    p_status_type_id,
                    p_created_by,
                    SYSDATE
                );
        END IF;

    END LOOP;

END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_INSERT_ITEM_STATUS_TYPES FOR BCPMS_OWNER.SP_INSERT_ITEM_STATUS_TYPES;


AUDIT RENAME ON BCPMS_OWNER.SP_INSERT_ITEM_STATUS_TYPES BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_INSERT_ITEM_STATUS_TYPES BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_INSERT_ITEM_STATUS_TYPES TO BCPMS_APP;