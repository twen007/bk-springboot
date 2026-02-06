CREATE OR REPLACE FUNCTION BCPMS_OWNER.to_base_36 (n integer) return varchar2
is
  q integer;
  r varchar2(100);
begin
  q := n;
  while q >= 36 loop
     r := chr(mod(q,36)+case when mod(q,36) < 10 then 48 else 55 end) || r;
     q := floor(q/36);
  end loop;
  r := chr(mod(q,36)+case when mod(q,36) < 10 then 48 else 55 end) || r;
  return lpad(r,3,'0');
end;
/


CREATE OR REPLACE SYNONYM BCPMS_APP.TO_BASE_36 FOR BCPMS_OWNER.TO_BASE_36;


GRANT EXECUTE ON BCPMS_OWNER.TO_BASE_36 TO BCPMS_APP;