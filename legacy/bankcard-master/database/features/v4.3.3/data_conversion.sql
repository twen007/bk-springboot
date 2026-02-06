CREATE OR REPLACE FUNCTION get_staff_ids(p_emails IN VARCHAR2) 
RETURN VARCHAR2 
IS
    l_ids VARCHAR2(4000);
BEGIN
    -- Initialize the IDs string
    l_ids := '';
    -- Split the input string by semicolon and loop through each email
    FOR r IN (
        SELECT people_id
        FROM nist_user_active 
        WHERE lower(email) IN (
            SELECT TRIM(REGEXP_SUBSTR(lower(p_emails), '[^;]+', 1, LEVEL)) AS email
            FROM dual
            CONNECT BY REGEXP_SUBSTR(lower(p_emails), '[^;]+', 1, LEVEL) IS NOT NULL
        )
    )
    LOOP
        -- Concatenate IDs with a comma
        l_ids := l_ids || r.people_id || ',';
    END LOOP;
    -- Remove the trailing comma, if any
    IF l_ids IS NOT NULL THEN
        l_ids := RTRIM(l_ids, ',');
    END IF;
    RETURN l_ids;
END;
/

UPDATE route
SET also_notify = get_staff_ids(also_notify)
WHERE also_notify IS NOT NULL;