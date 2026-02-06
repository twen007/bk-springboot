CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_billed(
        p_item_id IN INTEGER,
        p_transaction_number IN VARCHAR2,
        p_statement_date IN TIMESTAMP,
        o_row_count OUT INTEGER)
IS
BEGIN

    o_row_count := 0;

    UPDATE
        item
    SET
        transaction_number = NVL(p_transaction_number, transaction_number),
        statement_date = NVL(p_statement_date, statement_date)
    WHERE
        item_id = p_item_id;

    o_row_count := sql%rowcount;

END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_UPDATE_BILLED FOR BCPMS_OWNER.SP_UPDATE_BILLED;


AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_BILLED BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_UPDATE_BILLED BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_UPDATE_BILLED TO BCPMS_APP;