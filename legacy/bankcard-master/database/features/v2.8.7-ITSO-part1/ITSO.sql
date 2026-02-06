ALTER TABLE BCPMS_OWNER.REQUEST
 ADD IS_IT_PURCHASE  CHAR(1 BYTE)                  DEFAULT 'N'  ;


CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST
(
    REQUEST_ID,
    FY,
    NOTES,
    REQUESTER_ID,
    REQUESTER_NAME,
    CREATED_BY,
    CREATED_BY_NAME,
    CREATED_FOR,
    CREATED_FOR_NAME,
    CREATED_DATE,
    IS_SHOPPING_CART,
    REFERENCE_ID,
    UPDATED_BY,
    UPDATED_DATE,
    DELIVER_ADDRESS,
    DELIVER_TO_HOME,
    NEEDED_BY_DATE,
    REVIEWER_ID,
    REVIEWER_NAME,
    BANKCARD_APPROVING_OFFICIAL_ID,
    BAO_NAME,
    BANKCARD_HOLDER_ID,
    BH_NAME,
    VENDORS,
    ITEMS,
    TOTAL_COST,
    ACTUAL_TOTAL_COST,
    REQUISITION_NUMBER,
    ESTIMATED_TIME_OF_ARRIVAL,
    ORDER_NUMBER,
    GSA_SESSION_NUMBER,
    PURCHASE_ORDER_NUMBER,
    SUBMITTED_DATE,
    BCH_COMMENTS,
    APPROVAL_AMOUNT,
    DESCRIPTION,
    ROUTE_ID,
    ROUTE_NOTES,
    ROUTE_BY,
    ROUTE_BY_NAME,
    ROUTE_TO,
    ROUTE_TO_NAME,
    ROUTE_DATE,
    ROUTE_TYPE_ID,
    ROUTE_TYPE_NAME,
    ROUTE_STATUS_ID,
    ROUTE_STATUS_NAME,
    IS_DYNAMIC,
    IS_DYNAMIC_REROUTE,
    REROUTE_STACK,
    OU_ORG_ID,
    DIV_ORG_ID,
    GRP_ORG_ID,
    IS_IT_PURCHASE
)
BEQUEATH DEFINER
AS
      SELECT req.request_id,
             req.fy,
             req.notes,
             req.requester_id,
             get_user_name (req.requester_id)
                 AS requester_name,
             req.created_by,
             get_user_name (req.created_by)
                 AS created_by_name,
             req.created_for,
             get_user_name (req.created_for)
                 AS created_for_name,
             req.created_date,
             req.is_shopping_cart,
             req.reference_id,
             req.updated_by,
             req.updated_date,
             req.deliver_address,
             req.deliver_to_home,
             REQ.NEEDED_BY_DATE,
             REQ.REVIEWER_ID,
             get_user_name (req.REVIEWER_ID)
                 AS REVIEWER_Name,
             REQ.BANKCARD_APPROVING_OFFICIAL_ID,
             get_user_name (req.BANKCARD_APPROVING_OFFICIAL_ID)
                 AS BAO_NAME,
             REQ.BANKCARD_HOLDER_ID,
             get_user_name (req.BANKCARD_HOLDER_ID)
                 AS BH_NAME,
             rv.vendor_name,
             vri.items,
             get_request_total_cost (req.request_id)
                 AS total_cost,
             get_request_actual_total (req.request_id)
                 AS actual_total_cost,
             req.requisition_number,
             req.estimated_time_of_arrival,
             req.order_number,
             req.gsa_session_number,
             req.purchase_order_number,
             req.submitted_date,
             req.bch_comments,
             req.approval_amount,
             req.description,
             r.route_id,
             r.route_notes,
             r.route_by,
             r.ROUTE_BY_NAME,
             r.route_to,
             r.ROUTE_TO_NAME,
             r.route_date,
             r.route_type_id,
             r.route_type_name,
             r.route_status_id,
             r.route_status_name,
             r.is_dynamic,
             r.IS_DYNAMIC_REROUTE,
             r.REROUTE_STACK,
             req.ou_id,
             req.div_id,
             req.grp_id,
             req.is_it_purchase
        FROM request                req,
             v_request_current_route r,
             request_vendor_t       rv,
             v_request_items        vri
       --nist_user              u
       WHERE     req.request_id = r.request_id
             AND req.request_id = rv.request_id(+)
             AND req.request_id = vri.request_id
    --AND req.requester_id = u.people_id
    --AND req.created_by = 23826
    --AND r.route_type_id = 0
    ORDER BY req.created_date DESC;



CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_get_request_with_criteria (
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
    p_is_it_purchase                   IN     VARCHAR2,
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
MB-510, add IT Purchase criteria
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
                            reroute_stack,
                            is_it_purchase
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
			    AND ( p_is_it_purchase IS NULL
                                  OR is_it_purchase = p_is_it_purchase )
                            AND ( p_request_id IS NULL
                                  OR p_request_id = request_id )
                            AND ( p_fy IS NULL
                                  OR p_fy = fy );
            
            --TODO: the requester should be able to access his or her requests even if moved to another OU or division
            --so we should use a UNION to get requests by requester_id=user_people_id
            --If we add this, we also need to change app queries for getting single request record to allow the same thing there
            --so they can view the detail request info after selecting from search

END;
/


CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_request (
    p_request_id                       IN     INTEGER,
    p_requester_id                     IN     INTEGER,
    p_created_for                      IN     INTEGER,
    p_updated_by                       IN     INTEGER,
    p_notes                            IN     VARCHAR2,
    p_deliver_address                  IN     VARCHAR2,
    p_deliver_to_home                  IN     VARCHAR2,
    p_needed_by_date                   IN     TIMESTAMP,
    p_reviewer_id                      IN     INTEGER,
    p_bankcard_approving_official_id   IN     INTEGER,
    p_bankcard_holder_id               IN     INTEGER,
    p_estimated_time_of_arrival        IN     TIMESTAMP,
    p_requisition_number               IN     VARCHAR2,
    p_division_code                    IN     VARCHAR2,
    p_order_number                     IN     VARCHAR2,
    p_gsa_session_number               IN     VARCHAR2,
    p_purchase_order_number            IN     VARCHAR2,
    p_approval_amount                  IN     NUMBER,
    p_description                      IN     VARCHAR2,
    p_is_it_purchase                   IN     VARCHAR2,
    o_route_in_correct_state              OUT INTEGER,
    o_result_set                          OUT SYS_REFCURSOR)
IS
    l_route_type_id   INTEGER;
    update_purchase   INTEGER;
BEGIN
    o_route_in_correct_state := 1;
    update_purchase := 1;

    IF    p_order_number IS NOT NULL
       OR p_gsa_session_number IS NOT NULL
       OR p_purchase_order_number IS NOT NULL
       OR p_estimated_time_of_arrival IS NOT NULL
    THEN
        SELECT route_type_id
          INTO l_route_type_id
          FROM route
         WHERE     request_id = p_request_id
               AND route_id = (SELECT MAX (route_id)
                                   FROM route
                                  WHERE request_id = p_request_id);

        IF l_route_type_id <> 4 AND l_route_type_id <> 6
        THEN
            /*THEN -- Must be in state of 4 or 6 to modify the request.
                o_route_in_correct_state := 0;
                OPEN o_result_set FOR
                    SELECT
                        dummy
                    FROM
                        dual
                    WHERE
                        1 = 0;
                RETURN;*/
            update_purchase := 0;
        END IF;
    END IF;

    IF update_purchase = 1
    THEN
        UPDATE request
           SET requester_id = NVL (p_requester_id, requester_id),
               created_for = NVL (p_created_for, created_for),
               updated_by = NVL (p_updated_by, updated_by),
               updated_date = SYSDATE,
               notes = NVL (p_notes, notes),
               deliver_address = NVL (p_deliver_address, deliver_address),
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = NVL (p_needed_by_date, needed_by_date),
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id,
                        bankcard_approving_official_id),
               bankcard_holder_id =
                   NVL (p_bankcard_holder_id, bankcard_holder_id),
               estimated_time_of_arrival =
                   NVL (p_estimated_time_of_arrival,
                        estimated_time_of_arrival),
               requisition_number =
                   NVL (p_requisition_number, requisition_number),
               order_number = NVL (p_order_number, order_number),
               gsa_session_number =
                   NVL (p_gsa_session_number, gsa_session_number),
               purchase_order_number =
                   NVL (p_purchase_order_number, purchase_order_number),
               approval_amount = NVL (p_approval_amount, approval_amount),
               description = NVL (p_description, description),
               is_it_purchase=NVL (p_is_it_purchase, is_it_purchase)
         WHERE request_id = p_request_id;
    ELSE
        UPDATE request
           SET requester_id = NVL (p_requester_id, requester_id),
               created_for = NVL (p_created_for, created_for),
               updated_by = NVL (p_updated_by, updated_by),
               updated_date = SYSDATE,
               notes = NVL (p_notes, notes),
               deliver_address = NVL (p_deliver_address, deliver_address),
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = NVL (p_needed_by_date, needed_by_date),
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id,
                        bankcard_approving_official_id),
               bankcard_holder_id =
                   NVL (p_bankcard_holder_id, bankcard_holder_id),
               estimated_time_of_arrival =
                   NVL (p_estimated_time_of_arrival,
                        estimated_time_of_arrival),
               requisition_number =
                   NVL (p_requisition_number, requisition_number),
               approval_amount = NVL (p_approval_amount, approval_amount),
               description = NVL (p_description, description),
               is_it_purchase=NVL (p_is_it_purchase, is_it_purchase)
         WHERE request_id = p_request_id;
    END IF;

    OPEN o_result_set FOR SELECT request_id,
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
                                 is_it_purchase
                            FROM v_request
                           WHERE request_id = p_request_id;
