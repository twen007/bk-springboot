CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_insert_route_n (
    /**
    This SP is to insert a route normally (a fixed route defined in lk_route_type)
    09/2022 add route step, update current_route in request
    **/
    p_request_id            IN     INTEGER,
    p_route_type_id         IN     INTEGER,
    p_route_notes           IN     VARCHAR2,
    p_route_by              IN     INTEGER,
    p_route_status_id       IN     INTEGER,
    p_route_to              IN     INTEGER,
    p_is_dynamic            IN     INTEGER,
    p_is_dynamic_reroute    IN     INTEGER,
    p_reroute_stack         IN     INTEGER,
    p_also_notify           IN     VARCHAR2,
    p_route_step            IN     INTEGER,
    o_route_id                 OUT INTEGER,
    o_required_permission      OUT INTEGER)
IS
    l_supervisor_yn    VARCHAR2 (50);
    l_route_by_ou_id   INTEGER;
    l_route_to_ou_id   INTEGER;
    l_route_step       INTEGER;
    l_dynamic_type     VARCHAR2 (10);
    l_itso_approved    INTEGER := 0;
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

    --get needed data from current route of the request
    --first check if any route exists for the request
    SELECT COUNT (*)
      INTO l_route_step
      FROM route
     WHERE request_id = p_request_id;

    IF l_route_step = 0
    THEN
        --no route found, this route is the first
        l_route_step := 1;
    --select nvl(max(route_step),0)+1 into l_route_step from route where request_id = p_request_id;
    ELSE
        --find out current route's route step and increment it by 1 for this new route
        --NOTE: this assumes the current_route is correctly updated in the request record; if not, it will return no data found error
        SELECT route_step, dynamic_type
          INTO l_route_step, l_dynamic_type
          FROM route
         WHERE route_id = (SELECT current_route
                             FROM request
                            WHERE request_id = p_request_id);
                            
        --increment route step by 1
        l_route_step := l_route_step + 1;      
    END IF;
    
    --if this is a reject or return for info or route back, we need to clean out any not executed planned routes first before doing the insert
    --because the request is going back to the requester or a previous fixed route and will skip the planned routes(dynamic)
    if p_route_type_id = 5 or p_route_type_id = 13 or p_route_notes like '%routed back the request to a previous stage%'
    then
        delete from route where request_id=p_request_id and route_step>(l_route_step-1);      
    end if;
    

    --insert the new route
    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to,
                       is_dynamic,
                       is_dynamic_reroute,
                       reroute_stack,
                       also_notify,
                       route_step)
         VALUES (p_request_id,
                 p_route_type_id,
                 p_route_notes,
                 p_route_by,
                 SYSDATE,
                 p_route_status_id,
                 p_route_to,
                 p_is_dynamic,
                 p_is_dynamic_reroute,
                 p_reroute_stack,
                 p_also_notify,
                 l_route_step)
      RETURNING route_id
           INTO o_route_id;
           
    --check if current route is ITSO before insert a new route
    if l_dynamic_type = 'ITSO' then
        -- if ITSO didn't reject or return the request, it means it's approved (app only allows approve, reject or return)
        if p_route_type_id <> 5 and p_route_type_id <> 13 then
            l_itso_approved:=o_route_id;
        end if;
    end if;

    --if request ordered, update all item statues to ordered
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

    --update request with current route
    UPDATE request
       SET current_route = o_route_id
     WHERE request_id = p_request_id;
     
    --if ITSO approved, update request
    if l_itso_approved <>0 then
         UPDATE request
           SET itso_approved = l_itso_approved
         WHERE request_id = p_request_id;
    end if;

    -- Request is being submitted.
    IF p_route_type_id = 1
    THEN
        UPDATE request
           SET submitted_date = SYSDATE
         WHERE request_id = p_request_id;
    END IF;

END;
/