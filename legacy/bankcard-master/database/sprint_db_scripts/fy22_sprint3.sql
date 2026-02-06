
ALTER TABLE BCPMS_OWNER.REQUEST
MODIFY(NOTES VARCHAR2(1000 BYTE));

ALTER TABLE BCPMS_OWNER.ITEM
MODIFY(ITEM_DESCRIPTION NVARCHAR2(1000));

ALTER TABLE BCPMS_OWNER.ITEM
MODIFY(ITEM_NOTES VARCHAR2(1000 BYTE));

CREATE INDEX BCPMS_OWNER.IDX_PTC_OU_CODE ON BCPMS_OWNER.PROJECT_TASK
(P_ORG2_CODE);



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

    --get the current status, type and route_by of the request
    SELECT route_status_id, route_type_id, route_by
      INTO l_route_status_id, l_route_type_id, l_route_by
      FROM route
     WHERE     request_id = p_request_id
           AND route_id = (SELECT MAX (route_id)
                             FROM route
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
                       reroute_by)
         VALUES (p_request_id,
                 l_route_type_id,                            -- same as before
                 p_route_notes,                            -- the reroute note
                 p_route_by,                           -- update
                 SYSDATE,                                   --when it happened
                 l_route_status_id,                           --same as before
                 p_route_to, --a different person with the same privilige (from a reviewer to another reviewer, for example)
                 p_route_by)                             --who did the reroute
      RETURNING route_id
           INTO o_route_id;
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


create or replace PROCEDURE             sp_get_request_with_criteria(
		--add dynamic routing fields in the return which will be used to determine if reassign can be done in the search view
        p_username IN VARCHAR2,
        p_ou_org_id IN INTEGER,
        p_div_org_id IN INTEGER,
        p_grp_org_id IN INTEGER,
        p_requester_id IN INTEGER,
        p_requisition_number IN VARCHAR2,
        p_route_type_id IN INTEGER,
        p_from_date IN DATE,
        p_to_date IN DATE,
        p_vendor_name IN VARCHAR2,
        p_transaction_number IN VARCHAR2,
        p_item_name IN VARCHAR2,
        p_actual_total IN DOUBLE PRECISION,
        p_bankcard_holder_id IN INTEGER,
        p_request_id IN INTEGER,
        p_fy IN Integer,
        p_ptc IN VARCHAR2,
        p_stmt_date IN DATE,
        p_deliver_to_home IN VARCHAR2,
        result_set OUT SYS_REFCURSOR)
IS

    can_access_ou VARCHAR2(20);
    can_access_div VARCHAR2(20);
    can_access_group VARCHAR2(20);
    user_ou_org_id INTEGER;
    user_div_org_id INTEGER;
    user_grp_org_id INTEGER;
    user_people_id INTEGER;
    query_ou_org_id INTEGER;
    query_div_org_id INTEGER;
    query_grp_org_id INTEGER;
    query_requester_id INTEGER;

BEGIN

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
        LOWER(username) = LOWER(p_username);

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
        LOWER(username) = LOWER(p_username);

    -- Limit scope of what user can see based on role and identity.
    IF can_access_ou = 'Y' THEN
        IF p_ou_org_id IS NOT NULL AND p_ou_org_id <> user_ou_org_id THEN
            query_ou_org_id := -1; -- Prevent from accessing outside of their ou.
        ELSE
            query_ou_org_id := user_ou_org_id;
        END IF;
    ELSIF can_access_div = 'Y' THEN
        IF p_div_org_id IS NOT NULL AND p_div_org_id <> user_div_org_id THEN
            query_div_org_id := -1; -- Prevent from accessing outside of their division.
        ELSE
            query_div_org_id := user_div_org_id;
        END IF;
    ELSIF can_access_group = 'Y' THEN
        IF p_grp_org_id IS NOT NULL AND p_grp_org_id <> user_grp_org_id THEN
            query_grp_org_id := -1; -- Prevent from accessing outside of their group.
        ELSE
            query_grp_org_id := user_grp_org_id;
        END IF;
    ELSE
        IF p_requester_id IS NOT NULL AND p_requester_id <> user_people_id THEN
            query_requester_id := -1; -- Prevent from accessing somebody else.
        ELSE
            query_requester_id := user_people_id;
        END IF;
    END IF;

    OPEN result_set FOR
        SELECT
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
            ou_org_id = NVL(query_ou_org_id, ou_org_id)
                AND
            div_org_id = NVL(query_div_org_id, div_org_id)
                AND
            grp_org_id = NVL(query_grp_org_id, grp_org_id)
                AND
            requester_id = NVL(query_requester_id, requester_id)
                AND
            (LOWER(p_requisition_number) IS NULL OR LOWER(requisition_number) LIKE '%' || LOWER(p_requisition_number) || '%')
                AND
            route_type_id = NVL(p_route_type_id, route_type_id)
                AND
            (p_from_date IS NULL OR created_date >= p_from_date)
                AND
            (p_to_date IS NULL OR created_date <= p_to_date)
                AND
            (p_vendor_name IS NULL OR UPPER(vendors) LIKE '%' || UPPER(p_vendor_name) || '%')
                AND
            (p_actual_total IS NULL OR actual_total_cost = p_actual_total)
                AND
            (p_bankcard_holder_id IS NULL OR p_bankcard_holder_id = bankcard_holder_id)
                AND
            (p_transaction_number IS NULL OR LOWER(p_transaction_number) IN (SELECT LOWER(transaction_number) FROM item WHERE request_id = v_request.request_id))
                AND
            --MB-470    
            (p_item_name IS NULL OR regexp_replace(items,'[^a-zA-Z0-9 ]', ' ') LIKE '%' || p_item_name || '%')
                AND
            (p_ptc IS NULL OR LOWER(p_ptc) IN (SELECT LOWER(project_task) FROM item WHERE request_id = v_request.request_id))
                AND
            (p_stmt_date IS NULL OR p_stmt_date IN (SELECT statement_date FROM item WHERE request_id = v_request.request_id))
                AND
            (p_deliver_to_home IS NULL OR deliver_to_home = p_deliver_to_home)
                AND
            (p_request_id IS NULL OR p_request_id = request_id)
                AND
            (p_fy IS NULL OR p_fy = fy) ;
END;