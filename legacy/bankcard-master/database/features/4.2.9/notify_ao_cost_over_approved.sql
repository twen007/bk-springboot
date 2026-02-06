CREATE OR REPLACE PROCEDURE notify_ao_cost_over_approved AS
    CURSOR matched_records IS
        SELECT request_id,
         requisition_number,
         route_status_name,
         total_cost,
         actual_total_cost,
         approval_amount,
         bankcard_approving_official_id,
         get_user_email (bankcard_approving_official_id)     AS bao_email,
         bao_name,
         get_user_email (bankcard_holder_id) as bch_email,
         bh_name,
         items,
         notes,
         bch_comments
    FROM v_request
   WHERE     1 = 1
         AND fy = 25 
         AND (   (    route_type_id IN (4, 6, 7)
                  AND actual_total_cost > approval_amount)
              OR (route_type_id = 3 AND total_cost > approval_amount))
ORDER BY bankcard_approving_official_id;

    TYPE email_record IS RECORD (
         REQUEST_ID v_request.request_id%TYPE,
         requisition_number v_request.requisition_number%TYPE,
         route_status_name v_request.route_status_name%TYPE,
         total_cost v_request.total_cost%TYPE,
         actual_total_cost v_request.actual_total_cost%TYPE,
         approval_amount v_request.approval_amount%TYPE,
         bankcard_approving_official_id v_request.bankcard_approving_official_id%TYPE,
         bao_email nist_user.email%TYPE,
         bao_name v_request.bao_name%TYPE,
         bch_email nist_user.email%TYPE,
         bh_name v_request.bh_name%TYPE,
         items v_request.items%TYPE,
         notes v_request.notes%TYPE,
         bch_comments v_request.bch_comments%TYPE
    );
    TYPE email_table IS TABLE OF email_record;
    v_email_data email_table;
    v_current_email VARCHAR2(100);
    v_email_body CLOB; -- Use CLOB for larger email content
BEGIN

    OPEN matched_records;
    LOOP
        FETCH matched_records BULK COLLECT INTO v_email_data LIMIT 100; -- Fetch in bulk for efficiency
        EXIT WHEN v_email_data.COUNT = 0;
        FOR i IN 1 .. v_email_data.COUNT LOOP
            -- Grouping logic: Check if we are still processing the same email
            IF v_current_email IS NULL OR v_current_email != v_email_data(i).BAO_EMAIL THEN
                -- If we are switching to a new email, send the previous email (if any)
                IF v_current_email IS NOT NULL THEN
                    -- Send the email with the constructed HTML content
                    UTL_MAIL.SEND(
                        sender => 'MML.SystemsHelp@nist.gov',
                        recipients => 'xinweiw@nist.gov',  --v_current_email,
                        subject => 'Bankcard Requests - Cost exceeds Approved Amount ' || v_current_email,
                        message => v_email_body,
                        mime_type => 'text/html'
                    );
                END IF;
                -- Reset for the new email
                v_current_email := v_email_data(i).BAO_EMAIL;
                v_email_body := '<html><body>' ||
                                '<p>Dear ' || v_email_data(i).bao_name || ' ,</p>' ||
                                '<p>The following bankcard requests had actual totoal cost exceeding the approved amount:</p>' ||
                                '<table border="1" style="border-collapse: collapse;">' ||
                                '<tr><th>REQUEST_ID</th><th>APPROVAL_AMOUNT</th></tr>';
            END IF;
            -- Append the current record to the email body in HTML format
            v_email_body := v_email_body || 
                            '<tr><td>' || v_email_data(i).REQUEST_ID || 
                            '</td><td>' || v_email_data(i).APPROVAL_AMOUNT || 
                            '</td></tr>';
        END LOOP;
        -- Send the last email after exiting the loop
        IF v_current_email IS NOT NULL THEN
            v_email_body := v_email_body || '</table></body></html>'; -- Close the HTML tags
            UTL_MAIL.SEND(
                sender => 'MML.SystemsHelp@nist.gov', -- Replace with sender email
                recipients => 'xinweiw@nist.gov',  --v_current_email,
                subject => 'Bankcard Requests - Cost exceeds Approved Amount ' || v_current_email,
                message => v_email_body,
                mime_type => 'text/html'
            );
        END IF;
    END LOOP;
    CLOSE matched_records;
EXCEPTION
    WHEN OTHERS THEN
        -- Handle exceptions as needed
        RAISE;
END notify_ao_cost_over_approved;