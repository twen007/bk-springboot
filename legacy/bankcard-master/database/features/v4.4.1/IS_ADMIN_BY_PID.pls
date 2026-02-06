create or replace FUNCTION is_admin_by_pid(p_people_id IN INTEGER)
RETURN BOOLEAN
IS
    v_count NUMBER;
BEGIN
    -- Count the number of rows with the given people_id
    SELECT COUNT(*)
    INTO v_count
    FROM ADMIN_USERS a, nist_user_active b
    WHERE a.USER_NAME = b.username
    and b.people_id=p_people_id;

    IF v_count > 0 THEN RETURN TRUE;
    ELSE RETURN FALSE;
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        -- Handle any unexpected exceptions
        RETURN FALSE;
END;