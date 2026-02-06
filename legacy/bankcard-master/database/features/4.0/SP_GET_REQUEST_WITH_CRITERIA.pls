CREATE OR REPLACE PROCEDURE sp_get_request_with_criteria (
    p_username           IN VARCHAR2,
    p_ou_org_id          IN INTEGER,
    p_div_org_id         IN INTEGER,
    p_grp_org_id         IN INTEGER,
    p_requester_id       IN INTEGER,
    p_requisition_number IN VARCHAR2,
    p_route_type_id      IN INTEGER,
    p_from_date          IN DATE,
    p_to_date            IN DATE,
    p_vendor_name        IN VARCHAR2,
    p_transaction_number IN VARCHAR2,
    p_item_name          IN VARCHAR2,
    p_actual_total       IN DOUBLE PRECISION,
    p_bankcard_holder_id IN INTEGER,
    p_request_id         IN INTEGER,
    p_fy                 IN INTEGER,
    p_ptc                IN VARCHAR2,
    p_stmt_date          IN DATE,
    p_deliver_to_home    IN VARCHAR2,
    p_partial_order      IN VARCHAR2,
    --p_is_it_purchase     IN VARCHAR2,
    p_purchase_type_id   IN INTEGER,
    p_item_statuses      IN VARCHAR2,
    p_reviewer_id        IN INTEGER,
    result_set           OUT SYS_REFCURSOR
) IS

    can_access_ou        VARCHAR2(20);
    can_access_div       VARCHAR2(20);
    can_access_group     VARCHAR2(20);
    user_ou_org_id       INTEGER;
    user_div_org_id      INTEGER;
    user_grp_org_id      INTEGER;
    user_people_id       INTEGER;
    requester_div_org_id INTEGER;
    query_ou_org_id      INTEGER;
    query_div_org_id     INTEGER;
    query_grp_org_id     INTEGER;
    query_requester_id   INTEGER;
    l_detailed_to_ou     INTEGER := 0;
    l_detailed_to_div    INTEGER := 0;
    l_is_admin           NUMBER := is_admin(p_username);
BEGIN

