/* Formatted on 7/8/2019 3:35:22 PM (QP5 v5.313) */
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.copy_data_from_cpr
IS
    v_error_code   VARCHAR2 (1000);
    v_count        INTEGER;
/******************************************************************************
   NAME:       copy_data_from_cpr

   PURPOSE:    copy employee data from the CPR to our NIST_USER table (for performance purpose)

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        9/26/2017   Tony Wen       1. Created this procedure.
   1.1        7/08/2019   Tony Wen       2. Add data copy for both nist_user and nist_user_active table

   NOTES: This procedure will be run by a sheduled job

******************************************************************************/
BEGIN
    DELETE FROM nist_ou;

    INSERT INTO nist_ou
        (SELECT ORG_ID,
                ORG_CD,
                ORG_NAME,
                EFFECTIVE_DT,
                ORG_ACRNM,
                ACTIVE_YN,
                LAST_UPDATE_DT
           FROM S_NIST_OU);

    DELETE FROM nist_division;

    INSERT INTO nist_division
        (SELECT * FROM S_NIST_DIVISION);

    DELETE FROM nist_group;

    INSERT INTO nist_group
        (SELECT * FROM S_NIST_group);

    DELETE FROM nist_user;

    INSERT INTO nist_user
        (SELECT PEOPLE_ID,
                FIRST_NAME,
                LAST_NAME,
                MID_NAME,
                OU_ORG_ID,
                DIV_ORG_ID,
                GRP_ORG_ID,
                USERNAME,
                EMAIL,
                PHONE,
                SUPERVISOR_YN,
                LAST_UPDATE_DT,
                IS_DIVISION_CHIEF,
                IS_AO,
                IS_AA,
                IS_GROUP_LEADER,
                IS_SECRETARY,
                STAFF_TYPE,
                SUPERVISOR_PEOPLE_ID,
                ACTIVE_YN
           FROM ALL_USERS);

    --add a step to update users to supervisors if they are in the reviewer table
    UPDATE NIST_USER a
       SET SUPERVISOR_YN = 'Y'
     WHERE a.people_id IN (SELECT people_id FROM REVIEWER);

    --copy all users, including inactive users, into the all_user table
    DELETE FROM nist_user_active;

    INSERT INTO nist_user_active
        (SELECT PEOPLE_ID,
                FIRST_NAME,
                LAST_NAME,
                MID_NAME,
                OU_ORG_ID,
                DIV_ORG_ID,
                GRP_ORG_ID,
                USERNAME,
                EMAIL,
                PHONE,
                SUPERVISOR_YN,
                LAST_UPDATE_DT,
                IS_DIVISION_CHIEF,
                IS_AO,
                IS_AA,
                IS_GROUP_LEADER,
                IS_SECRETARY,
                STAFF_TYPE,
                SUPERVISOR_PEOPLE_ID
           FROM NIST_USERS);

    --add a step to update users to supervisors if they are in the reviewer table
    UPDATE NIST_USER_ACTIVE a
       SET SUPERVISOR_YN = 'Y'
     WHERE a.people_id IN (SELECT people_id FROM REVIEWER);
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
            || ' - COPY_DATA_FROM_CPR failed',
            v_error_code);
END copy_data_from_cpr;
/