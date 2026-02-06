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
BEGIN

/*
fix an issue where group id is passed in but not used in the search due to a previous change related
to MB-465 that allow everyone to have division access
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
        nist_user
    WHERE
        lower(username) = lower(p_username);

    -- Limit scope of what user can see based on role and identity.
    IF can_access_ou = 'Y' THEN
        IF
            p_ou_org_id IS NOT NULL
            AND p_ou_org_id <> user_ou_org_id
        THEN
            query_ou_org_id := -1; -- Prevent from accessing outside of their ou.
        ELSE
            query_ou_org_id := user_ou_org_id;
        END IF;
    ELSIF can_access_div = 'Y' THEN
        IF p_div_org_id IS NOT NULL
            AND p_div_org_id <> user_div_org_id
        THEN
            query_div_org_id := -1; -- Prevent from accessing outside of their division.
        ELSE
            query_div_org_id := user_div_org_id;
        END IF;
        
         --check to make sure the requester passed in is the same division as the logged user.
        IF p_requester_id IS NOT NULL THEN
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
    
     --MB-465 
    IF p_grp_org_id IS NOT NULL THEN
        query_grp_org_id := p_grp_org_id;
    END IF;
    OPEN result_set FOR SELECT
                            request_id,
                            fy,
                            notes,
                            requester_id,
                            requester_name,
                            created_by,
                            created_by_name,
                            created_for,
                            created_for_name,
                            created_date,
                            is_shopping_cart,
                            reference_id,
                            updated_by,
                            updated_date,
                            deliver_address,
                            deliver_to_home,
                            vendors,
                            items,
                            total_cost,
                            actual_total_cost,
                            requisition_number,
                            estimated_time_of_arrival,
                            order_number,
                            gsa_session_number,
                            purchase_order_number,
                            submitted_date,
                            bch_comments,
                            description,
                            approval_amount,
                            route_id,
                            route_type_id,
                            route_notes,
                            route_by,
                            route_by_name,
                            route_date,
                            route_status_id,
                            route_to,
                            route_to_name,
                            route_status_name,
                            route_type_name,
                            ou_org_id,
                            div_org_id,
                            grp_org_id,
                            needed_by_date,
                            reviewer_id,
                            reviewer_name,
                            bankcard_approving_official_id,
                            bao_name,
                            bankcard_holder_id,
                            bh_name,
                            is_dynamic,
                            is_dynamic_reroute,
                            reroute_stack
                        FROM
                            v_request
                        WHERE
                                ou_org_id = nvl(query_ou_org_id, ou_org_id)
                            AND div_org_id = nvl(query_div_org_id, div_org_id)
                            AND grp_org_id = nvl(query_grp_org_id, grp_org_id)
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
                            AND ( p_deliver_to_home IS NULL
                                  OR deliver_to_home = p_deliver_to_home )
                            AND ( p_request_id IS NULL
                                  OR p_request_id = request_id )
                            AND ( p_fy IS NULL
                                  OR p_fy = fy );
            
            --TODO: the requester should be able to access his or her requests even if moved to another OU or division
            --so we should use a UNION to get requests by requester_id=user_people_id
            --If we add this, we also need to change app queries for getting single request record to allow the same thing there
            --so they can view the detail request info after selecting from search

END;