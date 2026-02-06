CREATE OR REPLACE PROCEDURE BCPMS_OWNER.copy_data_from_cbs
IS
   v_error_code   VARCHAR2 (1000);
   v_count        INTEGER;
/******************************************************************************
   NAME:       copy_data_from_cbs

   PURPOSE:    copy employee data from the CPR to our NIST_USER table (for performance purpose)

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        3/09/2018   Tony Wen       1. Created this procedure.

   NOTES: This procedure will be run by a sheduled job

******************************************************************************/
BEGIN
   DELETE FROM project_task;

   INSERT INTO project_task
      (SELECT 
   PROJECT_CODE, LPAD(FUND_CODE, 4, '0') as FUND_CODE, P_ORG1_CODE, 
   P_ORG2_CODE, P_ORG3_CODE, LPAD(P_ORG4_CODE, 2, '0') as P_ORG4_CODE, 
   LPAD(P_ORG5_CODE, 2, '0') as P_ORG5_CODE, LPAD(P_ORG6_CODE, 2, '0') as P_ORG6_CODE, LPAD(P_ORG7_CODE, 2, '0') as P_ORG7_CODE, 
   PROJECT_DESCR, PROJECT_TYPE, TASK_CODE, 
   TASK_DESCR, BEGIN_DATE
FROM CBSIFUSR.CBS_NIST_PROJ_TASK_MV);

   DELETE FROM object_class;

   INSERT INTO object_class
       (SELECT LPAD(object1_code, 2, '0') || '-' || LPAD(object2_code, 2, '0') || '-' || LPAD(object3_code, 2, '0') || '-' || LPAD(object4_code, 2, '0') AS code,
          object4_descr AS description, OMB_OBJECT_CLASS,CF_CATEGORY,GOVERNMENTAL_FLAG,ACTIVE_STATUS,STATUS_DATE,USER_NAME,MODIFICATION_DATE,DEVICE_NAME
     FROM CBSIFUSR.OBJECT4
     where object1_code in (22,23,24,25,26,31) ) order by object1_code;

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
         SYS_CONTEXT ('userenv', 'db_name') || ' - COPY_DATA_FROM_CBS failed',
         v_error_code);
END copy_data_from_cbs;
/


AUDIT RENAME ON BCPMS_OWNER.COPY_DATA_FROM_CBS BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.COPY_DATA_FROM_CBS BY ACCESS WHENEVER NOT SUCCESSFUL;