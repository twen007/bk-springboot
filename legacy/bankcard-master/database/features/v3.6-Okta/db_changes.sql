SET DEFINE OFF;

CREATE OR REPLACE PROCEDURE BCPMS_OWNER.update_app_settings
IS
    v_error_code         VARCHAR2 (1000);
    p_db_name            VARCHAR2 (20) := '';
    p_app_env            VARCHAR2 (20) := 'DEV';
    p_app_url            VARCHAR2 (100)
                             --:= 'https://tstweb.nist.gov:7111/empbc/app/';
							 := 'https://tstweb.nist.gov:7111/empbc/app/index.html';
    p_notification_bcc   VARCHAR2 (20) := 'xinweiw@nist.gov';
/******************************************************************************
   NAME:       update_app_settings

   PURPOSE:    since the client wants to copy prod data to test daily and use test
               to debug prod issues, the app_settings data will be replaced by
               prod data. This SP should be called after data replicate to ensure
               the data is updated based on true db environment

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        9/26/2017   Tony Wen       1. Created this procedure.
   1.1	      06/10/2024  tony Wen       1. update app url to include index.html because it needs to match with OKTA redirect URI, which 
											is currently set as /empbc/app/index.html
										 2. when Tony is on leave and set out of office email, since his email is used in some notification as bcc
										    users would get his out of office email every time a notification is sent so make p_notification_bcc empty for production environment

   NOTES: This procedure will be run by a scheduled job at 7am; current data replicate
   happens around 6:50am

******************************************************************************/
BEGIN
    SELECT SYS_CONTEXT ('USERENV', 'DB_NAME') INTO p_db_name FROM DUAL;

    IF p_db_name LIKE '%dvi'
    THEN
        p_app_env := 'DEV';
        --p_app_url := 'https://tstweb.nist.gov:7111/empbc/app/';
		p_app_url := 'https://tstweb.nist.gov:7111/empbc/app/index.html';
    ELSIF p_db_name LIKE '%tsi'
    THEN
        p_app_env := 'TEST';
        --p_app_url := 'https://tstweb.nist.gov:7109/empbc/app/';
		p_app_url := 'https://tstweb.nist.gov:7109/empbc/app/index.html';
    ELSIF p_db_name LIKE '%pdi'
    THEN
        p_app_env := 'PROD';
        --p_app_url := 'https://emp.nist.gov/empbc/app/';
		p_app_url := 'https://emp.nist.gov/empbc/app/index.html';
		p_notification_bcc :='';
    ELSE
        UTL_MAIL.SEND (
            'xinweiw@nist.gov',
            'xinweiw@nist.gov',
            NULL,
            NULL,
               SYS_CONTEXT ('userenv', 'db_name')
            || ' - update_app_settings failed',
            'unknown db environment');
    END IF;

    UPDATE APP_SETTINGS
       SET APP_ENV = p_app_env,
           APP_URL = p_app_url,
           NOTIFICATION_BCC = p_notification_bcc;
           
EXCEPTION
    WHEN OTHERS
    THEN
        ROLLBACK;

        v_error_code := SQLERRM;
        UTL_MAIL.SEND (
            'xinweiw@nist.gov',
            'xinweiw@nist.gov',
            NULL,
            NULL,
               SYS_CONTEXT ('userenv', 'db_name')
            || ' - update_app_settings failed',
            v_error_code);
END update_app_settings;
/

--refresh data in app_settings for OKTA change and avoid getting out of office email from Tony
BEGIN
    -- Call
    BCPMS_OWNER.UPDATE_APP_SETTINGS;

    -- Transaction control
    COMMIT;
END;

