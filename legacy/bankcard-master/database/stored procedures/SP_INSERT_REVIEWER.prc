CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_insert_reviewer (
   p_people_id   IN INTEGER)
IS
   l_count   INTEGER;
BEGIN
   SELECT COUNT (*)
     INTO l_count
     FROM reviewer
    WHERE people_id = p_people_id;

   IF l_count = 0
   THEN
      INSERT INTO BCPMS_OWNER.REVIEWER (PEOPLE_ID,
                                        FIRST_NAME,
                                        LAST_NAME,
                                        MID_NAME,
                                        OU_ORG_ID,
                                        DIV_ORG_ID,
                                        GRP_ORG_ID,
                                        USERNAME,
                                        EMAIL,
                                        PHONE,
                                        LAST_UPDATE_DT,
                                        IS_DIVISION_CHIEF,
                                        IS_AO,
                                        IS_AA,
                                        IS_GROUP_LEADER,
                                        IS_SECRETARY,
                                        STAFF_TYPE,
                                        SUPERVISOR_PEOPLE_ID,
                                        SUPERVISOR_YN)
         (SELECT PEOPLE_ID,
                 FIRST_NAME,
                 LAST_NAME,
                 MID_NAME,
                 OU_ORG_ID,
                 DIV_ORG_ID,
                 GRP_ORG_ID,
                 USERNAME,
                 EMAIL,
                 PHONE,
                 LAST_UPDATE_DT,
                 IS_DIVISION_CHIEF,
                 IS_AO,
                 IS_AA,
                 IS_GROUP_LEADER,
                 IS_SECRETARY,
                 STAFF_TYPE,
                 SUPERVISOR_PEOPLE_ID,
                 'Y'
            FROM NIST_USER
           WHERE people_id = p_people_id);
   END IF;
END;
/