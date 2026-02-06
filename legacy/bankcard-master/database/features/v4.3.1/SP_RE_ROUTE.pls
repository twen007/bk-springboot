create or replace PROCEDURE             SP_RE_ROUTE (
    /**
    This procedure is created for MB-364 (re-route problem)
    created by:     ppg
    create date:    10/27/2019
    Last modified:  by ppg on 10/31/2019
    updated by Tony on 11/1/2019 after discovering the change would break the pullback function
    03/11/22 this function is doing what we called reassign now. It is different than the dynamic routing's reroute function
    we are also adding a check to see if the request is currently in dynamic routing. If it is, we cannot do the reassignment
    because 1. it will update a approver in the request. if the old approver started the dynamic routing, it would break the logic
    2. this SP will insert a new reassignment route, if dynamic routing is not finished, it will break the pending routes in dynamic routing.
    09/2022  added route_step code 
    **/
    p_request_id            IN     INTEGER,
    p_route_type_id         IN     INTEGER,
    p_route_notes           IN     VARCHAR2,
    p_route_by              IN     INTEGER,
    p_route_to              IN     INTEGER,
    o_route_id                 OUT INTEGER,
    o_required_permission      OUT INTEGER)
IS
    l_supervisor_yn     VARCHAR2 (50);
    l_route_by_ou_id    INTEGER;
    l_route_to_ou_id    INTEGER;
    l_route_status_id   INTEGER;
    l_route_type_id     INTEGER;
    l_route_by          INTEGER;
    l_route_step       INTEGER;

    v_error_code        NVARCHAR2 (20);
    v_error_message     NVARCHAR2 (2000);
BEGIN
    o_required_permission := 1;

    -- Make sure to/from are in the same ou.
    SELECT ou_org_id
      INTO l_route_by_ou_id
      FROM nist_user_active
     WHERE people_id = p_route_by;

    SELECT ou_org_id
      INTO l_route_to_ou_id
      FROM nist_user_active
     WHERE people_id = p_route_to;

    IF l_route_by_ou_id <> l_route_to_ou_id
    THEN
        o_required_permission := 0;
        o_route_id := -1;
        RETURN;
    END IF;

    --get the current status, type and route_by of the request, added route_step+1 09/2022
    SELECT route_status_id, route_type_id, route_by, route_step+1
      INTO l_route_status_id, l_route_type_id, l_route_by, l_route_step
      FROM route
     WHERE route_id = (SELECT current_route
                         FROM request
                        WHERE request_id = p_request_id);

    --if request is one of these statuses, it cannot be reroute
    IF l_route_status_id < 5 OR l_route_status_id in(10,11,12)  --MB461 allows a bch to transfer a request to a different bch, so 8,9,13 are allowed now  l_route_status_id > 8
    THEN
        o_required_permission := 0;
        o_route_id := -1;
        RETURN;
    --request is at the reviewer's hand
    ELSIF l_route_status_id = 5
    THEN
        --update reviewer id in request
        UPDATE request r
           SET r.REVIEWER_ID = p_route_to
         WHERE request_id = p_request_id;
    ELSIF l_route_status_id = 6
    THEN
        --update bao id in request
        UPDATE request r
           SET r.BANKCARD_APPROVING_OFFICIAL_ID = p_route_to
         WHERE request_id = p_request_id;
    ELSIF l_route_status_id = 16
    THEN
        --update fco in the request; issue 677
        UPDATE request r
           SET r.funds_certifying_official_id = p_route_to
         WHERE request_id = p_request_id;
    ELSIF l_route_status_id in (7,8,9,13)
    THEN
        --update bch id in request
        UPDATE request r
           SET r.BANKCARD_HOLDER_ID = p_route_to
         WHERE request_id = p_request_id;
    END IF;

    --route by should be updated because it can be pullback to previous step. The pullback function
    --checks if the user init the pullback is the same person who did the previous route, which is route_by
    --In the previous approach of creating two routes, one for reroute type=9, one for preserve the current route type,
    --it will break the pullback function since it removes the last route and log it in the pullback log table. The previous approach
    --would make the route history to show the reroute only, not sending the request to previous route stage.
    --The new approach is to insert the user (who clicked the reroute button)'s id to the reroute_by column (a new column)
    --and the route_to is the new route_to, type and status should stay the same
    --frontend UI need to change to check for reroute_by and display just a reroute history record
    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to,
                       reroute_by,
                       route_step)
         VALUES (p_request_id,
                 l_route_type_id,                            -- same as before
                 p_route_notes,                            -- the reroute note
                 p_route_by,                           -- update
                 SYSDATE,                                   --when it happened
                 l_route_status_id,                           --same as before
                 p_route_to, --a different person with the same privilige (from a reviewer to another reviewer, for example)
                 p_route_by,                         --who did the reroute
                 l_route_step)                            
      RETURNING route_id
           INTO o_route_id;
           
    --update request with current route
    UPDATE request
       SET current_route = o_route_id
     WHERE request_id = p_request_id;
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
             VALUES ('sp_re_route',
                     p_route_by,
                     v_error_code,
                     v_error_message);
END;