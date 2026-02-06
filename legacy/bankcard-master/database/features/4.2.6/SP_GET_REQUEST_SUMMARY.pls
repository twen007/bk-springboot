create or replace PROCEDURE             sp_get_request_summary(
        p_request_id IN INTEGER,
        result_set_request OUT SYS_REFCURSOR,
        result_set_vendors OUT SYS_REFCURSOR,
        result_set_items OUT SYS_REFCURSOR,
        result_set_file_attachments OUT SYS_REFCURSOR)
IS
BEGIN

    OPEN result_set_request FOR
        SELECT
            request_id,
            requisition_number,
            get_user_name(requester_id) AS requester_name,
            get_user_name(created_by) AS created_by_name,
            get_user_name(reviewer_id) AS reviewer_name,
            get_user_name(division_chief_id) AS dc_name,
            get_user_name(r.funds_certifying_official_id) AS fco_name,
            get_user_name(bankcard_approving_official_id) AS bao_name,
            get_user_name(bankcard_holder_id) AS bh_name,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 1 AND rownum = 1) AS request_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 2 AND rownum = 1) AS reviewer_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 14 AND rownum = 1) AS dc_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 16 AND rownum = 1) AS fco_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 3 AND rownum = 1) AS bao_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 4 AND rownum = 1) AS order_date,
            (SELECT route_date FROM route WHERE request_id = r.request_id AND route_type_id = 6 AND rownum = 1) AS deliver_date,
            needed_by_date,
            approval_amount,
            notes,
            deliver_address,
            r.fy --issue 589
        FROM
            request r
        WHERE
            request_id = p_request_id;

    OPEN result_set_vendors FOR
        SELECT
            rv.vendor_name,
            rv.ref_vendor_id as vendor_id,
            rv.web_url,
            rv.contact_person,
            rv.phone,
            rj.convenience_check,
            rj.convenience_check_just,
            rj.gsa_schedule,
            rj.gsa_schedule_just,
            rj.third_party_vendor,
            rj.third_party_vendor_just,
            rj.small_business,
            rj.small_business_just
        FROM
            request_vendor_t rv, request_justification rj
        WHERE rv.REQUEST_ID=rj.REQUEST_ID(+)
        and rv.request_id = p_request_id ;

    OPEN result_set_items FOR
        SELECT
            item_name,
            quantity,
            vendor_id,
            item_description,
            price,
            project_task,
            object_class,
            is_shipping,
            price_ordered,
            quantity_ordered,
            catelog_number,
            transaction_number,
            statement_date
        FROM
            item
        WHERE
            request_id = p_request_id
        ORDER BY
            item_id;

    OPEN result_set_file_attachments FOR
        SELECT
            file_id,
            file_name,
            file_size,
            file_type_code,
            fa.file_category_id,
            (SELECT file_category_name FROM lkup_file_category WHERE file_category_id = fa.file_category_id) AS file_category_name
        FROM
            file_attachment fa
        WHERE
            request_id = p_request_id
        ORDER BY
            file_id;

END;