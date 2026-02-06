package gov.nist.oism.asd.empbc.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.nist.oism.asd.empbc.db.ItemDao;
import gov.nist.oism.asd.empbc.db.RequestDao;
import gov.nist.oism.asd.empbc.db.RouteDao;
import gov.nist.oism.asd.empbc.db.UserDao;
import gov.nist.oism.asd.empbc.db.WsCallFailedRecordDao;
import gov.nist.oism.asd.empbc.model.IbbrChemicalItem;
import gov.nist.oism.asd.empbc.model.RequestRoute;
import gov.nist.oism.asd.empbc.model.Route;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.util.IbbrWSCalls;
import gov.nist.oism.asd.empbc.util.NistOrgWSCalls;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import gov.nist.oism.asd.empbc.model.WsCallFailedRecord;
import javax.servlet.http.HttpSession;
import lombok.Data;

@Path("/routes")
public class RouteService extends SsoService {

    private static final Logger LOG = Logger.getLogger(RouteService.class.getSimpleName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/routeHistory")
    public Response getRouteHistory(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RouteDao dao = new RouteDao();
        Map<String, Object> results = dao.selectRoutesForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetRouteHistoryResponse getRouteHistoryResponse = new GetRouteHistoryResponse();
        if (statusCode == StatusCode.OK) {
            List<Route> routes = (List<Route>) results.get(RouteDao.ROUTE_LIST_KEY);
            List<GetRouteHistoryResponse.RouteHistory> dataList = new ArrayList<>();
            routes.stream().map((route) -> {
                GetRouteHistoryResponse.RouteHistory data = new GetRouteHistoryResponse.RouteHistory();
                data.setRouteId(route.getId());
                data.setRouteBy(route.getRouteBy());
                data.setRouteByName(route.getRouteByName());
                data.setRouteByDelegate(route.getRouteByDelegate());
                data.setRouteByDelegateName(route.getRouteByDelegateName());
                data.setTypeId(route.getTypeId());
                data.setTypeName(route.getTypeName());
                data.setTimestamp(route.getRouteDate());
                data.setStatusId(route.getStatusId());
                data.setStatusName(route.getStatusName());
                data.setRequestId(route.getRequestId());
                data.setNotes(route.getNotes());
                data.setRouteTo(route.getRouteTo());
                data.setRouteToName(route.getRouteToName());
                data.setRerouteBy(route.getRerouteBy());
                data.setRerouteByName(route.getRerouteByName());
                data.setIsDynamic(route.getIsDynamic());
                data.setRerouteStack(route.getRerouteStack());
                data.setIsDynamicReroute(route.getIsDynamicReroute());
                data.setRouteStep(route.getRouteStep());
                data.setDynamicType(route.getDynamicType());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getRouteHistoryResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getRouteHistoryResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/latestRoute")
    public Response getLatestRoute(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RouteDao dao = new RouteDao();
        Map<String, Object> results = dao.selectLatestRouteForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetLatestRouteResponse getLatestRouteResponse = new GetLatestRouteResponse();
        if (statusCode == StatusCode.OK) {
            Route route = (Route) results.get(RouteDao.ROUTE_KEY);
            if (route != null) {
                GetLatestRouteResponse.Route data = new GetLatestRouteResponse.Route();
                data.setRouteId(route.getId());
                data.setRouteBy(route.getRouteBy());
                data.setRouteByName(route.getRouteByName());
                data.setTypeId(route.getTypeId());
                data.setTypeName(route.getTypeName());
                data.setTimestamp(route.getRouteDate());
                data.setStatusId(route.getStatusId());
                data.setStatusName(route.getStatusName());
                data.setRequestId(route.getRequestId());
                data.setNotes(route.getNotes());
                data.setRouteTo(route.getRouteTo());
                data.setRouteToName(route.getRouteToName());
                data.setRerouteBy(route.getRerouteBy());
                data.setRerouteByName(route.getRerouteByName());
                data.setIsDynamic(route.getIsDynamic());
                data.setRerouteStack(route.getRerouteStack());
                data.setIsDynamicReroute(route.getIsDynamicReroute());
                getLatestRouteResponse.setData(data);
            }
        }
        return Response.ok().entity(serializeResponseWithStatus(getLatestRouteResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requestedFor")
    public Response getRequestedFor(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectNistEmployeesInOu(authenticatedUser.getOuId(), filter);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetRequestedForResponse getRequestedForResponse = new GetRequestedForResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<GetRequestedForResponse.RequestedFor> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                GetRequestedForResponse.RequestedFor data = new GetRequestedForResponse.RequestedFor();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                return data;
            }).forEachOrdered((data) -> {
                if (data.getPeopleId().intValue() != authenticatedUser.getPeopleId().intValue()) {
                    dataList.add(data);
                } else {
                    LOG.info("Excluded " + data.getFullName());
                }
            });

            getRequestedForResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getRequestedForResponse, statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postRoute(@Context HttpServletRequest servletRequest, PostRouteRequest routeRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        boolean isDelegating = false;
        Integer trueUserId = null;
        String trueUsername = "";

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        // Check for submitted route and generated requisition number if necessary.
        //if (routeRequest != null && routeRequest.getTypeId() != null && routeRequest.getTypeId() == 1) {
        //issue 618, submit type is 14 now
        if (routeRequest != null && routeRequest.getTypeId() != null && routeRequest.getTypeId() == 14) {
            RequestRoute requestRoute = getRequestRoute(routeRequest.getRequestId());
            if (requestRoute == null) {
                LOG.info("Unable to generate requisition number");
                return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.FailedToGenerateRequisitionNumber)).build();
            }
            if (requestRoute.getRequisitionNumber() == null || requestRoute.getRequisitionNumber().isEmpty()) {
                String requisitionNumber = "";
                //String requisitionNumber = generateRequsitionNumber(requestRoute, servletRequest);

                //detailee mode
                if (session.getAttribute(DETAILEE_USERNAME) != null) {
                    requisitionNumber = generateRequisitionNumber(servletRequest, requestRoute.getFy().toString(),
                            requestRoute.getBankcardHolderId(), requestRoute.getRequesterId(), (String) session.getAttribute(UserService.DETAILEE_DIV_CODE), (String) session.getAttribute(UserService.DETAILEE_GROUP_CODE));
                } else {
                    requisitionNumber = generateRequisitionNumber(servletRequest, requestRoute.getFy().toString(),
                            requestRoute.getBankcardHolderId(), requestRoute.getRequesterId(), null, null);
                }
                LOG.info(String.format("requisitionNumber is %s", requisitionNumber));
                if (requisitionNumber == null || requisitionNumber.isEmpty()) {
                    LOG.info("Unable to generate requisition number");
                    return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.FailedToGenerateRequisitionNumber)).build();
                }

                RequestDao dao = new RequestDao();
                Map<String, Object> results = dao.updateRequisitionNumber(routeRequest.getRequestId(), requisitionNumber);
                StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
                if (statusCode != StatusCode.OK) {
                    LOG.info(String.format("Unable to update requisition number %s", requisitionNumber));
                    return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.InsertFailed)).build();
                }
            }
        }

        //issue587, show delegated approval when a delegate approves a request for the delegator
        if (session.getAttribute(UserService.DELEGATING_USERNAME) != null) {
            isDelegating = true;
            trueUsername = (String) session.getAttribute(DELEGATING_TRUE_USER);
            try {
                UserService userService = new UserService();
                User trueUser = userService.getUserByUsername(trueUsername);
                trueUserId = trueUser.getPeopleId();
            } catch (Exception caught) {
                LOG.log(Level.SEVERE, "Error getting people id of the delegator in delegation mode: " + caught.getMessage(), caught);
            }
        }

        Route route = new Route();
        route.setRequestId(routeRequest.getRequestId());
        route.setTypeId(routeRequest.getTypeId());
        route.setNotes(routeRequest.getNotes());
        //changed use supplied routeby if exists, otherwise, use logged in user id
        //this is needed for EL's ITSO approval which send IT Purchase req to ITSO after submission by the requester
        //but we try to mimic the normal way of submit to reviewer and then reroute to ITSO
        //if we use logged in user's id for route by, the req will be returned to the requester after ITSO approval
        if (routeRequest.getRouteBy() > 0) {
            route.setRouteBy(routeRequest.getRouteBy());
        } else {
            //if in delegaiion mode
            if (isDelegating) {
                //delegator user id
                route.setRouteBy(authenticatedUser.getPeopleId());
                //delegate user id/the person approves the request on behalf of the delegator
                route.setRouteByDelegate(trueUserId);
            } else {
                route.setRouteBy(authenticatedUser.getPeopleId());
            }
        }
        //route.setRouteBy(routeRequest.getRouteBy() > 0 ? routeRequest.getRouteBy() : authenticatedUser.getPeopleId());
        route.setStatusId(routeRequest.getStatusId());
        route.setRouteTo(routeRequest.getRouteTo());
        route.setIsDynamic(routeRequest.getIsDynamic());
        route.setRerouteStack(routeRequest.getRerouteStack());
        route.setIsDynamicReroute(routeRequest.getIsDynamicReroute());
        route.setAlsoNotify(routeRequest.getAlsoNotify());
        route.setRouteStep(routeRequest.getRouteStep());
        route.setDynamicType(routeRequest.getDynamicType());
        route.setOmitNotification(routeRequest.getOmitNotification());
        int statusId = route.getStatusId();

        // These checks are to make sure the next person(route_to) has the privilege of BAO or BCH
        if ((statusId == 6 || statusId == 7 || statusId == 8) && !(isRoutePermissable(route) || authenticatedUser.getAccessAdmin())) {
            LOG.info(String.format("The route is not permissiable. To: %d, Status type: %d ", route.getRouteTo(), route.getStatusId()));
            return Response.ok().entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
        }

        /*
           For archiving the request, cannot use the above since the route to is the offcial requester
           we can check whether the route from is a BAO though since only BAOs can mark a purchase as "received"
           which archives the request and route it back to the offcial requester
           statusId == 13
         */
        RouteDao dao = new RouteDao();
        Map<String, Object> results = dao.insertRoute(route);
        StatusCode statusCode = (StatusCode) results.get(RouteDao.STATUS_CODE_KEY);
        PostRouteResponse postRouteResponse = new PostRouteResponse();
        if (statusCode == StatusCode.OK) {
            if ((Boolean) results.get(RouteDao.REQUIRED_PERMISSION_KEY)) {
                postRouteResponse.setRouteId((Integer) results.get(RouteDao.ROUTE_ID_KEY));
                Boolean callCisproUserWS = (Boolean) results.get(RouteDao.CALL_NOTIFY_CISPRO_USER_KEY);
                Boolean callPcUserWS = (Boolean) results.get(RouteDao.CALL_NOTIFY_PROPERTY_CUSTODIAN_KEY);

                //notify cispro users
                if (callCisproUserWS) {
                    processCisproUsers((String) results.get(RouteDao.CISPRO_DIV_ORG_KEY), route.getRequestId());
                }
                //notify property custodians
                if (callPcUserWS) {
                    processPropertyCustodianUsers((String) results.get(RouteDao.PROPERTY_CUSTODIAN_DIV_ORG_KEY), route.getRequestId());
                }

                // Call IBBR with any chemical items. first check if web.xml has ibbr integeration turned on, then 
                //check if request is ordered
                if (shouldCallIbbr() && route.getTypeId() == 4) {
                    ItemDao itemDao = new ItemDao();
                    results = itemDao.selectIbbrChemicalItemsForRequest(routeRequest.getRequestId());
                    List<IbbrChemicalItem> ibbrChemicalItems = (List<IbbrChemicalItem>) results.get(ItemDao.IBBR_CHEMICAL_ITEMS_KEY);
                    if (ibbrChemicalItems != null && !ibbrChemicalItems.isEmpty()) {
                        LOG.info("There are chemical items with this ordered request");

                        for (IbbrChemicalItem ibbrChemicalItem : ibbrChemicalItems) {
                            // Only call if the is an IBBR room.

                            if (ibbrChemicalItem.getRoom() != null && !ibbrChemicalItem.getRoom().isEmpty()) {
                                SsoService.Error error = IbbrWSCalls.createIbbrChemicalItem(servletRequest, ibbrChemicalItem);
                                if (error != null) {
                                    LOG.info("Creating a WsCallFailedRecord in database");
                                    WsCallFailedRecord record = new WsCallFailedRecord();
                                    record.setIbbrRecord((Integer) results.get(ItemDao.IBBR_CHEMICAL_ITEM_ID_KEY), ibbrChemicalItem);
                                    record.setStatusCode(error.getCode());
                                    record.setErrorMessage(error.getDescription());
                                    // insert a record in web_service_call_failed_record table if the WS Call failed
                                    WsCallFailedRecordDao wsDao = new WsCallFailedRecordDao();
                                    Map<String, Object> wsResults = wsDao.insertWsCallFailedRecord(record);
                                    if (wsResults.get(WsCallFailedRecordDao.STATUS_CODE_KEY) != StatusCode.OK) {
                                        statusCode = (StatusCode) wsResults.get(WsCallFailedRecordDao.STATUS_CODE_KEY);
                                    }
                                }
                            } else {
                                LOG.info("There is no IBBR room with this chemical item, skipping IBBR WS");
                            }

                        }
                    } else {
                        LOG.info("There are no chemical items with this ordered request");
                    }
                }
            } else {
                statusCode = StatusCode.InsufficienPrivileges;
            }
        } else if (statusCode == StatusCode.RouteValidationFailed) {
            String validationErrorMessage = (String) results.get(RouteDao.ROUTE_VALIDATION_ERROR_MESSAGE_KEY);
            return Response.ok().entity(serializeStatusWithCustomizedErrorString(statusCode, validationErrorMessage)).build();
        }
        return Response.ok().entity(serializeResponseWithStatus(postRouteResponse, statusCode)).build();
    }

    @POST
    @Path("/reassign")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    /**
     * This method is added to solve the issue when a request is rerouted, it
     * doesn't change the reviewer, BAO or BCH id in the request table Also, the
     * last route is shown as reroute and doesn't reflect the current stage the
     * request should be on. It will call a new reroute method in the DAO which
     * calls a new SP that should take care of the issue listed above.
     *
     * 03/11/22 renamed reroute here to reassign because we don't want any
     * confusion between this function and the dynamic routing's reroute
     * function
     */
    public Response reAssign(@Context HttpServletRequest servletRequest, PostRouteRequest routeRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Route route = new Route();
        route.setRequestId(routeRequest.getRequestId());
        route.setTypeId(routeRequest.getTypeId());
        route.setNotes(routeRequest.getNotes());
        route.setRouteBy(authenticatedUser.getPeopleId());
        route.setStatusId(routeRequest.getStatusId());
        route.setRouteTo(routeRequest.getRouteTo());
        route.setIsDynamic(routeRequest.getIsDynamic());
        route.setRerouteStack(routeRequest.getRerouteStack());
        route.setIsDynamicReroute(routeRequest.getIsDynamicReroute());

        int statusId = route.getStatusId();

        // These checks are to make sure the next person(route_to) has the privilege
        if ((statusId == 6 || statusId == 7 || statusId == 8 || statusId == 16) && !(isRoutePermissable(route) || authenticatedUser.getAccessAdmin())) {
            LOG.info(String.format("The route is not permissiable. To: %d, Status type: %d ", route.getStatusId(), route.getRouteTo()));
            return Response.ok().entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
        }

        RouteDao dao = new RouteDao();
        Map<String, Object> results = dao.reRoute(route);
        StatusCode statusCode = (StatusCode) results.get(RouteDao.STATUS_CODE_KEY);
        PostRouteResponse postRouteResponse = new PostRouteResponse();
        if (statusCode == StatusCode.OK) {
            if ((Boolean) results.get(RouteDao.REQUIRED_PERMISSION_KEY)) {
                postRouteResponse.setRouteId((Integer) results.get(RouteDao.ROUTE_ID_KEY));
            } else {
                statusCode = StatusCode.InsufficienPrivileges;
            }
        } else if (statusCode == StatusCode.RouteValidationFailed) {
            String validationErrorMessage = (String) results.get(RouteDao.ROUTE_VALIDATION_ERROR_MESSAGE_KEY);
            return Response.ok().entity(serializeStatusWithCustomizedErrorString(statusCode, validationErrorMessage)).build();
        }
        return Response.ok().entity(serializeResponseWithStatus(postRouteResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/planned/{routeId}")
    /**
     * this method is for execute a planned route(dynamic routing) by updating
     * the route note and route date
     */
    public Response execPlannedRoute(@Context HttpServletRequest servletRequest, PutRouteRequest putRouteRequest, @PathParam("routeId") Integer routeId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Route route = new Route();
        route.setRequestId(putRouteRequest.getRequestId());
        route.setId(routeId);
        route.setNotes(putRouteRequest.getNotes());
        //need to update this since a planned route's route by can be changed if more dynamic routes were created after 
        //the planned one was created
        route.setRouteBy(authenticatedUser.getPeopleId());
        route.setDynamicType(putRouteRequest.getDynamicType());

        RouteDao dao = new RouteDao();
        Map<String, Object> results;
        if (putRouteRequest.getIsItsoApproval() != null && putRouteRequest.getIsItsoApproval() == true) {
            results = dao.updatePlannedRoute(route, true);
        } else {
            results = dao.updatePlannedRoute(route, false);
        }
        StatusCode statusCode = (StatusCode) results.get(RouteDao.STATUS_CODE_KEY);
        PutRouteResponse putRouteResponse = new PutRouteResponse();
        if (statusCode == StatusCode.OK) {
            putRouteResponse.setRowCount((Integer) results.get(RouteDao.ROW_COUNT_KEY));
        }
        return Response.ok().entity(serializeResponseWithStatus(putRouteResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{routeId}")
    /**
     * This method is for when changing the requester
     */
    public Response putRoute(@Context HttpServletRequest servletRequest, PutRouteRequest putRouteRequest, @PathParam("routeId") Integer routeId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        if (putRouteRequest.getStatusId() != 1 && putRouteRequest.getStatusId() != 12) {
            LOG.info("Invalid route");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.InvalidRoute)).build();
        }

        Route route = new Route();
        route.setId(routeId);
        route.setTypeId(putRouteRequest.getTypeId());
        route.setNotes(putRouteRequest.getNotes());
        route.setRouteBy(authenticatedUser.getPeopleId());
        route.setStatusId(putRouteRequest.getStatusId());
        route.setRouteTo(putRouteRequest.getRouteTo());

        RouteDao dao = new RouteDao();
        Map<String, Object> results = dao.updateRoute(route);
        StatusCode statusCode = (StatusCode) results.get(RouteDao.STATUS_CODE_KEY);
        PutRouteResponse putRouteResponse = new PutRouteResponse();
        if (statusCode == StatusCode.OK) {
            putRouteResponse.setRowCount((Integer) results.get(RouteDao.ROW_COUNT_KEY));
        }
        return Response.ok().entity(serializeResponseWithStatus(putRouteResponse, statusCode)).build();
    }

    private void processCisproUsers(String divOrgCode, Integer requestId) {
        if (divOrgCode != null && divOrgCode.length() == 5) {

            try {
                StringBuilder employeeNumbers = new StringBuilder("");
                NistOrgWSCalls.MmlCisproUsersUrlCall mmlCisproUsersUrlCall = NistOrgWSCalls.callMmlCisproUsersService(divOrgCode);
                if (mmlCisproUsersUrlCall != null && mmlCisproUsersUrlCall.cims_power_users != null) {
                    mmlCisproUsersUrlCall.cims_power_users.forEach((cisproUser) -> {
                        employeeNumbers.append(cisproUser.employee_number).append(",");
                    });

                    // Strip out any trailing commas.
                    String peopleIds = employeeNumbers.toString();
                    if (peopleIds.endsWith(",")) {
                        peopleIds = peopleIds.substring(0, peopleIds.length() - 1);
                    }

                    if (peopleIds.length() > 0) {
                        RouteDao dao = new RouteDao();
                        dao.notifyCisproUsers(requestId, peopleIds);
                    }
                }
            } catch (Exception caught) {
                LOG.log(Level.SEVERE, caught.getMessage(), caught);
            }
        }
    }

    private void processPropertyCustodianUsers(String divOrgCode, Integer requestId) {
        if (divOrgCode != null && divOrgCode.length() == 3) {

            try {
                StringBuilder employeeNumbers = new StringBuilder("");
                NistOrgWSCalls.MmlPropertyCustodianUsersUrlCall mmlPcUsersUrlCall = NistOrgWSCalls.callMmlPcUsersService(divOrgCode);
                if (mmlPcUsersUrlCall != null && mmlPcUsersUrlCall.property_custodians != null) {
                    mmlPcUsersUrlCall.property_custodians.forEach((pcUser) -> {
                        employeeNumbers.append(pcUser.employee_number).append(",");
                    });

                    // Strip out any trailing commas.
                    String peopleIds = employeeNumbers.toString();
                    if (peopleIds.endsWith(",")) {
                        peopleIds = peopleIds.substring(0, peopleIds.length() - 1);
                    }

                    if (peopleIds.length() > 0) {
                        RouteDao dao = new RouteDao();
                        dao.notifyPropertyCustodianUsers(requestId, peopleIds);
                    }
                }
            } catch (Exception caught) {
                LOG.log(Level.SEVERE, caught.getMessage(), caught);
            }
        }
    }

    private boolean isRoutePermissable(Route route) {
        //for dynamic route, since the route to can be anyone in the OU, we don't check role
        if (route.getIsDynamic() == 1) {
            return true;
        }

        Integer routeToPeopleId = route.getRouteTo();
        if (routeToPeopleId == null) {
            LOG.log(Level.SEVERE, "RouteTo peopleId is null");
            return false;
        }

        List<String> userRoles = getUserRoles(routeToPeopleId);

        return switch (route.getStatusId()) {
            case 6 ->
                isUserInRole(new String[]{"Bankcard Approving Official"}, userRoles);
            case 16 ->
                isUserInRole(new String[]{"Funds Certifying Official"}, userRoles);
            case 7, 8 ->
                isUserInRole(new String[]{"Bankcard Holder"}, userRoles);
            default ->
                isUserInRole(new String[]{"Senior Management Advisor", "Administrative Officer", "Executive Officer"}, userRoles);
        };
    }

    private RequestRoute getRequestRoute(Integer requestId) {
        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(RouteDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            return (RequestRoute) results.get(RequestDao.REQUEST_ROUTE_KEY);
        }
        return null;
    }

    public static class GetRouteHistoryResponse extends JsonStatus {

        public static class RouteHistory {

            private Integer mRouteId;
            private Integer mRouteBy;
            private String mRouteByName;
            private Integer routeByDelegate;
            private String routeByDelegateName;
            private Date mTimestamp;
            private Integer mTypeId;
            private String mTypeName;
            private Integer mStatusId;
            private String mStatusName;
            private Integer mRequestId;
            private String mNotes;
            private Integer mRouteTo;
            private String mRouteToName;
            private Integer mRerouteBy;
            private String mRerouteByName;
            private Integer isDynamic;
            private Integer rerouteStack;
            private Integer isDynamicReroute;
            private Integer routeStep;
            private String dynamicType;

            public String getRouteByDelegateName() {
                return routeByDelegateName;
            }

            public void setRouteByDelegateName(String routeByDelegateName) {
                this.routeByDelegateName = routeByDelegateName;
            }

            public Integer getRouteByDelegate() {
                return routeByDelegate;
            }

            public void setRouteByDelegate(Integer routeByDelegate) {
                this.routeByDelegate = routeByDelegate;
            }

            public Integer getRouteStep() {
                return routeStep;
            }

            public void setRouteStep(Integer routeStep) {
                this.routeStep = routeStep;
            }

            public String getDynamicType() {
                return dynamicType;
            }

            public void setDynamicType(String dynamicType) {
                this.dynamicType = dynamicType;
            }

            public Integer getIsDynamicReroute() {
                return isDynamicReroute;
            }

            public void setIsDynamicReroute(Integer isDynamicReroute) {
                this.isDynamicReroute = isDynamicReroute;
            }

            public Integer getRerouteStack() {
                return rerouteStack;
            }

            public void setRerouteStack(Integer rerouteStack) {
                this.rerouteStack = rerouteStack;
            }

            public Integer getIsDynamic() {
                return isDynamic;
            }

            public void setIsDynamic(Integer isDynamic) {
                this.isDynamic = isDynamic;
            }

            public Integer getRouteId() {
                return mRouteId;
            }

            public void setRouteId(Integer routeId) {
                mRouteId = routeId;
            }

            public Integer getRouteBy() {
                return mRouteBy;
            }

            public void setRouteBy(Integer routeBy) {
                mRouteBy = routeBy;
            }

            public String getRouteByName() {
                return mRouteByName;
            }

            public void setRouteByName(String routeByName) {
                mRouteByName = routeByName;
            }

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            public Date getTimestamp() {
                return mTimestamp;
            }

            public void setTimestamp(Date timestamp) {
                mTimestamp = timestamp;
            }

            public Integer getTypeId() {
                return mTypeId;
            }

            public void setTypeId(Integer typeId) {
                mTypeId = typeId;
            }

            public String getTypeName() {
                return mTypeName;
            }

            public void setTypeName(String typeName) {
                mTypeName = typeName;
            }

            public Integer getStatusId() {
                return mStatusId;
            }

            public void setStatusId(Integer statusId) {
                mStatusId = statusId;
            }

            public String getStatusName() {
                return mStatusName;
            }

            public void setStatusName(String statusName) {
                mStatusName = statusName;
            }

            public Integer getRequestId() {
                return mRequestId;
            }

            public void setRequestId(Integer requestId) {
                mRequestId = requestId;
            }

            public String getNotes() {
                return mNotes;
            }

            public void setNotes(String notes) {
                mNotes = notes;
            }

            public Integer getRouteTo() {
                return mRouteTo;
            }

            public void setRouteTo(Integer routeTo) {
                mRouteTo = routeTo;
            }

            public String getRouteToName() {
                return mRouteToName;
            }

            public void setRouteToName(String routeToName) {
                mRouteToName = routeToName;
            }

            public Integer getRerouteBy() {
                return mRerouteBy;
            }

            public void setRerouteBy(Integer rerouteBy) {
                mRerouteBy = rerouteBy;
            }

            public String getRerouteByName() {
                return mRerouteByName;
            }

            public void setRerouteByName(String rerouteByName) {
                mRerouteByName = rerouteByName;
            }
        }

        private List<GetRouteHistoryResponse.RouteHistory> mData;

        public List<GetRouteHistoryResponse.RouteHistory> getData() {
            return mData;
        }

        public void setData(List<GetRouteHistoryResponse.RouteHistory> data) {
            mData = data;
        }
    }

    public static class GetLatestRouteResponse extends JsonStatus {

        public static class Route {

            private Integer mRouteId;
            private Integer mRouteBy;
            private String mRouteByName;
            private Date mTimestamp;
            private Integer mTypeId;
            private String mTypeName;
            private Integer mStatusId;
            private String mStatusName;
            private Integer mRequestId;
            private String mNotes;
            private Integer mRouteTo;
            private String mRouteToName;
            private Integer mRerouteBy;
            private String mRerouteByName;
            private Integer isDynamic;
            private Integer rerouteStack;
            private Integer isDynamicReroute;

            public Integer getIsDynamicReroute() {
                return isDynamicReroute;
            }

            public void setIsDynamicReroute(Integer isDynamicReroute) {
                this.isDynamicReroute = isDynamicReroute;
            }

            public Integer getRerouteStack() {
                return rerouteStack;
            }

            public void setRerouteStack(Integer rerouteStack) {
                this.rerouteStack = rerouteStack;
            }

            public Integer getIsDynamic() {
                return isDynamic;
            }

            public void setIsDynamic(Integer isDynamic) {
                this.isDynamic = isDynamic;
            }

            public Integer getRouteId() {
                return mRouteId;
            }

            public void setRouteId(Integer routeId) {
                mRouteId = routeId;
            }

            public Integer getRouteBy() {
                return mRouteBy;
            }

            public void setRouteBy(Integer routeBy) {
                mRouteBy = routeBy;
            }

            public String getRouteByName() {
                return mRouteByName;
            }

            public void setRouteByName(String routeByName) {
                mRouteByName = routeByName;
            }

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            public Date getTimestamp() {
                return mTimestamp;
            }

            public void setTimestamp(Date timestamp) {
                mTimestamp = timestamp;
            }

            public Integer getTypeId() {
                return mTypeId;
            }

            public void setTypeId(Integer typeId) {
                mTypeId = typeId;
            }

            public String getTypeName() {
                return mTypeName;
            }

            public void setTypeName(String typeName) {
                mTypeName = typeName;
            }

            public Integer getStatusId() {
                return mStatusId;
            }

            public void setStatusId(Integer statusId) {
                mStatusId = statusId;
            }

            public String getStatusName() {
                return mStatusName;
            }

            public void setStatusName(String statusName) {
                mStatusName = statusName;
            }

            public Integer getRequestId() {
                return mRequestId;
            }

            public void setRequestId(Integer requestId) {
                mRequestId = requestId;
            }

            public String getNotes() {
                return mNotes;
            }

            public void setNotes(String notes) {
                mNotes = notes;
            }

            public Integer getRouteTo() {
                return mRouteTo;
            }

            public void setRouteTo(Integer routeTo) {
                mRouteTo = routeTo;
            }

            public String getRouteToName() {
                return mRouteToName;
            }

            public void setRouteToName(String routeToName) {
                mRouteToName = routeToName;
            }

            public Integer getRerouteBy() {
                return mRerouteBy;
            }

            public void setRerouteBy(Integer rerouteBy) {
                mRerouteBy = rerouteBy;
            }

            public String getRerouteByName() {
                return mRerouteByName;
            }

            public void setRerouteByName(String rerouteByName) {
                mRerouteByName = rerouteByName;
            }
        }

        private GetLatestRouteResponse.Route mData;

        public GetLatestRouteResponse.Route getData() {
            return mData;
        }

        public void setData(GetLatestRouteResponse.Route data) {
            mData = data;
        }
    }

    public static class GetRequestedForResponse extends JsonStatus {

        public static class RequestedFor {

            private Integer mPeopleId;
            private String mFullName;

            public Integer getPeopleId() {
                return mPeopleId;
            }

            public void setPeopleId(Integer peopleId) {
                mPeopleId = peopleId;
            }

            public String getFullName() {
                return mFullName;
            }

            public void setFullName(String fullName) {
                mFullName = fullName;
            }
        }

        private List<GetRequestedForResponse.RequestedFor> mData;

        public List<GetRequestedForResponse.RequestedFor> getData() {
            return mData;
        }

        public void setData(List<GetRequestedForResponse.RequestedFor> data) {
            mData = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PostRouteRequest {

        private Integer requestId;
        private Integer typeId;
        private String notes;
        private Integer routeBy;
        private Integer statusId;
        private Integer routeTo;
        private Integer isDynamic;
        private Integer rerouteStack;
        private Integer isDynamicReroute;
        private Integer fy;
        private String alsoNotify;
        private Integer routeStep;
        private String dynamicType;
        private Integer omitNotification;

    }

    public static class PostRouteResponse extends JsonStatus {

        private Integer mRouteId;

        public Integer getRouteId() {
            return mRouteId;
        }

        public void setRouteId(Integer routeId) {
            mRouteId = routeId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutRouteRequest {

        private Integer requestId;
        private Integer mTypeId;
        private String mNotes;
        private Integer mStatusId;
        private Integer mRouteTo;
        private String dynamicType;
        //if this route update is a result of a ITSO approval, we need to perform one more step to 
        //update the request's status after the route update
        private Boolean isItsoApproval;

        public Boolean getIsItsoApproval() {
            return isItsoApproval;
        }

        public void setIsItsoApproval(Boolean isItsoApproval) {
            this.isItsoApproval = isItsoApproval;
        }

        public Integer getRequestId() {
            return requestId;
        }

        public void setRequestId(Integer requestId) {
            this.requestId = requestId;
        }

        public String getDynamicType() {
            return dynamicType;
        }

        public void setDynamicType(String dynamicType) {
            this.dynamicType = dynamicType;
        }

        public Integer getTypeId() {
            return mTypeId;
        }

        public void setTypeId(Integer typeId) {
            mTypeId = typeId;
        }

        public String getNotes() {
            return mNotes;
        }

        public void setNotes(String notes) {
            mNotes = notes;
        }

        public Integer getStatusId() {
            return mStatusId;
        }

        public void setStatusId(Integer statusId) {
            mStatusId = statusId;
        }

        public Integer getRouteTo() {
            return mRouteTo;
        }

        public void setRouteTo(Integer routeTo) {
            mRouteTo = routeTo;
        }
    }

    public static class PutRouteResponse extends JsonStatus {

        private Integer mRowCount;

        public Integer getRowCount() {
            return mRowCount;
        }

        public void setRowCount(Integer rowCount) {
            mRowCount = rowCount;
        }
    }
}
