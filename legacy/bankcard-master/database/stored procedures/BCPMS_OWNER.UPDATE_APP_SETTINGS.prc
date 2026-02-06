CREATE OR REPLACE PROCEDURE BCPMS_OWNER.update_app_settings
IS
    v_error_code         VARCHAR2 (1000);
    p_db_name            VARCHAR2 (20) := '';
    p_app_env            VARCHAR2 (20) := 'DEV';
    p_app_url            VARCHAR2 (100)
                             := 'https://tstweb.nist.gov:7111/empbc/app/';
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

   NOTES: This procedure will be run by a scheduled job at 7am; current data replicate
   happens around 6:50am

******************************************************************************/
BEGIN
    SELECT SYS_CONTEXT ('USERENV', 'DB_NAME') INTO p_db_name FROM DUAL;

    IF p_db_name LIKE '%dvi'
    THEN
        p_app_env := 'DEV';
        p_app_url := 'https://tstweb.nist.gov:7111/empbc/app/';
    ELSIF p_db_name LIKE '%tsi'
    THEN
        p_app_env := 'TEST';
        p_app_url := 'https://tstweb.nist.gov:7109/empbc/app/';
    ELSIF p_db_name LIKE '%pdi'
    THEN
        p_app_env := 'PROD';
        p_app_url := 'https://emp.nist.gov/empbc/app/';
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