--update urls for OKTA and make g_bcc empty
CREATE OR REPLACE PACKAGE BODY BCPMS_OWNER.EMAIL_PKG AS
    /*
    Created Date:                   October 2020
    Creator:                        ppg
    Purpose:                        Handles all email notifications for the NIST Employee Bankcard application
    
    10/7/21:                        added a check in CHECK_REQUEST_FOR_NO_BCH so if no match found, no email out
    10/6/22:                        only send notification if it's production; use the new v_request_current_route_n; 
    */

    g_sid                           VARCHAR2 (20)       := '';
    g_prod                          boolean             := false;
    g_app_url                       VARCHAR2 (200)      := '';
    g_dfweekday                     VARCHAR2 (9)        := 'TUESDAY';
    
    g_from                          VARCHAR2(100)       := 'empbc@nist.gov';
    g_to                            VARCHAR2 (300)      := '';
    g_cc                            VARCHAR2 (300)      := '';
    g_bcc                           VARCHAR2 (300)      := '';
    s_subject_reminder              VARCHAR2 (500)      := 'REMINDER NOTIFICATION';
    s_subject_notify                VARCHAR2 (500)      := 'NOTIFICATION OF UPCOMING NEED BY DATE';
    s_subject_check_no_bch          VARCHAR2 (500)      := 'CHECK_REQUEST_FOR_NO_BCH';
    s_subject_check_invalid_bch     VARCHAR2 (500)      := 'CHECK_REQUEST_FOR_INVALID_BCH';
    g_subject                       VARCHAR2 (500)      := '';
    g_header                        VARCHAR2 (1000)     := 'Dear ';
    s_msg_reminder                  VARCHAR2 (3000)     := 'This is a reminder that a bankcard purchase request $1 is waiting for you';
    s_msg_notify                    VARCHAR2 (3000)     := 'This is a notification that a bankcard purchase request $1 has a need by date on $2 but the purchase is still not made.';
    s_msg_check_no_bch              VARCHAR2 (3000)     := 'This is to notify you that the following requests have no BCH:<br><br>$1';
    s_msg_check_invalid_bch         VARCHAR2 (3000)     := 'This is to notify you that the following requests have an invalid BCH:<br><br>$1';
    g_msg                           VARCHAR2 (3000)     := '';
    g_msg_end                       VARCHAR2 (20)       := '';
    g_footer                        VARCHAR2 (400)      := '<br><br>This message is auto generated by the OU Bankcard Request and Approval system. Please do not reply to this email.';
    g_to_name                       VARCHAR2 (30)       := '';
    g_errcode                       VARCHAR2 (20)       := '';
    g_errmsg                        VARCHAR2 (2000)     :='';

    function IS_PROD return boolean
    is
    begin
        if g_sid is not null then return g_prod; end if;
        
        SELECT UPPER (SYS_CONTEXT ('USERENV', 'DB_NAME')) INTO g_sid FROM DUAL;
          
        if g_sid = 'MML2PDI' then g_prod := true; end if;
        
        return g_prod;
    end;
    
    procedure GET_APP_URL
    is
    begin    
        select APP_URL into g_app_url from APP_SETTINGS;

        g_app_url := g_app_url || '?subview=pendingrequests/'; -- '#pendingrequests/';

    end;
    
    procedure UPDATE_LAST_EMAIL_DATE (
        route_id                    IN ROUTE.ROUTE_ID%TYPE
    )
    is
    begin
        if route_id is null or route_id < 1 then return; end if;
        
        update ROUTE set LAST_EMAIL_DATE = sysdate where ROUTE_ID = route_id;
        
        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('SEND_EMAIL', route_id, g_errcode, g_errmsg);
    end;
    
    procedure SEND_EMAIL
    is
    begin
        if g_from is null or length (g_from) < 5 or g_to is null or length (g_to) < 5 then return; end if;
        if g_subject is null or length (g_subject) < 10 or g_msg is null or length (g_msg) < 10 then return; end if;
        
        UTL_MAIL.SEND(
            sender          => g_from,
            recipients      => g_to,
            bcc		        => g_bcc,
            subject         => g_subject,
            mime_type       => 'text/html',
            message         => g_msg
        );
        
        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('SEND_EMAIL', 0, g_errcode, g_errmsg);
    end;

    procedure REMINDER_CHECK
        /*
        Created Date:                   October 2020
        Creator:                        ppg
        Purpose:                        periodically send email notifications to remind the reviewers, BAOs and BCHs of a pending action
        To test:                        exec BCPMS_OWNER.EMAIL_PKG.REMINDER_CHECK;
        */
    is
        l_request_id                 INTEGER               := 0;
        l_route_type_id              INTEGER               := 0;
        l_to_id                      INTEGER               := 0;        
        l_route_date                 DATE                  := null;
        l_last_email_date            DATE                  := null;
        l_today_weekday              VARCHAR2 (10)         := '';
        l_pref_weekday               VARCHAR2 (10)         := '';
        l_counter                    integer               := 0;
        l_today                      VARCHAR2 (9)          := '';             
    begin
        get_app_url;
        
        select trim(to_char(SYSDATE, 'DAY')) into l_today from dual;
    
        declare cursor cur_route is
        select REQUEST_ID, ROUTE_ID, ROUTE_TYPE_ID, ROUTE_BY, REROUTE_BY, ROUTE_TO, ROUTE_DATE, LAST_EMAIL_DATE
        from ROUTE r left outer join PREFERENCES p on r.ROUTE_TO = p.PEOPLE_ID
        where (p.PREF_VALUE is null and l_today = g_dfweekday or upper(p.PREF_VALUE) = l_today)
            and ROUTE_TYPE_ID in (1,2,3,11) 
            and trunc(route_date)>sysdate-120 --add this condition to stop reminders for requests older than 120 days
            and r.ROUTE_ID = (
            select ROUTE_ID from ROUTE s
            where r.REQUEST_ID = s.REQUEST_ID
            order by ROUTE_ID desc fetch first rows only)
        order by REQUEST_ID;
        begin
            for rec in cur_route loop
                l_request_id := rec.REQUEST_ID;
                l_route_date := rec.ROUTE_DATE;
                l_last_email_date := rec.LAST_EMAIL_DATE;
                
                l_route_type_id := rec.ROUTE_TYPE_ID;
                l_to_id := rec.ROUTE_TO;
                g_to := get_user_email (l_to_id);
                g_to_name := get_user_name (l_to_id);
                
                if l_route_type_id = 1 or l_route_type_id = 11 then
                    g_msg_end := ' to review.';
                elsif l_route_type_id = 2 then
                    g_msg_end := ' to approve.';
                elsif l_route_type_id = 3 then
                    g_msg_end := ' to order.';
                end if;
                
                g_subject := s_subject_reminder;
                               
                if not is_prod then
                    l_counter := l_counter + 1;
                    --if l_counter > 3 then continue; end if; --in dev and test we don't want to receive too many emails

                    g_to := 'xinweiw@nist.gov';
                    g_bcc := g_to;
                    g_subject := '(#' || l_counter || ' Sent from ' || g_sid || ') ' || s_subject_reminder;
                end if;
                
                g_msg := g_header || g_to_name || ':<br><br>' || replace (s_msg_reminder, '$1', l_request_id || ' (' || g_app_url || l_request_id || ')') || g_msg_end || g_footer;
                
                if is_prod then    
                    send_email;
                end if;
                
                update_last_email_date (rec.ROUTE_ID);

            end loop;
        end;

        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('REMINDER_CHECK', l_request_id, g_errcode, g_errmsg);
    end;
    
    


