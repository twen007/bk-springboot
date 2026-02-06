CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_delete_request(
        p_people_id IN INTEGER,
        p_request_id IN INTEGER,
        o_permission_state OUT INTEGER,
        o_row_count OUT INTEGER)
IS

    l_created_by_id INTEGER;
    l_route_type_id INTEGER;
    l_route_status_id INTEGER;

BEGIN

    o_permission_state := 0;
    o_row_count := 0;

    SELECT
        created_by INTO l_created_by_id
    FROM
        request
    WHERE
        request_id = p_request_id;

    IF p_people_id <> l_created_by_id THEN
        o_permission_state := 1; -- Created by is not the person deleting.
        RETURN;
    END IF;

    SELECT
        route_type_id,
        route_status_id
    INTO
        l_route_type_id,
        l_route_status_id
    FROM
        route
    WHERE
        request_id = p_request_id
            AND
        route_date = (SELECT MAX(route_date) FROM route WHERE request_id = p_request_id);

    IF l_route_type_id <> 0 OR (l_route_status_id <> 1 AND l_route_status_id <> 12) THEN
        o_permission_state := 2; -- Not in correct state for deleting.
        RETURN;
    END IF;

    DELETE FROM
        request
    WHERE
        request_id = p_request_id;

    o_row_count := sql%rowcount;

END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_DELETE_REQUEST FOR BCPMS_OWNER.SP_DELETE_REQUEST;


AUDIT RENAME ON BCPMS_OWNER.SP_DELETE_REQUEST BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_DELETE_REQUEST BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_DELETE_REQUEST TO BCPMS_APP;