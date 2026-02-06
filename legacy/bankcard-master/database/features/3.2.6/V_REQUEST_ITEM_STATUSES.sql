/* a view contains requests with their items' current statuses(unique) */
DROP VIEW BCPMS_OWNER.V_REQUEST_ITEM_STATUSES;

CREATE OR REPLACE FORCE VIEW BCPMS_OWNER.V_REQUEST_ITEM_STATUSES
(
    REQUEST_ID,
    ITEM_STATUSES
)
BEQUEATH DEFINER
AS
      SELECT request_id,
             LISTAGG (item_status_type_id, ',' ON OVERFLOW TRUNCATE)
                 WITHIN GROUP (ORDER BY item_status_type_id)    AS item_statuses
        FROM (SELECT DISTINCT a.request_id, d.item_status_type_id
                FROM request a, item b, v_item_current_status d
               WHERE a.request_id = b.request_id AND b.item_id = d.item_id)
    GROUP BY request_id;