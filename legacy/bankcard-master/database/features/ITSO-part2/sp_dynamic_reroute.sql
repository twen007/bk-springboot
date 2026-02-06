CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_dynamic_reroute (
    /**
    This SP is for performing dynamic reroute
    it will add two route record, one for the reroute itself from A -> B, one for a planned return route, which after B approves it, it will have
    a route date and go back to A.
    So, get the current route step, add +2 for any planned route with steps>current step, then insert the two routes, and finally
    update current route to first inserted route in the request"
    **/
    p_request_id    IN     INTEGER,
    --p_route_type_id         IN     INTEGER, no need, route type stay the same
    p_route_notes   IN     VARCHAR2,
    p_route_by      IN     INTEGER,                      -- get it from server
    --p_route_status_id       IN     INTEGER, no need, route status stay the same
    p_route_to      IN     INTEGER,
    p_dynamic_type  IN     VARCHAR2, --not actually needed but try to make same number of params for both DR and AA
    --p_is_dynamic            IN     INTEGER, --no need, it should be 1
    --p_is_dynamic_reroute    IN     INTEGER, --no need, it should be 1
    --p_reroute_stack         IN     INTEGER, --no need
    --p_also_notify           IN     VARCHAR2, --no need
    --p_route_step            IN     INTEGER, --no need, get it from current route in a select
    o_route_id         OUT INTEGER,     --should be the first insert's route id
    o_required_permission      OUT INTEGER  --no need to check permission for dynamic routes but reserves it
                                  )
IS
    l_route_type_id     INTEGER;
    l_route_status_id   INTEGER;
    l_route_step        INTEGER;
    l_is_dynamic        INTEGER;
BEGIN
    o_required_permission := 1;
    --get needed data from current route of the request
    SELECT route_step, route_type_id, route_status_id, is_dynamic
      INTO l_route_step, l_route_type_id, l_route_status_id, l_is_dynamic
      FROM route
     WHERE route_id = (SELECT current_route
                         FROM request
                        WHERE request_id = p_request_id);

    --update any existing route steps after the current step(increment by 2 since for DR, we need to insert two routes)
    -- to make space for the new routes
    UPDATE route
       SET route_step = route_step + 2
     WHERE request_id = p_request_id AND route_step > l_route_step;



    --insert the first new route
    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to,
                       is_dynamic,
                       is_dynamic_reroute,
                       route_step,
                       dynamic_type)
         VALUES (p_request_id,
                 l_route_type_id,
                 p_route_notes,
                 p_route_by,
                 SYSDATE,
                 l_route_status_id,
                 p_route_to,
                 1,
                 1,
                 l_route_step + 1,
                 p_dynamic_type) --'DR' or 'ITSO'
      RETURNING route_id
           INTO o_route_id;

    --insert the second new route, which is the returning route back to who init the reroute
    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to,
                       is_dynamic,
                       is_dynamic_reroute,
                       route_step)
         VALUES (p_request_id,
                 l_route_type_id,
                 NULL,       --a planned route has no note 
                 p_route_to, --this is the person receive the reroute, who will route back to the init person after click the approval button
                 NULL,       --a planned route has no excution date
                 l_route_status_id,
                 p_route_by, --this is the person init the reroute, who will be the route_to for the planned "return route"
                 l_is_dynamic,     --if this planned route is DRed from a fixed route, then it should be 0. if it's a DR from a dynamic route, it is 1
                 0,     --when update a planned route, we change this to 1 so in case when is_dynamic =0, we know this route is a response to a DRed route
                 l_route_step + 2 --the planned route's route_step is 1 higher than the first route above
                 );


    --update request with current route
    UPDATE request
       SET current_route = o_route_id
     WHERE request_id = p_request_id;
END;
/