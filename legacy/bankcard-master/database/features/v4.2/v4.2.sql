--track the mission critical approver (DC) in the request
ALTER TABLE BCPMS_OWNER.REQUEST
ADD (DIVISION_CHIEF_ID INTEGER);

--FCO is fixed route now (16,16)
Insert into BCPMS_OWNER.LKUP_ROUTE_TYPE (ROUTE_TYPE_ID,ROUTE_TYPE_NAME) values (16,'sent to FCO');

--data conversion to update past FCO record to fixed route record
update route
set route_type_id=16,
    is_dynamic=0,
    dynamic_type=null
where dynamic_type='FCO';

--for the new purchased items report for AO & SMA
CREATE OR REPLACE FORCE EDITIONABLE VIEW "BCPMS_OWNER"."V_PURCHASED_ITEM" 
AS 
  SELECT
        vp.fy as FY,
        vp.ou_id as OU_ID,
        get_ou_code_by_org_id(vp.ou_id) as OU,
        vp.div_id as DIV_ID,
        get_div_code_by_org_id(vp.div_id) as Division,
        vp.grp_id as GRP_ID,
        get_group_code_by_org_id(vp.grp_id) as "GROUP",
        i.request_id as REQUEST_ID,
        vp.requisition_number, 
        vp.route_type_id as purchase_status_id,
        rt.route_type_name                   AS purchase_status,
        vp.bankcard_holder_id,
        get_user_name(vp.bankcard_holder_id) AS bankcard_holder,
        i.item_id,
        i.item_name,
        i.project_task,
        i.object_class,
        vics.ITEM_STATUS_TYPE_ID as item_status_id,
        it.item_status_type_name             AS item_status,
        i.item_description,
        i.price_ordered,
        i.quantity_ordered,
        i.item_notes,
        i.date_received as received_date,
        i.catelog_number,
        i.transaction_number,
        i.statement_date
    FROM
        item                     i,
        v_item_current_status    vics,
        v_purchase_current_route vp,
        lkup_route_type          rt,
        lkup_item_status_type    it
    WHERE
            i.request_id = vp.request_id
        AND i.item_id = vics.item_id
        AND vics.item_status_type_id = it.item_status_type_id
        AND vp.route_type_id = rt.route_type_id
        AND vics.item_status_type_id <> 4 --not canceled
        AND price_ordered <> 0 --cost is not zero
    ORDER BY
        vp.ou_id,
        vp.div_id,
        vp.grp_id,
        vp.request_id;