END;
/


create or replace PROCEDURE             sp_get_audit_report(
        p_request_id IN INTEGER,
        result_set_request OUT SYS_REFCURSOR,
        result_set_vendors OUT SYS_REFCURSOR,
        result_set_items OUT SYS_REFCURSOR,
        result_set_just OUT SYS_REFCURSOR,
        result_set_routes OUT SYS_REFCURSOR)
IS
BEGIN

/**
05/07/2021
MB-422
added additional column in the justification block
removed two order bys because only single record is returned
BANK-510
add is IT purchase data
**/

    OPEN result_set_request FOR
        SELECT
            request_id,
            requisition_number,
            get_user_name(requester_id) AS requester_name,
            get_user_name(created_by) AS created_by_name,
            get_user_name(reviewer_id) AS reviewer_name,
            get_user_name(bankcard_approving_official_id) AS bao_name,
            get_user_name(bankcard_holder_id) AS bh_name,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 1 AND rownum = 1) AS request_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 2 AND rownum = 1) AS reviewer_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 3 AND rownum = 1) AS bao_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 4 AND rownum = 1) AS order_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 6 AND rownum = 1) AS deliver_date,
            approval_amount
        FROM
            request r
        WHERE
            request_id = p_request_id;

    OPEN result_set_vendors FOR
       SELECT 
            vendor_name,
            ref_vendor_id as vendor_id,
            web_url,
            contact_person,
            phone
         
        FROM
             request_vendor_t rv
        WHERE
            rv.request_id = p_request_id;
        --only one vendor per request now, no need to order by
        --ORDER BY
        --    ref_vendor_id;

    OPEN result_set_items FOR
        SELECT
            item_name,
            quantity_ordered AS quantity,
            vendor_id,
            item_description,
            price_ordered AS price,
            project_task,
            object_class,
            is_shipping
        FROM
            item
        WHERE
            request_id = p_request_id
        ORDER BY
            item_id;

    OPEN result_set_just FOR
        SELECT
            r.request_id,
	    r.is_it_purchase,
            built_in_vendor,
            professional_org,
            convenience_check,
            convenience_check_just,
            gsa_schedule,
            gsa_schedule_just,
            third_party_vendor,
            third_party_vendor_just,
            price_is_reasonable_just,
            small_business,
            small_business_just,
            commercial_vendor,
            commercial_vendor_just
        FROM
            request r, request_justification rj
        WHERE
            r.request_id = p_request_id AND r.request_id = rj.request_id(+);
        
        -- MB-480 get route history only for BCH and BAO approvals/re-approvals, which are required for audit purpose
        OPEN result_set_routes FOR
            SELECT
                r.request_id,
               rr.*
            FROM
                request r, v_route_history rr, route rt
            WHERE
                r.request_id = p_request_id AND r.request_id = rr.request_id(+)
                AND rr.route_id = rt.route_id
                and rr.route_type_id in (2,3) 
                and rt.is_dynamic = 0
                order by rr.route_id;
       
END;


create or replace PROCEDURE             UP_CHECK_PROCESS_REQUEST (
    p_request_id       INT,
    p_rc           OUT NVARCHAR2)