/*
fix an issue where group id is passed in but not used in the search due to a previous change related
to MB-465 that allow everyone to have division access

changed return from v_request to use * instead of column names

10/17/22 added query to check for detailed privilege when user's ou or div is not the same as the query string's ou or div
         changed logic to allow search for self's requests (requester is the user) no matter if the user moved to a different org
3/7/23 added condition to search or item statuses using "OR"; added ability to search for partial order

02/2024 fixed issue related to detailee mode when no division is selected
*/

    -- To be modified in the query.
    query_ou_org_id := p_ou_org_id;
    query_div_org_id := p_div_org_id;
    query_grp_org_id := p_grp_org_id;
    query_requester_id := p_requester_id;

    -- Get privileges.
    SELECT
        access_ou,
        access_div,
        access_group
    INTO
        can_access_ou,
        can_access_div,
        can_access_group
    FROM
        user_privileges
    WHERE
        lower(username) = lower(p_username);

    -- Find the ou/div/grp/people id of user.
    SELECT
        ou_org_id,
        div_org_id,
        grp_org_id,
        people_id
    INTO
        user_ou_org_id,
        user_div_org_id,
        user_grp_org_id,
        user_people_id
    FROM
        nist_user_active --changed from nist_user because only active users can do search
    WHERE
        lower(username) = lower(p_username);

    IF l_is_admin = 1 THEN
        --app admin can access everything 
        query_ou_org_id := p_ou_org_id;
        query_div_org_id := p_div_org_id;
        query_requester_id := p_requester_id;
    ELSE

    -- Limit scope of what user can see based on role and identity.
        IF can_access_ou = 'Y' THEN
            IF
                p_ou_org_id IS NOT NULL
                AND p_ou_org_id <> user_ou_org_id
            THEN
                SELECT
                    COUNT(*)
                INTO l_detailed_to_ou
                FROM
                    user_detailed
                WHERE
                        people_id = user_people_id
                    AND ou_org_id = p_ou_org_id
                    AND access_ou = 'Y'
                    AND ( valid_until_date IS NULL
                          OR trunc(valid_until_date) >= trunc(sysdate) );

                IF l_detailed_to_ou > 0 THEN
                    query_ou_org_id := p_ou_org_id;
                ELSE
                    query_ou_org_id := -1;
                END IF;

            END IF;
   --         query_ou_org_id := -1; -- Prevent from accessing outside of their ou.
   --     ELSE
   --         query_ou_org_id := user_ou_org_id;
   --     END IF;
        ELSIF can_access_div = 'Y' THEN
            IF
                p_div_org_id IS NOT NULL
                AND p_div_org_id <> user_div_org_id
            THEN
                SELECT
                    COUNT(*)
                INTO l_detailed_to_div
                FROM
                    user_detailed
                WHERE
                        people_id = user_people_id
                    AND div_org_id = p_div_org_id
                --02/2024 commented out the line below to fix a defect; it should be access_div
                --and access_ou='Y'
                    AND access_div = 'Y'
                    AND ( valid_until_date IS NULL
                          OR trunc(valid_until_date) >= trunc(sysdate) );

                IF l_detailed_to_div > 0 THEN
                    query_div_org_id := p_div_org_id;
                ELSE
                    query_div_org_id := -1;
                END IF;

            END IF;
        
        
     --       query_div_org_id := -1; -- Prevent from accessing outside of their division.
      --  ELSE
      --      query_div_org_id := user_div_org_id;
     --   END IF;


            IF p_requester_id IS NOT NULL THEN
            --for this one, if a user try to search requests made by self, we should allow it no matter whether the user
            --changed division or not;
                IF p_requester_id = user_people_id THEN
                --if self is the requester, we allow access
                    query_requester_id := p_requester_id;
                ELSE
                --if checking for someone else's request
                --check to make sure the requester passed in is the same division as the logged user.
                    SELECT
                        div_org_id
                    INTO requester_div_org_id
                    FROM
                        nist_user_active
                    WHERE
                        people_id = p_requester_id;

                    IF requester_div_org_id <> user_div_org_id THEN
                        query_requester_id := -1; -- Prevent from accessing somebody else in other division.
                    ELSE
                        query_requester_id := p_requester_id;
                    END IF;

                END IF;

            END IF;
        --MB-465 everyone has division access now so just use p_grp_org_id in query if not null
   -- ELSIF can_access_group = 'Y' THEN
   --     IF p_grp_org_id IS NOT NULL AND p_grp_org_id <> user_grp_org_id THEN
  --          query_grp_org_id := -1; -- Prevent from accessing outside of their group.
   --     ELSE
    --        query_grp_org_id := user_grp_org_id;
    --    END IF;
    
    --ELSE
    --    IF p_requester_id IS NOT NULL AND p_requester_id <> user_people_id THEN
    --        query_requester_id := -1; -- Prevent from accessing somebody else.
    --    ELSE
    --        query_requester_id := user_people_id;
    --    END IF;

        END IF;
    END IF;
    
    
     --MB-465 
    IF p_grp_org_id IS NOT NULL THEN
        query_grp_org_id := p_grp_org_id;
    END IF;
    
    
    --select matched records
    OPEN result_set FOR SELECT
                            *
                        FROM
                            v_request
                        WHERE
                                ou_org_id = nvl(query_ou_org_id, user_ou_org_id) -- 02/2024 changed to user_ou_org_id
                                
                                -- changed to user_div_org_id from div_org_id; can't make it always true if user select no division
                                -- Babk-574 4/14/24 changed back to div_org_id since some BCHs covers multiple division and if they put in just
                                -- a requisition#, it will return nothing; need ti think a way to make both work
                            AND div_org_id = nvl(query_div_org_id, div_org_id)
                            AND grp_org_id = nvl(query_grp_org_id, grp_org_id) --if user selected a group, use that group OR always true
                            AND requester_id = nvl(query_requester_id, requester_id)
                            AND ( lower(p_requisition_number) IS NULL
                                  OR lower(requisition_number) LIKE '%'
                                                                    || lower(p_requisition_number)
                                                                    || '%' )
                            AND route_type_id = nvl(p_route_type_id, route_type_id)
                            AND ( p_from_date IS NULL
                                  OR created_date >= p_from_date )
                            AND ( p_to_date IS NULL
                                  OR created_date <= p_to_date )
                            AND ( p_vendor_name IS NULL
                                  OR upper(vendors) LIKE '%'
                                                         || upper(p_vendor_name)
                                                         || '%' )
                            AND ( p_actual_total IS NULL
                                  OR actual_total_cost = p_actual_total )
                            AND ( p_bankcard_holder_id IS NULL
                                  OR p_bankcard_holder_id = bankcard_holder_id )
                            AND ( p_reviewer_id IS NULL
                                  OR p_reviewer_id = reviewer_id )
                            AND ( p_transaction_number IS NULL
                                  OR lower(p_transaction_number) IN (
                                SELECT
                                    lower(transaction_number)
                                FROM
                                    item
                                WHERE
                                    request_id = v_request.request_id
                            ) )
                            AND
                            --MB-470    
                             ( p_item_name IS NULL
                                  OR regexp_replace(items, '[^a-zA-Z0-9 ]', ' ') LIKE '%'
                                                                                      || p_item_name
                                                                                      || '%' )
                            AND                                                          
                            --search by item statuses
                             ( p_item_statuses IS NULL
                                  OR  
                            --replace made all commas to |, which is like OR with regexp_like; item_statuses contains all unique statuses of items in a request
                            --it matches if any of value(2 or 5) in the p_item_statuses(e.g. '2,5') matches with any part in item_statuses (e.g. '1,3,5')
                                   REGEXP_LIKE ( item_statuses,
                                                   replace(p_item_statuses, ',', '|') ) )
                            AND ( p_ptc IS NULL
                                  OR lower(p_ptc) IN (
                                SELECT
                                    lower(project_task)
                                FROM
                                    item
                                WHERE
                                    request_id = v_request.request_id
                            ) )
                            AND ( p_stmt_date IS NULL
                                  OR p_stmt_date IN (
                                SELECT
                                    statement_date
                                FROM
                                    item
                                WHERE
                                    request_id = v_request.request_id
                            ) )
                            AND ( p_partial_order IS NULL
                                    -- if looking for partial order, the item_statuses should contains '2,3' (2 - ordered, 3 - delivered)
                                  OR ( p_partial_order = 'Y'
                                       AND REGEXP_LIKE ( item_statuses,
                                                         '2,3' ) ) )
                            AND ( p_deliver_to_home IS NULL
                                  OR deliver_to_home = p_deliver_to_home )
                            --AND ( p_is_it_purchase IS NULL
                            --      OR is_it_purchase = p_is_it_purchase )
                            --BANK-566
                            AND ( p_purchase_type_id IS NULL
                                  OR purchase_type_id = p_purchase_type_id )
                            AND ( p_request_id IS NULL
                                  OR p_request_id = request_id )
                            AND ( p_fy IS NULL
                                  OR p_fy = fy );
            
            --TODO: the requester should be able to access his or her requests even if moved to another OU or division
            --so we should use a UNION to get requests by requester_id=user_people_id
            --If we add this, we also need to change app queries for getting single request record to allow the same thing there
            --so they can view the detail request info after selecting from search

END;