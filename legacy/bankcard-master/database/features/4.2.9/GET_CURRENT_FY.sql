CREATE OR REPLACE FUNCTION GET_CURRENT_FY RETURN INTEGER IS
    v_today DATE := SYSDATE; -- Get today's date
    v_year VARCHAR2(2);
BEGIN
    -- Check if today's date is less than or equal to September 30th of the current year
    IF TO_CHAR(v_today, 'MMDD') <= '0930' THEN
        -- If true, return the two-digit year of the current year
        v_year := MOD(EXTRACT(YEAR FROM v_today), 100);
    ELSE
        -- If false, return the two-digit year of the next year
        v_year := MOD(EXTRACT(YEAR FROM v_today) + 1, 100);
    END IF;
    RETURN v_year;
END GET_CURRENT_FY;
