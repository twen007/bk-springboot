CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_CHECK_PROCESS_REQUEST (
    p_request_id       INT,
    p_rc           OUT NVARCHAR2)
IS
    v_request_ct   INT;
    v_codes        VARCHAR2 (10);

    CURSOR v_vendor_cur IS
        -- check for request_vendor for justfications
        SELECT a.*, b.VENDOR_NAME
          FROM request_vendor a, vendor b
         WHERE a.VENDOR_ID = b.VENDOR_ID AND a.request_id = p_request_id;
BEGIN
    /*

        2019-10-21 remove check for justification in the sp_check_submit_request and move the logic here
        so at the step when BCH approves the purchase, this sp is called to check for justification and prompt the user
        if required justification data is not saved.

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



    FOR i IN v_vendor_cur
    LOOP
        -- convenience check just
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
        ELSE
            -- gsa
            IF i.gsa_schedule = 'N'
            THEN
                IF (i.gsa_schedule_just IS NULL OR i.gsa_schedule_just = '')
                THEN
                    p_rc :=
                           p_rc
                        || 'GSA schedule justification is missing for vendor: '
                        || i.VENDOR_NAME
                        || '.'
                        || CHR (13)
                        || CHR (10);
                END IF;

                -- price is reasonable
                IF    i.price_is_reasonable_just IS NULL
                   OR i.price_is_reasonable_just = ''
                THEN
                    p_rc :=
                           p_rc
                        || 'Price is reasonable justification is missing for vendor: '
                        || i.VENDOR_NAME
                        || '.'
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
                        || 'Third party vendor justification is missing for vendor: '
                        || i.VENDOR_NAME
                        || '.'
                        || CHR (13)
                        || CHR (10);
                END IF;
            END IF;                                                     -- gsa

            -- small business
            IF     i.small_business = 'N'
               AND (   i.small_business_just IS NULL
                    OR i.small_business_just = '')
            THEN
                p_rc :=
                       p_rc
                    || 'Small Business justification is missing for vendor: '
                    || i.VENDOR_NAME
                    || '.'
                    || CHR (13)
                    || CHR (10);
            END IF;
        END IF;                                           -- convenience check
    END LOOP;
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
/

CREATE OR REPLACE SYNONYM BCPMS_APP.UP_CHECK_PROCESS_REQUEST FOR BCPMS_OWNER.UP_CHECK_PROCESS_REQUEST;


AUDIT RENAME ON BCPMS_OWNER.UP_CHECK_PROCESS_REQUEST BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.UP_CHECK_PROCESS_REQUEST BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.UP_CHECK_PROCESS_REQUEST TO BCPMS_APP;