CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_re_route (
/**
This procedure is created for MB-364 (re-route problem)
created by:     ppg
create date:    10/27/2019
Last modified:  by ppg on 10/31/2019
**/
    p_request_id            IN     INTEGER,
    p_route_type_id         IN     INTEGER,
    p_route_notes           IN     VARCHAR2,
    p_route_by              IN     INTEGER,
    --p_route_status_id       IN     INTEGER,
    p_route_to              IN     INTEGER,
    o_route_id                 OUT INTEGER,
    o_required_permission      OUT INTEGER)
IS
    l_supervisor_yn    VARCHAR2 (50);
    l_route_by_ou_id   INTEGER;
    l_route_to_ou_id   INTEGER;
    l_route_status_id  INTEGER;
    l_route_type_id  INTEGER;
    
    v_error_code      NVARCHAR2 (20);
   v_error_message   NVARCHAR2 (2000);

BEGIN
    o_required_permission := 1;

    -- Make sure to/from are in the same ou.
    SELECT ou_org_id
      INTO l_route_by_ou_id
      FROM nist_user
     WHERE people_id = p_route_by;

    SELECT ou_org_id
      INTO l_route_to_ou_id
      FROM nist_user
     WHERE people_id = p_route_to;

    IF l_route_by_ou_id <> l_route_to_ou_id
    THEN
        o_required_permission := 0;
        o_route_id := -1;
        RETURN;
    END IF;
    
    --get the current status of the request
    SELECT route_status_id, route_type_id
    INTO l_route_status_id, l_route_type_id
    FROM route
    where request_id = p_request_id and route_id = 
        (select max (route_id) from route where request_id = p_request_id);
    
    IF l_route_status_id < 5 OR l_route_status_id > 8 THEN  
        return;
    ELSIF l_route_status_id = 5 THEN
        UPDATE request r
        SET r.REVIEWER_ID = p_route_to
        WHERE request_id = p_request_id;

        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (
            p_request_id,
            9, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            5, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;
        
        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (
            p_request_id,
            l_route_type_id, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            5, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;

    ELSIF l_route_status_id = 6 THEN
        UPDATE request r
        SET r.BANKCARD_APPROVING_OFFICIAL_ID = p_route_to
        WHERE request_id = p_request_id;

        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (
            p_request_id,
            9, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            6, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;
        
        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (
            p_request_id,
            l_route_type_id, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            6, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;
        
    ELSIF l_route_status_id < 9 THEN
        UPDATE request r
        SET r.BANKCARD_HOLDER_ID = p_route_to
        WHERE request_id = p_request_id;
        
        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (p_request_id,
            9, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            l_route_status_id, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;
        
        INSERT INTO route (
            request_id,
            route_type_id,
            route_notes,
            route_by,
            route_date,
            route_status_id,
            route_to)
        VALUES (p_request_id,
            l_route_type_id, --p_route_type_id,
            p_route_notes,
            p_route_by,
            SYSDATE,
            l_route_status_id, --p_route_status_id,
            p_route_to)
        RETURNING route_id
        INTO o_route_id;
    END IF;
    
--this will be replaced by the bc_exception package to log the exceptions in the future.
exception 
    WHEN OTHERS
   THEN
      ROLLBACK;
      v_error_code := SQLCODE;
      v_error_message := SUBSTR (SQLERRM, 1, 2000);

      INSERT INTO db_error (source,
                            user_id,
                            code,
                            MESSAGE)
           VALUES ('sp_re_route',
                   p_route_by,
                   v_error_code,
                   v_error_message);
end;
/

GRANT EXECUTE ON BCPMS_OWNER.SP_RE_ROUTE TO BCPMS_APP;
CREATE OR REPLACE SYNONYM BCPMS_APP.SP_RE_ROUTE FOR BCPMS_OWNER.SP_RE_ROUTE;