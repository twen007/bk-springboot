CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_user_privileges(
        p_username IN VARCHAR2,
        p_change_ptc IN VARCHAR2,
        p_reroute IN VARCHAR2,
        p_access_group IN VARCHAR2,
        p_access_div IN VARCHAR2,
        p_access_ou IN VARCHAR2)
IS
BEGIN
    DELETE FROM user_privileges WHERE username = LOWER(p_username);

    INSERT INTO user_privileges
        (
            username,
            change_ptc,
            reroute,
            access_group,
            access_div,
            access_ou
        )
    VALUES
        (
            LOWER(p_username),
            NVL(p_change_ptc, 'N'),
            NVL(p_reroute, 'N'),
            NVL(p_access_group, 'N'),
            NVL(p_access_div, 'N'),
            NVL(p_access_ou, 'N')
        );
    
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_UPDATE_USER_PRIVILEGES FOR BCPMS_OWNER.SP_UPDATE_USER_PRIVILEGES;


AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_USER_PRIVILEGES BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_USER_PRIVILEGES BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_UPDATE_USER_PRIVILEGES TO BCPMS_APP;