procedure NOTIFY_BCH_OF_UP_ORDER
        /*
        Created Date:                   Decemberber 2020
        Creator:                        ppg
        Purpose:                        Notify BCHs of upcoming unpurchased orders when their need_by_date is within a defined number of days
        To test:                        exec BCPMS_OWNER.EMAIL_PKG.NOTIFY_BCH_OF_UP_ORDER;
        
        6/11/2021: changed to use v_request_current_route becuase the original query doesn't exclude records correctly
                   e.g. run original query on 6/11/21, request 36604 was purchased on 6/8/21 but was still showing in the 
                   returned records. (Megan reported)
        */
    is
        l_request_id                INTEGER               := 0;
        l_to_id                     INTEGER               := 0;
        l_nbd                       VARCHAR2 (10)         := '';
        l_threshold                 INTEGER               := 7;
        l_counter                   integer               := 0;
        
    begin
        get_app_url;
            
        select to_number (SYS_VALUE) into l_threshold from SYSTEM_VARS where SYS_KEY = 'NEED_BY_DATE_THRESHOLD';
        if l_threshold is null or l_threshold < 1 or l_threshold > 14 then l_threshold := 7; end if;
    
        declare cursor cur_route is
        select r.REQUEST_ID, BANKCARD_HOLDER_ID, to_char (NEEDED_BY_DATE,'mm/dd/yyyy') as nbd
        from 
        v_request_current_route_n r,
        --route r 
        REQUEST q
        where r.REQUEST_ID = q.REQUEST_ID
        and q.needed_by_date is not null and q.bankcard_holder_id is not null
        and exists (select * from NIST_USER where q.bankcard_holder_id = people_id)
        and trunc (NEEDED_BY_DATE) - trunc (sysdate) = l_threshold
        --and trunc (NEEDED_BY_DATE) - trunc (sysdate) < l_threshold -- for testing
        and r.ROUTE_TYPE_ID in (1,2,3,11) 
        --and r.ROUTE_ID = (
        --    select ROUTE_ID from ROUTE s
        --    where r.REQUEST_ID = s.REQUEST_ID
        --    order by r.ROUTE_ID desc fetch first rows only)
        order by r.REQUEST_ID;
        begin
            for rec in cur_route loop
                l_request_id := rec.REQUEST_ID;
                l_to_id := rec.BANKCARD_HOLDER_ID;

                l_nbd := rec.nbd;
                g_to := get_user_email (l_to_id);
                g_to_name := get_user_name (l_to_id);
                
                g_msg := g_header || g_to_name || ':<br><br>' || s_msg_notify || g_footer;
                g_msg := replace (g_msg, '$1', l_request_id || ' (' || g_app_url || l_request_id || ')');
                g_msg := replace (g_msg, '$2', l_nbd);
                
                g_subject := s_subject_notify;
                
                if not is_prod then
                    l_counter := l_counter + 1;
                    if l_counter > 3 then continue; end if; --in dev and test we don't want to receive too many emails

                    g_to := 'xinweiw@nist.gov';
                    g_bcc := g_to;
                    g_subject := '(#' || l_counter || ' Sent from ' || g_sid || ') ' || s_subject_notify;
                end if;
                
                if is_prod then 
                    --turn on this to help debug NCNR tayelor brown's issue that she received 
                    --needed by date notification for other bch's request
                    g_bcc := ''; --'xinweiw@nist.gov';
                    send_email;
                end if;
                --dbms_output.put_line ('request id = ' || l_request_id);
            end loop;
        end;
        
        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('NOTIFY_BCH_OF_UP_ORDER', l_request_id, g_errcode, g_errmsg);
                dbms_output.put_line (g_errcode || ' - ' || 'NOTIFY_BCH_OF_UP_ORDER, request id = ' || l_request_id || ', ' || g_errmsg);
    end;
	
	
	procedure CHECK_REQUEST_FOR_NO_BCH
        /*
        Created Date:                   January 2021
        Creator:                        ppg
        Purpose:                        Check requests for null BCH, if found, notify the MML Systems Support
        To test:                        exec BCPMS_OWNER.EMAIL_PKG.CHECK_REQUEST_FOR_NO_BCH;
        */
    is
            l_request_id                INTEGER               := 0;
            l_msg                       VARCHAR2 (3000)       := '';
            is_found_rec boolean := false;
    begin
        declare cursor cur_route is
        select r.REQUEST_ID, BANKCARD_HOLDER_ID
        from v_request_current_route_n r, REQUEST q
        where r.REQUEST_ID = q.REQUEST_ID
        and q.bankcard_holder_id is null
        and r.ROUTE_TYPE_ID in (1,2,3,11) 
        --and r.ROUTE_ID = (
        --    select ROUTE_ID from ROUTE s
        --    where r.REQUEST_ID = s.REQUEST_ID
        --    order by r.ROUTE_ID desc fetch first rows only)
        order by r.REQUEST_ID;
        begin
            for rec in cur_route loop
                is_found_rec := true;
                l_request_id := rec.REQUEST_ID;
                if l_msg is not null then
                    l_msg := l_msg || ',<br>';
                end if;
                l_msg := l_msg || l_request_id;
            end loop;

            g_subject := s_subject_check_no_bch;
            g_to := 'MML.SystemsHelp@nist.gov';
            g_bcc := ''; --'xinwei.wen@nist.gov';
            g_msg := g_header || 'MML Systems Supports:<br><br>' || replace (s_msg_check_no_bch, '$1', l_msg) || g_footer;
                               
            if not is_prod then
                g_to := 'xinweiw@nist.gov';
                g_bcc := g_to;
                g_subject := '(Sent from ' || g_sid || ') ' || s_subject_check_no_bch;
            end if;
              
            --use this to check for any match found; if no match, we don't really need to send the email  
            if is_found_rec and is_prod then 
                send_email;
            end if;
            
        end;
        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('CHECK_REQUEST_FOR_NO_BCH', l_request_id, g_errcode, g_errmsg);
                dbms_output.put_line (g_errcode || ' - ' || 'CHECK_REQUEST_FOR_NO_BCH, request id = ' || l_request_id || ', ' || g_errmsg);
    end;
	
	 procedure CHECK_REQUEST_FOR_INVALID_BCH
        /*
        Created Date:                   January 2021
        Creator:                        ppg
        Purpose:                        Check requests for invalid BCH, if found, notify the MML Systems Support
        To test:                        exec BCPMS_OWNER.EMAIL_PKG.CHECK_REQUEST_FOR_INVALID_BCH;
        */
    is
            l_request_id                INTEGER               := 0;
            l_msg                       VARCHAR2 (3000)       := '';
    begin
        declare cursor cur_route is
        select r.REQUEST_ID, BANKCARD_HOLDER_ID
        from v_request_current_route_n r, REQUEST q
        where r.REQUEST_ID = q.REQUEST_ID
        and q.bankcard_holder_id is not null
        and not exists (select * from NIST_USER_ACTIVE where q.bankcard_holder_id = people_id)
        and r.ROUTE_TYPE_ID in (1,2,3,11) 
        --and r.ROUTE_ID = (
        --   select ROUTE_ID from ROUTE s
        --    where r.REQUEST_ID = s.REQUEST_ID
        --    order by r.ROUTE_ID desc fetch first rows only)
        order by r.REQUEST_ID;
        
        begin
            for rec in cur_route loop
                l_request_id := rec.REQUEST_ID;
                if l_msg is not null then
                    l_msg := l_msg || ',<br>';
                end if;
                l_msg := l_msg || l_request_id;
            end loop;

            g_subject := s_subject_check_invalid_bch;
            g_to := 'MML.SystemsHelp@nist.gov';
            g_bcc := ''; --'xinwei.wen@nist.gov';
            g_msg := g_header || 'MML Systems Supports:<br><br>' || replace (s_msg_check_invalid_bch, '$1', l_msg) || g_footer;
                               
            if not is_prod then
                g_to := 'xinweiw@nist.gov';
                g_subject := '(Sent from ' || g_sid || ') ' || s_subject_check_invalid_bch;
            end if;
            
            if is_prod then    
                send_email;
            end if;
        end;
        exception
            when OTHERS then
                g_errcode := SQLCODE;
                g_errmsg := SUBSTR (SQLERRM, 1, 2000);
                insert into db_error (source, user_id, code, MESSAGE)
                values ('CHECK_REQUEST_FOR_INVALID_BCH', l_request_id, g_errcode, g_errmsg);
                dbms_output.put_line (g_errcode || ' - ' || 'CHECK_REQUEST_FOR_INVALID_BCH, request id = ' || l_request_id || ', ' || g_errmsg);
    end;

