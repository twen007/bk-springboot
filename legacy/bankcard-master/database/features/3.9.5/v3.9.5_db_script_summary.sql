SET DEFINE OFF;

--insert new route type and status
Insert into BCPMS_OWNER.LKUP_ROUTE_TYPE (ROUTE_TYPE_ID,ROUTE_TYPE_NAME) values (14,'sent to DC');
Insert into BCPMS_OWNER.LKUP_ROUTE_TYPE (ROUTE_TYPE_ID,ROUTE_TYPE_NAME) values (15,'sent to DR');

Insert into BCPMS_OWNER.LKUP_ROUTE_STATUS (ROUTE_STATUS_ID,ROUTE_STATUS_NAME) values (17,'The Division Chief reviews the form');
Insert into BCPMS_OWNER.LKUP_ROUTE_STATUS (ROUTE_STATUS_ID,ROUTE_STATUS_NAME) values (18,'The Director reviews the form');

--add new columns in request to store mission critial data
ALTER TABLE BCPMS_OWNER.REQUEST
ADD (MISSION_CRITICAL_CATEGORY_ID INTEGER);

ALTER TABLE BCPMS_OWNER.REQUEST
 ADD (MISSION_CRITICAL_JUSTIFICATION  VARCHAR2(2000));
 
--create lookup table for mission critical category and name; grant access
CREATE TABLE BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
(
  MISSION_CRITICAL_CATEGORY_ID    INTEGER,
  MISSION_CRITICAL_CATEGORY_NAME  NVARCHAR2(500)
);

CREATE UNIQUE INDEX BCPMS_OWNER.PK_LKUP_MISSION_CRITICAL_CATEGORY ON BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
(MISSION_CRITICAL_CATEGORY_ID);

ALTER TABLE BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY ADD (
  CONSTRAINT PK_LKUP_MISSION_CRITICAL_CATEGORY
  PRIMARY KEY
  (MISSION_CRITICAL_CATEGORY_ID)
  USING INDEX BCPMS_OWNER.PK_LKUP_MISSION_CRITICAL_CATEGORY
  ENABLE VALIDATE);


Insert into BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
   (MISSION_CRITICAL_CATEGORY_ID, MISSION_CRITICAL_CATEGORY_NAME)
 Values
   (1, 'Time Critical, Mission critical Research Need');
Insert into BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
   (MISSION_CRITICAL_CATEGORY_ID, MISSION_CRITICAL_CATEGORY_NAME)
 Values
   (2, 'Life safety');
Insert into BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
   (MISSION_CRITICAL_CATEGORY_ID, MISSION_CRITICAL_CATEGORY_NAME)
 Values
   (3, 'Physical and IT infrastructure maintenance and repair');
Insert into BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY
   (MISSION_CRITICAL_CATEGORY_ID, MISSION_CRITICAL_CATEGORY_NAME)
 Values
   (4, 'Other');
   
GRANT SELECT ON BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY TO ADMIN_PORTAL_OWNER;

GRANT DELETE, INSERT, SELECT, UPDATE ON BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY TO BCPMS_APP;

GRANT SELECT ON BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY TO BCPMS_READ;

--this query should be run by BCPMS_APP user
--the app doesn't use this lookup table yet so everything should work without this SYNONYM
--CREATE OR REPLACE SYNONYM BCPMS_APP.LKUP_MISSION_CRITICAL_CATEGORY FOR BCPMS_OWNER.LKUP_MISSION_CRITICAL_CATEGORY;


--create a funciton to return  mission critical category name by id
CREATE OR REPLACE FUNCTION BCPMS_OWNER.GET_MISSION_CRITICAL_CATEGORY_NAME (
    p_MISSION_CRITICAL_CATEGORY_ID   IN LKUP_MISSION_CRITICAL_CATEGORY.MISSION_CRITICAL_CATEGORY_ID%TYPE)
    RETURN NVARCHAR2
IS
    l_MISSION_CRITICAL_CATEGORY_NAME   NVARCHAR2 (200) := '';
BEGIN
    SELECT MISSION_CRITICAL_CATEGORY_NAME
      INTO l_MISSION_CRITICAL_CATEGORY_NAME
      FROM LKUP_MISSION_CRITICAL_CATEGORY m
     WHERE m.MISSION_CRITICAL_CATEGORY_ID = p_MISSION_CRITICAL_CATEGORY_ID;

    RETURN l_MISSION_CRITICAL_CATEGORY_NAME;
END;
/


