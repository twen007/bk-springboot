/* 
MB-341 modified to add PTC
fixed an issue caused by too many items in a request that listagg output more than 4000 characters, which
is over the limit Oracle allowed for the data type. (added "on overflow truncate" to truncate the extra)
*/
CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST_ITEMS
(
    REQUEST_ID,
    ITEMS
)
BEQUEATH DEFINER
AS
      SELECT request_id,
             LISTAGG (item_name, '<br>' on overflow truncate)
             WITHIN GROUP (ORDER BY
                               (CASE
                                    WHEN item_name IN
                                             ('Shipping ' || '&' || ' Handling',
                                              'Shipping Cost')
                                    THEN
                                        'zzz' || item_name
                                    ELSE
                                        item_name
                                END))    AS items
        FROM (SELECT DISTINCT request_id, item_name
                FROM (  SELECT a.request_id,
                               TO_CHAR (
                                      b.item_name
                                   || NVL2 (project_task,
                                            (' - PTC: ' || project_task),
                                            project_task)
                                   || NVL2 (item_notes,
                                            (' - Note: ' || item_notes),
                                            item_notes))    AS item_name
                          FROM request a, item b
                         WHERE A.request_id = b.request_id(+)
                      ORDER BY request_id))
    GROUP BY request_id;

AUDIT RENAME ON BCPMS_OWNER.V_REQUEST_ITEMS BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.V_REQUEST_ITEMS BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT SELECT ON BCPMS_OWNER.V_REQUEST_ITEMS TO BCPMS_APP;