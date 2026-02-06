CREATE OR REPLACE FUNCTION BCPMS_OWNER.requisition_num ( division_code in VARCHAR2 )
                  RETURN VARCHAR2 IS
BEGIN
    RETURN (division_code || substr(extract(year from add_months (SYSDATE(), + 3)), 4) || to_base_36(requisition_seq.nextval)) || 'B';
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.REQUISITION_NUM FOR BCPMS_OWNER.REQUISITION_NUM;


GRANT EXECUTE ON BCPMS_OWNER.REQUISITION_NUM TO BCPMS_APP;