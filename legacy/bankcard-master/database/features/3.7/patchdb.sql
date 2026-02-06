--add new status
Insert into LKUP_ROUTE_STATUS (ROUTE_STATUS_ID,ROUTE_STATUS_NAME) 
values (15,'The request is returned to the preparer for additional info or modification');

--exec this block to find all requests currently returned to preparer
--then update each record with the new status 15, which is a brand new status added
--to represent requests returned to preparer
DECLARE
    CURSOR c_records IS
        select a.request_id, b.route_id, b.route_type_id, b.route_status_id from request a, route b
        where a.current_route=b.route_id
        and b.route_type_id=13 and b.route_status_id=11;

BEGIN
    FOR rec IN c_records LOOP
        -- Update each record's status_id to 15
        UPDATE route
        SET route_status_id = 15
        WHERE route_id= rec.route_id;  -- Use the primary key or unique identifier

        -- Optionally, you can commit after each update
        -- COMMIT; 
    END LOOP;

    -- Commit the transaction if you want to save all changes at once
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        -- Handle exceptions
        ROLLBACK;  -- Rollback if there is an error
        DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;