END EMAIL_PKG;
/

--update URLs for OKTA
CREATE OR REPLACE TRIGGER BCPMS_OWNER.EMAIL_NOTICE
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
               get_request_total_cost (request_id)     AS total_cost,
               needed_by_date,
               notes
          INTO l_description,
               l_total_cost,
               l_needed_by_date,
               l_notes
          FROM request
         WHERE request_id = :new.request_id;

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



--update URLs for OKTA
CREATE OR REPLACE TRIGGER BCPMS_OWNER.EMAIL_NOTICE_2
    AFTER UPDATE OF ROUTE_DATE
    ON BCPMS_OWNER.ROUTE
    REFERENCING NEW AS NEW OLD AS OLD
    FOR EACH ROW
/*
    declare

    09/22 added to do notification if the route_date (null) is updated with a timestamp, which indicated a planned route was executed
    The route with route_date = null should always be a dynamic route (DR, AA, ITSO), which could happen when route_type in (2,3,4)

    02/20/23 check for saved request route and do not run logic for it since it doesn't need any notification
    */
DECLARE
    l_app_url                VARCHAR2 (200) := '';
    l_app_env                VARCHAR2 (200) := '';
    l_app_bcc                VARCHAR2 (200) := NULL;
    l_request_id             request.request_id%TYPE := :old.request_id;
    l_sender                 NVARCHAR2 (60);
    l_sender_email           NVARCHAR2 (60);
    l_receip                 NVARCHAR2 (60);
    l_receip_email           NVARCHAR2 (200);
    l_subject                NVARCHAR2 (200);
    l_cc_email               VARCHAR2 (250) := '';
    l_body                   VARCHAR2 (1000) := '';
    l_route_type_id          route.route_type_id%TYPE := :old.route_type_id;
    l_route_type_name        VARCHAR2 (50) := '';
    l_link                   NVARCHAR2 (300) := '';
    l_always_valid_link      NVARCHAR2 (300) := '';
    l_foot                   NVARCHAR2 (250)
        := '<br><br>This message is auto generated by the OU Bankcard Request and Approval system. Please do not reply to this email.';
    v_error_code             NVARCHAR2 (30);
    v_error_message          NVARCHAR2 (2000);