--recreate request view with added columns
CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST
(REQUEST_ID, FY, NOTES, REQUESTER_ID, REQUESTER_NAME, 
 CREATED_BY, CREATED_BY_NAME, CREATED_FOR, CREATED_FOR_NAME, CREATED_DATE, 
 IS_SHOPPING_CART, REFERENCE_ID, UPDATED_BY, UPDATED_DATE, DELIVER_ADDRESS, 
 DELIVER_TO_HOME, NEEDED_BY_DATE, REVIEWER_ID, REVIEWER_NAME, BANKCARD_APPROVING_OFFICIAL_ID, 
 BAO_NAME, FUNDS_CERTIFYING_OFFICIAL_ID, FCO_NAME, BANKCARD_HOLDER_ID, BH_NAME, 
 VENDORS, ITEMS, ITEM_STATUSES, TOTAL_COST, ACTUAL_TOTAL_COST, 
 REQUISITION_NUMBER, ESTIMATED_TIME_OF_ARRIVAL, ORDER_NUMBER, GSA_SESSION_NUMBER, PURCHASE_ORDER_NUMBER, 
 SUBMITTED_DATE, BCH_COMMENTS, APPROVAL_AMOUNT, DESCRIPTION, ROUTE_ID, 
 ROUTE_NOTES, ROUTE_BY, ROUTE_BY_NAME, ROUTE_TO, ROUTE_TO_NAME, 
 ROUTE_DATE, ROUTE_TYPE_ID, ROUTE_TYPE_NAME, ROUTE_STATUS_ID, ROUTE_STATUS_NAME, 
 IS_DYNAMIC, IS_DYNAMIC_REROUTE, REROUTE_STACK, ROUTE_STEP, DYNAMIC_TYPE, 
 OU_ORG_ID, DIV_ORG_ID, GRP_ORG_ID, IS_IT_PURCHASE, ITSO_APPROVED, 
 PURCHASE_TYPE_ID,MISSION_CRITICAL_CATEGORY_ID,MISSION_CRITICAL_JUSTIFICATION)
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
       --nist_user              u
       WHERE     req.request_id = r.request_id
             AND req.request_id = rv.request_id(+)
             AND req.request_id = vri.request_id(+)
             AND req.request_id = vris.request_id(+)
            order by req.request_id desc;
/

