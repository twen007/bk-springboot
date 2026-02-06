/* Formatted on 5/9/2019 12:00:53 PM (QP5 v5.313) */
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_insert_route (
    p_request_id            IN     INTEGER,
    p_route_type_id         IN     INTEGER,
    p_route_notes           IN     VARCHAR2,
    p_route_by              IN     INTEGER,
    p_route_status_id       IN     INTEGER,
    p_route_to              IN     INTEGER,
    o_route_id                 OUT INTEGER,
    o_required_permission      OUT INTEGER)
IS
    l_supervisor_yn    VARCHAR2 (50);
    l_route_by_ou_id   INTEGER;
    l_route_to_ou_id   INTEGER;
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

    ---05/24/2018, per request from casara since some divisions had lots of problem with checking the supervisor role for reviewer
    --we made the check always returning 'Y' for now until we find a better solution
    IF p_route_status_id = 5
    THEN                                         -- Check for supervisor role.
        SELECT 'Y'
          INTO l_supervisor_yn
          --supervisor_yn INTO l_supervisor_yn
          FROM nist_user
         WHERE people_id = p_route_to;

        IF l_supervisor_yn = 'N'
        THEN
            o_required_permission := 0;
            o_route_id := -1;
            RETURN;
        END IF;
    END IF;

    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to)
         VALUES (p_request_id,
                 p_route_type_id,
                 p_route_notes,
                 p_route_by,
                 SYSDATE,
                 p_route_status_id,
                 p_route_to)
      RETURNING route_id
           INTO o_route_id;

    IF p_route_status_id = 8
    THEN
        FOR i IN (SELECT item_id
                    FROM item
                   WHERE request_id = p_request_id)
        LOOP
            INSERT INTO item_status (item_id,
                                     item_status_type_id,
                                     created_by,
                                     created_date)
                 VALUES (i.item_id,
                         2,
                         p_route_by,
                         SYSDATE);
        END LOOP;
    END IF;

    -- Request is being submitted.
    IF p_route_type_id = 1
    THEN
        UPDATE request
           SET submitted_date = SYSDATE
         WHERE request_id = p_request_id;
    END IF;
-- Request is being approved, store the approved amount in the request which will be used to determine whether the request
--needs to go through another round of approval when the total price changes.
/**don't need this anymore since approval amount is now updated on the route panel when the BAO approves the request
IF p_route_type_id = 3 THEN
    UPDATE
        request
    SET
        approval_amount = get_request_total_cost (p_request_id)
    WHERE
        request_id = p_request_id;
END IF;
**/

END;
/