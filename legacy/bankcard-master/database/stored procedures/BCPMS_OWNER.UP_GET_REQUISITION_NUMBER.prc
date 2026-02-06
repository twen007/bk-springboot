CREATE OR REPLACE PROCEDURE BCPMS_OWNER.up_get_requisition_number (p_division_code in varchar2, p_return_val out varchar2)
AS
    v_next_num integer := 1;
BEGIN
    select 
        next_number into v_next_num 
    from 
        requisition_number 
    where division_code = p_division_code for update;

    update requisition_number
    set next_number = next_number + 1
    where division_code = p_division_code;
    commit;

    p_return_val := p_division_code || substr(extract(year from add_months (SYSDATE(), + 3)), 4) || to_base_36(v_next_num) || 'B';

    exception
        when NO_DATA_FOUND then
        begin        
            insert into requisition_number (division_code, next_number) 
            values (p_division_code, 2);
            v_next_num :=1;
            commit;
            p_return_val := p_division_code || substr(extract(year from add_months (SYSDATE(), + 3)), 4) || to_base_36(v_next_num) || 'B';
        end;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.UP_GET_REQUISITION_NUMBER FOR BCPMS_OWNER.UP_GET_REQUISITION_NUMBER;


AUDIT RENAME ON BCPMS_OWNER.UP_GET_REQUISITION_NUMBER BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.UP_GET_REQUISITION_NUMBER BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.UP_GET_REQUISITION_NUMBER TO BCPMS_APP;