CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_check_submit_request (
    p_request_id       INT,
    p_rc           OUT NVARCHAR2)
IS
    TYPE v_vendor_type IS TABLE OF request_vendor%ROWTYPE;

    v_vendor         v_vendor_type;

    TYPE v_item_type IS TABLE OF item%ROWTYPE;

    v_item           v_item_type;
    v_req_num        request.requisition_number%TYPE;
    v_org_cd         nist_group.org_cd%TYPE;
    v_request_ct     INT;
    v_has_chemical   CHAR (1) := 'N';
    v_codes          VARCHAR2 (10);
BEGIN
    /*
        2017-06-29 Youchun Yao
        1. the requisition number is generated.
        2. the official requester is recorded.
        3. If a vendor (non IT Buying Service) needs justification:
        a. If the vendor is not GSA schedule, it needs to have non GSA justification and price reasonable justification
        b. if the vendor is third party, it needs third party vendor justification
        c. if the vendor is not small business, it needs non small business justification
        4. the request contains at least one item and the item has a unit price and quantity
        5. each item must have a project task and object class

        to test:
        var rc varchar2(100);
        exec up_check_submit_request (1732, :rc);
        print rc;

        2017-09-05 add chemical checking
        2017-09-18 add group code
        2019-10-21 remove check for justification and move the logic to the step when BCH approves the purchase

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

    -- check for request_vendor for justfications
    SELECT *
      BULK COLLECT INTO v_vendor
      FROM request_vendor
     WHERE request_id = p_request_id;

    v_req_num := '';

    FOR indx IN 1 .. v_vendor.COUNT
    LOOP
        -- convenience check just
        IF v_vendor (indx).convenience_check = 'Y'
        THEN
            IF (   v_vendor (indx).convenience_check_just IS NULL
                OR v_vendor (indx).convenience_check_just = '')
            THEN
                p_rc :=
                       p_rc
                    || 'Convenience check justification is missing for vendor: '
                    || TO_CHAR (v_vendor (indx).vendor_id)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;
        /** ELSE
            -- gsa
            IF v_vendor (indx).gsa_schedule = 'N'
            THEN
                IF (   v_vendor (indx).gsa_schedule_just IS NULL
                    OR v_vendor (indx).gsa_schedule_just = '')
                THEN
                    p_rc :=
                           p_rc
                        || 'GSA schedule justification is missing for vendor: '
                        || TO_CHAR (v_vendor (indx).vendor_id)
                        || '.'
                        || CHR (13)
                        || CHR (10);
                END IF;

                -- price is reasonable
                IF    v_vendor (indx).price_is_reasonable_just IS NULL
                   OR v_vendor (indx).price_is_reasonable_just = ''
                THEN
                    p_rc :=
                           p_rc
                        || 'Price is reasonable justification is missing for vendor: '
                        || TO_CHAR (v_vendor (indx).vendor_id)
                        || '.'
                        || CHR (13)
                        || CHR (10);
                END IF;

                --third party
                IF     v_vendor (indx).third_party_vendor = 'Y'
                   AND (   v_vendor (indx).third_party_vendor_just IS NULL
                        OR v_vendor (indx).third_party_vendor_just = '')
                THEN
                    p_rc :=
                           p_rc
                        || 'Third party vendor justification is missing for vendor: '
                        || TO_CHAR (v_vendor (indx).vendor_id)
                        || '.'
                        || CHR (13)
                        || CHR (10);
                END IF;
            END IF;                                                     -- gsa

            -- small business
            IF     v_vendor (indx).small_business = 'N'
               AND (   v_vendor (indx).small_business_just IS NULL
                    OR v_vendor (indx).small_business_just = '')
            THEN
                p_rc :=
                       p_rc
                    || 'Small Business justification is missing for vendor: '
                    || TO_CHAR (v_vendor (indx).vendor_id)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF; */
        END IF;                                    -- convenience check
    END LOOP;

    -- checkk for items
    SELECT *
      BULK COLLECT INTO v_item
      FROM item
     WHERE request_id = p_request_id;

    IF v_item.COUNT = 0
    THEN
        p_rc :=
               p_rc
            || 'The request does not contain any item. '
            || CHR (13)
            || CHR (10);
    ELSE
        FOR indx2 IN 1 .. v_item.COUNT
        LOOP
            IF v_item (indx2).price IS NULL
            THEN
                p_rc :=
                       p_rc
                    || 'Unit price is missing for item: '
                    || TO_CHAR (v_item (indx2).item_id)
                    || '-'
                    || TO_CHAR (v_item (indx2).item_name)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            IF v_item (indx2).quantity IS NULL
            THEN
                p_rc :=
                       p_rc
                    || 'Quantity is missing for item: '
                    || TO_CHAR (v_item (indx2).item_id)
                    || '-'
                    || TO_CHAR (v_item (indx2).item_name)
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;

            IF v_has_chemical = 'N'
            THEN
                v_has_chemical := v_item (indx2).chemical;
            END IF;
        /* 2017-07-14 comment out.
                    if v_item(indx2).project_task is null then
                       p_rc := p_rc || 'Project task is missing for item: ' || to_char(v_item(indx2).item_id) || '-' || to_char(v_item(indx2).item_name) || '.' || chr(13) || chr(10);
                    end if;
                    if v_item(indx2).object_class is null then
                       p_rc := p_rc || 'Object class is missing for item: ' || to_char(v_item(indx2).item_id) || '-' || to_char(v_item(indx2).item_name) || '.' || chr(13) || chr(10);
                    end if;
        */
        END LOOP;
    END IF;

    IF LENGTH (p_rc) IS NULL
    THEN
        --check requisition number
        /**remove this logic becuase the req num is generated in admin portal now
         --the check req num logic is now in the server code when trying to insert a route with status =1 submitted
        SELECT requisition_number
          INTO v_req_num
          FROM request
         WHERE request_id = p_request_id;

        -- if not find, create
        IF (v_req_num IS NULL OR v_req_num = '')
        THEN
           SELECT div.org_cd
             INTO v_org_cd
             FROM request r
                  INNER JOIN nist_user u ON r.requester_id = u.people_id
                  INNER JOIN nist_division div ON u.div_org_id = div.org_id
            WHERE r.request_id = p_request_id;

           up_get_requisition_number (v_org_cd, v_req_num);

           UPDATE request
              SET requisition_number = v_req_num
            WHERE request_id = p_request_id;

           COMMIT;
        END IF;
  **/
        --get div/group code
        IF v_has_chemical = 'Y'
        THEN
            SELECT g.org_cd
              INTO v_codes
              FROM request  r
                   INNER JOIN nist_user u ON r.requester_id = u.people_id
                   INNER JOIN nist_group g ON g.org_id = u.grp_org_id
             WHERE r.request_id = p_request_id;

            p_rc := 'Y,' || v_codes;
        END IF;
    END IF;
EXCEPTION
    WHEN OTHERS
    THEN
        p_rc :=
               'Error ('
            || TO_CHAR (SQLCODE)
            || ') occurs from UP_CHECK_SUBMIT_REQUEST for request id: '
            || TO_CHAR (p_request_id)
            || '. '
            || SQLERRM;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.UP_CHECK_SUBMIT_REQUEST FOR BCPMS_OWNER.UP_CHECK_SUBMIT_REQUEST;


AUDIT RENAME ON BCPMS_OWNER.UP_CHECK_SUBMIT_REQUEST BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.UP_CHECK_SUBMIT_REQUEST BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.UP_CHECK_SUBMIT_REQUEST TO BCPMS_APP;