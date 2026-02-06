--add these indexes, which should improve query speed a bit in some request searching senarioes

CREATE INDEX BCPMS_OWNER.IDX_ITEM_NOTES ON BCPMS_OWNER.ITEM
(ITEM_NOTES);

CREATE UNIQUE INDEX BCPMS_OWNER.IDX_CURRENT_ROUTE_ID ON BCPMS_OWNER.REQUEST
(CURRENT_ROUTE);

CREATE INDEX BCPMS_OWNER.IDX_ITEM_STATUS_ITEM_ID_FK ON BCPMS_OWNER.ITEM_STATUS
(ITEM_ID);

CREATE INDEX BCPMS_OWNER.IDX_FA_REQ_ID_FK ON BCPMS_OWNER.FILE_ATTACHMENT
(REQUEST_ID);

CREATE INDEX BCPMS_OWNER.IDX_ITEM_IS_TAGGABLE ON BCPMS_OWNER.ITEM
(IS_TAGGABLE_EQUIPMENT);



--found this SP is the only SP that used requester_name from the v_request view
--change it to use get name function call so in case we remove all the get_user_name() columns
--from the v_request query (to speed up performance), this SP still compiles

CREATE OR REPLACE PROCEDURE BCPMS_OWNER.up_notify_property_custodians (
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
    from v_request
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

