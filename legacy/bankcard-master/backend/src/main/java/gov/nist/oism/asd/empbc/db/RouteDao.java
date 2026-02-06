package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.Route;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouteDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(RouteDao.class.getSimpleName());

    public static final String DEFAULT_ROUTE_KEY = "default_route_key";
    public static final String ROUTE_LIST_KEY = "route_list_key";
    public static final String ROUTE_KEY = "route_key";
    public static final String ROUTE_ID_KEY = "route_id_key";
    public static final String ROUTE_VALIDATION_ERROR_MESSAGE_KEY = "route_validation_error_message_key";
    public static final String CALL_NOTIFY_CISPRO_USER_KEY = "call_notify_cispro_user_key";
    public static final String CISPRO_DIV_ORG_KEY = "cispro_div_org_key";
    public static final String CALL_NOTIFY_PROPERTY_CUSTODIAN_KEY = "call_notify_property_custodian_key";
    public static final String PROPERTY_CUSTODIAN_DIV_ORG_KEY = "property_custodian_div_org_key";
    public static final String REQUIRED_PERMISSION_KEY = "required_permission_key";

    public Map<String, Object> selectRoutesForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "SELECT "
                + "r.route_id, "
                + "r.request_id, "
                + "r.route_type_id, "
                + "r.route_notes, "
                + "r.route_by, "
                + "get_user_name(r.route_by) AS route_by_name, "
                + "r.route_by_delegate, "
                + "nvl2(r.route_by_delegate, get_user_name(r.route_by_delegate),'') AS route_by_delegate_name, "
                + "r.route_date, "
                + "r.route_status_id, "
                + "r.route_to, "
                + "get_user_name(r.route_to) AS route_to_name, "
                + "lrs.route_status_name, "
                + "lrt.route_type_name, "
                + "r.reroute_by, "
                + "get_user_name(r.reroute_by) AS reroute_by_name, "
                + "r.is_dynamic, "
                + "r.is_dynamic_reroute, "
                + "r.reroute_stack, "
                + "r.route_step, "
                + "r.dynamic_type "
                + " FROM "
                + "route r"
                + " LEFT OUTER JOIN lkup_route_status lrs ON r.route_status_id = lrs.route_status_id"
                + " LEFT OUTER JOIN lkup_route_type lrt on r.route_type_id = lrt.route_type_id"
                + " WHERE "
                + "r.request_id = ?"
                + " ORDER BY "
                + "r.route_step";
        LOG.info(String.format("sql: %s, requestId: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<Route> routes = new ArrayList<>();
            pstmt.setInt(1, requestId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    Route route = new Route();

                    // Route id.
                    int routeId = rset.getInt("route_id");
                    if (!rset.wasNull()) {
                        route.setId(routeId);
                    }

                    // Request id.
                    requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        route.setRequestId(requestId);
                    }

                    // Route type id.
                    int routeTypeId = rset.getInt("route_type_id");
                    if (!rset.wasNull()) {
                        route.setTypeId(routeTypeId);
                    }

                    // Route notes.
                    route.setNotes(rset.getString("route_notes"));

                    // Route by.
                    int routeBy = rset.getInt("route_by");
                    if (!rset.wasNull()) {
                        route.setRouteBy(routeBy);
                    }

                    // Route by name.
                    route.setRouteByName(rset.getString("route_by_name"));
                    
                     // Route by.delegate
                    int routeByDelegate = rset.getInt("route_by_delegate");
                    if (!rset.wasNull()) {
                        route.setRouteByDelegate(routeByDelegate);
                    }

                    // Route by delegate name.
                    route.setRouteByDelegateName(rset.getString("route_by_delegate_name"));

                    // Route date.
                    Timestamp routeDate = rset.getTimestamp("route_date");
                    if (!rset.wasNull()) {
                        route.setRouteDate(routeDate);
                    }

                    // Route status id.
                    int routeStatusId = rset.getInt("route_status_id");
                    if (!rset.wasNull()) {
                        route.setStatusId(routeStatusId);
                    }

                    // Route status name.
                    route.setStatusName(rset.getString("route_status_name"));

                    // Route type name.
                    route.setTypeName(rset.getString("route_type_name"));

                    // Route to.
                    int routeTo = rset.getInt("route_to");
                    if (!rset.wasNull()) {
                        route.setRouteTo(routeTo);
                    }

                    // Route to name.
                    route.setRouteToName(rset.getString("route_to_name"));

                    //re Route by.
                    int rerouteBy = rset.getInt("reroute_by");
                    if (!rset.wasNull()) {
                        route.setRerouteBy(rerouteBy);
                    }

                    //re Route by name.
                    route.setRerouteByName(rset.getString("reroute_by_name"));

                    int isDynamic = rset.getInt("is_dynamic");
                    if (!rset.wasNull()) {
                        route.setIsDynamic(isDynamic);
                    }

                    int rerouteStack = rset.getInt("reroute_stack");
                    if (!rset.wasNull()) {
                        route.setRerouteStack(rerouteStack);
                    }

                    int isDynamicReroute = rset.getInt("is_dynamic_reroute");
                    if (!rset.wasNull()) {
                        route.setIsDynamicReroute(isDynamicReroute);
                    }

                    int routeStep = rset.getInt("route_step");
                    if (!rset.wasNull()) {
                        route.setRouteStep(routeStep);
                    }

                    String dynamicType = rset.getString("dynamic_type");
                    if (!rset.wasNull()) {
                        route.setDynamicType(dynamicType);
                    }

                    routes.add(route);
                }
            }

            results.put(ROUTE_LIST_KEY, routes);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectLatestRouteForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "SELECT "
                + "r.route_id, "
                + "r.request_id, "
                + "r.route_type_id, "
                + "r.route_notes, "
                + "r.route_by, "
                + "get_user_name(r.route_by) AS route_by_name, "
                + "r.route_date, "
                + "r.route_status_id, "
                + "r.route_to, "
                + "get_user_name(r.route_to) AS route_to_name, "
                + "lrs.route_status_name, "
                + "lrt.route_type_name, "
                + "r.reroute_by, "
                + "get_user_name(r.reroute_by) AS reroute_by_name, "
                + "r.is_dynamic, "
                + "r.is_dynamic_reroute, "
                + "r.reroute_stack "
                + " FROM "
                + "route r"
                + " LEFT OUTER JOIN lkup_route_status lrs ON r.route_status_id = lrs.route_status_id"
                + " LEFT OUTER JOIN lkup_route_type lrt on r.route_type_id = lrt.route_type_id"
                + " WHERE "
                + "r.request_id = ?"
                + " AND "
                + "r.route_date = (SELECT MAX(route_id) FROM route WHERE request_id = ?)";
        LOG.info(String.format("sql: %s, requestId: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            pstmt.setInt(2, requestId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    Route route = new Route();

                    // Route id.
                    int routeId = rset.getInt("route_id");
                    if (!rset.wasNull()) {
                        route.setId(routeId);
                    }

                    // Request id.
                    requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        route.setRequestId(requestId);
                    }

                    // Route type id.
                    int routeTypeId = rset.getInt("route_type_id");
                    if (!rset.wasNull()) {
                        route.setTypeId(routeTypeId);
                    }

                    // Route notes.
                    route.setNotes(rset.getString("route_notes"));

                    // Route by.
                    int routeBy = rset.getInt("route_by");
                    if (!rset.wasNull()) {
                        route.setRouteBy(routeBy);
                    }

                    // Route by name.
                    route.setRouteByName(rset.getString("route_by_name"));

                    // Route date.
                    Timestamp routeDate = rset.getTimestamp("route_date");
                    if (!rset.wasNull()) {
                        route.setRouteDate(routeDate);
                    }

                    // Route status id.
                    int routeStatusId = rset.getInt("route_status_id");
                    if (!rset.wasNull()) {
                        route.setStatusId(routeStatusId);
                    }

                    // Route status name.
                    route.setStatusName(rset.getString("route_status_name"));

                    // Route type name.
                    route.setTypeName(rset.getString("route_type_name"));

                    // Route to.
                    int routeTo = rset.getInt("route_to");
                    if (!rset.wasNull()) {
                        route.setRouteTo(routeTo);
                    }

                    // Route to name.
                    route.setRouteToName(rset.getString("route_to_name"));

                    //re Route by.
                    int rerouteBy = rset.getInt("reroute_by");
                    if (!rset.wasNull()) {
                        route.setRerouteBy(rerouteBy);
                    }

                    //re Route by name.
                    route.setRerouteByName(rset.getString("reroute_by_name"));

                    int isDynamic = rset.getInt("is_dynamic");
                    if (!rset.wasNull()) {
                        route.setIsDynamic(isDynamic);
                    }

                    int rerouteStack = rset.getInt("reroute_stack");
                    if (!rset.wasNull()) {
                        route.setRerouteStack(rerouteStack);
                    }

                    int isDynamicReroute = rset.getInt("is_dynamic_reroute");
                    if (!rset.wasNull()) {
                        route.setIsDynamicReroute(isDynamicReroute);
                    }

                    results.put(ROUTE_KEY, route);
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertRoute(Route route) {

        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (route.getRequestId() == null || route.getRouteBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        try ( Connection connection = getConnection(false);) {
            results = insertRouteWithConnection(connection, route);
            statusCode = (StatusCode) results.get(STATUS_CODE_KEY);
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

    public Map<String, Object> reRoute(Route route) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        Connection connection = null;
        if (route.getRequestId() == null || route.getRouteBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        try {
            connection = getConnection(false);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        System.out.println("calling re-Route");
        //MB-364
        String sql = "{ call sp_re_route(?, ?, ?, ?, ?, ?, ?)";

        try {
            CallableStatement cstmt = connection.prepareCall(sql);

            /// Request id.
            if (route.getRequestId() != null) {
                cstmt.setInt("p_request_id", route.getRequestId());
            } else {
                cstmt.setNull("p_request_id", Types.INTEGER);
            }

            // Type id.
            if (route.getTypeId() != null) {
                cstmt.setInt("p_route_type_id", route.getTypeId());
            } else {
                cstmt.setNull("p_route_type_id", Types.INTEGER);
            }

            // Notes.
            if (route.getNotes() != null && !route.getNotes().isEmpty()) {
                cstmt.setString("p_route_notes", route.getNotes().trim());
            } else {
                cstmt.setNull("p_route_notes", Types.VARCHAR);
            }

            // Route by.
            cstmt.setInt("p_route_by", route.getRouteBy());

            // Status id. Peter decided to get it from the route table
            /*if (route.getStatusId() != null) {
                cstmt.setInt("p_route_status_id", route.getStatusId());
            } else {
                cstmt.setNull("p_route_status_id", Types.INTEGER);
            }*/
            // Route to.
            if (route.getRouteTo() != null) {
                cstmt.setInt("p_route_to", route.getRouteTo());
            } else {
                cstmt.setNull("p_route_to", Types.INTEGER);
            }

            // Route id.
            cstmt.registerOutParameter("o_route_id", Types.INTEGER);

            // Required permission.
            cstmt.registerOutParameter("o_required_permission", Types.INTEGER);

            // Do the insert and get back the route id.
            cstmt.execute();

            results.put(ROUTE_ID_KEY, cstmt.getInt("o_route_id"));
            results.put(REQUIRED_PERMISSION_KEY, cstmt.getInt("o_required_permission") == 1);

            results.put(STATUS_CODE_KEY, statusCode);

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

    public Map<String, Object> notifyCisproUsers(Integer requestId, String peopleIds) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null || peopleIds == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "{ call up_notify_cispro(?, ?) }";
        LOG.info(sql);
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, requestId);
            cstmt.setString(2, peopleIds);
            cstmt.execute();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> notifyPropertyCustodianUsers(Integer requestId, String peopleIds) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null || peopleIds == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "{ call up_notify_property_custodians(?, ?) }";
        LOG.info(sql);
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setInt(1, requestId);
            cstmt.setString(2, peopleIds);
            cstmt.execute();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    protected Map<String, Object> insertRouteWithConnection(Connection connection, Route route) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        results.put(CALL_NOTIFY_CISPRO_USER_KEY, Boolean.FALSE);
        results.put(CALL_NOTIFY_PROPERTY_CUSTODIAN_KEY, Boolean.FALSE);
        if (route.getRequestId() == null || route.getRouteBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        // Check for validation if submitting. Also get data for whether we need to notify CISPRO user and/or Property Custodians
        if (1 == route.getTypeId()) {
            Map<String, Object> validationResults = validateSubmitRequestWithConnection(connection, route.getRequestId());
            statusCode = (StatusCode) validationResults.get(STATUS_CODE_KEY);
            if (statusCode == StatusCode.OK) {
                String validationErrorMessage = (String) validationResults.get(ROUTE_VALIDATION_ERROR_MESSAGE_KEY);
                if (validationErrorMessage != null && !validationErrorMessage.isEmpty()) {
                    if (!validationErrorMessage.startsWith("Y,")) {
                        statusCode = StatusCode.RouteValidationFailed;
                        results.put(ROUTE_VALIDATION_ERROR_MESSAGE_KEY, validationErrorMessage);
                        results.put(STATUS_CODE_KEY, statusCode);
                    } else {
                        String[] embeddedDivOrgCode = validationErrorMessage.split((","));
                        if (embeddedDivOrgCode.length == 2 && embeddedDivOrgCode[1] != null) {
                            results.put(CALL_NOTIFY_CISPRO_USER_KEY, Boolean.TRUE);
                            results.put(CISPRO_DIV_ORG_KEY, embeddedDivOrgCode[1]);
                        }
                    }
                }

                String pcDivCode = (String) validationResults.get(PROPERTY_CUSTODIAN_DIV_ORG_KEY);
                if (pcDivCode != null && !pcDivCode.isEmpty()) {
                    results.put(CALL_NOTIFY_PROPERTY_CUSTODIAN_KEY, Boolean.TRUE);
                    results.put(PROPERTY_CUSTODIAN_DIV_ORG_KEY, pcDivCode);
                } else {
                    results.put(CALL_NOTIFY_PROPERTY_CUSTODIAN_KEY, Boolean.FALSE);
                }
            } else {
                results.put(STATUS_CODE_KEY, statusCode);
            }
        } else if (4 == route.getTypeId()) {
            Map<String, Object> validationResults = validateProcessRequestWithConnection(connection, route.getRequestId());
            statusCode = (StatusCode) validationResults.get(STATUS_CODE_KEY);
            if (statusCode == StatusCode.OK) {
                String validationErrorMessage = (String) validationResults.get(ROUTE_VALIDATION_ERROR_MESSAGE_KEY);
                if (validationErrorMessage != null && !validationErrorMessage.isEmpty() && !validationErrorMessage.startsWith("Y,")) {
                    statusCode = StatusCode.RouteValidationFailed;
                    results.put(ROUTE_VALIDATION_ERROR_MESSAGE_KEY, validationErrorMessage);
                    results.put(STATUS_CODE_KEY, statusCode);
                }
            } else {
                results.put(STATUS_CODE_KEY, statusCode);
            }
        }

        if (statusCode != StatusCode.OK) {
            return results;
        }

        Boolean isDynamic = false;
        if (route.getIsDynamic() != null && route.getIsDynamic() == 1) {
            isDynamic = true;
        }

        //normal insert
        String sql = "{ call sp_insert_route_n(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //for dynamic route, need to do insert before
        if (isDynamic) {
            if (route.getDynamicType().equals("DR") || route.getDynamicType().equals("ITSO")) {
                sql = "{ call sp_dynamic_reroute(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else if (route.getDynamicType().equals("AA") || route.getDynamicType().equals("FCO")) {//|| route.getDynamicType().equals("ITSO")
                sql = "{ call sp_approve_and_add_route(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
        }

        LOG.info(String.format("sql: %s, route_by: %d", sql, route.getRouteBy()));
        try ( CallableStatement cstmt = connection.prepareCall(sql);) {

            if (!isDynamic) {
                // Request id.
                if (route.getRequestId() != null) {
                    cstmt.setInt("p_request_id", route.getRequestId());
                } else {
                    cstmt.setNull("p_request_id", Types.INTEGER);
                }

                // Type id.
                if (route.getTypeId() != null) {
                    cstmt.setInt("p_route_type_id", route.getTypeId());
                } else {
                    cstmt.setNull("p_route_type_id", Types.INTEGER);
                }

                // Notes.
                if (route.getNotes() != null && !route.getNotes().isEmpty()) {
                    cstmt.setString("p_route_notes", route.getNotes().trim());
                } else {
                    cstmt.setNull("p_route_notes", Types.VARCHAR);
                }

                // Also Notify.
                if (route.getAlsoNotify() != null && !route.getAlsoNotify().isEmpty()) {
                    cstmt.setString("p_also_notify", route.getAlsoNotify().trim());
                } else {
                    cstmt.setNull("p_also_notify", Types.VARCHAR);
                }

                // Route by.
                cstmt.setInt("p_route_by", route.getRouteBy());

                // Status id.
                if (route.getStatusId() != null) {
                    cstmt.setInt("p_route_status_id", route.getStatusId());
                } else {
                    cstmt.setNull("p_route_status_id", Types.INTEGER);
                }

                // Route to.
                if (route.getRouteTo() != null) {
                    cstmt.setInt("p_route_to", route.getRouteTo());
                } else {
                    cstmt.setNull("p_route_to", Types.INTEGER);
                }

                if (route.getIsDynamic() != null) {
                    cstmt.setInt("p_is_dynamic", route.getIsDynamic());
                } else {
                    cstmt.setInt("p_is_dynamic", 0);
                }

                if (route.getRerouteStack() != null) {
                    cstmt.setInt("p_reroute_stack", route.getRerouteStack());
                } else {
                    cstmt.setInt("p_reroute_stack", 0);
                }

                if (route.getIsDynamicReroute() != null) {
                    cstmt.setInt("p_is_dynamic_reroute", route.getIsDynamicReroute());
                } else {
                    cstmt.setInt("p_is_dynamic_reroute", 0);
                }

                // route step
                if (route.getRouteStep() != null) {
                    cstmt.setInt("p_route_step", route.getRouteStep());
                } else {
                    cstmt.setNull("p_route_step", Types.INTEGER);
                }
                
                 //route by delegate
                if (route.getRouteByDelegate() != null) {
                    cstmt.setInt("p_route_by_delegate", route.getRouteByDelegate());
                } else {
                    cstmt.setNull("p_route_by_delegate", Types.INTEGER);
                }
                
                if (route.getOmitNotification()!= null) {
                    cstmt.setInt("p_omit_notification", route.getOmitNotification());
                } else {
                    cstmt.setInt("p_omit_notification", 0);
                }

                // Route id.
                cstmt.registerOutParameter("o_route_id", Types.INTEGER);

                // Required permission.
                cstmt.registerOutParameter("o_required_permission", Types.INTEGER);
            } else {
                if (route.getRequestId() != null) {
                    cstmt.setInt("p_request_id", route.getRequestId());
                } else {
                    cstmt.setNull("p_request_id", Types.INTEGER);
                }

                if (route.getNotes() != null && !route.getNotes().isEmpty()) {
                    cstmt.setString("p_route_notes", route.getNotes().trim());
                } else {
                    cstmt.setNull("p_route_notes", Types.VARCHAR); 
                }

                cstmt.setInt("p_route_by", route.getRouteBy());

                if (route.getRouteTo() != null) {
                    cstmt.setInt("p_route_to", route.getRouteTo());
                } else {
                    cstmt.setNull("p_route_to", Types.INTEGER);
                }

                if (route.getDynamicType() != null && !route.getDynamicType().isEmpty()) {
                    cstmt.setString("p_dynamic_type", route.getDynamicType().trim());
                } else {
                    cstmt.setNull("p_dynamic_type", Types.VARCHAR);
                }
                
                // Also Notify.
                if (route.getAlsoNotify() != null && !route.getAlsoNotify().isEmpty()) {
                    cstmt.setString("p_also_notify", route.getAlsoNotify().trim());
                } else {
                    cstmt.setNull("p_also_notify", Types.VARCHAR);
                }
                
                //route by delegate
                if (route.getRouteByDelegate() != null) {
                    cstmt.setInt("p_route_by_delegate", route.getRouteByDelegate());
                } else {
                    cstmt.setNull("p_route_by_delegate", Types.INTEGER);
                }
                

                cstmt.registerOutParameter("o_route_id", Types.INTEGER);
                cstmt.registerOutParameter("o_required_permission", Types.INTEGER);
            }

            // Do the insert and get back the route id.
            cstmt.execute();

            results.put(ROUTE_ID_KEY, cstmt.getInt("o_route_id"));
            results.put(REQUIRED_PERMISSION_KEY, cstmt.getInt("o_required_permission") == 1);
            if(cstmt.getInt("o_route_id")==-1){ 
                statusCode = StatusCode.DatabaseError;
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRoute(Route route) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (route.getId() == null || route.getRouteBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE route "
                + " SET "
                + "route_type_id = ?, "
                + "route_notes = ?, "
                + "route_by = ?, "
                + "route_date = SYSDATE, "
                + "route_status_id = ?, "
                + "route_to = ?"
                + " WHERE "
                + "route_id = ?";
        LOG.info(String.format("sql: %s, route_by: %d", sql, route.getRouteBy()));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            // Type id.
            if (route.getTypeId() != null) {
                pstmt.setInt(1, route.getTypeId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }

            // Notes.
            if (route.getNotes() != null && !route.getNotes().isEmpty()) {
                pstmt.setString(2, route.getNotes().trim());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            // Route by.
            pstmt.setInt(3, route.getRouteBy());

            // Status id.
            if (route.getStatusId() != null) {
                pstmt.setInt(4, route.getStatusId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            // Route to.
            if (route.getRouteTo() != null) {
                pstmt.setInt(5, route.getRouteTo());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            // Route id.
            pstmt.setInt(6, route.getId());

            // Do the update.
            int rowCount = pstmt.executeUpdate();

            results.put(ROW_COUNT_KEY, rowCount);
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

    public Map<String, Object> updatePlannedRoute(Route route, Boolean isItsoApproval) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (route.getId() == null || route.getRouteBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE route SET "
                + "route_notes = ?, "
                + "route_by = ?, "
                + "route_date = SYSDATE, "
                //this is used by route history message so we know when is_dynamic = 0 and is_dynamic_reroute = 1
                //the route is an approved dynamic route back to a fixed route approver and the msg will not use "submitted"
                //"approved","ordered"... and will just use "reviewed"
                + "is_dynamic_reroute = 1 "
                + " WHERE "
                + "route_id = ?";

        String sql2 = "UPDATE request SET "
                + "current_route = ? "
                + "WHERE request_id = ?";

        String sql3 = "UPDATE request SET "
                + "itso_approved = ? "
                + "WHERE request_id = ?";

        LOG.info(String.format("sql: %s, route_by: %d", sql, route.getRouteBy()));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            // Notes.
            if (route.getNotes() != null && !route.getNotes().isEmpty()) {
                pstmt.setString(1, route.getNotes().trim());
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }

            // Route by.
            pstmt.setInt(2, route.getRouteBy());

            // Route id.
            pstmt.setInt(3, route.getId());

            // Do the update.
            int rowCount = pstmt.executeUpdate();

            results.put(ROW_COUNT_KEY, rowCount);

            if (rowCount == 1) {//should update 1; after update succeed, update current route in request
                try ( PreparedStatement pstmt2 = connection.prepareStatement(sql2)) {
                    pstmt2.setInt(1, route.getId());
                    pstmt2.setInt(2, route.getRequestId());

                    pstmt2.executeUpdate();
                } catch (Exception caught) {
                    statusCode = StatusCode.DatabaseError;
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }

                if (isItsoApproval) {
                    try ( PreparedStatement pstmt3 = connection.prepareStatement(sql3)) {
                        pstmt3.setInt(1, route.getId());
                        pstmt3.setInt(2, route.getRequestId());
                        pstmt3.executeUpdate();
                    } catch (Exception caught) {
                        statusCode = StatusCode.DatabaseError;
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }
                }

            } else {
                statusCode = StatusCode.DatabaseError;
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

    private Map<String, Object> validateSubmitRequestWithConnection(Connection connection, int requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        String sql = "{ call up_check_submit_request(?, ?, ?) }";
        LOG.info(sql);
        try ( CallableStatement cstmt = connection.prepareCall(sql);) {
            cstmt.setInt(1, requestId);
            cstmt.registerOutParameter(2, Types.VARCHAR);
            cstmt.registerOutParameter(3, Types.VARCHAR);
            cstmt.execute();
            String validation = cstmt.getString(2);
            String propCustDivCode = cstmt.getString(3);
            results.put(ROUTE_VALIDATION_ERROR_MESSAGE_KEY, validation);
            results.put(PROPERTY_CUSTODIAN_DIV_ORG_KEY, propCustDivCode);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    //move the check for justification from submit step to process step
    private Map<String, Object> validateProcessRequestWithConnection(Connection connection, int requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        String sql = "{ call up_check_process_request(?, ?) }";
        LOG.info(sql);
        try ( CallableStatement cstmt = connection.prepareCall(sql);) {
            cstmt.setInt(1, requestId);
            cstmt.registerOutParameter(2, Types.VARCHAR);
            cstmt.execute();
            String validation = cstmt.getString(2);
            results.put(ROUTE_VALIDATION_ERROR_MESSAGE_KEY, validation);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
}
