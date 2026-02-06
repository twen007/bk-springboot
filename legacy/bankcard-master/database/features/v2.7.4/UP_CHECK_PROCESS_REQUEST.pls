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
                    || 'GSA schedule justification is missing.<br>'
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