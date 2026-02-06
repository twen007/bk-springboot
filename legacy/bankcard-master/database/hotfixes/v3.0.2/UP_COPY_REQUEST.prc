/* Formatted on 10/5/2022 12:14:18 PM (QP5 v5.388) */
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_copy_request (
    p_request_id       INT,
    p_created_by       INT,
    p_rc           OUT INT)
IS
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
                         updated_by)
         VALUES (v_request_row.notes,
                 v_request_row.requester_id,
                 v_request_row.created_for,
                 v_request_row.is_shopping_cart,
                 v_request_row.request_id,
                 v_request_row.deliver_address,
                 p_created_by,
                 p_created_by)
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
