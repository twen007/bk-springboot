CREATE OR REPLACE FUNCTION BCPMS_OWNER.is_associate (
   p_request_id   IN request.request_id%TYPE)
   RETURN CHAR
IS
   -- this function is used in send email trigger
   l_email                  nist_user.email%TYPE := '';
   l_people_id              nist_user.people_id%TYPE;
   l_supervisor_people_id   nist_user.people_id%TYPE;
BEGIN
   SELECT people_id
     INTO l_people_id
     FROM nist_user
    WHERE     people_id = (SELECT created_for
                             FROM request
                            WHERE request_id = p_request_id)
          AND staff_type = 'NIST Associate';

   IF l_people_id IS NOT NULL
   THEN
      SELECT supervisor_people_id
        INTO l_supervisor_people_id
        FROM nist_user
       WHERE     people_id = l_people_id
             AND supervisor_people_id <> (SELECT requester_id
                                            FROM request
                                           WHERE request_id = p_request_id);

      IF l_supervisor_people_id IS NOT NULL
      THEN
         SELECT email
           INTO l_email
           FROM nist_user
          WHERE people_id = l_supervisor_people_id;
      END IF;
   END IF;

   RETURN l_email;
EXCEPTION
   WHEN NO_DATA_FOUND
   THEN
      RETURN '';
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.IS_ASSOCIATE FOR BCPMS_OWNER.IS_ASSOCIATE;


GRANT EXECUTE ON BCPMS_OWNER.IS_ASSOCIATE TO BCPMS_APP;