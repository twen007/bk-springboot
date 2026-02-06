CREATE OR REPLACE FUNCTION is_admin(p_username IN VARCHAR2)
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    -- Count the number of rows with the given username
    SELECT COUNT(*)
    INTO v_count
    FROM ADMIN_USERS
    WHERE USER_NAME = p_username;
    
    IF v_count > 0 THEN RETURN 1;
    ELSE RETURN 0;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Handle any unexpected exceptions
        RETURN 0;
END;
/