/


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "BCPMS_OWNER"."V_REQUEST" ("REQUEST_ID", "FY", "NOTES", "REQUESTER_ID", "REQUESTER_NAME", "CREATED_BY", "CREATED_BY_NAME", "CREATED_FOR", "CREATED_FOR_NAME", "CREATED_DATE", "IS_SHOPPING_CART", "REFERENCE_ID", "UPDATED_BY", "UPDATED_DATE", "DELIVER_ADDRESS", "DELIVER_TO_HOME", "NEEDED_BY_DATE", "REVIEWER_ID", "REVIEWER_NAME", "DIVISION_CHIEF_ID", "DIVISION_CHIEF_NAME", "BANKCARD_APPROVING_OFFICIAL_ID", "BAO_NAME", "FUNDS_CERTIFYING_OFFICIAL_ID", "FCO_NAME", "BANKCARD_HOLDER_ID", "BH_NAME", "VENDORS", "ITEMS", "ITEM_STATUSES", "TOTAL_COST", "ACTUAL_TOTAL_COST", "REQUISITION_NUMBER", "ESTIMATED_TIME_OF_ARRIVAL", "ORDER_NUMBER", "GSA_SESSION_NUMBER", "PURCHASE_ORDER_NUMBER", "SUBMITTED_DATE", "BCH_COMMENTS", "APPROVAL_AMOUNT", "DESCRIPTION", "ROUTE_ID", "ROUTE_NOTES", "ROUTE_BY", "ROUTE_BY_NAME", "ROUTE_TO", "ROUTE_TO_NAME", "ROUTE_DATE", "ROUTE_TYPE_ID", "ROUTE_TYPE_NAME", "ROUTE_STATUS_ID", "ROUTE_STATUS_NAME", "IS_DYNAMIC", "IS_DYNAMIC_REROUTE", "REROUTE_STACK", "ROUTE_STEP", "DYNAMIC_TYPE", "OU_ORG_ID", "DIV_ORG_ID", "GRP_ORG_ID", "IS_IT_PURCHASE", "ITSO_APPROVED", "PURCHASE_TYPE_ID", "MISSION_CRITICAL_CATEGORY_ID", "MISSION_CRITICAL_JUSTIFICATION") AS 
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
	     REQ.DIVISION_CHIEF_ID,
             get_user_name (req.DIVISION_CHIEF_ID)
                 AS DIVISION_CHIEF_Name,
             REQ.BANKCARD_APPROVING_OFFICIAL_ID,
             get_user_name (req.BANKCARD_APPROVING_OFFICIAL_ID)
                 AS BAO_NAME,
             req.FUNDS_CERTIFYING_OFFICIAL_ID,
             get_user_name (req.FUNDS_CERTIFYING_OFFICIAL_ID)
                 AS FCO_NAME,
             REQ.BANKCARD_HOLDER_ID,
             get_user_name (req.BANKCARD_HOLDER_ID)
                 AS BH_NAME,
             rv.vendor_name,
             vri.items,
             vris.item_statuses,
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
             r.ROUTE_STEP,
             r.dynamic_type,
             req.ou_id,
             req.div_id,
             req.grp_id,
             req.is_it_purchase,
             req.itso_approved,
             req.purchase_type_id,
            req.MISSION_CRITICAL_CATEGORY_ID,
            req.MISSION_CRITICAL_JUSTIFICATION
        FROM request                  req,
             v_request_current_route_n r,
             request_vendor_t         rv,
             v_request_items          vri,
             v_request_item_statuses vris
       WHERE     req.request_id = r.request_id
             AND req.request_id = rv.request_id(+)
             AND req.request_id = vri.request_id(+)
             AND req.request_id = vris.request_id(+)
            order by req.request_id desc;
/


--forgot to change v_request to request in the last update
create or replace PROCEDURE             up_notify_property_custodians (
    p_request_id    INT,
    p_people_id     NVARCHAR2)
IS
    TYPE v_item_type IS TABLE OF item%ROWTYPE;

    v_item                v_item_type;
    l_subject             NVARCHAR2 (100)
        :=    'Bankcard request: Taggable Equipment order ('
           || p_request_id
           || ')';
    l_body                NVARCHAR2 (20000) := '';
    l_always_valid_link   NVARCHAR2 (300) := NULL;
    l_foot                NVARCHAR2 (200)
        := '<br><br>For OU Bankcard Request and Approval System technical support, please contact the support team via email at mml.systemshelp@nist.gov or reply to this email.';
    l_sender_email        NVARCHAR2 (50) := 'MML.SystemsHelp@nist.gov';
    l_cc_email            NVARCHAR2 (200) := NULL;
    l_app_url             VARCHAR2 (200) := '';
    l_app_env             VARCHAR2 (200) := '';
    l_app_bcc             VARCHAR2 (200) := NULL;
    l_receip_email        NVARCHAR2 (100) := '';
    l_email_refcur        SYS_REFCURSOR;
    l_email               NVARCHAR2 (100);
    l_sql                 NVARCHAR2 (500)
        :=    'select email from nist_user where people_id in ('
           || p_people_id
           || ')';
    v_error_code          NVARCHAR2 (20);
    v_error_message       NVARCHAR2 (2000);
    v_requester_name      NVARCHAR2 (30);

    CURSOR cur_item
    IS
        SELECT ROWNUM  AS COUNT,
               a.ITEM_NAME,
               a.quantity,
               a.ITEM_DESCRIPTION,
               a.OBJECT_CLASS
          FROM item a
         WHERE a.request_id = p_request_id
               AND a.IS_TAGGABLE_EQUIPMENT = 'Y';
