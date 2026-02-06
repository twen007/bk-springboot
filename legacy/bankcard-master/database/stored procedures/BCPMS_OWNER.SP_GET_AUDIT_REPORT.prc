CREATE OR REPLACE PROCEDURE
    BCPMS_OWNER.sp_get_audit_report(
        p_request_id IN INTEGER,
        result_set_request OUT SYS_REFCURSOR,
        result_set_vendors OUT SYS_REFCURSOR,
        result_set_items OUT SYS_REFCURSOR)
IS
BEGIN

    OPEN result_set_request FOR
        SELECT
            request_id,
            requisition_number,
            get_user_name(requester_id) AS requester_name,
            get_user_name(created_by) AS created_by_name,
            get_user_name(reviewer_id) AS reviewer_name,
            get_user_name(bankcard_approving_official_id) AS bao_name,
            get_user_name(bankcard_holder_id) AS bh_name,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 1 AND rownum = 1) AS request_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 2 AND rownum = 1) AS reviewer_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 3 AND rownum = 1) AS bao_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 4 AND rownum = 1) AS order_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 6 AND rownum = 1) AS deliver_date
        FROM
            request r
        WHERE
            request_id = p_request_id;

    OPEN result_set_vendors FOR
        SELECT
            vendor_name,
            v.vendor_id,
            web_url,
            contact_person,
            phone,
            convenience_check,
            convenience_check_just,
            gsa_schedule,
            gsa_schedule_just,
            third_party_vendor,
            third_party_vendor_just,
            small_business,
            small_business_just
        FROM
            vendor v, request_vendor rv
        WHERE
            rv.request_id = p_request_id AND v.vendor_id = rv.vendor_id(+)
        ORDER BY
            v.vendor_id;

    OPEN result_set_items FOR
        SELECT
            item_name,
            quantity_ordered AS quantity,
            vendor_id,
            item_description,
            price_ordered AS price,
            project_task,
            object_class,
            is_shipping
        FROM
            item
        WHERE
            request_id = p_request_id
        ORDER BY
            item_id;

END;
/

CREATE OR REPLACE SYNONYM BCPMS_APP.SP_GET_AUDIT_REPORT FOR BCPMS_OWNER.SP_GET_AUDIT_REPORT;


AUDIT RENAME ON BCPMS_OWNER.SP_GET_AUDIT_REPORT BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON BCPMS_OWNER.SP_GET_AUDIT_REPORT BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_GET_AUDIT_REPORT TO BCPMS_APP;