BEGIN
    --only care if the update is the execution of a route
    --exclude saved request, which doesn't need notification
    IF :new.route_date IS NOT NULL and :new.route_type_id <> 0
    THEN
        l_sender := get_user_name (:new.route_by);
        l_sender_email := get_user_email (:new.route_by);
        l_receip := get_user_name (:old.route_to);
        l_receip_email := get_user_email (:old.route_to);
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
               ' Please click this link if you need the details of this order: '
            || l_app_url
            || '?subview=pendingrequests/' --'#pendingrequests/'
            || :old.request_id
            || '.';

        l_always_valid_link :=
               'Please be noted that the request moves within the approval process over time and the link above may not always work. To <b>view this request at any time</b>, click this link: '
            || l_app_url
            || '?subview=requestsearching/' --'#requestsearching/'
            || :old.request_id;


        --since all updates on route_date are approvals for a planned route(dynamic approve)
        --we can use a universal body msg
        l_body :=
               l_sender
            || ' reviewed a bankcard purchase request and routed it to you for approval.'
            || l_link;
          
        --for display request's status for email subject
        SELECT route_type_name
          INTO l_route_type_name
          FROM lkup_route_type
         WHERE route_type_id = l_route_type_id;


        --prevent dev and test email sent to users
        IF l_app_env = 'DEV'
        THEN
            l_receip_email := l_app_bcc;
            l_subject := 'TEST TEST TEST from Development!!! ' || l_subject;
        ELSIF l_app_env = 'TEST'
        THEN
            l_receip_email := l_app_bcc;
            l_subject := 'TEST TEST TEST !!!' || l_subject;
        ELSE
            l_receip_email := l_receip_email;
            l_subject := l_subject;
        END IF;

        --sent notification based on route
        --2018-01-29 xw: if sender is the one who routes the request to him or herself, there's no need to send a notification
        IF l_sender_email <> l_receip_email
        THEN                                  
                up_send_email (
                    l_sender_email,
                    l_receip_email,
                    l_cc_email,
                    l_app_bcc,
                    l_subject || ' has been ' || l_route_type_name,
                       'Dear '
                    || l_receip
                    || ':<br><br>'
                    || l_body
                    || '<br><br>'
                    || l_always_valid_link
                    || l_foot);
        END IF;
    END IF;
EXCEPTION
    WHEN OTHERS
    THEN
        v_error_code := SQLCODE;
        v_error_message := SUBSTR (SQLERRM, 1, 2000);

        INSERT INTO db_error (source,
                              user_id,
                              code,
                              MESSAGE)
             VALUES ('email_notice_2',
                     :old.request_id,
                     v_error_code,
                     v_error_message);
END EMAIL_NOTICE_2;
/


--update URLs for OKTA
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
    --BANK-563
    l_requsition_num NVARCHAR2(30);

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
    
    SELECT REQUISITION_NUMBER
    into l_requsition_num
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
           'Attention CIMS Power User(s)<br><br>'
        || 'You are receiving this email because the following chemical order (Requisition#: ' ||  l_requsition_num
        || ') was submitted to the OU Bankcard Request and Approval System. Please contact the Chemical Owner(s) listed below if you need additional information.<br><br>';

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
        || '?subview=requestsearching/' --'#requestsearching/'
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


--update URLs for OKTA
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

    SELECT requester_name
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

commit;
