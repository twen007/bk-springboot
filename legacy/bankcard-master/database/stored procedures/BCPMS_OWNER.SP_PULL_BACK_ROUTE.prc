CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_pull_back_route (
   p_request_id   IN INTEGER,
   p_route_by     IN INTEGER)
IS
   l_app_url                  VARCHAR2 (200) := '';
   l_app_env                  VARCHAR2 (200) := '';
   l_route_count              INTEGER;
   l_permission_count         INTEGER;
   l_last_route_id            INTEGER;
   l_last_route_to_peopleId   INTEGER;
   l_sender_email             NVARCHAR2 (50) := '';
   l_receip                   NVARCHAR2 (50) := '';
   l_receip_email             NVARCHAR2 (100) := '';
   l_subject                  NVARCHAR2 (100)
                                 := 'The Bankcard Purchase Request  ';
BEGIN
   l_route_count := 0;
   l_permission_count := 0;

   --select environment variables
   SELECT APP_ENV, APP_URL
     INTO l_app_env, l_app_url
     FROM APP_SETTINGS;

   -- Make sure more than one route exists for the request.
   SELECT COUNT (*)
     INTO l_route_count
     FROM route
    WHERE request_id = p_request_id;

   -- Make sure the last route of the request is routed by the user.
   SELECT COUNT (*)
     INTO l_permission_count
     FROM route r
    WHERE     request_id = p_request_id
          AND route_id =
                 (SELECT MAX (route_id)
                    FROM route
                   WHERE request_id = r.request_id AND route_by = p_route_by);


   IF l_route_count <= 1
   THEN
      raise_application_error (
         -20101,
         'This request is at its initial step and cannot be pulled back.');
   END IF;

   IF l_permission_count <> 1
   THEN
      raise_application_error (
         -20102,
         'The last route of this request was not done by you. You do not have permission to pull it back.');
   END IF;


  

   SELECT route_id, route_to
     INTO l_last_route_id, l_last_route_to_peopleId
     FROM route r
    WHERE     request_id = p_request_id
          AND route_id = (SELECT MAX (route_id)
                            FROM route
                           WHERE request_id = r.request_id);


   l_sender_email := get_user_email (p_route_by);
   l_receip := get_user_name (l_last_route_to_peopleId);
   l_receip_email := get_user_email (l_last_route_to_peopleId);


   IF l_receip_email IS NULL OR LENGTH (l_receip_email) = 0
   THEN
      raise_application_error (
         -20103,
         'Error from request pull back SP. Email recipient is missing.');
   END IF;
   
    IF l_app_env = 'DEV'
   THEN
      l_receip_email := 'xinweiw@nist.gov';
      l_subject := 'TEST TEST TEST from Development!!! ' || l_subject;
   ELSIF l_app_env = 'TEST'
   THEN
      l_receip_email := 'xinweiw@nist.gov';
      l_subject := 'TEST TEST TEST !!!' || l_subject;
   ELSE
      l_receip_email := l_receip_email;
      l_subject := l_subject;
   END IF;
   
   --write the to be deleted route record to the log table
   INSERT INTO route_pullback_log (
                   route_id,
                   request_id,
                   route_type_id,
                   route_notes,
                   route_by,
                   route_date,
                   route_status_id,
                   route_to,
                   pullback_by)
   SELECT route_id,
          request_id,
          route_type_id,
          route_notes,
          route_by,
          route_date,
          route_status_id,
          route_to,
          p_route_by
   from route
   WHERE route_id = l_last_route_id;

   --delete the last route record
   --NOTE: cannot use the appoach of insert a record deplicated from the second latest route record
   --because it would prevent another pullback
   DELETE FROM route
         WHERE route_id = l_last_route_id;

   -- if sender is the one who routes the request to him or herself, there's no need to send a notification
   IF l_sender_email <> l_receip_email
   THEN
      up_send_email (
         l_sender_email,
         l_receip_email,
         NULL,
         NULL,
         l_subject || p_request_id || '  was pulled back',
            'Dear '
         || l_receip
         || ':<br><br>'
         || 'The Bankcard purchase request '
         || p_request_id
         || ' was pulled back to the previous step by the previous approver.');
   END IF;
END;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.SP_PULL_BACK_ROUTE FOR BCPMS_OWNER.SP_PULL_BACK_ROUTE;


AUDIT RENAME ON BCPMS_OWNER.SP_PULL_BACK_ROUTE BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_PULL_BACK_ROUTE BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_PULL_BACK_ROUTE TO BCPMS_APP;