BEGIN

    --select environment variables
    SELECT APP_ENV, APP_URL, NOTIFICATION_BCC
      INTO l_app_env, l_app_url, l_app_bcc
      FROM APP_SETTINGS;

    --changed to use function call instead. This is the only SP that use requester_name (one of the name column)
    --by changing this, v_request might be updated to reduce name columns to improve squery speed
    --names can be set in the server code instead using employee cache
    --SELECT requester_name
    SELECT get_user_name (requester_id)
    into v_requester_name
    from request
    where request_id = p_request_id;


    -- build email list
    OPEN l_email_refcur FOR l_sql;

    LOOP
        FETCH l_email_refcur INTO l_email;

        EXIT WHEN l_email_refcur%NOTFOUND;
        l_receip_email := l_receip_email || l_email || ';';
    END LOOP;

    CLOSE l_email_refcur;

    l_body :=
           'Attention Property Custodian(s)<br><br>'
        || 'You are receiving this email because the following taggable equipment order was submitted to the OU Bankcard Request and Approval System. Please contact the requester listed below if you need additional information.<br><br>';


    IF LENGTH (l_receip_email) IS NULL
    THEN
         --if no Property Custodian User, update the receip email address and email content to send it our helpbox so we can looking into the issue
        l_receip_email := 'mml.systemshelp@nist.gov';
        l_body := 'Attention NIST Org Admins<br><br>  no one was assigned Property Custodian role for the organization this request belongs to. Please investigate!<br><br>' || l_body;
    
    END IF;


    
    FOR i IN cur_item
    LOOP
        l_body :=
               l_body
            || '<table border=1 style="table-layout: fixed; width: 665px;">'
            || '<b><tr><td style="width:42%">Data Field</td><td style="width:58%">Taggable Equipment #'
            || i.COUNT
            || '</td></tr></b>'
            || '<tr><td >Requester Name</td><td>'
            || v_requester_name
            || '</td></tr>'
            || '<tr><td>Item Name</td><td>'
            || i.ITEM_NAME
            || '</td></tr>'
            || '<tr><td>Item Description</td><td>'
            || i.ITEM_DESCRIPTION
            || '</td></tr>'
            || '<tr><td>Quantity</td><td>'
            || i.quantity
            || '</td></tr>'
            || '<tr><td>Object Class</td><td>'
            || i.OBJECT_CLASS
            || '</td></tr></table><br>';
    END LOOP;


    IF l_app_env = 'DEV'
    THEN
        l_receip_email := 'xinweiw@nist.gov';
        l_subject := 'TEST TEST TEST from Development!!! ' || l_subject;
    ELSIF l_app_env = 'TEST'
    THEN
        l_receip_email := 'xinweiw@nist.gov';
        l_subject := 'TEST TEST TEST !!!' || l_subject;
    ELSE
        l_receip_email := TRIM (l_receip_email);
        --xw 2/6/2018 fixed a error when the l_receip_email have an extra ; in the end which causes error when send email
        --the code below removes the last character, which is the ;
        l_receip_email :=
            SUBSTR (l_receip_email, 1, LENGTH (l_receip_email) - 1);
        l_subject := l_subject;
    END IF;

    l_always_valid_link :=
           '<br><br>To <b>view detail information of the taggable equipment in this request at any time</b>, click this link: '
        || l_app_url
        || '?subview=requestsearching/'  --'#requestsearching/'
        || p_request_id;

    up_send_email (l_sender_email,
                   l_receip_email,
                   l_cc_email,
                   l_app_bcc,
                   l_subject,
                   l_body || l_always_valid_link || l_foot);
EXCEPTION
    WHEN OTHERS
    THEN
        v_error_code := SQLCODE;
        v_error_message := SUBSTR (SQLERRM, 1, 2000);

        INSERT INTO db_error (source,
                              user_id,
                              code,
                              MESSAGE)
                 VALUES (
                               'up_notify_property_custodians - '
                            || p_request_id
                            || ' - '
                            || l_receip_email,
                            p_people_id,
                            v_error_code,
                            v_error_message);
END;
/


