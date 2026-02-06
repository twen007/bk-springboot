CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_get_user_profile(
        p_username IN VARCHAR2,
        result_set OUT SYS_REFCURSOR)
IS
BEGIN

    OPEN result_set FOR
        SELECT
            people_id,
            first_name,
            last_name,
            mid_name,
            ou_org_id,
            div_org_id,
            grp_org_id,
            username,
            email,
            phone,
            supervisor_yn,
            last_update_dt,
            staff_type,
            supervisor_people_id,
            (SELECT org_cd FROM nist_division WHERE org_id = div_org_id) AS div_org_cd,
            (SELECT org_cd FROM nist_ou WHERE org_id = ou_org_id) AS ou_org_cd,
			(SELECT org_cd FROM nist_group WHERE org_id = grp_org_id) AS group_org_cd

        FROM
            nist_user
        WHERE
            LOWER(username) = LOWER(p_username);

END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_GET_USER_PROFILE FOR BCPMS_OWNER.SP_GET_USER_PROFILE;


AUDIT RENAME ON BCPMS_OWNER.SP_GET_USER_PROFILE BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_GET_USER_PROFILE BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_GET_USER_PROFILE TO BCPMS_APP;