--update request will include mission critical data
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_request (
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

--copy request will include mission critical data
create or replace PROCEDURE             UP_copy_request (
    p_request_id       INT,
    p_created_by       INT,
    p_rc           OUT INT)
IS
/* 
    2/3/23 added copy for required field is_it_purchase 
    3/2024 added copy for required field purchase_type_id 
    2/2025 added copy for required field mission critical category and justification
*/
    v_request_id        request.request_id%TYPE;
    v_created_date      TIMESTAMP := SYSDATE ();

    CURSOR v_request_cursor IS
        SELECT *
          FROM request
         WHERE request_id = p_request_id;

    v_request_row       v_request_cursor%ROWTYPE;

    CURSOR v_item_cursor IS
        SELECT *
          FROM item
         WHERE request_id = p_request_id AND vendor_id <> -9999; --BANK-523 do not copy credit item when copying requests

    v_item_row          v_item_cursor%ROWTYPE;
    v_item_id           item.item_id%TYPE;
    v_error_code        NVARCHAR2 (20);
    v_error_message     NVARCHAR2 (2000);
    --init route status to 1
    v_route_status_id   route.route_status_id%TYPE := 1;
    l_route_id          INTEGER;
BEGIN
    p_rc := 0;

    --request table
    OPEN v_request_cursor;

    IF v_request_cursor%NOTFOUND
    THEN
        RETURN;
    END IF;

    FETCH v_request_cursor INTO v_request_row;

    INSERT INTO request (notes,
                         requester_id,
                         created_for,
                         is_shopping_cart,
                         reference_id,
                         deliver_address,
                         created_by,
                         updated_by,
                         is_it_purchase,
                         purchase_type_id,
                         mission_critical_category_id,
                         mission_critical_justification)
         VALUES (v_request_row.notes,
                 v_request_row.requester_id,
                 v_request_row.created_for,
                 v_request_row.is_shopping_cart,
                 v_request_row.request_id,
                 v_request_row.deliver_address,
                 p_created_by,
                 p_created_by,
                 v_request_row.is_it_purchase,
                 v_request_row.purchase_type_id,
                 v_request_row.mission_critical_category_id,
                 v_request_row.mission_critical_justification)
      RETURNING request_id
           INTO v_request_id;

    IF v_request_id IS NULL
    THEN
        RETURN;
    END IF;

    --determine the route status
    IF v_request_row.requester_id <> p_created_by
    THEN
        v_route_status_id := 12;
    END IF;

    --need to insert a request route "saved" to whoever copied the request
    INSERT INTO ROUTE (REQUEST_ID,
                       ROUTE_TYPE_ID,
                       ROUTE_BY,
                       ROUTE_DATE,
                       ROUTE_STATUS_ID,
                       ROUTE_TO,
                       ROUTE_STEP)
         VALUES (v_request_id,
                 0,
                 p_created_by,
                 SYSDATE,
                 v_route_status_id,
                 p_created_by,
                 1)
      RETURNING route_id
           INTO l_route_id;

    --update request with current route
    UPDATE request
       SET current_route = l_route_id
     WHERE request_id = v_request_id;

    --insert into request_vendor_t
    INSERT INTO request_vendor_t (request_id,
                                  ref_vendor_id,
                                  vendor_name,
                                  convenience_check,
                                  created_by,
                                  updated_by)
        SELECT v_request_id,
               ref_vendor_id,
               vendor_name,
               convenience_check,
               p_created_by,
               p_created_by
          FROM request_vendor_t
         WHERE request_id = p_request_id;

    --insert into request_justification
    INSERT INTO request_justification (request_id,
                                       convenience_check,
                                       convenience_check_just,
                                       professional_org,
                                       gsa_schedule,
                                       gsa_schedule_just,
                                       third_party_vendor,
                                       third_party_vendor_just,
                                       price_is_reasonable_just,
                                       small_business,
                                       small_business_just,
                                       created_by,
                                       updated_by,
                                       built_in_vendor)
        SELECT v_request_id,
               convenience_check,
               convenience_check_just,
               professional_org,
               gsa_schedule,
               gsa_schedule_just,
               third_party_vendor,
               third_party_vendor_just,
               price_is_reasonable_just,
               small_business,
               small_business_just,
               p_created_by,
               p_created_by,
               built_in_vendor
          FROM request_justification
         WHERE request_id = p_request_id;

    --item table
    OPEN v_item_cursor;

    LOOP
        FETCH v_item_cursor INTO v_item_row;

        EXIT WHEN v_item_cursor%NOTFOUND;

        INSERT INTO item (request_id,
                          item_type,
                          vendor_id,
                          catelog_number,
                          item_name,
                          item_description,
                          price,
                          quantity,
                          purpose,
                          chemical,
                          shopping_cart_file_id,
                          item_status_id,
                          object_class,
                          is_precious_metal,
                          is_shipping,
                          price_ordered,
                          quantity_ordered)
             VALUES (v_request_id,
                     v_item_row.item_type,
                     v_item_row.vendor_id,
                     v_item_row.catelog_number,
                     v_item_row.item_name,
                     v_item_row.item_description,
                     v_item_row.price,
                     v_item_row.quantity,
                     v_item_row.purpose,
                     v_item_row.chemical,
                     v_item_row.shopping_cart_file_id,
                     v_item_row.item_status_id,
                     v_item_row.object_class,
                     v_item_row.is_precious_metal,
                     v_item_row.is_shipping,
                     v_item_row.price,
                     v_item_row.quantity)
          RETURNING item_id
               INTO v_item_id;

        IF v_item_row.chemical = 'Y'
        THEN
            INSERT INTO item_chemical (item_id,
                                       owner_id,
                                       primary_user_id,
                                       location,
                                       sub_location,
                                       cas_number,
                                       chemical_form,
                                       chemical_grade,
                                       manufacturer_name,
                                       catalog_number,
                                       catalog_number_quantity,
                                       containers_per_package,
                                       amount_per_container,
                                       lables_needed,
                                       container_type,
                                       expiration_date,
                                       health_nfpa_value,
                                       flammability_nfpa_value,
                                       reactivity_nfpa_value,
                                       special_code_nfpa_value,
                                       is_radioactive_material,
                                       biohazard_registration_req,
                                       special_instruction,
                                       cispro_remarks,
                                       CONTAINER_TOTAL,
                                       PRODUCT_URL)
                SELECT v_item_id,
                       owner_id,
                       primary_user_id,
                       location,
                       sub_location,
                       cas_number,
                       chemical_form,
                       chemical_grade,
                       manufacturer_name,
                       catalog_number,
                       catalog_number_quantity,
                       containers_per_package,
                       amount_per_container,
                       lables_needed,
                       container_type,
                       expiration_date,
                       health_nfpa_value,
                       flammability_nfpa_value,
                       reactivity_nfpa_value,
                       special_code_nfpa_value,
                       is_radioactive_material,
                       biohazard_registration_req,
                       special_instruction,
                       cispro_remarks,
                       CONTAINER_TOTAL,
                       PRODUCT_URL
                  FROM item_chemical
                 WHERE item_id = v_item_row.item_id;
        END IF;

        INSERT INTO ITEM_STATUS (ITEM_ID,
                                 ITEM_STATUS_TYPE_ID,
                                 CREATED_BY,
                                 CREATED_DATE)
             VALUES (v_item_id,
                     1,
                     p_created_by,
                     SYSDATE);

        v_item_id := NULL;
    END LOOP;

    CLOSE v_item_cursor;

    COMMIT;
    p_rc := v_request_id;
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
             VALUES ('up_copy_request',
                     p_created_by,
                     v_error_code,
                     v_error_message);

        p_rc := -1;
/*
    2017-07-24:
    requests with request_vondor and item records
    3011
    1861
    1848
    1129

    to test:
    declare
        p_request_id int := 1441;
        p_rc int;
    begin
        up_copy_request(p_request_id, 27170, p_rc);
        dbms_output.put_line('New request id: ' || p_rc);
    end;

    to check result:
    select r.*, rv.*, i.*, ic.*
    from request r
    left outer join REQUEST_VENDOR rv on r.request_id = rv.request_id
    left outer join item i on r.request_id = i.request_id
    left outer join item_chemical ic on ic.item_id = i.item_id
    where r.request_id in (1441, 3034);

*/

END;
/


--email notification will handle new route to DC and DR; also included new data (Official Requester and their division and group, mission critical category and justification 
--in the the email body sent to DC and DR
create or replace TRIGGER BCPMS_OWNER.EMAIL_NOTICE
    AFTER INSERT
    ON bcpms_owner.route
    REFERENCING NEW AS new OLD AS old
    FOR EACH ROW
DECLARE
    /*
    declare
    l_server environment.server_name%type; l_url environment.web_url%type; l_dtap environment.dtap%type;
    begin
    select server_name, web_url, dtap into l_server, l_url, l_dtap
    from environment
    where server_name = (select global_name FROM global_name);
    dbms_output.put_line(l_server || ', ' || l_url || ', ' || l_dtap);
    end;

    09/22 changed to only do notification if the route_date is not null after decide to insert future/planning/not executed routes in the route table

    02/20/23 check for saved request route and do not run logic for it since it doesn't need any notification
    */


    l_app_url                VARCHAR2 (200) := '';
    l_app_env                VARCHAR2 (200) := '';
    l_app_bcc                VARCHAR2 (200) := NULL;
    l_request_id             request.request_id%TYPE := :new.request_id;
    l_sender                 VARCHAR2 (60);
    l_sender_email           VARCHAR2 (120);
    l_receip                 VARCHAR2 (60);
    l_receip_email           VARCHAR2 (400);
    l_subject                VARCHAR2 (400);
    l_cc_email               VARCHAR2 (400) := '';
    l_body                   VARCHAR2 (8000) := '';
    l_route_type_id          route.route_type_id%TYPE := :new.route_type_id;
    l_route_status_id        route.route_status_id%TYPE := :new.route_status_id;
    --BANK 505
    l_also_notify            VARCHAR2 (400) := :new.also_notify;
    l_route_type_name        VARCHAR2 (60) := '';
    l_link                   NVARCHAR2 (300) := '';
    l_purchase_link          NVARCHAR2 (300) := '';
    l_received_link          NVARCHAR2 (300) := '';
    l_reject_link            NVARCHAR2 (300) := '';
    l_reject_prepared_link   NVARCHAR2 (300) := '';
    l_always_valid_link      NVARCHAR2 (300) := '';
    l_request_creator        request.created_by%TYPE := 0;
    l_route_to               route.route_to%TYPE := :new.route_to;
    l_foot                   NVARCHAR2 (250)
        := '<br><br>This message is auto generated by the OU Bankcard Request and Approval system. Please do not reply to this email.';
    l_requisition_number     request.requisition_number%TYPE;
    l_created_for_id         request.created_for%TYPE;
    l_supervisor_email       nist_user.email%TYPE := '';
    l_item_name              item.item_name%TYPE;
    l_item_list              VARCHAR2 (3500) := '';
    v_error_code             VARCHAR2 (30);
    v_error_message          VARCHAR2 (2000);

    --MB-382
    l_body2                  VARCHAR2 (2000) := '';
    l_sender2                NVARCHAR2 (60) := '';
    l_sender2_email          NVARCHAR2 (120) := '';
    l_receip2                NVARCHAR2 (60) := '';
    l_receip2_email          NVARCHAR2 (400) := '';
    l_send_req_to_bch        BOOLEAN := FALSE;
    l_req_cnt                NUMBER (2) := 0;
    l_requester_id           NUMBER := 0;
    l_deliv_to_home          NVARCHAR2 (1) := 'N';


    --MB-502
    l_body_extra             VARCHAR2 (4000) := '<br><br><br>Here is a brief summary of the request:';
    l_app_instruction        VARCHAR2 (200) :='<br>After the request is reviewed, please go to the [Routes] tab to approve or reject it.';
    l_description            VARCHAR2 (2000) := '';
    l_total_cost             NUMBER (10, 2);
    l_needed_by_date         DATE;
    l_notes                  VARCHAR2 (2000) := '';
    l_requester_name        VARCHAR2 (100) := '';
    l_grp_code              VARCHAR2 (10) := '';
    l_mission_critial_category_name    VARCHAR2 (200) := '';
    l_mission_critial_justification    VARCHAR2 (2000) := '';
    l_body_notify            VARCHAR2 (4000) := '';

    --add this to check for ITSO route and use the also_notify to send email to backup ITSO
    l_dynamic_type            route.dynamic_type %TYPE := :new.dynamic_type ;



    -- 2017-09-06 yy: remove.
    --for chemical and preciousmetal notivications
    --l_is_chemical ITEM.CHEMICAL%TYPE;
    --l_is_precious_metal item.is_precious_metal%TYPE;
    -- 2018-01-18 yy: removed test receip email and test title
    --2018-01-25 xw: rejected request should have use #requesttracking, not #pendingrequests

    -- 2017-10-30 yy: for cancel request
    CURSOR l_item_cur IS
          SELECT item_name
            FROM item
           WHERE request_id = l_request_id
        ORDER BY item_name;
BEGIN
    --only care if the insert is the execution of a route (exclude saved request, which doesn't need notification);
    --if route_date is null, it's just for planning and no need to send any notificaiton
    IF :new.route_date IS NOT NULL and :new.route_type_id <> 0
    THEN
        l_sender := get_user_name (:new.route_by);
        l_sender_email := get_user_email (:new.route_by);
        l_receip := get_user_name (:new.route_to);
        l_receip_email := get_user_email (:new.route_to);
        l_subject :=
               'The Bankcard Purchase Request '
            || l_request_id
            || ' (Req # '
            || TRIM (get_req_num (l_request_id))
            || ') ';

        --the purpose of email notification is to defeated if we cannot find the recipient's email
        IF l_receip_email IS NULL OR LENGTH (l_receip_email) = 0
        THEN
            raise_application_error (
                -20101,
                'Error from email_notice trigger. Email recipient is missing.');
        END IF;


        --select environment variables used for email in DEV, Test or Prod
        SELECT app_env, app_url, notification_bcc
          INTO l_app_env, l_app_url, l_app_bcc
          FROM app_settings;

        --use environment variables to form links in email
        l_link :=
               ' Please click this link to review the order: '
            || l_app_url
            || '?subview=pendingrequests/'  --'#pendingrequests/'
            || :new.request_id
            || '.';
        --|| ' to see the detail.';
        --2018-01-25 xw: rejected request should have use #requesttracking, not #pendingrequests
        --2018-01-25 xw: rejected request from requester to creator should use #preparedrequests
        l_reject_link :=
               ' Please click this link: '
            || l_app_url
            || '?subview=requesttracking/' --'#requesttracking/'
            || :new.request_id
            || ' to see the detail.';

        l_reject_prepared_link :=
               ' Please click this link: '
            || l_app_url
            || '?subview=preparedrequests/' --'#preparedrequests/'
            || :new.request_id
            || ' to see the detail.';

        --this link is in the email send from
        l_purchase_link :=
               ' Please click this link: '
            || l_app_url
            || '?subview=purchases' --'#purchases'
            || ' and select request '
            || :new.request_id
            || ' to see the detail.';

        l_received_link :=
               ' Please click this link: '
            || l_app_url
            || '?subview=historicalrequests/' --'#historicalrequests/'
            || :new.request_id
            || ' to see the detail.';

        l_always_valid_link :=
               'Please be noted that the request moves within the approval process over time and the link above may not always work. To <b>view this request at any time</b>, click this link: '
            || l_app_url
            || '?subview=requestsearching/' --'#requestsearching/'
            || :new.request_id;

        --create item list
        OPEN l_item_cur;                            -- PL/SQL evaluates factor

        LOOP
            FETCH l_item_cur INTO l_item_name;

            EXIT WHEN l_item_cur%NOTFOUND;
            l_item_list :=
                l_item_list || '&emsp;&emsp;' || l_item_name || '<br>';
        END LOOP;

        CLOSE l_item_cur;

        SELECT description,
               get_request_total_cost (request_id),
               needed_by_date,
               notes,
               get_user_name (requester_id),
               get_group_code_by_org_id(grp_id),
               GET_MISSION_CRITICAL_CATEGORY_NAME(MISSION_CRITICAL_CATEGORY_ID),
               MISSION_CRITICAL_Justification
          INTO l_description,
               l_total_cost,
               l_needed_by_date,
               l_notes,
               l_requester_name,
               l_grp_code,
               l_mission_critial_category_name,
               l_mission_critial_justification
          FROM request
         WHERE request_id = :new.request_id;
         
         
        --issue 621 
        IF l_route_type_id = 14 OR l_route_type_id = 15
        THEN
            l_body_extra := l_body_extra || '<br><br>&emsp;Official Requester: ' || l_requester_name;
            l_body_extra := l_body_extra || '<br><br>&emsp;Group: ' || l_grp_code;
            IF l_mission_critial_category_name IS NOT NULL
            THEN
                l_body_extra := l_body_extra || '<br><br>&emsp;Mission Critical Category: ' || l_mission_critial_category_name;
            END IF;
            
             IF l_mission_critial_justification IS NOT NULL
            THEN
                l_body_extra := l_body_extra || '<br><br>&emsp;Mission Critical Justification: ' || l_mission_critial_justification;
            END IF;
        
        END IF;
         

        IF l_description IS NOT NULL
        THEN
            l_body_extra := l_body_extra || '<br><br>&emsp;Description: ' || l_description;
        END IF;

        IF l_needed_by_date IS NOT NULL
        THEN
            l_body_extra :=
                   l_body_extra
                || '<br><br>&emsp;Need by Date: '
                || l_needed_by_date;
        END IF;

        IF l_notes IS NOT NULL
        THEN
            l_body_extra :=
                   l_body_extra
                || '<br><br>&emsp;Additional Comments or Instructions: '
                || l_notes;
        END IF;

        IF :new.route_notes IS NOT NULL
        THEN
            l_body_extra :=
                   l_body_extra
                || '<br><br>&emsp;Requester provided this message: '
                || :new.route_notes;
        END IF;

        l_body_extra :=
               l_body_extra
            || '<br><br>&emsp;Estimated Total Cost: $'
            || l_total_cost;
        l_body_extra :=
               l_body_extra
            || '<br><br>&emsp;Included Items: <br>'
            || l_item_list
            || '<br><br>';



        --MB-382 once a request is approved by BAO, check if the items need to deliver to requester's home
        --email instruction to requester about the BCH will contact to get the address in separate email
        IF l_route_type_id = 3
        THEN
            -- select COUNT (*) into l_req_cnt from request where REQUEST_ID = l_request_id;
            -- IF l_req_cnt = 1
            -- THEN
            SELECT requester_id, deliver_to_home
              INTO l_requester_id, l_deliv_to_home
              FROM request
             WHERE request_id = l_request_id;

            IF     l_deliv_to_home IS NOT NULL
               AND l_deliv_to_home = 'Y'
               AND l_requester_id IS NOT NULL
               AND l_requester_id > 0
            THEN
                l_send_req_to_bch := TRUE;
                --get BCH name and email
                l_sender2 := get_user_name (:new.route_to);
                l_sender2_email := get_user_email (:new.route_to);
                --get requester name and email
                l_receip2 := get_user_name (l_requester_id);
                l_receip2_email := get_user_email (l_requester_id);
            END IF;
        --END IF;
        END IF;



        --2018-6-20 xw: added a update statement to update the approval_amount in the request table so
        --it can be used to determine whether the request needs to be sent back if BCH changed the price
        /**don't need this anymore since approval amount is now updated on the route panel when the BAO approves the requestIF l_route_type_id = 3
       THEN
          UPDATE REQUEST
             SET APPROVAL_AMOUNT =get_request_total_cost (l_request_id)
           WHERE request_id = l_request_id;
       END IF;**/


        --based on route type, create email body
        CASE l_route_type_id
            WHEN 14
                THEN
                    l_body :=
                           l_sender
                        || ' approved and routed a mission critical bankcard purchase request to you for approval. '
                        || l_link
                        || l_body_extra               
                        || l_app_instruction;

                    l_body_notify :=
                           l_sender
                        || ' would like to notify you about a mission critical bankcard purchase request submission currently reviewing by the Division Chief. '
                        || l_body_extra;

             WHEN 15
                THEN
                    l_body :=
                           l_sender
                        || ' approved and routed a mission critical bankcard purchase request to you for approval. '
                        || l_link
                        || l_body_extra               
                        || l_app_instruction;

                    l_body_notify :=
                           l_sender
                        || ' would like to notify you about a mission critical bankcard purchase request submission currently reviewing by the OU Director. '
                        || l_body_extra;

            WHEN 1
            THEN
                l_body :=
                       l_sender
                    || ' submitted a bankcard purchase request to you for review. '
                    || l_link
                    || l_body_extra               
                    || l_app_instruction;

                l_body_notify :=
                       l_sender
                    || ' would like to notify you about a bankcard purchase request submission. '
                    || l_body_extra;
            WHEN 2
            THEN
                l_body :=
                       l_sender
                    || ' routed a bankcard purchase request to you for approval.'
                    || l_link
                    || l_app_instruction;
            WHEN 3
            THEN
                l_body :=
                       l_sender
                    || ' approved and routed a bankcard purchase request to you for process/order.'
                    || l_link
                    || l_app_instruction;

                --MB-382
                IF l_send_req_to_bch
                THEN
                    l_body2 :=
                           ' You requested this bankcard order to be delivered to your home address.  Since we cannot store your home address in the bankcard system, please send me the delivery address via encrypted email for this order.'
                        || l_link;
                END IF;
            --2018-01-29 xw: ordered request becomes a purchase and cannot be accessed in #pendingrequests
            --2021-02-16 BCH ordered the items and the request goes to the BCH's purchase view
            --since it's the same person, send an email to the bch doesn't make any sense.
            WHEN 4
            THEN
                l_body :=
                       l_sender
                    || ' ordered the item(s) in the bankcard purchase request for you.'
                    || l_reject_link; --reject link actually can be used for tracking submitted request

                --|| l_purchase_link;

                --2018-01-25 xw: rejected request should have use #requesttracking, not #pendingrequests
                --when 5 then l_body := l_sender || ' rejected the bankcard purchase request.' || l_reject_link;

                --MB-472 add body link for email send to the requester or other fed or associate
                SELECT created_by, created_for, requester_id
                  INTO l_request_creator, l_created_for_id, l_requester_id
                  FROM request
                 WHERE request_id = l_request_id;

                IF l_created_for_id IS NOT NULL AND l_created_for_id > 0
                THEN
                    --get associate name and email
                    l_receip := get_user_name (l_created_for_id);
                    l_receip_email := get_user_email (l_created_for_id);
                ELSE
                    --get requester name and email
                    l_receip := get_user_name (l_requester_id);
                    l_receip_email := get_user_email (l_requester_id);
                END IF;
            WHEN 5
            THEN
                --2020-03-05 app currently reoute to requester only when request is rejected so we no longer need the
                --    l_reject_prepared_link and l_cc_email. if this changes again in the future, we still cannot do cc
                --    because only the preparer can use the l_reject_prepared_link. the receipient or the cced person may not be the preparer
                --            BEGIN
                --                SELECT created_by
                --                  INTO l_request_creator
                --                  FROM request
                --                 WHERE request_id = l_request_id;
                --
                --                IF l_request_creator = l_route_to
                --                THEN
                --                    l_body :=
                --                           l_sender
                --                        || ' rejected the bankcard purchase request.'
                --                        || l_reject_prepared_link;
                --                ELSE
                --                    l_body :=
                --                           l_sender
                --                        || ' rejected the bankcard purchase request.'
                --                        || l_reject_link;
                --                END IF;
                --
                --                l_cc_email := get_user_email (l_request_creator);
                --                IF l_app_env <> 'PROD'
                --                THEN
                --                    l_cc_email := 'xinweiw@nist.gov';
                --                END IF;
                --
                --           END;

                l_body :=
                       l_sender
                    || ' rejected the bankcard purchase request.'
                    || l_reject_link;
            --03/21/2022 MB-452
            WHEN 13
            THEN
                l_body :=
                       l_sender
                    || ' returned the bankcard purchase request to you for additional info or modification.'
                    || l_reject_link;
            --2018-01-29 xw: delivered request remain in the purchase view and cannot be accessed in #pendingrequests
            WHEN 6
            THEN
                l_body :=
                       'The bankcard purchase request has been delivered.'
                    || l_purchase_link;
            --2018-01-29 xw: delivered request remain in the purchase view and cannot be accessed in #pendingrequests
            WHEN 7 --#7 might not be needed in the future. for now just keep it in case we need to come back
            THEN
                l_body :=
                       'The bankcard purchase request was received. The request record was archived for record keeping.'
                    || l_received_link;
            -- 2017-10-30 yy: if cancel, send requisition number and item name list, do not send url link.
            WHEN 8
            THEN
                BEGIN
                    SELECT requisition_number
                      INTO l_requisition_number
                      FROM request
                     WHERE request_id = l_request_id;

                    l_body :=
                           l_sender
                        || ' has canceled the bankcard purchase request. The requisition number is: '
                        || l_requisition_number
                        || '. <br><br>Items include: <br><br>'
                        || l_item_list;
                END;
            WHEN 9
            THEN
                l_body :=
                       l_sender
                    || ' rerouted a bankcard purchase request to you.'
                    || l_link;
            WHEN 10
            THEN
                l_body :=
                       l_sender
                    || ' updated the bankcard purchase request.'
                    || l_link;
            WHEN 11
            THEN
                l_body :=
                       l_sender
                    || ' re-submitted a bankcard purchase request to you for review. '
                    || l_link
                    || l_body_extra
                    || '<br>After the request is reviewed, please go to the [Routes] tab to submit or reject it.';
            --2018-01-26 xw: added notification to requester when someone prepared a request and send it to the requester for review
            WHEN 12
            THEN
                l_body :=
                       l_sender
                    || ' prepared a bankcard purchase request to you for review. '
                    || l_link
                    || l_body_extra
                    || '<br>After the request is reviewed, please go to the [Routes] tab to submit or reject it.';
            ELSE
                RETURN;
        END CASE;

        --if the insert is a dynamic route, overwrite the body msg to a different one without using 
        --fixed route words (submitted, reviewed, approved, ordered...)
        if :new.dynamic_type = 'DR' or :new.dynamic_type = 'AA' then
        l_body :=
                       l_sender
                    || ' routed a bankcard purchase request to you for review.'
                    || l_link
                    || l_body_extra
                    || l_app_instruction;
        end if;

        --issue 585: for orgs that need explicit FCO approval, they FCO approvals are auto generated and sent after BAOs approve them and have dynamic_type = 'FCO'
        if :new.dynamic_type = 'FCO' then
        l_body :=
                       l_sender
                    || ' routed a bankcard purchase request to you for approval.'
                    || l_link
                    || l_body_extra
                    || l_app_instruction;
        end if;

        if :new.dynamic_type = 'ITSO' then
        l_body :=
                       l_sender
                    || ' routed a bankcard purchase request to you for ITSO Approval.'
                    || l_link
                    || l_body_extra
                    || l_app_instruction;
        end if;

        SELECT route_type_name
          INTO l_route_type_name
          FROM lkup_route_type
         WHERE route_type_id = l_route_type_id;

        --select route_status_name into l_message from lkup_route_status where route_status_id = :new.route_status_id;

        --1/3/2023, also check if ';' is the last char becuase if only one email is selected, there's no ';'
        if l_also_notify is not null and SUBSTR (l_also_notify, -1) = ';'
        then
            --11/26/2022, fix invalid address error by removing the extra ; char in the end
            l_also_notify :=
            SUBSTR (l_also_notify, 1, LENGTH (l_also_notify) - 1);
        end if;

        --prevent dev and test email sent to users
        IF l_app_env = 'DEV'
        THEN
            l_receip_email := l_app_bcc;
            l_supervisor_email := l_app_bcc;
            l_receip2_email := l_app_bcc;
            if l_also_notify is not null
            then
                l_also_notify := l_app_bcc;
            end if;
            l_subject := 'TEST TEST TEST from Development!!! ' || l_subject;
        ELSIF l_app_env = 'TEST'
        THEN
            l_receip_email := l_app_bcc;
            l_supervisor_email := l_app_bcc;
            l_receip2_email := l_app_bcc;
            if l_also_notify is not null
            then
                l_also_notify := l_app_bcc;
            end if;
            l_subject := 'TEST TEST TEST !!!' || l_subject;
        ELSE
            l_receip_email := l_receip_email;
            l_supervisor_email := l_supervisor_email;
            l_subject := l_subject;
        END IF;

        --sent notification based on route
        --2018-01-29 xw: if sender is the one who routes the request to him or herself, there's no need to send a notification
        IF l_sender_email <> l_receip_email
        THEN
            --2022-02-25 find this code below and commented it out since we decided to not use HOST anymore in bankcard app

            -- check associate
            --so I added the additional logic in the is_associate function
            --2020-03-05 xw: if a request is submitted by an employee for an associates (l_route_status_id = 4),
            --the first review notification should send to the NIST Host for the associates
            --        IF
            --            l_route_type_id = 1
            --            AND l_route_status_id = 4
            --        THEN
            --            l_supervisor_email := is_associate(l_request_id);
            --            IF l_supervisor_email IS NOT NULL THEN
            --                l_body := l_sender || ' submitted a bankcard purchase request for an associate under your supervision. Please check the link to verify it.';
            --                IF l_app_env <> 'PROD' THEN
            --                    l_supervisor_email := 'xinweiw@nist.gov';
            --                END IF;
            --                up_send_email(l_sender_email, l_supervisor_email, l_cc_email, l_app_bcc, l_subject
            --                    --|| :new.request_id
            --                 || ' has been made for an associate',
            --                             'Dear supervisor:<br><br>' --|| l_body || '<br><br>Email should send to ' || l_supervisor_email || '.' || l_link || l_foot);
            --                             || l_body
            --                             || l_link
            --                             || '<br><br>'
            --                             || l_always_valid_link
            --                             || l_foot);
            --
            --            END IF;

            --        ELSIF l_route_type_id <> 7 THEN -- 7 is received.

            IF l_route_type_id <> 7
            THEN                                             -- 7 is received.
                up_send_email (
                    l_sender_email,
                    l_receip_email,
                    l_cc_email,
                    l_app_bcc,
                    l_subject                             --|| :new.request_id
                              || ' has been ' || l_route_type_name,
                       'Dear '
                    || l_receip
                    || ':<br><br>'
                    || l_body
                    || '<br><br>'
                    || l_always_valid_link
                    || l_foot);
            END IF;
        END IF;

        --also notify via share for submission
        --11/16/2022, add the check l_dynamic_type is null so only submission would send this email (because currently ITSO route may meet the first two conditions too)
        IF l_route_type_id = 1 AND l_also_notify IS NOT NULL AND l_dynamic_type is null
        THEN
            up_send_email (
                l_sender_email,
                l_also_notify,
                l_cc_email,
                l_app_bcc,
                l_subject                                 
                || ' has been ' || l_route_type_name,
                 l_body_notify
                || '<br>'
                || 'To <b>view this request at any time</b>, click this link: '
                || l_app_url
                || '?subview=requestsearching/' --'#requestsearching/'
                || :new.request_id
                || '</b>'
                || l_foot);
        END IF;

         --also notify any backup ITSO for ITSO approval

        IF l_dynamic_type = 'ITSO' AND l_also_notify IS NOT NULL
        THEN
            up_send_email (
                l_sender_email,
                l_also_notify,
                l_cc_email,
                l_app_bcc,
                l_subject                              
                || ' requires ITSO approval. ',
                'Dear Deputy ITSO, <br>we want to notify you about a IT bankcard purchase request that requires a ITSO approval.' 
                || l_link
                || l_body_extra
                || '<br><br>'
                || 'To <b>view this request at any time</b>, click this link: '
                || l_app_url
                || '?subview=requestsearching/' --'#requestsearching/'
                || :new.request_id
                || '</b>'
                || l_foot);
        END IF;

        --MB-382 notify requester if deliver to home is true and BAO approved
        IF l_send_req_to_bch AND l_sender_email <> l_receip_email
        THEN
            up_send_email (
                l_sender2_email,
                l_receip2_email,
                l_cc_email,
                l_app_bcc,
                l_subject                                    --|| l_request_id
                          || ' has been ' || l_route_type_name,
                   'Dear '
                || l_receip2
                || ':<br><br>'
                || l_body2
                || '<br><br>'
                || l_always_valid_link
                || l_foot);
        END IF;
    END IF;
--if submitting a request, sent email for chemical and precious metal items
/*  2017-09-06 youchun yao: move the chemical notification to up_check_submit_request and up_notify_cispro
    if l_route_type_id = 1 then
        l_subject := '';
        l_body := '';

        OPEN l_item_cur;  -- PL/SQL evaluates factor
        LOOP
        FETCH l_item_cur INTO l_is_chemical, l_is_precious_metal;
        EXIT WHEN l_item_cur%NOTFOUND;
            -- chemical
            if l_is_chemical = 'Y' then
                l_subject := 'TEST TEST TEST!!! The Bankcard Purchase Request '  || l_request_id || ' for chemical item';
                l_body := 'Dear chemical regulator:<br><br>' || l_sender || ' submitted a bankcard purchase request to purchase chemical item. ';
            --precious metal
            elsif l_is_precious_metal = 1 then
                l_subject := 'TEST TEST TEST!!! The Bankcard Purchase Request '  || l_request_id || ' for precious metal item';
                l_body := 'Dear precious metal regulator:<br><br>' || l_sender || ' submitted a bankcard purchase request to purchase precious metal item. ';
            end if;
            if length(l_subject) > 0 then
                up_send_email(
                    l_sender_email,
                    l_test_receip_email,
                    l_test_cc_email,
                    l_subject,
                    l_body || l_link || l_foot);
            end if;
            l_subject := '';

        END LOOP;
        CLOSE l_item_cur;
    end if;
*/

EXCEPTION
    WHEN OTHERS
    THEN
        v_error_code := SQLCODE;
        v_error_message := SUBSTR (SQLERRM, 1, 2000);

        INSERT INTO db_error (source,
                              user_id,
                              code,
                              MESSAGE)
             VALUES ('email_notice',
                     :new.request_id,
                     v_error_code,
                     v_error_message);
END;
/

--reconpile all invalid db objects
BEGIN
    FOR obj IN (SELECT object_name, object_type 
                FROM all_objects 
                WHERE owner = 'BCPMS_OWNER' 
                  AND status = 'INVALID') 
    LOOP
        BEGIN
            IF obj.object_type IN ('PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'FUNCTION', 'TRIGGER', 'VIEW') THEN
                EXECUTE IMMEDIATE 'ALTER ' || obj.object_type || ' ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE';
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