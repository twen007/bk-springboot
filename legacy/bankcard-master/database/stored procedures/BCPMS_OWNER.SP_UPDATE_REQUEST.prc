/* Formatted on 9/9/2019 5:13:14 PM (QP5 v5.313) 
09/09/2019 Tony update: instead of return a correct state by check the status, simply not updating the
columns that only make sense for purchases becuase sometimes a purchase could get pull back 
for reasons such as change BCH or update approval amount so it becomes a requests again but
with purchase cols not null and the original approach prevent any update to the request and it
gets stuck forever.

added deliver_to_home column update
*/
CREATE OR REPLACE PROCEDURE BCPMS_OWNER.sp_update_request (
    p_request_id                       IN     INTEGER,
    p_requester_id                     IN     INTEGER,
    p_created_for                      IN     INTEGER,
    p_updated_by                       IN     INTEGER,
    p_notes                            IN     VARCHAR2,
    p_deliver_address                  IN     VARCHAR2,
    p_deliver_to_home                  IN     VARCHAR2,
    p_needed_by_date                   IN     TIMESTAMP,
    p_reviewer_id                      IN     INTEGER,
    p_bankcard_approving_official_id   IN     INTEGER,
    p_bankcard_holder_id               IN     INTEGER,
    p_estimated_time_of_arrival        IN     TIMESTAMP,
    p_requisition_number               IN     VARCHAR2,
    p_division_code                    IN     VARCHAR2,
    p_order_number                     IN     VARCHAR2,
    p_gsa_session_number               IN     VARCHAR2,
    p_purchase_order_number            IN     VARCHAR2,
    p_approval_amount                  IN     NUMBER,
    o_route_in_correct_state              OUT INTEGER,
    o_result_set                          OUT SYS_REFCURSOR)
IS
    l_route_type_id   INTEGER;
    update_purchase   INTEGER;
BEGIN
    o_route_in_correct_state := 1;
    update_purchase := 1;

    IF    p_order_number IS NOT NULL
       OR p_gsa_session_number IS NOT NULL
       OR p_purchase_order_number IS NOT NULL
       OR p_estimated_time_of_arrival IS NOT NULL
    THEN
        SELECT route_type_id
          INTO l_route_type_id
          FROM route
         WHERE     request_id = p_request_id
               AND route_date = (SELECT MAX (route_date)
                                   FROM route
                                  WHERE request_id = p_request_id);

        IF l_route_type_id <> 4 AND l_route_type_id <> 6
        THEN
            /*THEN -- Must be in state of 4 or 6 to modify the request.
                o_route_in_correct_state := 0;
                OPEN o_result_set FOR
                    SELECT
                        dummy
                    FROM
                        dual
                    WHERE
                        1 = 0;
                RETURN;*/
            update_purchase := 0;
        END IF;
    END IF;

    IF update_purchase = 1
    THEN
        UPDATE request
           SET requester_id = NVL (p_requester_id, requester_id),
               created_for = NVL (p_created_for, created_for),
               updated_by = NVL (p_updated_by, updated_by),
               updated_date = SYSDATE,
               notes = NVL (p_notes, notes),
               deliver_address = NVL (p_deliver_address, deliver_address),
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = NVL (p_needed_by_date, needed_by_date),
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id,
                        bankcard_approving_official_id),
               bankcard_holder_id =
                   NVL (p_bankcard_holder_id, bankcard_holder_id),
               estimated_time_of_arrival =
                   NVL (p_estimated_time_of_arrival,
                        estimated_time_of_arrival),
               requisition_number =
                   NVL (p_requisition_number, requisition_number),
               order_number = NVL (p_order_number, order_number),
               gsa_session_number =
                   NVL (p_gsa_session_number, gsa_session_number),
               purchase_order_number =
                   NVL (p_purchase_order_number, purchase_order_number),
               approval_amount = NVL (p_approval_amount, approval_amount)
         WHERE request_id = p_request_id;
    ELSE
        UPDATE request
           SET requester_id = NVL (p_requester_id, requester_id),
               created_for = NVL (p_created_for, created_for),
               updated_by = NVL (p_updated_by, updated_by),
               updated_date = SYSDATE,
               notes = NVL (p_notes, notes),
               deliver_address = NVL (p_deliver_address, deliver_address),
               deliver_to_home=NVL (p_deliver_to_home, deliver_to_home),
               needed_by_date = NVL (p_needed_by_date, needed_by_date),
               reviewer_id = NVL (p_reviewer_id, reviewer_id),
               bankcard_approving_official_id =
                   NVL (p_bankcard_approving_official_id,
                        bankcard_approving_official_id),
               bankcard_holder_id =
                   NVL (p_bankcard_holder_id, bankcard_holder_id),
               estimated_time_of_arrival =
                   NVL (p_estimated_time_of_arrival,
                        estimated_time_of_arrival),
               requisition_number =
                   NVL (p_requisition_number, requisition_number),
               approval_amount = NVL (p_approval_amount, approval_amount)
         WHERE request_id = p_request_id;
    END IF;

    OPEN o_result_set FOR SELECT request_id,
                                 notes,
                                 requester_id,
                                 requester_name,
                                 created_by,
                                 created_by_name,
                                 created_for,
                                 created_for_name,
                                 created_date,
                                 is_shopping_cart,
                                 reference_id,
                                 updated_by,
                                 updated_date,
                                 deliver_address,
                                 deliver_to_home,
                                 vendors,
                                 items,
                                 total_cost,
                                 requisition_number,
                                 estimated_time_of_arrival,
                                 order_number,
                                 gsa_session_number,
                                 purchase_order_number,
                                 submitted_date,
                                 bch_comments,
                                 approval_amount,
                                 route_id,
                                 route_type_id,
                                 route_notes,
                                 route_by,
                                 route_by_name,
                                 route_date,
                                 route_status_id,
                                 route_to,
                                 route_to_name,
                                 route_status_name,
                                 route_type_name,
                                 ou_org_id,
                                 div_org_id,
                                 grp_org_id,
                                 needed_by_date,
                                 reviewer_id,
                                 reviewer_name,
                                 bankcard_approving_official_id,
                                 bao_name,
                                 bankcard_holder_id,
                                 bh_name
                            FROM v_request
                           WHERE request_id = p_request_id;
END;
/

CREATE OR REPLACE SYNONYM BCPMS_APP.SP_UPDATE_REQUEST FOR BCPMS_OWNER.SP_UPDATE_REQUEST;


AUDIT RENAME ON
    BCPMS_OWNER.SP_UPDATE_REQUEST
    BY ACCESS WHENEVER SUCCESSFUL;
AUDIT RENAME ON
    BCPMS_OWNER.SP_UPDATE_REQUEST
    BY ACCESS WHENEVER NOT SUCCESSFUL;

GRANT EXECUTE ON BCPMS_OWNER.SP_UPDATE_REQUEST TO BCPMS_APP;