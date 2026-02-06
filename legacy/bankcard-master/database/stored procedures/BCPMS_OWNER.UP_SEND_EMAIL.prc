CREATE OR REPLACE PROCEDURE BCPMS_OWNER.UP_SEND_EMAIL (
p_sender in nvarchar2, 
p_receip in nvarchar2,
p_cc in nvarchar2 := null,
p_bcc in nvarchar2 := null,
p_subject in nvarchar2,
p_message in nvarchar2
)
AS 
l_rc varchar2(1) := 'N';
--l_type varchar2(50) := 'text/plan; charset=us_ascii';
l_type varchar2(50) := 'text/html; charset=windows-1250';
begin
    utl_mail.send (
        p_sender,
        p_receip,
        p_cc, -- cc
        p_bcc, -- bcc
        p_subject,
        p_message,
        l_type, --v_type, 
        null, --priority
        null); -- replyto
    l_rc := 'Y';
end;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.UP_SEND_EMAIL FOR BCPMS_OWNER.UP_SEND_EMAIL;


AUDIT RENAME ON BCPMS_OWNER.UP_SEND_EMAIL BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.UP_SEND_EMAIL BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.UP_SEND_EMAIL TO BCPMS_APP;