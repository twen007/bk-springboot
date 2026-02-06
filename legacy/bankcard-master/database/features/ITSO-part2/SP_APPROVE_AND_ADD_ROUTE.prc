CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_approve_and_add_route (
    /**
    This SP is for performing dynamic approve and add a route OR approve and add a ITSO route
    it will add one route record before the next step if any. if there's a next step A->B, this action will add a route record from A -> X, 
    which has a step before the original next route; after X approves it, it will go to B
    So, get the current route step, add +1 for any planned route with steps>current step, then insert a new route, and finally
    update current route to the inserted route in the request"
    **/
    p_request_id    IN     INTEGER,
    --p_route_type_id         IN     INTEGER, no need, route type stay the same
    p_route_notes   IN     VARCHAR2,
    p_route_by      IN     INTEGER,                      -- get it from server
    --p_route_status_id       IN     INTEGER, no need, route status stay the same
    p_route_to      IN     INTEGER,
    p_dynamic_type  IN     VARCHAR2, --AA or ITSO 
    --p_is_dynamic            IN     INTEGER, --no need, it should be 1
    --p_is_dynamic_reroute    IN     INTEGER, --no need, it should be 1
    --p_reroute_stack         IN     INTEGER, --no need
    --p_also_notify           IN     VARCHAR2, --no need
    --p_route_step            IN     INTEGER, --no need, get it from current route in a select
    o_route_id         OUT INTEGER,     --should be the new insert's route id
    o_required_permission      OUT INTEGER  --no need to check permission for dynamic routes for now but reserve it 
                                  )
IS
    l_route_type_id     INTEGER;
    l_route_status_id   INTEGER;
    l_route_step        INTEGER;
BEGIN
    o_required_permission := 1;
    --get needed data from current route of the request
    SELECT route_step, route_type_id, route_status_id
      INTO l_route_step, l_route_type_id, l_route_status_id
      FROM route
     WHERE route_id = (SELECT current_route
                         FROM request
                        WHERE request_id = p_request_id);

    --update any existing route steps to make space for the new route
    UPDATE route
       SET route_step = route_step + 1
     WHERE request_id = p_request_id AND route_step > l_route_step;



    --insert the new route before any not executed route
    INSERT INTO route (request_id,
                       route_type_id,
                       route_notes,
                       route_by,
                       route_date,
                       route_status_id,
                       route_to,
                       is_dynamic,
                       dynamic_type,
                       route_step)
         VALUES (p_request_id,
                 l_route_type_id,
                 p_route_notes,
                 p_route_by,
                 SYSDATE,
                 l_route_status_id,
                 p_route_to,
                 1,
                 p_dynamic_type,
                 l_route_step + 1)
      RETURNING route_id
           INTO o_route_id;

    --update request with current route
    UPDATE request
       SET current_route = o_route_id
     WHERE request_id = p_request_id;
END;
/