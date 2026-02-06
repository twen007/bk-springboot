CREATE OR REPLACE FUNCTION get_emp_emails(p_emp_ids IN VARCHAR2) 
RETURN VARCHAR2 
IS
    l_emails VARCHAR2(4000);
BEGIN
    -- Initialize the emails string
    l_emails := '';
    -- Split the input string by comma and loop through each ID
    FOR r IN (
        SELECT email 
        FROM nist_user_active a
        WHERE a.people_id IN (
            SELECT TRIM(REGEXP_SUBSTR(p_emp_ids, '[^,]+', 1, LEVEL)) AS people_id
            FROM dual
            CONNECT BY REGEXP_SUBSTR(p_emp_ids, '[^,]+', 1, LEVEL) IS NOT NULL
        )
    )
    LOOP
        -- Concatenate emails with a semicolon
        l_emails := l_emails || r.email || ';';
    END LOOP;
    -- Remove the trailing semicolon, if any
    IF l_emails IS NOT NULL THEN
        l_emails := RTRIM(l_emails, ';');
    END IF;
    RETURN l_emails;
END;
/
