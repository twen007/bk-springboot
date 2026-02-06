--added this table to store detailed staff grants
--may add a admin UI to manage it later
--user from a different OU can add a record to gain access to requests from a OU
--e.g. to grant Joe access to MML at OU level (joe is a OU ITSO, for example), use this
-- Insert into BCPMS_OWNER.USER_DETAILED
--   (PEOPLE_ID, OU_ORG_ID, ACCESS_GROUP, ACCESS_DIV, ACCESS_OU, 
--    VALID_UNTIL_DATE)
-- Values
--   (16774, 13213, 'Y', 'Y', 'Y', 
--    TO_DATE('10/8/2023', 'MM/DD/YYYY'));

--if only grant Joe a MML division 630 access, for example, as a temporary bankcard approving official, use this
-- Insert into BCPMS_OWNER.USER_DETAILED
--   (PEOPLE_ID, OU_ORG_ID, DIV_ORG_ID, ACCESS_GROUP, ACCESS_DIV, ACCESS_OU, 
--    VALID_UNTIL_DATE)
-- Values
--   (16774, 13213, 13252 'N', 'Y', 'Y', 
--    TO_DATE('10/8/2023', 'MM/DD/YYYY'));

GRANT SELECT ON BCPMS_OWNER.USER_DETAILED TO BCPMS_APP;

--remember to create synonyms in the app schema for the added table
CREATE SYNONYM BCPMS_APP.USER_DETAILED FOR BCPMS_OWNER.USER_DETAILED;
