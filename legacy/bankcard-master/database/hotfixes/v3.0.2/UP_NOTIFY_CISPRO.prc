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
        := '<br><br>For OU Bankcard Request and Approval System technical support, please contact the support team via email at mml.systemshelp@nist.gov or reply to this email.';
    l_sender_email        NVARCHAR2 (50) := 'MML.SystemsHelp@nist.gov';
    l_cc_email            NVARCHAR2 (200) := NULL;
    l_app_url             VARCHAR2 (200) := '';
    l_app_env             VARCHAR2 (200) := '';
    l_app_bcc             VARCHAR2 (200) := NULL;
    l_receip_email        NVARCHAR2 (500) := '';
    l_email_refcur        SYS_REFCURSOR;
    l_email               NVARCHAR2 (100);
    l_sql                 NVARCHAR2 (500)
        :=    'select email from nist_user where people_id in ('
           || p_people_id
           || ')';
    v_error_code          NVARCHAR2 (20);
    v_error_message       NVARCHAR2 (2000);
    v_vendor_name  REQUEST_VENDOR_T.VENDOR_NAME%TYPE;

    CURSOR cur_item
    IS
        SELECT ROWNUM  AS COUNT,
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
          FROM item a, item_chemical c
         WHERE a.ITEM_ID = c.ITEM_ID
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

    SELECT vendor_name
    into v_vendor_name
    from request_vendor_t
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
           'Attention CIMS Power User(s)<br><br>'
        || 'You are receiving this email because the following chemical order was submitted to the OU Bankcard Request and Approval System. Please contact the Chemical Owner(s) listed below if you need additional information.<br><br>';

    IF LENGTH (l_receip_email) IS NULL
    THEN
        --if no CIMS Power User, update the receip email address and email content to send it our helpbox so we can looking into the issue
        l_receip_email := 'mml.systemshelp@nist.gov';
        l_body := 'Attention NIST Org Admins<br><br>  no one was assigned CIMS Power User role for the organization this request belongs to. Please investigate!<br><br>' || l_body;
    END IF;


    
    -- add items info
    FOR i IN cur_item
    LOOP
        l_body :=
               l_body
            || '<table border=1 style="table-layout: fixed; width: 665px;">'
            || '<b><tr><td style="width:42%">Data Field</td><td style="width:58%">Chemical #'
            || i.COUNT
            || '</td></tr></b>'
            || '<tr><td >Vendor Name</td><td>'
            || v_vendor_name
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