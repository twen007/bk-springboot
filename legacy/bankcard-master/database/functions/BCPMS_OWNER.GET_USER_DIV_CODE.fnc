CREATE OR REPLACE FUNCTION BCPMS_OWNER.get_user_div_code (
   p_people_id   IN NIST_USER.PEOPLE_ID%TYPE)
   RETURN NVARCHAR2
IS
   l_div_code   NVARCHAR2 (10) := '';
BEGIN
   SELECT org_cd
     INTO l_div_code
     FROM nist_user u
          INNER JOIN nist_division div ON u.div_org_id = div.org_id
    WHERE people_id = p_people_id;

   RETURN l_div_code;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.GET_USER_DIV_CODE FOR BCPMS_OWNER.GET_USER_DIV_CODE;


GRANT EXECUTE ON BCPMS_OWNER.GET_USER_DIV_CODE TO BCPMS_APP;