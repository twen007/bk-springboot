--INSERTING into BCPMS_OWNER.USER_DETAILED 
--need to know the user's name and the orgs that he or she need detailee access to 
--use name to find people_id, use org info to find org_id(s)
--ou_org_id is required, div and group org id can be nulls
--for ou level access, add ou id and access_ou='Y', make valid_until_date a future date(1 yr from now, maybe)
--for div level access, add ou id, div id and access_ou='N' access_div='Y' access_group='Y'
--if need multiple div or group access, add multiple rows for divs or groups
--app has server side cache of 5 minutes for detailee privilege data so any change made in the db takes 5 mins for the app to get the updates
--common querie to find info:

--select * from nist_ou where active_yn='Y'
--

--select * from nist_division where active_yn='Y'
-- and org_id=13225;

--select * from nist_group where active_yn='Y'
--where org_id=13934;

--select * from nist_user_active
--where last_name='Falcone';

--now the sample script to insert priv
SET DEFINE OFF;
Insert into BCPMS_OWNER.USER_DETAILED (PEOPLE_ID,OU_ORG_ID,DIV_ORG_ID,GRP_ORG_ID,ACCESS_OU,ACCESS_DIV,ACCESS_GROUP,VALID_UNTIL_DATE) 
values (227162,13213,13259,null,'N','Y','N',to_date('01-OCT-23','DD-MON-RR'));
Insert into BCPMS_OWNER.USER_DETAILED (PEOPLE_ID,OU_ORG_ID,DIV_ORG_ID,GRP_ORG_ID,ACCESS_OU,ACCESS_DIV,ACCESS_GROUP,VALID_UNTIL_DATE) 
values (227162,13213,13666,null,'N','Y','N',to_date('01-OCT-23','DD-MON-RR'));
