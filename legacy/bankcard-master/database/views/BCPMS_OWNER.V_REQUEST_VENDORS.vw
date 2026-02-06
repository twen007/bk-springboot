/* MB-241 added IT Buying Service if used to buy */

CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST_VENDORS
(
    REQUEST_ID,
    VENDORS
)
BEQUEATH DEFINER
AS
      SELECT request_id,
             LISTAGG (vendor_name, '<br>') WITHIN GROUP (ORDER BY vendor_name)
                 AS vendors
        FROM (SELECT a.request_id, TO_CHAR (C.vendor_name) AS vendor_name
                FROM request a, request_vendor b, vendor c
               WHERE     A.request_id = b.request_id(+)
                     AND B.vendor_id = C.vendor_id(+)
              UNION
              SELECT DISTINCT request_id, 'IT Buying Service' AS Vendor_name
                FROM item
               WHERE vendor_id = -99
              ORDER BY request_id)
    GROUP BY request_id;


CREATE OR REPLACE SYNONYM BCPMS_APP.V_REQUEST_VENDORS FOR BCPMS_OWNER.V_REQUEST_VENDORS;

AUDIT RENAME ON BCPMS_OWNER.V_REQUEST_VENDORS BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.V_REQUEST_VENDORS BY ACCESS WHENEVER NOT SUCCESSFUL;


GRANT SELECT ON BCPMS_OWNER.V_REQUEST_VENDORS TO BCPMS_APP;