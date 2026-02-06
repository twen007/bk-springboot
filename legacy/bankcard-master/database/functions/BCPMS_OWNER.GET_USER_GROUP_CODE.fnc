CREATE OR REPLACE FUNCTION BCPMS_OWNER.get_user_group_code (
   p_people_id   IN NIST_USER.PEOPLE_ID%TYPE)
   RETURN NVARCHAR2
IS
   l_group_code   NVARCHAR2 (10) := '';
BEGIN
   SELECT org_cd
     INTO l_group_code
     FROM nist_user u INNER JOIN nist_group grp ON u.grp_org_id = grp.org_id
    WHERE people_id = p_people_id;

   RETURN l_group_code;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.GET_USER_GROUP_CODE FOR BCPMS_OWNER.GET_USER_GROUP_CODE;


GRANT EXECUTE ON BCPMS_OWNER.GET_USER_GROUP_CODE TO BCPMS_APP;