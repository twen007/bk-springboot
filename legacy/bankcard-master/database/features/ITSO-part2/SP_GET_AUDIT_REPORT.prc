/* Formatted on 9/27/2022 2:15:07 PM (QP5 v5.360) */
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_get_audit_report (
    p_request_id         IN     INTEGER,
    result_set_request      OUT SYS_REFCURSOR,
    result_set_vendors      OUT SYS_REFCURSOR,
    result_set_items        OUT SYS_REFCURSOR,
    result_set_just         OUT SYS_REFCURSOR,
    result_set_routes       OUT SYS_REFCURSOR)
IS
BEGIN
    /**
    05/07/2021
    MB-422
    added additional column in the justification block
    removed two order bys because only single record is returned
    **/

    OPEN result_set_request FOR
        SELECT request_id,
               requisition_number,
               get_user_name (requester_id)
                   AS requester_name,
               get_user_name (created_by)
                   AS created_by_name,
               get_user_name (reviewer_id)
                   AS reviewer_name,
               get_user_name (bankcard_approving_official_id)
                   AS bao_name,
               get_user_name (bankcard_holder_id)
                   AS bh_name,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 1
                       AND ROWNUM = 1)
                   AS request_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 2
                       AND ROWNUM = 1)
                   AS reviewer_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 3
                       AND ROWNUM = 1)
                   AS bao_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 4
                       AND ROWNUM = 1)
                   AS order_date,
               (SELECT route_date
                  FROM route
                 WHERE     request_id = r.request_id
                       AND route_type_id = 6
                       AND ROWNUM = 1)
                   AS deliver_date,
               approval_amount
          FROM request r
         WHERE request_id = p_request_id;

    OPEN result_set_vendors FOR SELECT vendor_name,
                                       ref_vendor_id     AS vendor_id,
                                       web_url,
                                       contact_person,
                                       phone
                                  FROM request_vendor_t rv
                                 WHERE rv.request_id = p_request_id;

    --only one vendor per request now, no need to order by
    --ORDER BY
    --    ref_vendor_id;

    OPEN result_set_items FOR   SELECT item_name,
                                       quantity_ordered     AS quantity,
                                       vendor_id,
                                       item_description,
                                       price_ordered        AS price,
                                       project_task,
                                       object_class,
                                       is_shipping
                                  FROM item
                                 WHERE request_id = p_request_id
                              ORDER BY item_id;

    OPEN result_set_just FOR
        SELECT r.request_id,
               r.is_it_purchase,
               built_in_vendor,
               professional_org,
               convenience_check,
               convenience_check_just,
               gsa_schedule,
               gsa_schedule_just,
               third_party_vendor,
               third_party_vendor_just,
               price_is_reasonable_just,
               small_business,
               small_business_just,
               commercial_vendor,
               commercial_vendor_just
          FROM request r, request_justification rj
         WHERE     r.request_id = p_request_id
               AND r.request_id = rj.request_id(+);

    -- MB-480 get route history only for BCH and BAO approvals/re-approvals, which are required for audit purpose
    -- modified to include any ITSO approval route 
    OPEN result_set_routes FOR
          SELECT *
            FROM (SELECT rr.*, rt.route_step, 'ITSO' AS dynamic_type
                    FROM v_route_history rr, route rt
                   WHERE     rr.route_id = rt.route_id
                         AND rt.route_id = (SELECT itso_approved
                                              FROM request
                                             WHERE request_id = p_request_id)
                  UNION
                  SELECT rr.*, rt.route_step, NULL AS dynamic_type
                    FROM request r, v_route_history rr, route rt
                   WHERE     r.request_id = p_request_id
                         AND r.request_id = rr.request_id(+)
                         AND rr.route_id = rt.route_id
                         AND rr.route_type_id IN (2, 3)
                         AND rt.is_dynamic = 0)
        ORDER BY route_step;
END;
/