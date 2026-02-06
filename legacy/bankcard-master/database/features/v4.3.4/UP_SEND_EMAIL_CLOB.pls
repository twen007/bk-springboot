create or replace procedure             UP_SEND_EMAIL_CLOB (
p_sender in nvarchar2, 
p_receip in nvarchar2,
p_cc in nvarchar2 := null,
p_bcc in nvarchar2 := null,
p_subject in nvarchar2,
p_message in CLOB
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