create or replace PROCEDURE             sp_update_request (
/**
changed the query returning request back to use *
changed some columns update to not use nvl

**/
    p_request_id                       IN     INTEGER,
    p_requester_id                     IN     INTEGER,
    p_created_for                      IN     INTEGER,
    p_updated_by                       IN     INTEGER,
    p_notes                            IN     VARCHAR2,
    p_deliver_address                  IN     VARCHAR2,
    p_deliver_to_home                  IN     VARCHAR2,
    p_needed_by_date                   IN     TIMESTAMP,
    p_reviewer_id                      IN     INTEGER,
    p_division_chief_id                IN     INTEGER,
    p_bankcard_approving_official_id   IN     INTEGER,
    p_funds_certifying_official_id     IN     INTEGER,
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
    p_purchase_type_id                   IN     INTEGER,
    p_mission_critical_category_id                   IN     INTEGER,
    p_mission_critical_justification                   IN     VARCHAR2,
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
               deliver_address = p_deliver_address,
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = p_needed_by_date,
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               division_chief_id= NVL (p_division_chief_id, division_chief_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id, bankcard_approving_official_id),
               funds_certifying_official_id =
                    NVL (p_funds_certifying_official_id, funds_certifying_official_id),
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
               description = p_description,
               is_it_purchase=NVL (p_is_it_purchase, is_it_purchase),
               purchase_type_id = NVL (p_purchase_type_id, purchase_type_id),
               mission_critical_category_id = NVL (p_mission_critical_category_id, mission_critical_category_id),
               mission_critical_justification = NVL (p_mission_critical_justification, mission_critical_justification)
         WHERE request_id = p_request_id;
    ELSE
        UPDATE request
           SET requester_id = NVL (p_requester_id, requester_id),
               created_for = NVL (p_created_for, created_for),
               updated_by = NVL (p_updated_by, updated_by),
               updated_date = SYSDATE,
               notes = NVL (p_notes, notes),
               deliver_address = p_deliver_address,
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = p_needed_by_date,
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               division_chief_id= NVL (p_division_chief_id, division_chief_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id,
                        bankcard_approving_official_id),
               funds_certifying_official_id =
                    NVL (p_funds_certifying_official_id, funds_certifying_official_id),
               bankcard_holder_id =
                   NVL (p_bankcard_holder_id, bankcard_holder_id),
               estimated_time_of_arrival =
                   NVL (p_estimated_time_of_arrival,
                        estimated_time_of_arrival),
               requisition_number =
                   NVL (p_requisition_number, requisition_number),
               approval_amount = NVL (p_approval_amount, approval_amount),
               description = p_description,
               is_it_purchase=NVL (p_is_it_purchase, is_it_purchase),
               purchase_type_id = NVL (p_purchase_type_id, purchase_type_id),
               mission_critical_category_id = NVL (p_mission_critical_category_id, mission_critical_category_id),
               mission_critical_justification = NVL (p_mission_critical_justification, mission_critical_justification)
         WHERE request_id = p_request_id;
    END IF;

    OPEN o_result_set FOR SELECT *
                            FROM v_request
                           WHERE request_id = p_request_id;
END;
/


--add type id 16 in select route because FCO now is fixed route with route type 16
--data conversion will also update past records with type = 16 for FCO routes
create or replace PROCEDURE             sp_get_audit_report (
    p_request_id         IN     INTEGER,
    result_set_request      OUT SYS_REFCURSOR,
    result_set_vendors      OUT SYS_REFCURSOR,
    result_set_items        OUT SYS_REFCURSOR,
    result_set_just         OUT SYS_REFCURSOR,
    result_set_routes       OUT SYS_REFCURSOR)
IS
BEGIN
    /**
    05/07/2021
    MB-422
    added additional column in the justification block
    removed two order bys because only single record is returned
    **/
    
    --issue 585 audit report become available when type=3 and status=7
    --for orgs that use explicit FCO routing, a request needs to be approved by BAO first and FCO second before routing to BCH
    --so if FUNDS_CERTIFYING_OFFICIAL_ID in the request is 0, it means the org of the request doesn't use explicit FCO routing
    --and the BAO and FCO is the same staff
    
    --issue 619 add mission critical justification and DC/DR approval to audit report

    OPEN result_set_request FOR
       SELECT request_id,
               requisition_number,
               get_user_name (requester_id)
                   AS requester_name,
               get_user_name (created_by)
                   AS created_by_name,
               get_user_name (reviewer_id)
                   AS reviewer_name,
               get_user_name (bankcard_approving_official_id)
                   AS bao_name,
               CASE
                WHEN FUNDS_CERTIFYING_OFFICIAL_ID is NULL or FUNDS_CERTIFYING_OFFICIAL_ID = 0 THEN get_user_name (bankcard_approving_official_id)
                     ELSE get_user_name (FUNDS_CERTIFYING_OFFICIAL_ID)
               END    AS fco_name,
               get_user_name (bankcard_holder_id)
                   AS bh_name,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 1
                       AND ROWNUM = 1)
                   AS request_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 2
                       AND ROWNUM = 1)
                   AS reviewer_date,                 
                CASE
                   WHEN FUNDS_CERTIFYING_OFFICIAL_ID is NULL or FUNDS_CERTIFYING_OFFICIAL_ID = 0 THEN
                         (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 3
                       AND ROWNUM = 1)
                  ELSE 
                   (SELECT route_date
                      FROM route
                     WHERE     request_id = r.request_id
                           AND route_status_id = 16
                           AND ROWNUM = 1)
                END   AS bao_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 3
                       AND ROWNUM = 1)
                       AS fco_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 4
                       AND ROWNUM = 1)
                   AS order_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 6
                       AND ROWNUM = 1)
                   AS deliver_date,
               approval_amount,
               GET_MISSION_CRITICAL_CATEGORY_NAME(r.mission_critical_category_id) as mission_critical_category_name,
               r.mission_critical_justification
          FROM request r
         WHERE request_id = p_request_id;

    OPEN result_set_vendors FOR SELECT vendor_name,
                                       ref_vendor_id     AS vendor_id,
                                       web_url,
                                       contact_person,
                                       phone
                                  FROM request_vendor_t rv
                                 WHERE rv.request_id = p_request_id;

    --only one vendor per request now, no need to order by
    --ORDER BY
    --    ref_vendor_id;

    OPEN result_set_items FOR   SELECT item_name,
                                       quantity_ordered     AS quantity,
                                       vendor_id,
                                       item_description,
                                       price_ordered        AS price,
                                       project_task,
                                       object_class,
                                       is_shipping
                                  FROM item
                                 WHERE request_id = p_request_id
                              ORDER BY item_id;

    OPEN result_set_just FOR
        SELECT r.request_id,
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
          FROM request r, request_justification rj
         WHERE     r.request_id = p_request_id
               AND r.request_id = rj.request_id(+);

    -- MB-480 get route history only for BCH and BAO approvals/re-approvals, which are required for audit purpose
    -- modified to include any ITSO approval route 
    OPEN result_set_routes FOR
         /** SELECT *
            FROM (SELECT rr.*, rt.route_step, 'ITSO' AS dynamic_type
                    FROM v_route_history rr, route rt
                   WHERE     rr.route_id = rt.route_id
                         AND rt.route_id = (SELECT itso_approved
                                              FROM request
                                             WHERE request_id = p_request_id)
                  UNION
                  SELECT rr.*, rt.route_step, NULL AS dynamic_type
                    FROM request r, v_route_history rr, route rt
                   WHERE     r.request_id = p_request_id
                         AND r.request_id = rr.request_id(+)
                         AND rr.route_id = rt.route_id
                         AND rr.route_type_id IN (2, 3)
                         AND rt.is_dynamic = 0)
        ORDER BY route_step;
        
        **/
        
        SELECT rr.route_id, rr.route_type_id, rr.route_status_id,  --rr.route_status_name, rr.route_type_name, rr.route_notes,
               rr.route_by_name, rr.route_by,
               rr.route_to_name, rr.route_to, rt.route_step, 
               rt.is_dynamic, rt.dynamic_type, rr.route_date
        FROM
            v_route_history rr, route rt
        WHERE rt.request_id = p_request_id
            AND rt.request_id = rr.request_id
            AND rr.route_id = rt.route_id
            AND rr.route_type_id IN ( 1, 2, 3, 14, 15, 16 )
        ORDER BY
            route_step;
END;
/










































-- used for 'BCPMS_OWNER'
-- for any schema, declare owner variable and use it in queries instead of hardcoded string
BEGIN
    FOR obj IN (SELECT object_name, object_type
                FROM all_objects
                WHERE owner = 'BCPMS_OWNER'
                  AND status = 'INVALID')
    LOOP
        BEGIN
            IF obj.object_type IN ('PACKAGE', 'PROCEDURE', 'FUNCTION', 'TRIGGER', 'VIEW') THEN
                EXECUTE IMMEDIATE 'ALTER ' || obj.object_type || ' ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE';
            ELSIF obj.object_type = 'PACKAGE BODY' THEN
                EXECUTE IMMEDIATE 'ALTER PACKAGE ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE BODY';
            ELSIF obj.object_type = 'TYPE' THEN
                EXECUTE IMMEDIATE 'ALTER TYPE ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE';
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Error compiling ' || obj.object_type || ' ' || obj.object_name || ': ' || SQLERRM);
        END;
    END LOOP;
END;
/