CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_insert_route_n (
    /**
    This SP is to insert a route normally (a fixed route defined in lk_route_type)
    09/2022 add route step, update current_route in request
    
    10/6/22 add more exception handling, return -1 when current_route or route_step is null
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
    l_current_route INTEGER;
    v_error_code        NVARCHAR2 (20);
    v_error_message     NVARCHAR2 (2000);
    l_detailed_to_ou     INTEGER :=0;
    l_detailed_to_div    INTEGER :=0;
BEGIN
    o_required_permission := 1;

    -- Make sure to/from are in the same ou.
    
    --10/17/22 since detailed user from a different ou can access and approve requests
    --we cannot assume the route_from's ou is the request's ou, so change the query to get request's ou
    select ou_id INTO l_route_by_ou_id
      from request where request_id=p_request_id;

    SELECT ou_org_id
      INTO l_route_to_ou_id
      FROM nist_user
     WHERE people_id = p_route_to;

    IF l_route_by_ou_id <> l_route_to_ou_id
    THEN
        select count(*) into l_detailed_to_ou from user_detailed where 
                people_id= p_route_to --check if the route_to
                and ou_org_id =l_route_by_ou_id --was granted access to the ou of request
                and access_ou='Y'
                and (valid_until_date is null or trunc(valid_until_date) >= trunc(sysdate));
                
            if  l_detailed_to_ou = 0
            then
                o_required_permission := -1;
                o_route_id := -1;
                RETURN;
            end if;
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
        SELECT current_route into l_current_route
                             FROM request
                            WHERE request_id = p_request_id;
                            
        if l_current_route is null
        then
        o_route_id := -1;
            RETURN;
        end if;
        
        --NOTE: this assumes the current_route is correctly updated in the request record; if not, it will return no data found error
        SELECT route_step, dynamic_type
          INTO l_route_step, l_dynamic_type
          FROM route
         WHERE route_id = l_current_route;
        
        --if a route exists without a step, we cannot do the insert
        if l_route_step is null
        then
        o_route_id := -1;
            RETURN;
        end if;
        
        --increment route step by 1
        l_route_step := l_route_step + 1;
    END IF;

    --if this is a reject or return for info or route back, we need to clean out any not executed planned routes first before doing the insert
    --because the request is going back to the requester or a previous fixed route and will skip the planned routes(dynamic)
    IF    p_route_type_id = 5
       OR p_route_type_id = 13
       OR p_route_notes LIKE '%routed back the request to a previous stage%'
    THEN
        DELETE FROM
            route
              WHERE     request_id = p_request_id
                    AND route_step > (l_route_step - 1);
    END IF;


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
    IF l_dynamic_type = 'ITSO'
    THEN
        -- if ITSO didn't reject or return the request, it means it's approved (app only allows approve, reject or return)
        IF p_route_type_id <> 5 AND p_route_type_id <> 13
        THEN
            l_itso_approved := o_route_id;
        END IF;
    END IF;

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
    IF l_itso_approved <> 0
    THEN
        UPDATE request
           SET itso_approved = l_itso_approved
         WHERE request_id = p_request_id;
    END IF;

    -- Request is being submitted.
    IF p_route_type_id = 1
    THEN
        UPDATE request
           SET submitted_date = SYSDATE
         WHERE request_id = p_request_id;
    END IF;

EXCEPTION
    WHEN OTHERS
    THEN
        v_error_code := SQLCODE;
        v_error_message := SUBSTR (SQLERRM, 1, 2000);

        INSERT INTO db_error (source,
                              user_id,
                              code,
                              MESSAGE)
             VALUES ('sp_insert_route_n',
                     p_route_by,
                     v_error_code,
                     v_error_message);

END;
/