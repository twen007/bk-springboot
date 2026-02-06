-- increase from 100 to 400 since it may contain many email addresses
ALTER TABLE BCPMS_OWNER.ROUTE
MODIFY(ALSO_NOTIFY VARCHAR2(400 BYTE));