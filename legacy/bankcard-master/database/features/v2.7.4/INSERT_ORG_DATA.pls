create or replace TRIGGER INSERT_ORG_DATA
BEFORE INSERT OR UPDATE
ON REQUEST
REFERENCING NEW AS New OLD AS Old
FOR EACH ROW
DECLARE
tmpVar NUMBER;
/******************************************************************************
   NAME:       INSERT_ORG_DATA
   PURPOSE:    

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        3/10/2021      xinweiw       1. Created this trigger.

   NOTES:

   Automatically available Auto Replace Keywords:
      Object Name:     INSERT_ORG_DATA
      Sysdate:         3/10/2021
      Date and Time:   3/10/2021, 4:56:38 PM, and 3/10/2021 4:56:38 PM
      Username:        xinweiw (set in TOAD Options, Proc Templates)
      Table Name:      REQUEST (set in the "New PL/SQL Object" dialog)
      Trigger Options:  (set in the "New PL/SQL Object" dialog)
******************************************************************************/
BEGIN
             
             select b.ou_org_id, b.div_org_id, b.grp_org_id
             into :new.ou_id, :new.div_id, :new.grp_id
 from nist_user b where b.people_id = :new.REQUESTER_ID;

   EXCEPTION
     WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
       RAISE;
END INSERT_ORG_DATA;