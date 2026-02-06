--for issue 594, users want to increase the unit price precision to 3
--we cannot increase precision if column is not empty
--so we have create a new column with increase precision, copy old column's values to the new column
--drop the old column and rename the new column with old column's name
ALTER TABLE BCPMS_OWNER.ITEM
ADD (PRICE_NEW NUMBER(10,3));

UPDATE BCPMS_OWNER.ITEM SET PRICE_NEW = ROUND(PRICE, 3);

ALTER TABLE BCPMS_OWNER.ITEM
DROP COLUMN PRICE;

ALTER TABLE BCPMS_OWNER.ITEM
RENAME COLUMN PRICE_NEW TO PRICE;

--Item table is used in many places. change to it would make many db objects invalid
--so compile all invalids after we change the item table
BEGIN
    FOR obj IN (SELECT object_name, object_type
                FROM all_objects
                WHERE owner = 'BCPMS_OWNER'
                  AND status = 'INVALID')
    LOOP
        BEGIN
            IF obj.object_type IN ('PACKAGE', 'PROCEDURE', 'FUNCTION', 'TRIGGER', 'VIEW') THEN
                EXECUTE IMMEDIATE 'ALTER ' || obj.object_type || ' ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE';
            ELSIF obj.object_type = 'PACKAGE BODY' THEN
                EXECUTE IMMEDIATE 'ALTER PACKAGE ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE BODY';
            ELSIF obj.object_type = 'TYPE' THEN
                EXECUTE IMMEDIATE 'ALTER TYPE ' || 'BCPMS_OWNER' || '.' || obj.object_name || ' COMPILE';
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Error compiling ' || obj.object_type || ' ' || obj.object_name || ': ' || SQLERRM);
        END;
    END LOOP;
END;
/

