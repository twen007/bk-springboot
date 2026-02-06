CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_item_process(
        p_item_id IN INTEGER,
        p_price IN DOUBLE PRECISION,
        p_quantity IN INTEGER,
        p_price_ordered IN DOUBLE PRECISION,
        p_quantity_ordered IN INTEGER,
        p_item_notes IN VARCHAR2,
        p_date_received IN TIMESTAMP,
        p_item_status_type_id IN INTEGER,
        p_created_by IN INTEGER,
        o_route_type_id OUT INTEGER,
        o_row_count OUT INTEGER)
IS
BEGIN

    o_row_count := 0;

    SELECT
        route_type_id INTO o_route_type_id
    FROM
        route r, item i
    WHERE
        i.item_id = p_item_id
            AND
        i.request_id = r.request_id
            AND
        r.route_date = (SELECT MAX(route_date) FROM route WHERE request_id = r.request_id);
    --x.w. changed to processed or delivered
    IF o_route_type_id = 4 OR o_route_type_id = 6 THEN

        UPDATE
            item
        SET
            price = NVL(p_price, price),
            quantity = NVL(p_quantity, quantity),
            price_ordered = NVL(p_price_ordered, price_ordered),
            quantity_ordered = NVL(p_quantity_ordered, quantity_ordered),
            item_notes = NVL(p_item_notes, item_notes),
            date_received = NVL(p_date_received, date_received)
        WHERE
            item_id = p_item_id;

        o_row_count := sql%rowcount;

        IF p_item_status_type_id IS NOT NULL THEN

            INSERT INTO
                item_status
                    (
                        item_id,
                        item_status_type_id,
                        created_by,
                        created_date
                    )
                VALUES
                    (
                        p_item_id,
                        p_item_status_type_id,
                        p_created_by,
                        SYSDATE
                    );
        END IF;

    END IF;

END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_UPDATE_ITEM_PROCESS FOR BCPMS_OWNER.SP_UPDATE_ITEM_PROCESS;


AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_ITEM_PROCESS BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_ITEM_PROCESS BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_UPDATE_ITEM_PROCESS TO BCPMS_APP;