IS
    v_request_ct         INT;
    v_vendor_ct          INT;
    v_justification_ct   INT;
    v_codes              VARCHAR2 (10);

    CURSOR v_justification_cur
    IS
        SELECT a.REF_VENDOR_ID AS vendor_id, a.VENDOR_NAME, b.*
          FROM request_vendor_t a, request_justification b
         WHERE a.request_id = b.request_id AND a.request_id = p_request_id;
BEGIN
/*
modified validation error code because we only have one vendor per request now
*/
    p_rc := '';

    -- check if the request is exists
    SELECT COUNT (*)
      INTO v_request_ct
      FROM request
     WHERE request_id = p_request_id;

    IF v_request_ct = 0
    THEN
        p_rc := 'No request found';
        RETURN;
    END IF;

    -- check if the vendor exists (a request should have one and only one vendor after the vendor redesign in 02/2021)
    SELECT COUNT (*)
      INTO v_vendor_ct
      FROM request_vendor_t
     WHERE request_id = p_request_id;

    IF v_vendor_ct = 0
    THEN
        p_rc := 'No vendor found';
        RETURN;
    END IF;

    -- check if the justification exists (even for built-in vendors, it should have a record in the request_justification table)
    SELECT COUNT (*)
      INTO v_justification_ct
      FROM request_justification
     WHERE request_id = p_request_id;

    IF v_justification_ct = 0
    THEN
        p_rc := 'No justification found';
        RETURN;
    END IF;

    -- check for justfications detail

    FOR i IN v_justification_cur
    LOOP
        --built-in vendor
        IF i.built_in_vendor IS NOT NULL AND i.built_in_vendor < 0
        THEN
            --no need to check for justification for built-in vendor
            RETURN;
        END IF;
        
        IF i.professional_org  = 'Y'
        THEN
        
        --no need to check for justification for professional_org vendor
            RETURN;
        END IF;
        

        -- for non built-in vendor, check justification detail
        IF i.convenience_check = 'Y'
        THEN
            IF (   i.convenience_check_just IS NULL
                OR i.convenience_check_just = '')
            THEN
                p_rc :=
                       p_rc
                    || 'Convenience check justification is missing for vendor: '
                    || i.VENDOR_NAME
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;
        END IF;

        --based on discussion with Michele on 2/11/2021, even for Convenience check, users still need to do the list of other justifications
        -- gsa
        IF i.gsa_schedule = 'N'
        THEN
            IF (i.gsa_schedule_just IS NULL OR i.gsa_schedule_just = '')
            THEN
                p_rc :=
                       p_rc
		    --wording update
                    || 'Not using a mandatory source pricing justification is missing.<br>'
                    --|| 'GSA schedule justification is missing for vendor: '
                    --|| i.VENDOR_NAME
                    --|| '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            -- price is reasonable
            IF    i.price_is_reasonable_just IS NULL
               OR i.price_is_reasonable_just = ''
            THEN
                p_rc :=
                       p_rc
                       || 'Price is reasonable justification is missing.<br>'
                    --|| 'Price is reasonable justification is missing for vendor: '
                    --|| i.VENDOR_NAME
                    --|| '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            --third party
            IF     i.third_party_vendor = 'Y'
               AND (   i.third_party_vendor_just IS NULL
                    OR i.third_party_vendor_just = '')
            THEN
                p_rc :=
                       p_rc
                       || 'Third party vendor justification is missing.<br>'
                    --|| 'Third party vendor justification is missing for vendor: '
                   -- || i.VENDOR_NAME
                    --|| '.'
                    || CHR (13)
                    || CHR (10);
            END IF;
        END IF;                                                         -- gsa

        -- small business
        /*BANK-512, we no longer collect this justification from the app
        IF     i.small_business = 'N'
           AND (i.small_business_just IS NULL OR i.small_business_just = '')
        THEN
            p_rc :=
                   p_rc
                || 'Small Business justification is missing.<br>'
                --|| 'Small Business justification is missing for vendor: '
                --|| i.VENDOR_NAME
                --|| '.'
                || CHR (13)
                || CHR (10);
        END IF;
        */
        
    END LOOP;
-- justification check ends

EXCEPTION
    WHEN OTHERS
    THEN
        p_rc :=
               'Error ('
            || TO_CHAR (SQLCODE)
            || ') occurs from UP_CHECK_PROCESS_REQUEST for request id: '
            || TO_CHAR (p_request_id)
            || '. '
            || SQLERRM;
END;