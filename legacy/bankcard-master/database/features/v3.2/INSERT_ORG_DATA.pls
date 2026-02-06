CREATE OR REPLACE TRIGGER insert_org_data BEFORE
    INSERT OR UPDATE ON request
    REFERENCING
            NEW AS new
            OLD AS old
    FOR EACH ROW
DECLARE
    tmpvar NUMBER;
/******************************************************************************
   NAME:       INSERT_ORG_DATA
   PURPOSE:    

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        3/10/2021      xinweiw       1. Created this trigger.
   1.1        11/10/2022     xinweiw       2. to handle the new function that allow a detailed employee to create request for a org other than the employee's current org

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

    --if backend pass 0 as the  group_id, it means we use the requester's current org data
    IF :new.grp_id = 0 THEN
        SELECT
            b.ou_org_id,
            b.div_org_id,
            b.grp_org_id
        INTO
            :new.ou_id,
            :new.div_id,
            :new.grp_id
        FROM
            nist_user b
        WHERE
            b.people_id = :new.requester_id;

    ELSE
    -- if group_id is not 0, it means the employee's request is for a different group, get the ou and div for that group
        SELECT
            b.ou_org_id,
            b.div_org_id
        INTO
            :new.ou_id,
            :new.div_id
        FROM
            nist_group b
        WHERE
            b.org_id = :new.grp_id;

    END IF;
EXCEPTION
    WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
        RAISE;
END insert_org_data;