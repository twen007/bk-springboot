package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.db.RequestDao.RequestQueryParameters;
import gov.nist.oism.asd.empbc.model.AuditReport;
import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.model.Item;
import gov.nist.oism.asd.empbc.model.Request;
import gov.nist.oism.asd.empbc.model.RequestRoute;
import gov.nist.oism.asd.empbc.model.RequestSummaryReport;
import gov.nist.oism.asd.empbc.model.RequestVendor;
import gov.nist.oism.asd.empbc.model.RequestJustification;
import gov.nist.oism.asd.empbc.model.Route;
import gov.nist.oism.asd.empbc.model.Vendor;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.StringUtil;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import oracle.jdbc.OracleTypes;

public class RequestDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(RequestDao.class.getSimpleName());

    public static final String REQUEST_ROUTE_KEY = "request_route_key";
    public static final String REQUEST_ROUTE_LIST_KEY = "request_route_list_key";
    public static final String ID_KEY = "id_key";
    public static final String SAVED_REQUESTS_COUNT_KEY = "saved_requests_count_key";
    public static final String ACTIVE_REQUESTS_COUNT_KEY = "active_requests_count_key";
    public static final String INBOX_REQUESTS_COUNT_KEY = "inbox_requests_count_key";
    public static final String AUDIT_REPORT_KEY = "audit_report_key";
    public static final String REQUEST_SUMMARY_REPORT_KEY = "request_summary_report_key";
    public static final String ROUTE_ID_KEY = RouteDao.ROUTE_ID_KEY;
    public static final String ROUTE_IN_CORRECT_STATE_KEY = "route_in_correct_state_key";
    public static final String PERMISSION_STATE_KEY = "permission_state_key";
    public static final String ERROR_CODE_KEY = "error_code_key";
    public static final String ERROR_MESSAGE_KEY = "error_message_key";
    public static String REQUEST_QUERY = "select * from v_request";

    //02/2024, no longer need to pass user since we don't use ouId of the user in t query anymore
    //and for an quick query like this, privilege check can be done in service layer
    public Map<String, Object> selectRequest(Integer requestId) {//, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        var sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "request_id = ?";
        // + " AND "
        // + "ou_org_id = ?";
        LOG.log(Level.INFO, "sql: {0}, request_id: {1}", new Object[]{sql, requestId});
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            //pstmt.setInt(2, user.getOuId());
            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    setRequestResults(null, results, rset);
                } else {
                    //TODO: there's a usecase where a user resumes a saved request but got error because the user
                    //changed ou. since the ou_org_id doesn't match, even though the request is still there (with previous ou id), the
                    //query returns nothing. Also, if we introduce new funtion for detailed employee to submit request for both
                    //the employee's ou and detailed ou, we will run into this issue too. We need to think a way to handle it.
                    results.put(REQUEST_ROUTE_KEY, null);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectRequestWithParameters(RequestQueryParameters parameters) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (parameters == null || StringUtil.isEmpty(parameters.getUsername())) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_get_request_with_criteria(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        LOG.log(Level.INFO, "sql: {0}", sql);
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {
            setStringParam(cstmt, "p_username", parameters.getUsername());
            setIntParam(cstmt, "p_ou_org_id", parameters.getOuId());
            setIntParam(cstmt, "p_div_org_id", parameters.getDivisionId());
            setIntParam(cstmt, "p_grp_org_id", parameters.getGroupId());
            setIntParam(cstmt, "p_requester_id", parameters.getRequesterId());
            setStringParam(cstmt, "p_requisition_number", parameters.getRequisitionNumber());
            setIntParam(cstmt, "p_route_type_id", parameters.getRouteTypeId());
            setTimestampParam(cstmt, "p_from_date", parameters.getFromDate());
            setTimestampParam(cstmt, "p_to_date", parameters.getToDate());
            setStringParam(cstmt, "p_vendor_name", parameters.getVendorName());
            setStringParam(cstmt, "p_transaction_number", parameters.getTransactionNumber());
            setStringParam(cstmt, "p_item_name", parameters.getItemName());
            setStringParam(cstmt, "p_item_statuses", parameters.getItemStatuses());
            setDoubleParam(cstmt, "p_actual_total", parameters.getActualTotal());
            setIntParam(cstmt, "p_bankcard_holder_id", parameters.getBankcardHolderId());
            setIntParam(cstmt, "p_request_id", parameters.getRequestId());
            setIntParam(cstmt, "p_fy", parameters.getFy());
            setStringParam(cstmt, "p_ptc", parameters.getPtc());
            setTimestampParam(cstmt, "p_stmt_date", parameters.getStatementDate());
            setBooleanYNParam(cstmt, "p_partial_order", parameters.getPartialOrder());
            setBooleanYNParam(cstmt, "p_taggable", parameters.getTaggable());
            setBooleanYNParam(cstmt, "p_deliver_to_home", parameters.getDelivToHome());
            setIntParam(cstmt, "p_purchase_type_id", parameters.getPurchaseTypeId());
            setIntParam(cstmt, "p_reviewer_id", parameters.getReviewerId());

            // Result set.
            cstmt.registerOutParameter("result_set", OracleTypes.CURSOR);

            cstmt.execute();

            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = cstmt.getObject("result_set", ResultSet.class);) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            String errorMessage = String.format("Exception during database operation. Parameters: %s. Error: %s", parameters, caught.getMessage());
            LOG.log(Level.SEVERE, errorMessage, caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    //not used now
    public Map<String, Object> selectRequestCounts(Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (createdBy == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "SELECT count(1) AS count FROM request WHERE created_by = ?";
        LOG.log(Level.INFO, "sql: {0}, createdBy: {1}", new Object[]{sql, createdBy});
        try ( Connection connection = getConnection(true);) {

            // Saved requests count.
            try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
                setInt(pstmt, 1, createdBy);
                try ( ResultSet rset = pstmt.executeQuery();) {
                    Integer count = 0;
                    if (rset.next()) {

                        // Created by count.
                        count = rset.getInt("count");
                        results.put(SAVED_REQUESTS_COUNT_KEY, count);
                    }
                }
            }

            // Active requests count.
            sql = "SELECT "
                    + "count(1) AS count"
                    + " FROM "
                    + "request req, route r"
                    + " WHERE "
                    + "req.request_id = r.request_id"
                    + " AND "
                    + "(req.created_by = ? OR req.requester_id = ?)"
                    + " AND "
                    + "(r.route_status_id NOT IN (10, 13))";
            LOG.info(String.format("sql: %s", sql));
            try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
                setInt(pstmt, 1, createdBy);
                setInt(pstmt, 2, createdBy);
                try ( ResultSet rset = pstmt.executeQuery();) {
                    Integer count = 0;
                    if (rset.next()) {

                        // Created by count.
                        count = rset.getInt("count");
                        results.put(ACTIVE_REQUESTS_COUNT_KEY, count);
                    }
                }
            }

            // Inbox requests count.
            sql = "SELECT "
                    + "count(1) AS count"
                    + " FROM "
                    + "route"
                    + " WHERE "
                    + "route_to = ?"
                    + " AND "
                    + "(route_status_id NOT IN (1, 10, 13))";
            LOG.info(String.format("sql: %s", sql));
            try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
                setInt(pstmt, 1, createdBy);
                try ( ResultSet rset = pstmt.executeQuery();) {
                    Integer count = 0;
                    if (rset.next()) {

                        // Created by count.
                        count = rset.getInt("count");
                        results.put(INBOX_REQUESTS_COUNT_KEY, count);
                    }
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectSavedRequestRoutes(Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "created_by = ?"
                + " AND "
                + "route_status_id in (1,12)";
        //"route_type_id = 0";
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectSubmittedRequestRoutes(Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "requester_id = ? "
                + //after adding the prepared view, the submitted view should only show requests submitted by the requester
                //"(created_by = ? OR requester_id = ?)" +
                " AND "
                + "route_type_id > 0 AND route_type_id <> 12  AND route_type_id <> 7";
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            //pstmt.setInt(2, peopleId);
            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectPendingRequestRoutes(Integer peopleId, boolean isItso) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "route_to = ?"
                + " AND ("
                //removed 5 - rejected so rejected requests would show in pending actions
                //since at BCH stage, we can do dynamic routing, the request can have type =4
                + " ( is_dynamic = 1 AND route_type_id NOT IN (0, 6, 7, 8) ) "
                + " OR "
                //or if current route is not dynamic
                + "( is_dynamic = 0 AND route_type_id NOT IN (0, 4, 6, 7, 8) )"
                + " ) ";

        //backup ITSO wants to see ITSO requets in their pendings
        if (isItso) {
            String sql2 = " select a.* from v_request a, route b "
                    + "where a.route_id=b.route_id "
                    + "and a.dynamic_type='ITSO' "
                    + "and b.also_notify like ?";
            sql = sql + " UNION " + sql2;
        }
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            if (isItso) {
                setString(pstmt, 2, "%" + peopleId + "%");
            }

            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectPreparedRequestRoutes(Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "created_by = ?"
                + " AND "
                + "requester_id <> ?"
                + " AND "
                + "route_type_id <> 0";
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            setInt(pstmt, 2, peopleId);
            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectProcessedRequestRoutes(Integer peopleId, Integer fy, String showPurchaseWithMissingStmtDt) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " r WHERE "
                + "r.route_to = ?"
                + " AND "
                + "r.route_status_id in (8,9)";

        if (fy != null) {
            sql = sql + " and r.fy = ? ";
        }

        if (showPurchaseWithMissingStmtDt.equalsIgnoreCase("true")) {
            sql = sql + " and  exists (select 1 from item i where i.request_id=r.request_id and i.statement_date is null)";
        }

        //should not use route type becuase the "reroute" type cannot show whether the request is processed or not
        //"route_type_id in (4, 6)";
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            if (fy != null) {
                pstmt.setInt(2, fy);
            }
            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectArchivedRequestRoutes(Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = RequestDao.REQUEST_QUERY
                + " WHERE "
                + "route_to = ?"
                + " AND "
                + "route_status_id = 13";
        //"route_type_id = 7";
        LOG.info(String.format("sql: %s, people_id: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            setInt(pstmt, 1, peopleId);
            List<RequestRoute> requests = new ArrayList<>();
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    setRequestResults(requests, results, rset);
                }
            }

            results.put(REQUEST_ROUTE_LIST_KEY, requests);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectAuditReport(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_get_audit_report(?, ?, ?, ?, ?, ?) }";
        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {
            setIntParam(cstmt, "p_request_id", requestId);
            cstmt.registerOutParameter("result_set_request", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_vendors", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_items", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_just", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_routes", OracleTypes.CURSOR);
            AuditReport auditReport = new AuditReport();
            cstmt.execute();
            try ( ResultSet rsetRequest = cstmt.getObject("result_set_request", ResultSet.class);  ResultSet rsetVendors = cstmt.getObject("result_set_vendors", ResultSet.class);  ResultSet rsetItems = cstmt.getObject("result_set_items", ResultSet.class);  ResultSet rsetJust = cstmt.getObject("result_set_just", ResultSet.class);  ResultSet rsetRoutes = cstmt.getObject("result_set_routes", ResultSet.class);) {
                if (rsetRequest.next()) {

                    // Request id.
                    requestId = rsetRequest.getInt("request_id");
                    if (!rsetRequest.wasNull()) {
                        auditReport.setRequestId(requestId);
                    }

                    // Requisition number.
                    auditReport.setRequisitionNumber(rsetRequest.getString("requisition_number"));

                    // Requester name.
                    auditReport.setRequesterName(rsetRequest.getString("requester_name"));

                    // Created by name.
                    auditReport.setCreatedByName(rsetRequest.getString("created_by_name"));

                    // Reviewer name.
                    auditReport.setReviewerName(rsetRequest.getString("reviewer_name"));

                    // Bao name.
                    auditReport.setBaoName(rsetRequest.getString("bao_name"));

                    // fco name.
                    auditReport.setFcoName(rsetRequest.getString("fco_name"));

                    // Bh name.
                    auditReport.setBhName(rsetRequest.getString("bh_name"));

                    // Request date.
                    auditReport.setRequestDate(getTimestamp(rsetRequest, "request_date"));

                    // Reviewer date.
                    auditReport.setReviewerDate(getTimestamp(rsetRequest, "reviewer_date"));

                    // Bao date.
                    auditReport.setBaoDate(getTimestamp(rsetRequest, "bao_date"));

                    // fco date.
                    auditReport.setFcoDate(getTimestamp(rsetRequest, "fco_date"));

                    // Order date.
                    auditReport.setOrderDate(getTimestamp(rsetRequest, "order_date"));

                    // Deliver date.
                    auditReport.setDeliverDate(getTimestamp(rsetRequest, "deliver_date"));

                    //Approval amount, MB-418
                    auditReport.setApprovalAmount(getDouble(rsetRequest, "approval_amount"));

                    //issue 619, mission critial data
                    auditReport.setMissionCriticalCategoryName(rsetRequest.getString("mission_critical_category_name"));
                    auditReport.setMissionCriticalJustification(rsetRequest.getString("mission_critical_justification"));
                }

                //vendor data 
                int vendorId = 0;
                String vendorName = "";
                if (rsetVendors.next()) {
                    RequestVendor requestVendor = new RequestVendor();
                    Vendor vendor = new Vendor();
                    requestVendor.setVendor(vendor);

                    // Vendor name.
                    vendorName = rsetVendors.getString("vendor_name");
                    vendor.setName(vendorName);

                    // Vendor id.
                    vendorId = rsetVendors.getInt("vendor_id");
                    if (!rsetVendors.wasNull()) {
                        requestVendor.setVendorId(vendorId);
                    }

                    // Web url.
                    vendor.setWebUrl(rsetVendors.getString("web_url"));

                    // Contact name.
                    vendor.setContactName(rsetVendors.getString("contact_person"));

                    // Phone number.
                    vendor.setPhoneNumber(rsetVendors.getString("phone"));

                    auditReport.setRequestVendor(requestVendor);
                }

                List<Item> items = new ArrayList<>();
                while (rsetItems.next()) {
                    Item item = new Item();

                    // Item name.
                    item.setItemName(rsetItems.getString("item_name"));

                    // Quantity.
                    item.setQuantity(getInt(rsetItems, "quantity"));

                    //this is used to determine shipping & credit items
                    item.setVendorId(getInt(rsetItems, "vendor_id"));

                    // Item description.
                    item.setDescription(rsetItems.getString("item_description"));

                    // Price.
                    item.setPrice(getDouble(rsetItems, "price"));

                    // Project task.
                    item.setProjectTask(rsetItems.getString("project_task"));

                    // Object class.
                    item.setObjectClass(rsetItems.getString("object_class"));

                    // Is shipping.
                    item.setIsShipping(StringToBool(getString(rsetItems, "is_shipping")));

                    // Statement date.
                    item.setStatementDate(getTimestamp(rsetItems, "statement_date"));

                    items.add(item);
                }
                auditReport.setItems(items);

                //get Justification data
                RequestJustification requestJust = new RequestJustification();
                if (rsetJust.next()) {
                    //IT Purchase
                    requestJust.setIsItPurchase(rsetJust.getString("is_it_purchase"));

                    // Convenience check.
                    requestJust.setConvenienceCheck(StringToBool(getString(rsetJust, "convenience_check")));

                    // Convenience check justification.
                    requestJust.setConvenienceCheckJust(rsetJust.getString("convenience_check_just"));

                    //can be null or a negative number
                    Integer builtInVendor = getInt(rsetJust, "built_in_vendor");
                    String professionalOrg = rsetJust.getString("professional_org");
                    boolean isProfessionalOrg = "Y".equals(professionalOrg);
                    String sbReason = "TO THE MAXIMUM EXTENT POSSIBLE, SMALL VENDORS WERE CONSIDERED";

                    //no justification record, everything except the requestId is null
                    //if built in vendor is used, builtInVendor will not be null
                    //if professionalOrg is selected, it won't be null
                    if (builtInVendor == null && professionalOrg == null) {
                        String reason2 = "Justification not added yet";
                        requestJust.setGsaScheduleJust(reason2);
                        requestJust.setCommercialVendorJust(reason2);
                        requestJust.setThirdPartyVendorJust(reason2);
                        requestJust.setPriceIsReasonableJust(reason2);
                        requestJust.setSmallBusinessJust(sbReason); //reason2);
                    } else if (builtInVendor != null && builtInVendor < 0) {
                        String reason = vendorName + " used, no justifications required";
                        requestJust.setGsaScheduleJust(reason);
                        requestJust.setCommercialVendorJust(reason);
                        requestJust.setThirdPartyVendorJust(reason);
                        requestJust.setPriceIsReasonableJust(reason);
                        requestJust.setSmallBusinessJust(reason);
                        // Gsa schedule.
                        requestJust.setGsaSchedule(false);
                    } else if (isProfessionalOrg) {
                        String reason1 = "Vendor is a non-business, no justifications required";
                        requestJust.setGsaScheduleJust(reason1);
                        requestJust.setCommercialVendorJust(reason1);
                        requestJust.setThirdPartyVendorJust(reason1);
                        requestJust.setPriceIsReasonableJust(reason1);
                        requestJust.setSmallBusinessJust(reason1);
                        // Gsa schedule.
                        requestJust.setGsaSchedule(false);
                    } else {

                        // Gsa schedule.
                        requestJust.setGsaSchedule(StringToBool(getString(rsetJust, "gsa_schedule")));

                        // Gsa schedule justification.
                        if (requestJust.getGsaSchedule()) {
                            //BANK-546
                            requestJust.setGsaScheduleJust("Purchase made through a mandatory source of supply");
                        } else {
                            requestJust.setGsaScheduleJust(rsetJust.getString("gsa_schedule_just"));
                        }

                        // Commercial vendor.
                        requestJust.setCommercialVendor(StringToBool(getString(rsetJust, "commercial_vendor")));

                        // Third party vendor.
                        requestJust.setThirdPartyVendor("Y".equals(rsetJust.getString("third_party_vendor")));

                        // Third party vendor justification.
                        if (requestJust.getGsaSchedule() || "N".equals(rsetJust.getString("third_party_vendor"))) {
                            requestJust.setThirdPartyVendorJust("Third party credit card process not used");
                        } else {
                            requestJust.setThirdPartyVendorJust(rsetJust.getString("third_party_vendor_just"));
                        }

                        // commercial vendor justification. audit report Q2 is it an open market purchase
                        if (requestJust.getGsaSchedule() || "N".equals(rsetJust.getString("commercial_vendor"))) {
                            requestJust.setCommercialVendorJust("Not a commercial vendor.");
                        } else {
                            requestJust.setCommercialVendorJust(rsetJust.getString("commercial_vendor_just"));
                        }

                        // Price is reasonable justification.
                        if (requestJust.getGsaSchedule()) {
                            //BANK-546
                            requestJust.setPriceIsReasonableJust("Purchase made through a mandatory source of supply");
                        } else {
                            requestJust.setPriceIsReasonableJust(rsetJust.getString("price_is_reasonable_just"));
                        }

                        // Small business.
                        requestJust.setSmallBusiness("Y".equals(rsetJust.getString("small_business")));

                        // Small business justification.
                        if (requestJust.getSmallBusiness()) {
                            requestJust.setSmallBusinessJust("Vendor is a small business");
                        } else {
                            requestJust.setSmallBusinessJust(rsetJust.getString("small_business_just"));
                        }
                    }
                }

                auditReport.setRequestJustification(requestJust);

                List<Route> routes = new ArrayList<>();
                while (rsetRoutes.next()) {
                    Route route = new Route();

                    route.setId(rsetRoutes.getInt("route_id"));
                    route.setTypeId(rsetRoutes.getInt("route_type_id"));
                    route.setStatusId(rsetRoutes.getInt("route_status_id"));
                    route.setRouteBy(rsetRoutes.getInt("route_by"));
                    route.setRouteByName(rsetRoutes.getString("route_by_name"));
                    route.setRouteTo(rsetRoutes.getInt("route_to"));
                    route.setRouteToName(rsetRoutes.getString("route_to_name"));
                    route.setRouteStep(rsetRoutes.getInt("route_step"));
                    route.setRouteDate(getTimestamp(rsetRoutes, "route_date"));
                    route.setIsDynamic(rsetRoutes.getInt("is_dynamic"));
                    route.setDynamicType(rsetRoutes.getString("dynamic_type"));

                    routes.add(route);
                }
                auditReport.setRoutes(routes);
            }

            results.put(AUDIT_REPORT_KEY, auditReport);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectRequestSummaryReport(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_get_request_summary(?, ?, ?, ?, ?) }";
        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {
            cstmt.setInt("p_request_id", requestId);
            cstmt.registerOutParameter("result_set_request", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_vendors", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_items", OracleTypes.CURSOR);
            cstmt.registerOutParameter("result_set_file_attachments", OracleTypes.CURSOR);
            RequestSummaryReport requestSummaryReport = new RequestSummaryReport();
            cstmt.execute();
            try ( ResultSet rsetRequest = cstmt.getObject("result_set_request", ResultSet.class);  ResultSet rsetVendors = cstmt.getObject("result_set_vendors", ResultSet.class);  ResultSet rsetItems = cstmt.getObject("result_set_items", ResultSet.class);  ResultSet rsetFileAttachments = cstmt.getObject("result_set_file_attachments", ResultSet.class);) {
                if (rsetRequest.next()) {

                    // Request id.
                    requestId = rsetRequest.getInt("request_id");
                    if (!rsetRequest.wasNull()) {
                        requestSummaryReport.setRequestId(requestId);
                    }

                    // Requisition number.
                    requestSummaryReport.setRequisitionNumber(rsetRequest.getString("requisition_number"));

                    // Requester name.
                    requestSummaryReport.setRequesterName(rsetRequest.getString("requester_name"));

                    // Created for name.
                    requestSummaryReport.setCreatedByName(rsetRequest.getString("created_by_name"));

                    // Reviewer name.
                    requestSummaryReport.setReviewerName(rsetRequest.getString("reviewer_name"));

                    requestSummaryReport.setDcName(rsetRequest.getString("dc_name"));

                    String fcoName = rsetRequest.getString("fco_name");
                    String baoName = rsetRequest.getString("bao_name");
                    //for approvals before 03/2025, bao and fco are the same person, after that, policy updated to BAO and FCO cannot be the same person
                    if (fcoName == null || StringUtil.isEmpty(fcoName)) {
                        fcoName = baoName;
                    }
                    requestSummaryReport.setFcoName(fcoName);

                    // Bao name.
                    requestSummaryReport.setBaoName(baoName);

                    // Bh name.
                    requestSummaryReport.setBhName(rsetRequest.getString("bh_name"));

                    // Request date.
                    requestSummaryReport.setRequestDate(getTimestamp(rsetRequest, "request_date"));

                    // Reviewer date.
                    Timestamp reviewerDate = getTimestamp(rsetRequest, "reviewer_date");
                    Timestamp dcDate = getTimestamp(rsetRequest, "dc_date");

                    if (reviewerDate == null) {
                        reviewerDate = dcDate;
                    }

                    requestSummaryReport.setReviewerDate(reviewerDate); // This might be null if both are null
                    requestSummaryReport.setDcDate(dcDate); // This might be null

                    //fco date
                    Timestamp fcoDate = rsetRequest.getTimestamp("fco_date");
                    // Bao date.
                    Timestamp baoDate = rsetRequest.getTimestamp("bao_date");

                    if (fcoDate == null) {
                        fcoDate = baoDate;
                    }

                    if (!rsetRequest.wasNull()) {
                        requestSummaryReport.setFcoDate(fcoDate);
                    }

                    if (!rsetRequest.wasNull()) {
                        requestSummaryReport.setBaoDate(baoDate);
                    }

                    // Order date.
                    requestSummaryReport.setOrderDate(getTimestamp(rsetRequest, "order_date"));

                    // Deliver date.
                    requestSummaryReport.setDeliverDate(getTimestamp(rsetRequest, "deliver_date"));

                    // Needed by date.
                    requestSummaryReport.setNeededByDate(getTimestamp(rsetRequest, "needed_by_date"));

                    //Approval amount, MB-418
                    requestSummaryReport.setApprovalAmount(getDouble(rsetRequest, "approval_amount"));

                    // Notes.
                    requestSummaryReport.setNotes(rsetRequest.getString("notes"));

                    // Deliver address.
                    requestSummaryReport.setDeliverAddress(rsetRequest.getString("deliver_address"));

                    //fy; issue 589
                    requestSummaryReport.setFy(rsetRequest.getInt("fy"));
                }

                if (rsetVendors.next()) {
                    RequestVendor requestVendor = new RequestVendor();
                    Vendor vendor = new Vendor();
                    requestVendor.setVendor(vendor);

                    // Vendor name.
                    vendor.setName(rsetVendors.getString("vendor_name"));

                    // Vendor id.
                    int vendorId = rsetVendors.getInt("vendor_id");
                    if (!rsetVendors.wasNull()) {
                        requestVendor.setVendorId(vendorId);
                    }

                    // Web url.
                    vendor.setWebUrl(rsetVendors.getString("web_url"));

                    // Contact name.
                    vendor.setContactName(rsetVendors.getString("contact_person"));

                    // Phone number.
                    vendor.setPhoneNumber(rsetVendors.getString("phone"));

                    // Convenience check.
                    requestVendor.setConvenienceCheck("Y".equals(rsetVendors.getString("convenience_check")));

                    // Convenience check justification.
                    requestVendor.setConvenienceCheckJustification(rsetVendors.getString("convenience_check_just"));

                    // Gsa schedule.
                    requestVendor.setGsaSchedule("Y".equals(rsetVendors.getString("gsa_schedule")));

                    // Gsa schedule justification.
                    requestVendor.setGsaScheduleJustification(rsetVendors.getString("gsa_schedule_just"));

                    //this report doesn't need these two value        
                    // commercial vendor.
                    //requestVendor.setCommercialVendor("Y".equals(rsetVendors.getString("commercial_vendor")));
                    // commercial vendor justification.
                    //requestVendor.setCommercialVendorJustification(rsetVendors.getString("commercial_vendor_just"));
                    // Third party vendor.
                    requestVendor.setThirdPartyVendor("Y".equals(rsetVendors.getString("third_party_vendor")));

                    // Third party vendor justification.
                    requestVendor.setThirdPartyJustification(rsetVendors.getString("third_party_vendor_just"));

                    // Small business.
                    requestVendor.setSmallBusiness("Y".equals(rsetVendors.getString("small_business")));

                    // Small business justification.
                    requestVendor.setSmallBusinessJustification(rsetVendors.getString("small_business_just"));

                    requestSummaryReport.setRequestVendor(requestVendor);
                }

                List<Item> items = new ArrayList<>();
                while (rsetItems.next()) {
                    Item item = new Item();

                    // Item name.
                    item.setItemName(rsetItems.getString("item_name"));

                    // Quantity.
                    item.setQuantity(getInt(rsetItems, "quantity"));

                    // Vendor id.
                    item.setVendorId(getInt(rsetItems, "vendor_id"));

                    // Item description.
                    item.setDescription(rsetItems.getString("item_description"));

                    // Price.
                    item.setPrice(getDouble(rsetItems, "price"));

                    // Project task.
                    item.setProjectTask(rsetItems.getString("project_task"));

                    // Object class.
                    item.setObjectClass(rsetItems.getString("object_class"));

                    // Is shipping.
                    item.setIsShipping("Y".equals(rsetItems.getString("is_shipping")));

                    // Price ordered.
                    item.setActualPrice(getDouble(rsetItems, "price_ordered"));

                    // Quantity ordered.
                    item.setActualQuantity(getInt(rsetItems, "quantity_ordered"));

                    //339
                    item.setCatalogNumber(rsetItems.getString("catelog_number"));
                    item.setTransactionNumber(rsetItems.getString("transaction_number"));
                    item.setStatementDate(getTimestamp(rsetItems, "statement_date"));

                    items.add(item);
                }
                requestSummaryReport.setItems(items);

                List<FileAttachment> fileAttachments = new ArrayList<>();
                while (rsetFileAttachments.next()) {
                    FileAttachment fileAttachment = new FileAttachment();

                    // File id.
                    fileAttachment.setId(rsetFileAttachments.getInt("file_id"));

                    // File name.
                    fileAttachment.setName(rsetFileAttachments.getString("file_name"));

                    // File size.
                    fileAttachment.setSize(getInt(rsetFileAttachments, "file_size"));

                    // File type code.
                    fileAttachment.setTypeCode(rsetFileAttachments.getString("file_type_code"));

                    // File category id.
                    fileAttachment.setCategoryId(getInt(rsetFileAttachments, "file_category_id"));

                    // File category name.
                    fileAttachment.setCategoryName(rsetFileAttachments.getString("file_category_name"));

                    fileAttachments.add(fileAttachment);
                }

                requestSummaryReport.setFileAttachments(fileAttachments);
            }

            results.put(REQUEST_SUMMARY_REPORT_KEY, requestSummaryReport);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertRequest(Request request) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (request.getCreatedBy() == null || request.getFy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "INSERT INTO"
                + " request "
                + "("
                + "requester_id, "
                + "created_by, "
                + "created_for, "
                + "created_date, "
                + "updated_by, "
                + "updated_date, "
                + "notes, "
                + "deliver_address, "
                + "description, "
                + "requisition_number, "
                + "needed_by_date, "
                + "reviewer_id, "
                + "bankcard_approving_official_id, "
                + "bankcard_holder_id, "
                + "fy, "
                + "is_it_purchase, "
                + "purchase_type_id, "
                + "mission_critical_category_id, "
                + "mission_critical_justification, "
                + "grp_id "
                + ")"
                + " VALUES "
                + "(?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LOG.info(String.format("sql: %s, created_by: %d, requisition_number, %s", sql, request.getCreatedBy(), request.getRequisitionNumber()));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"request_id"});) {

            // Requester id.
            setInt(pstmt, 1, request.getRequesterId());

            // Created by.
            pstmt.setInt(2, request.getCreatedBy());

            // Created for.
            setInt(pstmt, 3, request.getCreatedFor());

            // Updated by. Same as created by.
            pstmt.setInt(4, request.getCreatedBy());

            // Notes.
            setString(pstmt, 5, request.getNotes());

            // Delivery address.
            setString(pstmt, 6, request.getDeliveryAddress());

            //Description
            pstmt.setString(7, request.getDescription()); //.trim());

            // Requisition number.
            setString(pstmt, 8, request.getRequisitionNumber());

            // Needed by date.
            setTimestamp(pstmt, 9, request.getNeededByDate());

            // Reviewer id.
            setInt(pstmt, 10, request.getReviewerId());
            // Bankcard approving official id.
            setInt(pstmt, 11, request.getBankcardApprovingOfficialId());
            // Bankcard holder id.
            setInt(pstmt, 12, request.getBankcardHolderId());
            pstmt.setInt(13, request.getFy());
            // is IT Purchase
            if (request.getIsItPurchase() != null) {
                pstmt.setString(14, request.getIsItPurchase());// ? "Y" : "N");
            } else {
                pstmt.setString(14, "N");
            }
            setInt(pstmt, 15, request.getPurchaseTypeId());
            setInt(pstmt, 16, request.getMissionCriticalCategoryId());
            setString(pstmt, 17, request.getMissionCriticalJustification());
            setInt(pstmt, 18, request.getGroupId());

            // Do the insert and get back the generated key.
            int rowCount = pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (rowCount != 0 && generatedKeys != null && generatedKeys.next()) {
                int requestId = generatedKeys.getInt(1);
                generatedKeys.close();
                results.put(ID_KEY, requestId);

                // Now insert a route.
                Route route = new Route();
                route.setTypeId(0); // Route saved.
                route.setStatusId((Objects.equals(request.getRequesterId(), request.getCreatedBy())) ? 1 : 12);
                route.setRequestId((Integer) results.get(ID_KEY));
                route.setRouteBy(request.getCreatedBy());
                route.setRouteTo(request.getCreatedBy());
                //since this is the first route (saved), it won't be a dynamic route
                route.setIsDynamic(0);
                RouteDao dao = new RouteDao();
                Map<String, Object> resultsForRouteInsert = dao.insertRouteWithConnection(connection, route);
                statusCode = (StatusCode) resultsForRouteInsert.get(STATUS_CODE_KEY);
                if (statusCode == StatusCode.OK) {
                    results.put(ROUTE_ID_KEY, resultsForRouteInsert.get(RouteDao.ROUTE_ID_KEY));
                } else {
                    statusCode = StatusCode.InsertFailed;
                }
            } else {
                statusCode = StatusCode.InsertFailed;
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertRequestCopy(Integer requestId, Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null || createdBy == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call up_copy_request(?, ?, ?) }";

        LOG.info(String.format("sql: %s, request_id %d, created_by: %d", sql, requestId, createdBy));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {

            cstmt.setInt("p_request_id", requestId);
            cstmt.setInt("p_created_by", createdBy);
            cstmt.registerOutParameter("p_rc", Types.INTEGER);

            cstmt.executeUpdate();

            requestId = cstmt.getInt("p_rc");
            if (requestId > 0) {
                results.put(ID_KEY, requestId);
            } else {
                statusCode = StatusCode.InsertFailed;
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRequest(Request request, String divisionCode) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (request.getId() == null || request.getUpdatedBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_update_request(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
        LOG.info(String.format("sql: %s, updated_by: %d, approval_amount: %,.2f", sql, request.getUpdatedBy(), request.getApprovalAmount()));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {

            setIntParam(cstmt, "p_requester_id", request.getRequesterId());
            setIntParam(cstmt, "p_created_for", request.getCreatedFor());
            setIntParam(cstmt, "p_updated_by", request.getUpdatedBy());
            setStringParam(cstmt, "p_notes", request.getNotes());
            setStringParam(cstmt, "p_deliver_address", request.getDeliveryAddress());
            setBooleanYNOrDefaultParam(cstmt, "p_deliver_to_home", request.getDelivToHome(), "N");
            setTimestampParam(cstmt, "p_needed_by_date", request.getNeededByDate());
            setIntParam(cstmt, "p_reviewer_id", request.getReviewerId());
            setIntParam(cstmt, "p_division_chief_id", request.getDivisionChiefId());
            setIntParam(cstmt, "p_bankcard_approving_official_id", request.getBankcardApprovingOfficialId());
            setIntParam(cstmt, "p_funds_certifying_official_id", request.getFundsCertifyingOfficialId());
            setIntParam(cstmt, "p_bankcard_holder_id", request.getBankcardHolderId());
            setTimestampParam(cstmt, "p_estimated_time_of_arrival", request.getEstimatedTimeOfArrival());
            setStringParam(cstmt, "p_requisition_number", request.getRequisitionNumber());
            setStringParam(cstmt, "p_division_code", divisionCode); // divisionCode is a direct method parameter
            setStringParam(cstmt, "p_order_number", request.getOrderNumber());
            setStringParam(cstmt, "p_gsa_session_number", request.getGsaSessionNumber());
            setStringParam(cstmt, "p_purchase_order_number", request.getPurchaseOrderNumber());
            setDoubleParam(cstmt, "p_approval_amount", request.getApprovalAmount());
            setStringParam(cstmt, "p_description", request.getDescription());
            setStringOrDefaultParam(cstmt, "p_is_it_purchase", request.getIsItPurchase(), "N");
            setIntParam(cstmt, "p_purchase_type_id", request.getPurchaseTypeId());
            setIntParam(cstmt, "p_mission_critical_category_id", request.getMissionCriticalCategoryId());
            setStringParam(cstmt, "p_mission_critical_justification", request.getMissionCriticalJustification());
            setIntParam(cstmt, "p_request_id", request.getId());

            // Route in correct state 0/1.
            cstmt.registerOutParameter("o_route_in_correct_state", Types.INTEGER);

            // Result set.
            cstmt.registerOutParameter("o_result_set", OracleTypes.CURSOR);

            cstmt.execute();

            results.put(ROUTE_IN_CORRECT_STATE_KEY, cstmt.getInt("o_route_in_correct_state") == 1 ? true : false);

            try ( ResultSet rset = cstmt.getObject("o_result_set", ResultSet.class);) {
                if (rset.next()) {
                    setRequestResults(null, results, rset);
                }
            }

            if (statusCode == StatusCode.OK) {
                LOG.info("commit happened");
                connection.commit();
            } else {
                LOG.info("rollback happened");
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRequestDescription(Integer requestId, String description) {
        return updateRequestFieldById(requestId, "description", description);
    }

    public Map<String, Object> updateBchComments(Integer requestId, String bchComments) {
        return updateRequestFieldById(requestId, "bch_comments", bchComments);
    }

    public Map<String, Object> updateFco(Integer requestId, Integer fcoId) {
        return updateRequestFieldById(requestId, "funds_certifying_official_id", fcoId);
    }

    public Map<String, Object> updateDC(Integer requestId, Integer dcId) {
        return updateRequestFieldById(requestId, "division_chief_id", dcId);
    }

    /**
     * handles single string field update for request table
     *
     * @param requestId
     * @param fieldName
     * @param value
     * @return Map<String, Object>
     */
    private Map<String, Object> updateRequestFieldById(Integer requestId, String fieldName, Object value) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE request SET " + fieldName + " = ? WHERE request_id = ?";
        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            if (value != null) {
                if (value instanceof Integer) {
                    pstmt.setInt(1, (int) value);
                } else if (value instanceof String) {
                    pstmt.setString(1, value.toString().trim());
                } else {
                    throw new IllegalArgumentException("Unexpected parameter type: " + value.getClass().toString() + ". Expected: String, Integer");
                }
            }

            // Request id.
            pstmt.setInt(2, requestId);

            pstmt.executeUpdate();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRequisitionNumber(Integer requestId, String requisitionNumber) {
        return updateRequestFieldById(requestId, "requisition_number", requisitionNumber);
    }

    public Map<String, Object> updateFy(Integer requestId, Request req) {
        updateRequestFieldById(requestId, "fy", req.getFy());
        return updateRequestFieldById(requestId, "requisition_number", req.getRequisitionNumber());
    }

    public Map<String, Object> deleteRequest(Integer peopleId, Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null || requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_delete_request(?, ?, ?, ?) }";

        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {

            // People id.
            cstmt.setInt("p_people_id", peopleId);

            // Request id.
            cstmt.setInt("p_request_id", requestId);

            // Permission state.
            cstmt.registerOutParameter("o_permission_state", Types.INTEGER);

            // Row count.
            cstmt.registerOutParameter("o_row_count", Types.INTEGER);

            cstmt.execute();

            results.put(ROW_COUNT_KEY, cstmt.getInt("o_row_count"));
            results.put(PERMISSION_STATE_KEY, cstmt.getInt("o_permission_state"));

            connection.commit();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> callPullBackRoute(Integer requestId, Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null || requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_pull_back_route(?, ?) }";

        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {

            // Route by.
            cstmt.setInt("p_route_by", peopleId);

            // Request id.
            cstmt.setInt("p_request_id", requestId);

            cstmt.execute();
        } catch (SQLException caught) {
            Integer errorCode = caught.getErrorCode();
            results.put(ERROR_CODE_KEY, errorCode);
            String errorMessage;
            String logMessage;

            switch (errorCode) {
                case 20101:
                    errorMessage = caught.getMessage().substring(caught.getMessage().indexOf('~') + 1, caught.getMessage().lastIndexOf('~'));
                    logMessage = "This request with id: " + requestId + ", is at its initial step and cannot be pulled back.";
                    LOG.log(Level.INFO, logMessage, caught);
                    break;

                case 20102:
                    errorMessage = caught.getMessage().substring(caught.getMessage().indexOf('~') + 1, caught.getMessage().lastIndexOf('~'));
                    logMessage = "The last route of this request with id: " + requestId + ", was not done by you. You do not have permission to pull it back.";
                    LOG.log(Level.INFO, logMessage, caught);
                    break;

                case 20103:
                    errorMessage = caught.getMessage().substring(caught.getMessage().indexOf('~') + 1, caught.getMessage().lastIndexOf('~'));
                    logMessage = "Error from the request pull back SP for request with id: " + requestId + ". Email recipient is missing.";
                    LOG.log(Level.INFO, logMessage, caught);
                    break;

                default:
                    errorMessage = "There was an error during pull back for request with id: " + requestId;
                    LOG.log(Level.SEVERE, errorMessage, caught);
            }
            results.put(ERROR_MESSAGE_KEY, errorMessage);
            statusCode = StatusCode.DatabaseError;
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    private void setRequestResults(List<RequestRoute> requests, Map<String, Object> results, ResultSet rset) throws SQLException {
        RequestRoute requestRoute = new RequestRoute();
        Route route = new Route();

        // Request id.
        requestRoute.setId(getInt(rset, "request_id"));

        // fy
        requestRoute.setFy(getInt(rset, "fy"));

        // Notes.
        requestRoute.setNotes(rset.getString("notes"));

        // Requester id.
        requestRoute.setRequesterId(getInt(rset, "requester_id"));

        // Requester name.
        requestRoute.setRequesterName(rset.getString("requester_name"));

        // Created by.
        requestRoute.setCreatedBy(getInt(rset, "created_by"));

        // Created by name.
        requestRoute.setCreatedByName(rset.getString("created_by_name"));

        // Created for.
        requestRoute.setCreatedFor(getInt(rset, "created_for"));

        // Created for name.
        requestRoute.setCreatedForName(rset.getString("created_for_name"));

        // Created date.
        requestRoute.setCreatedDate(getTimestamp(rset, "created_date"));

        // Is shopping cart.
        requestRoute.setShoppingCart(rset.getString("is_shopping_cart"));

        // Reference id.
        requestRoute.setReferenceId(getInt(rset, "reference_id"));

        // Updated by.
        requestRoute.setUpdatedBy(getInt(rset, "updated_by"));

        // Updated date.
        requestRoute.setUpdatedDate(getTimestamp(rset, "updated_date"));

        // Delivery address.
        requestRoute.setDeliveryAddress(rset.getString("deliver_address"));

        // Delivery to home.
        requestRoute.setDelivToHome(StringToBool(getString(rset, "deliver_to_home")));

        // Vendors.
        requestRoute.setVendors(rset.getString("vendors"));

        // Items.
        requestRoute.setItems(rset.getString("items"));

        // Total cost.
        requestRoute.setTotalCost(getDouble(rset, "total_cost"));

        // Actual total cost.
        requestRoute.setActualTotalCost(getDouble(rset, "actual_total_cost"));

        // Requisition number.
        requestRoute.setRequisitionNumber(rset.getString("requisition_number"));

        // Estimated time of arrival.
        requestRoute.setEstimatedTimeOfArrival(getTimestamp(rset, "estimated_time_of_arrival"));

        // Order number.
        requestRoute.setOrderNumber(rset.getString("order_number"));

        // Gsa session number.
        requestRoute.setGsaSessionNumber(rset.getString("gsa_session_number"));

        // Purchase order number.
        requestRoute.setPurchaseOrderNumber(rset.getString("purchase_order_number"));

        // Submitted date.
        requestRoute.setSubmittedDate(getTimestamp(rset, "submitted_date"));

        // Bch comments.
        requestRoute.setBchComments(rset.getString("bch_comments"));

        // Description.
        requestRoute.setDescription(rset.getString("description"));

        // Approval amount.
        requestRoute.setApprovalAmount(getDouble(rset, "approval_amount"));

        // Ou id.
        requestRoute.setOuId(getInt(rset, "ou_org_id"));

        // Division id.
        requestRoute.setDivisionId(getInt(rset, "div_org_id"));

        // Group id.
        requestRoute.setGroupId(getInt(rset, "grp_org_id"));

        // Needed by date.
        requestRoute.setNeededByDate(getTimestamp(rset, "needed_by_date"));

        requestRoute.setReviewerId(getInt(rset, "reviewer_id"));
        requestRoute.setReviewerName(getString(rset, "reviewer_name"));
        requestRoute.setDivisionChiefId(getInt(rset, "division_chief_id"));
        requestRoute.setDcName(getString(rset, "division_chief_name"));

        // Bankcard approving official id.
        requestRoute.setBankcardApprovingOfficialId(getInt(rset, "bankcard_approving_official_id"));
        requestRoute.setBaoName(getString(rset, "bao_name"));

        // fco id.
        requestRoute.setFundsCertifyingOfficialId(getInt(rset, "funds_certifying_official_id"));
        requestRoute.setFcoName(getString(rset, "fco_name"));

        // Bankcard holder id.
        requestRoute.setBankcardHolderId(getInt(rset, "bankcard_holder_id"));
        requestRoute.setBhName(getString(rset, "bh_name"));

        // is IT Purchase
        String isItPurchase = rset.getString("is_it_purchase");
        if (!rset.wasNull()) {
            requestRoute.setIsItPurchase(isItPurchase);//"Y".equals(isItPurchase));
        } else {
            requestRoute.setIsItPurchase("N");//Boolean.FALSE);
        }

        requestRoute.setPurchaseTypeId(getInt(rset, "purchase_type_id"));
        requestRoute.setMissionCriticalCategoryId(getInt(rset, "mission_critical_category_id"));
        requestRoute.setMissionCriticalJustification(getString(rset, "mission_critical_justification"));

        // ITSO 
        requestRoute.setItsoApproved(getInt(rset, "itso_approved"));

        // Route.
        // Route id.
        route.setId(getInt(rset, "route_id"));

        // Route type id.
        route.setTypeId(getInt(rset, "route_type_id"));

        // Route notes.
        route.setNotes(rset.getString("route_notes"));

        // Route by.
        route.setRouteBy(getInt(rset, "route_by"));

        // Route by name.
        route.setRouteByName(rset.getString("route_by_name"));

        // Route date.
        route.setRouteDate(getTimestamp(rset, "route_date"));

        // Route status id.
        route.setStatusId(getInt(rset, "route_status_id"));

        // Route to.
        route.setRouteTo(getInt(rset, "route_to"));

        // Route to name.
        route.setRouteToName(rset.getString("route_to_name"));

        // Route status name.
        route.setStatusName(rset.getString("route_status_name"));

        // Route type name.
        route.setTypeName(rset.getString("route_type_name"));

        route.setIsDynamic(getInt(rset, "is_dynamic"));
        route.setRerouteStack(getInt(rset, "reroute_stack"));
        route.setIsDynamicReroute(getInt(rset, "is_dynamic_reroute"));
        route.setRouteStep(getInt(rset, "route_step"));

        route.setDynamicType(getString(rset, "dynamic_type"));

        requestRoute.setRoute(route);

        //single record return
        if (requests == null) {
            results.put(REQUEST_ROUTE_KEY, requestRoute);
        } else {
            //multiple records return
            requests.add(requestRoute);
        }

    }

    @Data
    public static class RequestQueryParameters {

        private String username;
        private Integer ouId;
        private Integer divisionId;
        private Integer groupId;
        private Integer requesterId;
        private String requisitionNumber;
        private Integer routeTypeId;
        private Date fromDate;
        private Date toDate;
        private String vendorName;
        private String transactionNumber;
        private String itemName;
        private String itemStatuses;
        private Double actualTotal;
        private Integer bankcardHolderId;
        private Integer reviewerId;
        private Integer requestId;
        private Integer fy;
        private String ptc;
        private Date statementDate;
        private Boolean delivToHome;
        private String description;
        private Boolean partialOrder;
        private Boolean taggable;
        private Integer purchaseTypeId;
    }
}
