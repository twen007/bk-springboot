/******************************************************************************
   NAME:       UP_NOTIFY_CISPRO

   PURPOSE:    notify CISPRO Power users about chemical item purchase

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        9/26/2017   Tony Wen       1. Created this procedure.
   1.1        8/08/2019   Tony Wen       2. Added more content to email body so most of the time, the 
                                            CISPRO Power users (now CIMS Power users) don't need to go to the app
                                            for chem item info (based on Andrea's request)

******************************************************************************/
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_NOTIFY_CISPRO (
    p_request_id    INT,
    p_people_id     NVARCHAR2)
IS
    TYPE v_item_type IS TABLE OF item%ROWTYPE;

    v_item                v_item_type;
    l_subject             NVARCHAR2 (100)
        :=    'Bankcard request: Chemical order ('
           || p_request_id
           || ')';
    l_body                NVARCHAR2 (20000) := '';
    l_always_valid_link   NVARCHAR2 (300) := NULL;
    l_foot                NVARCHAR2 (200)
        := '<br><br>For MML Bankcard Request and Approval System technical support, please contact the support team via email at mml.systemshelp@nist.gov or reply to this email.';
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

    CURSOR cur_item
    IS
        SELECT ROWNUM                            AS COUNT,
               b.VENDOR_NAME,
               a.ITEM_NAME,
               a.unit_issue,
               a.PRICE,
               a.quantity,
               c.catalog_number,
               get_user_name (c.owner_id)        AS chemical_Owner,
               get_user_name (c.primary_user_id) AS primary_User,
               c.location,
               c.sub_location,
               c.cas_number,
               c.MANUFACTURER_NAME,
               c.CONTAINER_TYPE,
               c.CONTAINERS_PER_PACKAGE,
               c.AMOUNT_PER_CONTAINER,
               c.CONTAINER_TOTAL,
               c.LABLES_NEEDED,
               c.PRODUCT_URL,
               c.CISPRO_REMARKS,
               c.SPECIAL_INSTRUCTION
          FROM item a, VENDOR b, item_chemical c
         WHERE     a.VENDOR_ID = b.VENDOR_ID
               AND a.ITEM_ID = c.ITEM_ID
               AND request_id = p_request_id
               AND chemical = 'Y';
BEGIN
    /*
        2017-09-06: if user orders chemical item, need to send notification to CISPro
        2018-01-18: removed test from title
    */

    --select environment variables
    SELECT APP_ENV, APP_URL, NOTIFICATION_BCC
      INTO l_app_env, l_app_url, l_app_bcc
      FROM APP_SETTINGS;



    -- build email list
    OPEN l_email_refcur FOR l_sql;

    LOOP
        FETCH l_email_refcur INTO l_email;

        EXIT WHEN l_email_refcur%NOTFOUND;
        l_receip_email := l_receip_email || l_email || ';';
    END LOOP;

    CLOSE l_email_refcur;

    IF LENGTH (l_receip_email) IS NULL
    THEN
        l_receip_email := 'craig.vogel@nist.gov';
    END IF;


    l_body :=
           'Attention CIMS Power User(s)<br><br>'
        || 'You are receiving this email because the following chemical order was submitted to the MML Bankcard Request and Approval System. Please contact the Chemical Owner(s) listed below if you need additional information.<br><br>';

    FOR i IN cur_item
    LOOP
        l_body :=
               l_body
            || '<table border=1 style="table-layout: fixed; width: 665px;">'
            || '<b><tr><td style="width:42%">Data Field</td><td style="width:58%">Chemical #'
            || i.COUNT
            || '</td></tr></b>'
            || '<tr><td >Vendor Name</td><td>'
            || i.vendor_name
            || '</td></tr>'
            || '<tr><td>Item Name</td><td>'
            || i.ITEM_NAME
            || '</td></tr>'
            || '<tr><td>Unit</td><td>'
            || i.unit_issue
            || '</td></tr>'
            || '<tr><td>Unit Price</td><td>'
            || i.price
            || '</td></tr>'
            || '<tr><td>Quantity</td><td>'
            || i.quantity
            || '</td></tr>'
            || '<tr><td>Catalog/Item Number</td><td>'
            || i.CATALOG_NUMBER
            || '</td></tr>'
            || '<tr><td>Chemical Owner</td><td>'
            || i.chemical_Owner
            || '</td></tr>'
            || '<tr><td>Primary User</td><td>'
            || i.primary_User
            || '</td></tr>'
            || '<tr><td>Location of Chemical (building/room)</td><td>'
            || i.LOCATION
            || '</td></tr>'
            || '<tr><td>Numbered location within room, if applicable</td><td>'
            || i.sub_location
            || '</td></tr>'
            || '<tr><td>CAS#</td><td>'
            || i.cas_number
            || '</td></tr>'
            || '<tr><td>Manufacturer Name</td><td>'
            || i.MANUFACTURER_NAME
            || '</td></tr>'
            || '<tr><td>Container Type</td><td>'
            || i.CONTAINER_TYPE
            || '</td></tr>'
            || '<tr><td>Containers Per Package (e.g., 12 per case)</td><td>'
            || i.CONTAINERS_PER_PACKAGE
            || '</td></tr>'
            || '<tr><td>Amount And Unit Per Container (e.g., 5 mL)</td><td>'
            || i.AMOUNT_PER_CONTAINER
            || '</td></tr>'
            || '<tr><td>Total Number of Containers</td><td>'
            || i.CONTAINER_TOTAL
            || '</td></tr>'
            || '<tr><td>Number of Barcode Labels Needed </td><td>'
            || i.LABLES_NEEDED
            || '</td></tr>'
            || '<tr><td>Web URL for Vendor Product Page</td><td>'
            || i.PRODUCT_URL
            || '</td></tr>'
            || '<tr><td>Remarks for CIMS Power User</td><td>'
            || i.CISPRO_REMARKS
            || '</td></tr>'
            || '<tr><td>Special Instructions</td><td>'
            || i.SPECIAL_INSTRUCTION
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
           '<br><br>To <b>view detail information of the chemcial items in this request at any time</b>, click this link: '
        || l_app_url
        || '#requestsearching/'
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
                               'up_notify_cispro - '
                            || p_request_id
                            || ' - '
                            || l_receip_email,
                            p_people_id,
                            v_error_code,
                            v_error_message);
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.UP_NOTIFY_CISPRO FOR BCPMS_OWNER.UP_NOTIFY_CISPRO;


AUDIT RENAME ON BCPMS_OWNER.UP_NOTIFY_CISPRO BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.UP_NOTIFY_CISPRO BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.UP_NOTIFY_CISPRO TO BCPMS_APP;