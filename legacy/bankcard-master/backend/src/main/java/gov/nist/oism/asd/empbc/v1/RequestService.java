package gov.nist.oism.asd.empbc.v1;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.oism.asd.empbc.db.FileAttachmentDao;
import gov.nist.oism.asd.empbc.db.ItemDao;
import gov.nist.oism.asd.empbc.db.RequestDao;
import gov.nist.oism.asd.empbc.model.Approval;
import gov.nist.oism.asd.empbc.model.AuditReport;
import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.model.Item;
import gov.nist.oism.asd.empbc.model.Request;
import gov.nist.oism.asd.empbc.model.RequestRoute;
import gov.nist.oism.asd.empbc.model.RequestSummaryReport;
import gov.nist.oism.asd.empbc.model.RequestVendor;
import gov.nist.oism.asd.empbc.model.RequestJustification;
import gov.nist.oism.asd.empbc.model.Route;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.Vendor;
import gov.nist.oism.asd.empbc.util.CommonUtil;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.StatusCodeException;
import gov.nist.oism.asd.empbc.util.ValidatorUtil;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

@Path("/requests")
public class RequestService extends SsoService {

    private static final Logger LOG = Logger.getLogger(RequestService.class.getSimpleName());
    private static final int NUM_ITEMS_IN_FIRST_PAGE_AUDIT_REPORT = 5;
    private static final int NUM_ITEMS_IN_ADDITIONAL_PAGE_AUDIT_REPORT = 16;
    private static final int NUM_ITEMS_IN_FIRST_PAGE_SUMMARY_REPORT = 2;
    private static final int NUM_ITEMS_IN_ADDITIONAL_PAGE_SUMMARY_REPORT = 6;
    private static final int NUM_FILE_ATTACHMENTS_SUMMARY_REPORT = 18;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRequestByCriteria(@Context HttpServletRequest servletRequest,
            @QueryParam("ouId") Integer ouId,
            @QueryParam("divisionId") Integer divisionId,
            @QueryParam("groupId") Integer groupId,
            @QueryParam("requesterId") Integer requesterId,
            @QueryParam("requisitionNumber") String reqNumString,
            @QueryParam("routeTypeId") Integer routeTypeId,
            @QueryParam("fromDate") String fromDateString,
            @QueryParam("toDate") String toDateString,
            @QueryParam("vendorName") String vendorName,
            @QueryParam("transactionNum") String transactionNum,
            @QueryParam("itemName") String itemName,
            @QueryParam("actualTotal") Double actualTotal,
            @QueryParam("bchId") Integer bchId,
            @QueryParam("requestId") Integer requestId,
            @QueryParam("fy") Integer fy,
            @QueryParam("ptc") String ptc,
            @QueryParam("partialOrder") Boolean partialOrder,
            @QueryParam("delivToHome") Boolean delivToHome,
            @QueryParam("statementDate") String statementDate,
            @QueryParam("description") String description,
            @QueryParam("itemStatuses") String itemStatuses,
            @QueryParam("purchaseTypeId") Integer purchaseTypeId,
            @QueryParam("reviewerId") Integer reviewerId,
            @QueryParam("taggable") Boolean taggable
    //@QueryParam("isItPurchase") Boolean isItPurc
    ) {

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        Date fromDate;
        Date toDate;
        Date stmtDate;
        try {
            fromDate = ValidatorUtil.parseDate(fromDateString, "fromDate");
            toDate = ValidatorUtil.parseDate(toDateString, "toDate");
            stmtDate = ValidatorUtil.parseDate(statementDate, "statementDate");
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
        }

        Boolean partOrder = null;
        if (partialOrder != null) {
            partOrder = partialOrder;
        }

        Boolean isTaggable = null;
        if (taggable != null) {
            isTaggable = taggable;
        }

        Boolean deliverToHome = null;
        if (delivToHome != null) {
            deliverToHome = delivToHome;
        }

        RequestDao.RequestQueryParameters queryParameters = new RequestDao.RequestQueryParameters();
        queryParameters.setUsername(authenticatedUser.getUsername());
        queryParameters.setOuId(ouId);
        queryParameters.setDivisionId(divisionId);
        queryParameters.setGroupId(groupId);
        queryParameters.setRequesterId(requesterId);
        queryParameters.setRequisitionNumber(reqNumString);
        queryParameters.setRouteTypeId(routeTypeId);
        queryParameters.setFromDate(fromDate);
        queryParameters.setToDate(toDate);
        queryParameters.setVendorName(vendorName);
        queryParameters.setTransactionNumber(transactionNum);
        //frontend allows every char, so we need to strip out weird chars here before passing it to DB
        //SP will do the same and try to match
        if (itemName != null) {
            itemName = itemName.replaceAll("[^a-zA-Z0-9 ]", " ");
        }
        queryParameters.setItemName(itemName);
        queryParameters.setItemStatuses(itemStatuses);
        queryParameters.setActualTotal(actualTotal);
        queryParameters.setBankcardHolderId(bchId);
        queryParameters.setReviewerId(reviewerId);
        queryParameters.setRequestId(requestId);
        queryParameters.setFy(fy);
        queryParameters.setPtc(ptc);
        queryParameters.setStatementDate(stmtDate);
        queryParameters.setPartialOrder(partOrder);
        queryParameters.setDelivToHome(deliverToHome);
        queryParameters.setTaggable(isTaggable);
        queryParameters.setPurchaseTypeId(purchaseTypeId);
        queryParameters.setDescription(description);
        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectRequestWithParameters(queryParameters);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetRequestsByCriteriaResponse getRequestsByCriteriaResponse = new GetRequestsByCriteriaResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getRequestsByCriteriaResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getRequestsByCriteriaResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}") 
    public Response getRequestById(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);

        // Early exit if user is not authenticated
        if (Objects.isNull(authenticatedUser) || Objects.isNull(authenticatedUser.getPeopleId())) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestRoute req = getRequestbyId(requestId);

        // Early exit if request is not found.
        if (Objects.isNull(req)) {
            LOG.log(Level.INFO, "Request with id:{0} not found", requestId.toString());
            return Response.status(Response.Status.NOT_FOUND).entity(serializeStatus(StatusCode.RequestNotFound)).build();
        }

        //check if request has OUid and if it's the same as user's
        int ouId = req.getOuId();
        boolean isSameOu = authenticatedUser.getOuId() == ouId;

        // Check if user is authorized to access the request, different OU
        if (!isSameOu && !isUserAllowedOuAccessWithDetailedPrivilege(authenticatedUser, ouId) && !authenticatedUser.getAccessAdmin()) {
            LOG.log(Level.INFO, "User with Id {0} is unauthorized to access request {1}", new Object[]{authenticatedUser.getPeopleId(), requestId});
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        // User is authorized, create the response
        GetRequestResponse getRequestResponse = new GetRequestResponse();
        getRequestResponse.setData(createRequestDeepCopyFrom(req));
        return Response.ok().entity(serializeResponseWithStatus(getRequestResponse, StatusCode.OK)).build();
    }

    @GET
    @Produces("application/pdf")
    @Path("/{requestId}/auditReport")
    public Response getAuditReport(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            //return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("The user is not authorized or session timed out. Please reload the application and try again.").build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectAuditReport(requestId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            AuditReport auditReport = (AuditReport) results.get(RequestDao.AUDIT_REPORT_KEY);
            if (auditReport != null) {
                try {
                    File webInfDirectory = new File(servletRequest.getServletContext().getRealPath("/WEB-INF"));
                    String outputFilename = UUID.randomUUID().toString().replace("-", "") + ".pdf";
                    boolean success = generateAuditReportPdf(outputFilename, webInfDirectory, auditReport);
                    if (success) {
                        File tempDirectory = new File(webInfDirectory, "temp");
                        if (!tempDirectory.exists()) {
                            tempDirectory.mkdirs();
                        }
                        final File pdfFile = new File(tempDirectory, outputFilename);
                        ResponseBuilder responseBuilder = Response.ok((Object) pdfFile);
                        String reportName = "audit_report.pdf";
                        if (auditReport.getRequisitionNumber() != null && !auditReport.getRequisitionNumber().isEmpty()) {
                            reportName = "AuditReport_" + auditReport.getRequisitionNumber().trim() + ".pdf";
                        }
                        responseBuilder.header("Content-Disposition", String.format("attachment; filename=\"%s\"", reportName));
                        Thread deleteFileFuture = new Thread() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(10000); // 10 seconds.
                                    pdfFile.delete();
                                } catch (Exception ignore) {
                                }
                            }
                        };
                        deleteFileFuture.start();
                        return responseBuilder.build();
                    }
                } catch (Exception caught) {
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }
            }
            return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("Cannot find Report").build();
        }

        return Response.ok().type(MediaType.TEXT_PLAIN).entity("Error generating report").build();
    }

    @GET
    @Produces("application/pdf")
    @Path("/{requestId}/requestSummaryReport")
    public Response getRequestSummaryReport(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.TEXT_PLAIN).entity("The user is not authorized or session timed out. Please reload the application and try again.").build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectRequestSummaryReport(requestId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            RequestSummaryReport requestSummaryReport = (RequestSummaryReport) results.get(RequestDao.REQUEST_SUMMARY_REPORT_KEY);
            if (requestSummaryReport != null) {
                try {
                    File webInfDirectory = new File(servletRequest.getServletContext().getRealPath("/WEB-INF"));
                    String outputFilename = UUID.randomUUID().toString().replace("-", "") + ".pdf";
                    boolean success = generateRequestSummaryReportPdf(outputFilename, webInfDirectory, requestSummaryReport);
                    if (success) {
                        File tempDirectory = new File(webInfDirectory, "temp");
                        if (!tempDirectory.exists()) {
                            tempDirectory.mkdirs();
                        }
                        final File pdfFile = new File(tempDirectory, outputFilename);
                        ResponseBuilder responseBuilder = Response.ok((Object) pdfFile);
                        String reportName = "request_summary_report.pdf";
                        if (requestSummaryReport.getRequisitionNumber() != null && !requestSummaryReport.getRequisitionNumber().isEmpty()) {
                            reportName = "RequestSummaryReport_" + requestSummaryReport.getRequisitionNumber().trim() + ".pdf";
                        }
                        responseBuilder.header("Content-Disposition", String.format("attachment; filename=\"%s\"", reportName));
                        Thread deleteFileFuture = new Thread() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(10000); // 10 seconds.
                                    pdfFile.delete();
                                } catch (Exception ignore) {
                                }
                            }
                        };
                        deleteFileFuture.start();
                        return responseBuilder.build();
                    }
                } catch (Exception caught) {
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }
            }
            return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("Cannot find Report").build();
        }

        return Response.ok().type(MediaType.TEXT_PLAIN).entity("Error generating report").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/saved")
    public Response getSavedRequests(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectSavedRequestRoutes(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetSavedRequestsResponse getSavedRequestsResponse = new GetSavedRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getSavedRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getSavedRequestsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/submitted")
    public Response getSubmittedRequests(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectSubmittedRequestRoutes(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetSubmittedRequestsResponse getSubmittedRequestsResponse = new GetSubmittedRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getSubmittedRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getSubmittedRequestsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/pending")
    public Response getPendingRequests(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        List<String> userRoles = getUserRoles(authenticatedUser.getPeopleId());

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectPendingRequestRoutes(authenticatedUser.getPeopleId(), isUserInRole(new String[]{"ITSO"}, userRoles));
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetPendingRequestsResponse getPendingRequestsResponse = new GetPendingRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getPendingRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getPendingRequestsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/prepared")
    public Response getPreparedRequests(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectPreparedRequestRoutes(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetPreparedRequestsResponse getPreparedRequestsResponse = new GetPreparedRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getPreparedRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getPreparedRequestsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/processed/{fy}")
    public Response getProcessedRequests(@Context HttpServletRequest servletRequest, @PathParam("fy") Integer fy,
            @QueryParam("showPurchaseWithMissingStmtDt") @DefaultValue("false") String showPurchaseWithMissingStmtDt) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectProcessedRequestRoutes(authenticatedUser.getPeopleId(), fy, showPurchaseWithMissingStmtDt);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetProcessedRequestsResponse getProcessedRequestsResponse = new GetProcessedRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getProcessedRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getProcessedRequestsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/archived")
    public Response getArchivedRequests(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectArchivedRequestRoutes(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetArchivedRequestsResponse getArchivedRequestsResponse = new GetArchivedRequestsResponse();
        if (statusCode == StatusCode.OK) {
            List<RequestRoute> requestRoutes = (List<RequestRoute>) results.get(RequestDao.REQUEST_ROUTE_LIST_KEY);
            List<RequestDeepCopy> dataList = new ArrayList<>();
            requestRoutes.stream().map((requestRoute) -> {
                return createRequestDeepCopyFrom(requestRoute);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getArchivedRequestsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getArchivedRequestsResponse, statusCode)).build();
    }

    //not used for now
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/counts")
    public Response getRequestCounts(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectRequestCounts(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        GetRequestCountsResponse getRequestCountsResponse = new GetRequestCountsResponse();
        if (statusCode == StatusCode.OK) {
            GetRequestCountsResponse.Count count = new GetRequestCountsResponse.Count();
            count.setSavedRequests((Integer) results.get(RequestDao.SAVED_REQUESTS_COUNT_KEY));
            count.setActiveRequests((Integer) results.get(RequestDao.ACTIVE_REQUESTS_COUNT_KEY));
            count.setInboxRequests((Integer) results.get(RequestDao.INBOX_REQUESTS_COUNT_KEY));

            getRequestCountsResponse.setData(count);
        }

        return Response.ok().entity(serializeResponseWithStatus(getRequestCountsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/items")
    public Response getRequestItems(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.selectItemsForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        GetItemsResponse getItemsResponse = new GetItemsResponse();
        if (statusCode == StatusCode.OK) {
            List<Item> items = (List<Item>) results.get(ItemDao.ITEMS_LIST_KEY);
            List<GetItemsResponse.Item> dataList = new ArrayList<>();
            items.stream().map((item) -> {
                GetItemsResponse.Item data = new GetItemsResponse.Item();
                data.setId(item.getId());
                data.setRequestId(item.getRequestId());
                //data.setType(item.getType());
                data.setVendorId(item.getVendorId());
                data.setCatalogNumber(item.getCatalogNumber());
                data.setItemName(item.getItemName());
                data.setDescription(item.getDescription());
                data.setPrice(item.getPrice());
                data.setQuantity(item.getQuantity());
                data.setActualPrice(item.getActualPrice());
                data.setActualQuantity(item.getActualQuantity());
                data.setPurpose(item.getPurpose());
                data.setChemical(item.getChemical());
                data.isTaggableEquipment = item.getIsTaggableEquipment();
                data.setProjectTask(item.getProjectTask());
                data.setShoppingCartFileId(item.getShoppingCartFileId());
                data.setStatusId(item.getStatusId());
                data.setObjectClass(item.getObjectClass());
                data.setLatestItemStatusTypeId(item.getLatestStatusTypeId());
                data.setLatestItemStatusTypeName(item.getLatestStatusTypeName());
                data.setIsShippingCost(item.getIsShipping());
                data.setItemNotes(item.getItemNotes());
                data.setUnitIssue(item.getUnitIssue());
                data.setDateReceived(item.getDateReceived());
                data.setTransactionNumber(item.getTransactionNumber());
                data.setStatementDate(item.getStatementDate());

                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });
            getItemsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getItemsResponse, statusCode)).build();
    }

    /**
     * API calls to get division chief data based on division passed in by the
     * client app e.g. for mission critical approval, the DC should be the
     * official requester's div DC, not the preparer(user logged in or user in
     * detailee mode)
     *
     * @param servletRequest
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{divCode}/divisionChiefs/")
    public Response getDivisionChiefByRequester(@Context HttpServletRequest servletRequest, @PathParam("divCode") String divCode) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        //TODO: this method should take reqId from the client app, then the req from db using the reqId and get divCode from 
        //the req. However, since req only have div_org_id and NSIT ORG API does not use org_id, this would require another
        //call to get the divCode using the div_org_id. To make things work fast for now, we will take divCode from client app 
        //directly.
        //RequestRoute req = getRequestbyId(requestId);
        //String divCode = "";
        //if (req != null) {
        //divCode=req.getDivisionId()
        //}
        return getDivisionChiefByDivisionCode(servletRequest, divCode);

    }

    /**
     * API calls to get missionCritialDrApprover data based on ou passed in by
     * the client app e.g. for mission critical director approval, the DR should
     * be the official requester's ou DR, not the preparer(user logged in or
     * user in detailee mode)
     *
     * @param servletRequest
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ouCode}/missionCritialDrApprover")
    public Response getMissionCritialDrApprover(@Context HttpServletRequest servletRequest, @PathParam("ouCode") String ouCode) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        return getDrApproverByOuCode(servletRequest, ouCode);

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/items/projectTask/{projectTask}")
    /**
     * update project task for all items in the request
     */
    public Response putProjectTaskForRequestItems(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, @PathParam("projectTask") String projectTask) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateProjectTaskForRequestItems(projectTask, requestId);
        PutProjectTaskForRequestItemsResponse putProjectTaskForRequestItemsResponse = new PutProjectTaskForRequestItemsResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putProjectTaskForRequestItemsResponse.setRowsUpdated((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putProjectTaskForRequestItemsResponse, statusCode)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/items/objectClass/{objectClass}")
    public Response putObjectClassForRequestItems(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, @PathParam("objectClass") String objectClass) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.updateObjectClassForRequestItems(objectClass, requestId);
        PutObjectClassForRequestItemsResponse putObjectClassForRequestItemsResponse = new PutObjectClassForRequestItemsResponse();
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            putObjectClassForRequestItemsResponse.setRowsUpdated((Integer) results.get(ItemDao.ROW_COUNT_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(putObjectClassForRequestItemsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/attachments")
    public Response getAttachments(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.selectFileAttachmentsForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);
        GetAttachmentsResponse getAttachmentsResponse = new GetAttachmentsResponse();
        if (statusCode == StatusCode.OK) {
            List<FileAttachment> fileAttachments = (List<FileAttachment>) results.get(FileAttachmentDao.FILE_ATTACHMENT_LIST_KEY);
            List<GetAttachmentsResponse.Attachment> dataList = new ArrayList<>();
            fileAttachments.stream().map((fileAttachment) -> {
                GetAttachmentsResponse.Attachment data = new GetAttachmentsResponse.Attachment();
                data.setId(fileAttachment.getId());
                data.setRequestId(fileAttachment.getRequestId());
                data.setCategoryId(fileAttachment.getCategoryId());
                data.setCategoryName(fileAttachment.getCategoryName());
                data.setName(fileAttachment.getName());
                data.setTypeCode(fileAttachment.getTypeCode());
                data.setSize(fileAttachment.getSize());
                data.setCreatedBy(fileAttachment.getCreatedBy());
                data.setCreatedByName(fileAttachment.getCreatedByName());
                data.setCreatedDate(fileAttachment.getCreatedDate());

                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });
            getAttachmentsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getAttachmentsResponse, statusCode)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRequest(@Context HttpServletRequest servletRequest, PostRequest postRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        String requisitionNumber = null;
        User sampleUser = null;

        Request request = new Request();
        request.setCreatedBy(authenticatedUser.getPeopleId());
        request.setCreatedFor(postRequest.getRequestedForId());
        request.setRequesterId(postRequest.getRequesterId());
        request.setNotes(postRequest.getComments());
        request.setDescription(postRequest.getDescription());
        request.setFy(postRequest.getFy());
        request.setIsItPurchase(postRequest.getIsItPurchase());
        request.setPurchaseTypeId(postRequest.getPurchaseTypeId());
        request.setOuId(postRequest.getOuId());
        request.setDivisionId(postRequest.getDivisionId());
        request.setGroupId(postRequest.getGroupId());
        request.setMissionCriticalCategoryId(postRequest.getMissionCriticalCategoryId());
        request.setMissionCriticalJustification(postRequest.getMissionCriticalJustification());

        //if this is a detail user request(made for a different group), we need to find a sample user from the group, then 
        //get default routes for that sample user
        if (postRequest.getIsDetailReq() != null && true == postRequest.getIsDetailReq()) {

            try {
                sampleUser = getSampleUserByGroupId(postRequest.getGroupId());
            } catch (StatusCodeException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(StatusCode.DatabaseError)).build();
            }
            UserService userService = new UserService();
            UserService.Route route = userService.getDefaultRoute(servletRequest, sampleUser.getPeopleId());

            //approvers
            if (route != null) {
                if (route.getReviewer() != null) {
                    request.setReviewerId(route.getReviewer().getPersonId());
                }
                if (route.getBankcardApprovingOfficial() != null) {
                    request.setBankcardApprovingOfficialId(route.getBankcardApprovingOfficial().getPersonId());
                }
                if (route.getBankcardHolder() != null) {
                    request.setBankcardHolderId(route.getBankcardHolder().getPersonId());
                }
            }

        } else {
            // for regular request (made for user's group)
            //indicate this request uses requester's ou, div, and group
            request.setGroupId(0);

            //MB-434 the user prepared the request for someone. we need to get default route for that someone
            if (!Objects.equals(request.getCreatedBy(), request.getRequesterId())) {
                UserService userService = new UserService();
                UserService.Route route = userService.getDefaultRoute(servletRequest, request.getRequesterId());
                if (route != null) {
                    if (route.getReviewer() != null) {
                        request.setReviewerId(route.getReviewer().getPersonId());
                    }
                    if (route.getBankcardApprovingOfficial() != null) {
                        request.setBankcardApprovingOfficialId(route.getBankcardApprovingOfficial().getPersonId());
                    }
                    if (route.getBankcardHolder() != null) {
                        request.setBankcardHolderId(route.getBankcardHolder().getPersonId());
                    }
                }
            } else {
                request.setReviewerId(postRequest.getReviewerId());
                request.setBankcardApprovingOfficialId(postRequest.getBankcardApprovingOfficialId());
                request.setBankcardHolderId(postRequest.getBankcardHolderId());
            }
        }

        request.setDeliveryAddress(postRequest.getDelivAddr());
        CommonUtil.setDateFromString(postRequest.getNeededByDate(), request::setNeededByDate, "neededByDate");

        if (requisitionNumber != null) {
            request.setRequisitionNumber(requisitionNumber);
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.insertRequest(request);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PostRequestResponse postRequestResponse = new PostRequestResponse();
        if (statusCode == StatusCode.OK) {
            RequestRoute req = getRequestbyId((Integer) results.get(RequestDao.ID_KEY));
            if (((StatusCode) results.get(RequestDao.STATUS_CODE_KEY)) == StatusCode.OK) {
                postRequestResponse.setData(createRequestDeepCopyFrom(req));
            }
        }
        return Response.ok().entity(serializeResponseWithStatus(postRequestResponse, statusCode)).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN) // This is because IE gets an error if json is returned.
    //@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/attachment")
    public Response postAttachment(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, UploadedFile base64File) { //FormDataMultiPart formParams){
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        FileAttachment fileAttachment = new FileAttachment();
        fileAttachment.setRequestId(requestId);
        fileAttachment.setCreatedBy(authenticatedUser.getPeopleId());

        //BANK-496
        //take json instead of MULTIPART_FORM_DATA so csrfguard filter can get token from header (frontend code is changed from form submit to ajax)
        byte[] contents = Base64.getDecoder().decode(base64File.getBase64Content());
        fileAttachment.setContent(contents);
        fileAttachment.setSize(contents.length);
        fileAttachment.setName(base64File.name);
        fileAttachment.setTypeCode(base64File.type);
        fileAttachment.setCategoryId(base64File.fileCategoryId);
        fileAttachment.setCategoryName(base64File.fileDescription);

        FileAttachmentDao dao = new FileAttachmentDao();
        Map<String, Object> results = dao.insertAttachment(fileAttachment);
        StatusCode statusCode = (StatusCode) results.get(FileAttachmentDao.STATUS_CODE_KEY);
        PostAttachmentResponse postAttachmentResponse = new PostAttachmentResponse();
        if (statusCode == StatusCode.OK) {
            postAttachmentResponse.setSuccess(true);
            postAttachmentResponse.setFileId((Integer) results.get(FileAttachmentDao.FILE_ID_KEY));
        } else {
            postAttachmentResponse.setSuccess(false);
            postAttachmentResponse.setCode(statusCode.getCode());
            postAttachmentResponse.setDescription(statusCode.getDescription());
        }
        return Response.ok().entity(postAttachmentResponse.toString()).build();
    }

    //file class consumed by file upload api, contains file content as base64 encoded string 
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UploadedFile {

        @JsonProperty("data")
        private String base64Content;
        public String name;
        public String type;
        public String fileDescription;
        public Integer fileCategoryId;

        public String getBase64Content() {
            return base64Content;
        }

        public void setBase64Content(String base64Content) {
            this.base64Content = base64Content;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/insertItemStatusTypes")
    public Response postItemStatusTypesForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PostItemStatusTypesForRequestRequest postItemStatusTypesForRequestRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ItemDao dao = new ItemDao();
        Map<String, Object> results = dao.insertItemStatusTypes(postItemStatusTypesForRequestRequest.getItemIds(), requestId, authenticatedUser.getPeopleId(), postItemStatusTypesForRequestRequest.getStatusTypeId());
        StatusCode statusCode = (StatusCode) results.get(ItemDao.STATUS_CODE_KEY);
        PostItemStatusTypesForRequestResponse postItemStatusTypesForRequestResponse = new PostItemStatusTypesForRequestResponse();

        return Response.ok().entity(serializeResponseWithStatus(postItemStatusTypesForRequestResponse, statusCode)).build();
    }

    /**
     * helper method to get a single request by requestId from DB
     *
     * @param requestId
     * @return
     */
    private RequestRoute getRequestbyId(Integer requestId) {
        RequestRoute req;
        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.selectRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);

        if (statusCode == StatusCode.OK) {
            req = (RequestRoute) results.get(RequestDao.REQUEST_ROUTE_KEY);
        } else {
            req = null;
        }
        return req;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}")
    public Response putRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PutRequest putRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        //get this request from database
        RequestRoute curReq = getRequestbyId(requestId);
        int typeId = 0;
        if (curReq != null) {
            typeId = curReq.getRoute().getTypeId();
        }

        //check if the request is a purchase, do the provilege check below, otherwise proceed to the update
        //the update procedure is changed to check purchase status and only update purchase cols when it's a purchase
        //the change is necesary becuase the old logic sometimes prevents any update from a pulled back purchase. 
        if (typeId == 4 || typeId == 6) {
            // Only a bankcard holder can update these values.
            if ((putRequest.getOrderNumber() != null && !putRequest.getOrderNumber().isEmpty())
                    || (putRequest.getGsaSessionNumber() != null && !putRequest.getGsaSessionNumber().isEmpty())
                    || (putRequest.getPurchaseOrderNumber() != null && !putRequest.getPurchaseOrderNumber().isEmpty())
                    || putRequest.getEstimatedTimeOfArrival() != null) {

                String[] roles = {"Bankcard Holder"};
                if (!isUserInRole(roles, authenticatedUser.getPeopleId())) {
                    LOG.info("User doesn't have role");
                    return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
                }
            }
        }

        String requisitionNumber = null;
        //in an update req, the org data is there, we can't use requester's org for sure because it could be a detailed req, so 
        //use group_id to get ou and div code for req# instead of the requesterId
        if (putRequest.getGenerateRequisitionNumber() != null && putRequest.getGenerateRequisitionNumber()) {
            //detailee mode
            if (session.getAttribute(DETAILEE_USERNAME) != null) {
                requisitionNumber = generateRequisitionNumber(servletRequest, putRequest.getFy().toString(),
                        putRequest.getBankcardHolderId(), putRequest.getRequesterId(), (String) session.getAttribute(UserService.DETAILEE_DIV_CODE), (String) session.getAttribute(UserService.DETAILEE_GROUP_CODE));
            } else {
                requisitionNumber = generateRequisitionNumber(servletRequest, putRequest.getFy().toString(),
                        putRequest.getBankcardHolderId(), putRequest.getRequesterId(), null, null);
            }

            if (requisitionNumber == null || requisitionNumber.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.FailedToGenerateRequisitionNumber)).build();
            }
        } else {
            LOG.info("Not generating requisition number with request insert.");
        }
        Request request = new Request();
        request.setId(requestId);
        //NOTE: didn't implement update FY here
        request.setUpdatedBy(authenticatedUser.getPeopleId());
        request.setCreatedFor(putRequest.getRequestedForId());
        request.setRequesterId(putRequest.getRequesterId());
        request.setNotes(putRequest.getComments());
        request.setDeliveryAddress(putRequest.getDelivAddr());
        //added 6/12/20 reltated to covid 19
        request.setDelivToHome(putRequest.getDelivToHome());
        request.setIsItPurchase(putRequest.getIsItPurchase());
        request.setPurchaseTypeId(putRequest.getPurchaseTypeId());
        request.setMissionCriticalCategoryId(putRequest.getMissionCriticalCategoryId());
        request.setMissionCriticalJustification(putRequest.getMissionCriticalJustification());
        CommonUtil.setDateFromString(putRequest.getNeededByDate(), request::setNeededByDate, "neededByDate");
        request.setReviewerId(putRequest.getReviewerId());
        request.setDivisionChiefId(putRequest.getDivisionChiefId());
        request.setBankcardApprovingOfficialId(putRequest.getBankcardApprovingOfficialId());
        request.setBankcardHolderId(putRequest.getBankcardHolderId());
        request.setFundsCertifyingOfficialId(putRequest.getFundsCertifyingOfficialId());
        CommonUtil.setDateFromString(putRequest.getEstimatedTimeOfArrival(), request::setEstimatedTimeOfArrival, "arrivalDate");

        //request.setEstimatedTimeOfArrival(putRequest.getEstimatedTimeOfArrival());
        request.setOrderNumber(putRequest.getOrderNumber());
        request.setGsaSessionNumber(putRequest.getGsaSessionNumber());
        request.setPurchaseOrderNumber(putRequest.getPurchaseOrderNumber());
        request.setApprovalAmount(putRequest.getApprovalAmount());
        request.setDescription(putRequest.getDescription());
        if (requisitionNumber != null) {
            request.setRequisitionNumber(requisitionNumber);
        }

        //DivisionCode is not used in the SP, lost track on when or why we had it in the first place
        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.updateRequest(request, authenticatedUser.getDivisionCode());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PutRequestResponse putRequestResponse = new PutRequestResponse();
        if (statusCode == StatusCode.OK) {
            if ((Boolean) results.get(RequestDao.ROUTE_IN_CORRECT_STATE_KEY)) {
                putRequestResponse.setData(createRequestDeepCopyFrom((RequestRoute) results.get(RequestDao.REQUEST_ROUTE_KEY)));
            } else {
                statusCode = StatusCode.RequestNotInCorrectRouteStateForOperation;
            }
        }

        return Response.ok().entity(serializeResponseWithStatus(putRequestResponse, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/description")
    public Response putDescription(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PutDescriptionRequest putDescriptionRequest) {

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.updateRequestDescription(requestId, putDescriptionRequest.getDescription());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PutDescriptionResponse putDescriptionResponse = new PutDescriptionResponse();

        return Response.ok().entity(serializeResponseWithStatus(putDescriptionResponse, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/requisitionNum")
    public Response putRequisitionNum(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PutRequest putRequest) {

        //who can update requisitionNumber? requester
        String requisitionNumber = generateRequisitionNumber(servletRequest, putRequest.getFy().toString(),
                putRequest.getBankcardHolderId(), putRequest.getRequesterId(), null, null);
        if (requisitionNumber == null || requisitionNumber.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.FailedToGenerateRequisitionNumber)).build();
        }
        RequestDao dao = new RequestDao();
        Request req = new Request();
        req.setFy(putRequest.getFy());
        req.setRequisitionNumber(requisitionNumber);
        Map<String, Object> results = dao.updateFy(requestId, req);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PutRequisitionNumberResponse putRequisitionNumberResponse = new PutRequisitionNumberResponse();
        putRequisitionNumberResponse.requisitionNumber = requisitionNumber;
        return Response.ok().entity(serializeResponseWithStatus(putRequisitionNumberResponse, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/bchComments")
    public Response putRequestBchComments(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PutRequestBchCommentsRequest putRequestBchCommentsRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        String[] roles = {"Bankcard Holder"};
        if (!isUserInRole(roles, authenticatedUser.getPeopleId())) {
            LOG.info("User doesn't have role");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.InsufficienPrivileges)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.updateBchComments(requestId, putRequestBchCommentsRequest.getBchComments());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PutRequestBchCommentsResponse putRequestBchCommentsResponse = new PutRequestBchCommentsResponse();

        return Response.ok().entity(serializeResponseWithStatus(putRequestBchCommentsResponse, statusCode)).build();
    }

    /**
     * update Funds Certifying Official of a request
     *
     * @param servletRequest
     * @param requestId
     * @param fcoId
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/fco")
    public Response putRequestFcoId(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, @QueryParam("fcoId") Integer fcoId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.updateFco(requestId, fcoId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        JsonStatus putRequestFcoIdResponse = new JsonStatus();

        return Response.ok().entity(serializeResponseWithStatus(putRequestFcoIdResponse, statusCode)).build();
    }

    /**
     * update Division Chief (mission critical approver) of a request
     *
     * @param servletRequest
     * @param requestId
     * @param dcId
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/dc")
    public Response putRequestDcId(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, @QueryParam("dcId") Integer dcId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.updateDC(requestId, dcId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        JsonStatus putRequestDcIdResponse = new JsonStatus();

        return Response.ok().entity(serializeResponseWithStatus(putRequestDcIdResponse, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/pullBackRoute")
    public Response putPullBackRoute(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.callPullBackRoute(requestId, authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);

        PutPullBackRouteResponse putPullBackRouteResponse = new PutPullBackRouteResponse();
        if (results.containsKey(RequestDao.ERROR_CODE_KEY)) {
            Error error = new Error();
            putPullBackRouteResponse.setError(error);
            putPullBackRouteResponse.getError().setCode((Integer) results.get(RequestDao.ERROR_CODE_KEY));
            putPullBackRouteResponse.getError().setDescription((String) results.get(RequestDao.ERROR_MESSAGE_KEY));
            putPullBackRouteResponse.setSuccess(false);
        }

        // Do a custom error message instead of the default.
        if (putPullBackRouteResponse.getError() != null) {
            return Response.ok().entity(putPullBackRouteResponse).build();
        }

        return Response.ok().entity(serializeResponseWithStatus(putPullBackRouteResponse, statusCode)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}/copy")
    public Response postRequestCopy(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.insertRequestCopy(requestId, authenticatedUser.getPeopleId());

        // NOTE: Temporary solution. 
        //put it's function in copy now
        //Map<String, Object> results2 = dao.makeupCopyRouteAndStatus(authenticatedUser.getPeopleId());
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        PostRequestCopyResponse postRequestCopyResponse = new PostRequestCopyResponse();
        if (statusCode == StatusCode.OK) {
            postRequestCopyResponse.setRequestId((Integer) results.get(RequestDao.ID_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(postRequestCopyResponse, statusCode)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}")
    public Response deleteRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        } else if (requestId == null) {
            LOG.info("Request id is null");
            return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.IncompleteData)).build();
        }

        RequestDao dao = new RequestDao();
        Map<String, Object> results = dao.deleteRequest(authenticatedUser.getPeopleId(), requestId);
        StatusCode statusCode = (StatusCode) results.get(RequestDao.STATUS_CODE_KEY);
        DeleteRequestResponse deleteRequestResponse = new DeleteRequestResponse();
        if (statusCode == StatusCode.OK) {

            // Permission state. 0 ==> good. 1 ==> insufficient permission, 2 ==> incorrect state.
            int permissionState = (Integer) results.get(RequestDao.PERMISSION_STATE_KEY);
            switch (permissionState) {

                case 0:
                    deleteRequestResponse.setRequestId((Integer) results.get(RequestDao.ID_KEY));
                    deleteRequestResponse.setRowCount((Integer) results.get(RequestDao.ROW_COUNT_KEY));
                    break;

                case 1:
                    statusCode = StatusCode.InsufficienPrivileges;
                    break;

                case 2:
                    statusCode = StatusCode.InvalidRequestState;
                    break;
            }
        }

        return Response.ok().entity(serializeResponseWithStatus(deleteRequestResponse, statusCode)).build();
    }

    private RequestDeepCopy createRequestDeepCopyFrom(RequestRoute requestRoute) {
        RequestDeepCopy requestDeepCopy = new RequestDeepCopy();
        if (requestRoute != null) {
            requestDeepCopy.setRequestId(requestRoute.getId());
            requestDeepCopy.setFy(requestRoute.getFy());
            requestDeepCopy.setRequisitionNumber(requestRoute.getRequisitionNumber());
            requestDeepCopy.setComments(requestRoute.getNotes());
            requestDeepCopy.setRequesterId(requestRoute.getRequesterId());
            requestDeepCopy.setCreatorId(requestRoute.getCreatedBy());
            requestDeepCopy.setReqDate(requestRoute.getCreatedDate());
            requestDeepCopy.setRequestedForId(requestRoute.getCreatedFor());
            requestDeepCopy.setRequestedForName(requestRoute.getCreatedForName());
            requestDeepCopy.setIsShoppingCart("Y".equals(requestRoute.getShoppingCart()));
            requestDeepCopy.setReferenceId(requestRoute.getReferenceId());
            requestDeepCopy.setUpdatedBy(requestRoute.getUpdatedBy());
            requestDeepCopy.setUpdatedDate(requestRoute.getUpdatedDate());
            requestDeepCopy.setDelivAddr(requestRoute.getDeliveryAddress());
            requestDeepCopy.setDelivToHome(requestRoute.getDelivToHome());
            requestDeepCopy.setIsItPurchase(requestRoute.getIsItPurchase());
            requestDeepCopy.setPurchaseTypeId(requestRoute.getPurchaseTypeId());
            requestDeepCopy.setItsoApproved(requestRoute.getItsoApproved());
            requestDeepCopy.setMissionCriticalCategoryId(requestRoute.getMissionCriticalCategoryId());
            requestDeepCopy.setMissionCriticalJustification(requestRoute.getMissionCriticalJustification());
            Route route = requestRoute.getRoute();
            if (route != null) {
                requestDeepCopy.setStatusCode(route.getStatusId());
                requestDeepCopy.setStatusText(route.getStatusName());
                requestDeepCopy.setApproverNote(route.getNotes());
                requestDeepCopy.setRouteId(route.getId());
                requestDeepCopy.setRouteFrom(route.getRouteBy());
                requestDeepCopy.setRouteByDelegate(route.getRouteByDelegate());
                requestDeepCopy.setRouteTo(route.getRouteTo());
                requestDeepCopy.setRouteFromName(route.getRouteByName());
                requestDeepCopy.setRouteByDelegateName(route.getRouteByDelegateName());
                requestDeepCopy.setRouteToName(route.getRouteToName());
                requestDeepCopy.setRouteTypeName(route.getTypeName());
                requestDeepCopy.setRouteDate(route.getRouteDate());
                requestDeepCopy.setRouteTypeId(route.getTypeId());
                requestDeepCopy.setIsDynamic(route.getIsDynamic());
                requestDeepCopy.setIsDynamicReroute(route.getIsDynamicReroute());
                requestDeepCopy.setRerouteStack(route.getRerouteStack());
                requestDeepCopy.setRouteStep(route.getRouteStep());
                requestDeepCopy.setDynamicType(route.getDynamicType());

            }
            requestDeepCopy.setRequesterName(requestRoute.getRequesterName());
            requestDeepCopy.setCreatorName(requestRoute.getCreatedByName());
            requestDeepCopy.setOuId(requestRoute.getOuId());
            requestDeepCopy.setDivisionId(requestRoute.getDivisionId());
            requestDeepCopy.setGroupId(requestRoute.getGroupId());
            requestDeepCopy.setTotalCost(requestRoute.getTotalCost());
            requestDeepCopy.setActualTotalCost(requestRoute.getActualTotalCost());
            requestDeepCopy.setVendors(requestRoute.getVendors());
            requestDeepCopy.setItems(requestRoute.getItems());
            requestDeepCopy.setNeededByDate(requestRoute.getNeededByDate());
            requestDeepCopy.setReviewerId(requestRoute.getReviewerId());
            requestDeepCopy.setDivisionChiefId(requestRoute.getDivisionChiefId());
            requestDeepCopy.setBankcardApprovingOfficialId(requestRoute.getBankcardApprovingOfficialId());
            requestDeepCopy.setFundsCertifyingOfficialId(requestRoute.getFundsCertifyingOfficialId());
            requestDeepCopy.setBankcardHolderId(requestRoute.getBankcardHolderId());
            requestDeepCopy.setReviewerName(requestRoute.getReviewerName());
            requestDeepCopy.setDcName(requestRoute.getDcName());
            requestDeepCopy.setBaoName(requestRoute.getBaoName());
            requestDeepCopy.setFcoName(requestRoute.getFcoName());
            requestDeepCopy.setBhName(requestRoute.getBhName());
            requestDeepCopy.setEstimatedTimeOfArrival(requestRoute.getEstimatedTimeOfArrival());
            requestDeepCopy.setOrderNumber(requestRoute.getOrderNumber());
            requestDeepCopy.setGsaSessionNumber(requestRoute.getGsaSessionNumber());
            requestDeepCopy.setPurchaseOrderNumber(requestRoute.getPurchaseOrderNumber());
            requestDeepCopy.setSubmittedDate(requestRoute.getSubmittedDate());
            requestDeepCopy.setBchComments(requestRoute.getBchComments());
            requestDeepCopy.setDescription(requestRoute.getDescription());
            requestDeepCopy.setApprovalAmount(requestRoute.getApprovalAmount());
        }
        return requestDeepCopy;
    }

    private boolean generateAuditReportPdf(String outputFilename, File webInfDirectory, AuditReport auditReport) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        // Update total cost and shipping cost.
        double shippingCost = 0;
        double totalCost = 0;
        for (Item item : auditReport.getItems()) {
            //MB-442
            if (item.getVendorId() != null && item.getItemName().equalsIgnoreCase("Shipping & Handling") && item.getPrice() != null) {
                //if (item.getVendorId() != null && item.getVendorId() == -999 && item.getPrice() != null) {
                shippingCost = item.getPrice();
                totalCost += shippingCost;
            } else {
                if (item.getPrice() != null && item.getQuantity() != null) {
                    totalCost += item.getPrice() * item.getQuantity();
                }
            }
        }

        File templateDirectory = new File(webInfDirectory, "template");
        File tempDirectory = new File(webInfDirectory, "temp");
        if (!tempDirectory.exists()) {
            tempDirectory.mkdirs();
        }

        String templateSelection = "AuditReport.pdf";
        RequestJustification justificationCheck = auditReport.getRequestJustification();
        int justificationBoundary = 65;
        int gsaScheduleCount = 0, priceIsReasonableCount = 0, thirdPartyCount = 0, smallBusinessCount = 0;
        int commercialVendorCount = 0;//no longer used
        if (justificationCheck.getGsaScheduleJust() != null) {
            gsaScheduleCount = justificationCheck.getGsaScheduleJust().length();
        }
        if (justificationCheck.getPriceIsReasonableJust() != null) {
            priceIsReasonableCount = justificationCheck.getPriceIsReasonableJust().length();
        }

        if (justificationCheck.getCommercialVendorJust() != null) {
            commercialVendorCount = justificationCheck.getCommercialVendorJust().length();
        }

        if (justificationCheck.getThirdPartyVendorJust() != null) {
            thirdPartyCount = justificationCheck.getThirdPartyVendorJust().length();
        }

        if (justificationCheck.getSmallBusinessJust() != null) {
            smallBusinessCount = justificationCheck.getSmallBusinessJust().length();
        }
        if (gsaScheduleCount > justificationBoundary
                || priceIsReasonableCount > justificationBoundary
                || thirdPartyCount > justificationBoundary
                || smallBusinessCount > justificationBoundary) {
            templateSelection = "AuditReportExpanded.pdf";
        }

        try ( PDDocument auditReportPdf = PDDocument.load(new File(templateDirectory, templateSelection));) {
            //main pdf page of the audit report
            PDAcroForm form = auditReportPdf.getDocumentCatalog().getAcroForm();

            setPdfFieldValue((PDTextField) form.getField("requisition_number"), auditReport.getRequisitionNumber());
            if (shippingCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("shipping_cost"), String.format("%.2f", shippingCost));
            }
            if (totalCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("total_cost"), String.format("%.2f", totalCost));
            }

            //MB-418
            Double approvalAmount = auditReport.getApprovalAmount();
            if (approvalAmount != null) {
                setPdfFieldValue((PDTextField) form.getField("approval_amount"), String.format("%.2f", approvalAmount));
            }

            // Reviewer.
            setPdfFieldValue((PDTextField) form.getField("reviewer_name"), auditReport.getReviewerName());
            if (auditReport.getReviewerDate() != null) {
                //setPdfFieldValue((PDTextField) form.getField("reviewer_approved_by_name"), auditReport.getReviewerName());
                setPdfFieldValue((PDTextField) form.getField("reviewer_approved_by_date"), dateFormat.format(auditReport.getReviewerDate()));
            }

            // Requester.
            setPdfFieldValue((PDTextField) form.getField("requestor_name"), auditReport.getRequesterName());

            //request date
            if (auditReport.getRequestDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("request_date"), dateFormat.format(auditReport.getRequestDate()));
            }

            // Bankcard holder.
            setPdfFieldValue((PDTextField) form.getField("bch_name"), auditReport.getBhName());
            if (auditReport.getOrderDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("bch_date_ordered"), dateFormat.format(auditReport.getOrderDate()));
            }

            // Bankcard approving official.
            setPdfFieldValue((PDTextField) form.getField("ao_name"), auditReport.getBaoName());
            if (auditReport.getBaoDate() != null) {
                //setPdfFieldValue((PDTextField) form.getField("ao_approved_by_name"), auditReport.getBaoName());
                setPdfFieldValue((PDTextField) form.getField("ao_approved_by_date"), dateFormat.format(auditReport.getBaoDate()));
            }

            // Fund Certifing official. 
            //issue 585, add new fields in DB and pdf template for orgs that use explicit fco approval
            setPdfFieldValue((PDTextField) form.getField("fco_name"), auditReport.getFcoName());
            if (auditReport.getFcoDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("fco_approved_by_date"), dateFormat.format(auditReport.getFcoDate()));
            }

            // Update vendor related stuff.
            RequestVendor requestVendor = auditReport.getRequestVendor();
            if (requestVendor != null && requestVendor.getVendor() != null) {
                Vendor vendor = requestVendor.getVendor();
                if (vendor != null) {
                    setPdfFieldValue((PDTextField) form.getField("vendor_name"), vendor.getName());
                    setPdfFieldValue((PDTextField) form.getField("vendor_website"), vendor.getWebUrl());
                    setPdfFieldValue((PDTextField) form.getField("vendor_phone"), vendor.getPhoneNumber());
                    setPdfFieldValue((PDTextField) form.getField("vendor_contact"), vendor.getContactName());
                }

            }

            //Justification
            RequestJustification requestJust = auditReport.getRequestJustification();
            if (requestJust != null) {
                //this field is in the vendor block in the pdf but it uses data in justification to fill out
                if (requestJust.getGsaSchedule() != null) {
                    setPdfFieldValue((PDTextField) form.getField("gsa_vendor_yn"), requestJust.getGsaSchedule() ? "y" : "n");
                }

                if (requestJust.getGsaScheduleJust() != null) {
                    setPdfFieldValue((PDTextField) form.getField("gsa_justification"), requestJust.getGsaScheduleJust());
                } else {
                    setPdfFieldValue((PDTextField) form.getField("gsa_justification"), "Justification not added yet");
                }

                if (requestJust.getPriceIsReasonableJust() != null) {
                    setPdfFieldValue((PDTextField) form.getField("price_is_reasonable_justification"), requestJust.getPriceIsReasonableJust());
                } else {
                    setPdfFieldValue((PDTextField) form.getField("price_is_reasonable_justification"), "Justification not added yet");
                }

                //BANK-510 since we don't ask user to provide justification for this section anymore, we will just display whether it is
                //a open market purchase and whether it's a IT Purchase
                if (requestJust.getCommercialVendorJust() != null) {
                    //BANK-546 found there are always text set for this justification in the DAO so it will not go to the else block
                    //since we agreed on what should display in the Q2 after we removed the "open market question, let's always use this logic
                    //    setPdfFieldValue((PDTextField) form.getField("commercial_justification"), requestJust.getCommercialVendorJust());
                    //} else {
                    //setPdfFieldValue((PDTextField) form.getField("commercial_justification"), "Justification not added yet");
                    String omj = "";

                    //if (requestJust.getCommercialVendor()) {
                    //since we removed the "is open market" question in v3.0.9, we no longer save anything for CommercialVendor
                    //the logic now is if user select "no" for "purchased from mandatory sources of supply", it means it's a open market purchase
                    if (requestJust.getGsaSchedule() != null) {
                        if (requestJust.getGsaSchedule()) {
                            omj = "The product or service is not an open market purchase. ";
                        } else {
                            omj = "The product or service is an open market purchase. ";
                        }
                    }

                    if (requestJust.getIsItPurchase() != null && requestJust.getIsItPurchase().equalsIgnoreCase("Y")) {
                        omj += " (IT Purchase)";
                    }

                    setPdfFieldValue((PDTextField) form.getField("commercial_justification"), omj);
                }

                if (requestJust.getThirdPartyVendorJust() != null) {
                    setPdfFieldValue((PDTextField) form.getField("third_party_justification"), requestJust.getThirdPartyVendorJust());
                } else {
                    setPdfFieldValue((PDTextField) form.getField("third_party_justification"), "Justification not added yet");
                }

                if (requestJust.getSmallBusinessJust() != null) {
                    setPdfFieldValue((PDTextField) form.getField("small_business_justification"), requestJust.getSmallBusinessJust());
                } else {
                    setPdfFieldValue((PDTextField) form.getField("small_business_justification"), "TO THE MAXIMUM EXTENT POSSIBLE, SMALL VENDORS WERE CONSIDERED");
                }
            }

            // Now do item stuff.
            int nonShippingCostIndex = 0;
            int uniqueFormFieldRenameIndex = 0;
            for (int i = 0; i < auditReport.getItems().size(); i++) {
                Item item = auditReport.getItems().get(i);
                if (item.getVendorId() != null && item.getItemName().equalsIgnoreCase("Shipping & Handling")) {
                    //if (item.getVendorId() != null && item.getVendorId() == -999) {
                    continue; // Shipping cost, skip an item row in the pdf.
                }
                nonShippingCostIndex++;
                int itemRowIndex = 1;
                if (nonShippingCostIndex <= NUM_ITEMS_IN_FIRST_PAGE_AUDIT_REPORT) {
                    itemRowIndex = nonShippingCostIndex;
                } else {
                    //here we are checking if we need to use an additional page for items
                    //if yes, load a new AuditReportItems template pdf file so the rest of the items are set in it

                    //TODO: when a new page is needed, we load the template and get the form from it
                    //so the form is not the form used in the previous page and PdfUtils.setAllFieldsReadOnly(form) only secures this page, not the previous one 
                    itemRowIndex = ((nonShippingCostIndex - NUM_ITEMS_IN_FIRST_PAGE_AUDIT_REPORT) % NUM_ITEMS_IN_ADDITIONAL_PAGE_AUDIT_REPORT); // 16 rows per page.
                    if (itemRowIndex == 1) {
                        PDDocument itemsPdf = PDDocument.load(new File(templateDirectory, "AuditReportItems.pdf"));
                        PDAcroForm itemForm = itemsPdf.getDocumentCatalog().getAcroForm();
                        setPdfFieldValue((PDTextField) itemForm.getField("requisition_number"), auditReport.getRequisitionNumber());
                        uniqueFormFieldRenameIndex++;
                        renameFormFields(itemForm, uniqueFormFieldRenameIndex);
                        //now names of the itemform were updated with correct index
                        //merge fields from the items form to the main form
                        form.getFields().addAll(itemForm.getFields());

                        //form = itemsPdf.getDocumentCatalog().getAcroForm();
                        //setPdfFieldValue((PDTextField) form.getField("requisition_number"), auditReport.getRequisitionNumber());
                        //uniqueFormFieldRenameIndex++;
                        //renameFormFields(form, uniqueFormFieldRenameIndex);
                        //add the items page to the main pdf file
                        auditReportPdf.addPage(itemsPdf.getPage(0));
                    }
                }
                setItemFields(form, item, itemRowIndex, uniqueFormFieldRenameIndex);
            }

            //issue 619, now do mission critical justification 
            //also do route approval history.
            //these two pages both use routes
            uniqueFormFieldRenameIndex = 0;

            //int ApprovedCount = 0;
            int itsoApprovedCount = 0;
            int reviewApprovedcount = 0;
            int baoApprovedcount = 0;
            int fcoApprovedcount = 0;
            ArrayList<Approval> auditRouteArray = new ArrayList<>();

            // Create a map to store routes by routeStep
            Map<Integer, Route> routeMap = new HashMap<>();

            List<Route> routes = auditReport.getRoutes();

            int updateRouteBy = 0;
            String updateRouteByName = "";
            Boolean enterDynamicRoute = false;
            String DynamicRouteType = "";
            int routeStep = 0;
            // Loop through the list of routes and populate the map
            for (Route route : routes) {
                //AA and DR routes should be excluded. 
                if (!enterDynamicRoute) {
                    //if we entering dynamic routing with AA, we need to know the first routeby of AA, 
                    if ("AA".equals(route.getDynamicType())) {
                        enterDynamicRoute = true;
                        DynamicRouteType = "AA";
                        updateRouteBy = route.getRouteBy();
                        updateRouteByName = route.getRouteByName();
                        //do not save this route in the map
                        continue;
                    } else if ("DR".equals(route.getDynamicType())) {
                        //if we entering dynamic routing with DR, we store the first routeby of the DR, delete all AA/DRs after that route,
                        //and also the first non dynamic route that has the routeTo = this routeby
                        enterDynamicRoute = true;
                        DynamicRouteType = "DR";
                        updateRouteBy = route.getRouteBy();
                        updateRouteByName = route.getRouteByName();
                        //do not save this route in the map
                        continue;
                    } else {
                        //save this route in the map
                        routeStep++;
                        route.setRouteStep(routeStep);
                        routeMap.put(routeStep, route);
                    }
                } else {
                    if ("ITSO".equals(route.getDynamicType()) || route.getIsDynamic() == 0) {
                        enterDynamicRoute = false;
                        //the first non dynamic route is the route that ends the AA route(s), and we need to keep it 
                        //and update that routes' routeby to this routeby so the find approver logic would work
                        if ("AA".equals(DynamicRouteType)) {
                            route.setRouteBy(updateRouteBy);
                            route.setRouteByName(updateRouteByName);
                            routeStep++;
                            route.setRouteStep(routeStep);
                            routeMap.put(routeStep, route);
                        } else if ("DR".equals(DynamicRouteType)) {
                            //this is a record that ends the DR route(s) and we don't need it
                            continue;
                        } else {
                            //save everything else
                            routeStep++;
                            route.setRouteStep(routeStep);
                            routeMap.put(routeStep, route);
                        }

                    }

                }
            }

            //find the latest approval in the routes for DC and DR 
            //the logic is go through all routes, find one, add in 
            PDDocument mcPdf = PDDocument.load(new File(templateDirectory, "MissionCriticalJustification.pdf"));
            PDAcroForm mcForm = mcPdf.getDocumentCatalog().getAcroForm();
            //merge fields from the route form to the main form
            form.getFields().addAll(mcForm.getFields());
            //set value for the requisition_number
            setPdfFieldValue((PDTextField) mcForm.getField("requisition_number"), auditReport.getRequisitionNumber());
            //set value for the mssion critical category and justification
            setPdfFieldValue((PDTextField) form.getField("mission_category"), auditReport.getMissionCriticalCategoryName());
            setPdfFieldValue((PDTextField) form.getField("mission_critical_justification"), auditReport.getMissionCriticalJustification());

            //set DC or DR approver name and date if any
            //step 1: find latest route to DR from the map 15, 18
            Route drOrDcRoute = null;
            Route route = null;
            for (int step = routeMap.size(); step >= 0; step--) {
                route = routeMap.get(step);
                if (route != null && route.getTypeId() == 15 && route.getStatusId() == 18) {
                    drOrDcRoute = route;
                    break; // Stop at the first match from the end
                }
            }

            if (drOrDcRoute == null) {
                //no dr route found, check dc route
                for (int step = routeMap.size(); step >= 0; step--) {
                    route = routeMap.get(step);
                    if (route != null && route.getTypeId() == 14 && route.getStatusId() == 17) {
                        drOrDcRoute = route;
                        break; // Stop at the first match from the end
                    }
                }
            }

            // Step 2: if we found a DC or DR route, when the DC or DR approved it, the next route is to the FCO with 16, 16
            //but since we switched the order of route from DC->BAO->FCO to DC->FCO->BAO, we cannot check the type and status anymore
            if (drOrDcRoute != null) {
                // Step 3: Get the previous route
                Integer nextRouteStep = drOrDcRoute.getRouteStep() + 1;
                Route nextRoute = routeMap.get(nextRouteStep);
                // Step 4: Check if nextRoute is found and matches routeTo
                //if yes, this route is either a DC or DR approval
                if (nextRoute != null && nextRoute.getRouteBy().equals(drOrDcRoute.getRouteTo())) {
                    // Step 5: set the routeByName and routeDate to the form fields in the MissionCriticalJustification template
                    setPdfFieldValue((PDTextField) form.getField("dc_name"), nextRoute.getRouteByName());
                    if (nextRoute.getRouteDate() != null) {
                        setPdfFieldValue((PDTextField) form.getField("dc_approved_by_date"), dateFormat.format(nextRoute.getRouteDate()));
                    }
                }
            } else {
                //didn't found any DC or DR route, the request has not reached to them yet
                //since the audit report is not available until BAO approved it, the code should not even reach here
            }

            auditReportPdf.addPage(mcPdf.getPage(0));

            //find reviewer, BAO and FCO approvals
            //loop through routeMap again to find and store approvals (routeMap doesn't have dynamic routes except ITSO)
            for (Map.Entry<Integer, Route> entry : routeMap.entrySet()) {
                route = entry.getValue();
                int type = route.getTypeId();
                int status = route.getStatusId();
                int step = route.getRouteStep();
                int routeBy = route.getRouteBy();
                int routeTo = route.getRouteTo();
                String dynamicType = route.getDynamicType();

                LOG.log(Level.INFO, "Type ID: {0}, Status ID: {1}, Route Step: {2}, Route By: {3}, Route To: {4}, Dynamic Type: {5}", new Object[]{type, status, step, routeBy, routeTo, dynamicType});
                //the order of approvals should be ITSO -> Reviewer -> FCO (if explicit route needed by the division) -> BAO -> BCH
                //there may be re-approves if the request is returned or rejected or route back
                if (type == 1 && status == 5) {
                    if ("ITSO".equals(dynamicType)) {
                        //search for ITSO approval
                        Approval r = findItsoApproval(routeMap, step + 1, routeTo, itsoApprovedCount);
                        if (r != null) {
                            itsoApprovedCount++;
                            auditRouteArray.add(r);
                        }
                    } else {
                        Approval r = null;
                        //request submitted before 3/6/25 should use findReviewerApprovalBeforeMC
                        //because route changes
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(2025, Calendar.MARCH, 6, 0, 0, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        Date comparisonDate = calendar.getTime();
                        // Compare the dates
                        if (auditReport.getRequestDate().before(comparisonDate)) {
                            //if requestDate is earlier than March 6, 2025
                            //search for reviewer approval (2,6) in the next route
                            r = findReviewerApprovalBeforeMC(routeMap, step + 1, routeTo, reviewApprovedcount);
                        } else {
                            //if requestDate is not earlier
                            //search for reviewer approval (14, 17) in the next route
                            r = findReviewerApproval(routeMap, step + 1, routeTo, reviewApprovedcount);
                        }

                        if (r != null) {
                            reviewApprovedcount++;
                            auditRouteArray.add(r);
                        }
                    }
                } else if (type == 16 && status == 16) { //FCO
                    //get FCO approval record
                    Approval r = findFcoApproval(routeMap, step + 1, routeTo, fcoApprovedcount);
                    if (r != null) {
                        fcoApprovedcount++;
                        auditRouteArray.add(r);
                    }
                } else if (type == 2 && status == 6) { //BAO
                    //check if next route's routeTo is the same as routeby in the current route
                    //cannot check next route's type and status since the order of routes changed from BAO-> FCO(v4.1) to FCO->BAO(v4.2)
                    //search for BAO approval
                    Approval r = findBaoApproval(routeMap, step + 1, routeTo, baoApprovedcount);
                    if (r != null) {
                        baoApprovedcount++;
                        auditRouteArray.add(r);
                    }
                } else {
                    //the rest should be BCH, DC or DR routes, which we took care already

                }

            }

            //issue 638, load the GPC checklsit template file
            PDDocument gpcChecklistPdf = PDDocument.load(new File(templateDirectory, "GPC_Checklist.pdf"));
            PDAcroForm gpcChecklistForm = gpcChecklistPdf.getDocumentCatalog().getAcroForm();
            if (requestJust.getIsItPurchase() != null && requestJust.getIsItPurchase().equalsIgnoreCase("Y")) {
                setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_it_purchase"), "Yes");
            } else {
                setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("not_it_purchase"), "Yes");
            }
            //section_508_compliant,is_energy_star
            //issue 664
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("section_508_compliant"), "Yes");

            //purchase card holder section checkboxes
            //checkbox of "The amount of this purchase is within my single..."
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_within_limit"), "Yes");

            //"This purchase is for official Government business only and screened..."
            //the app cannot determine if the purchase is a atypical exp but does have a note in the items panel with link to a guidance page in CBS portal
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_atypical_exp"), "Yes");

            //the checkbox of "I have checked the mandatory sources of supply...". the app;s justification
            //section asks for this question, if yes, it should be checked, if no, the app ask the user for
            //justifications of why not use it so regardless the answer, it should be checked here
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_mandatory_source"), "Yes");

            //"Request Sales Tax Exempt...", the app deosn't know. BCH should check this manually
            //"Pricing for this purchase is fair..."
            //since the app's justification section covers the price, it should be checked here
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_price_fair"), "Yes");

            //only check this one if it's a IT Purchase
            if (requestJust.getIsItPurchase() != null && requestJust.getIsItPurchase().equalsIgnoreCase("Y")) {
                setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_889_compliant"), "Yes");
            }

            //only check if the app records Third Party Vendor Justification
            if (requestJust.getThirdPartyVendor() != null && requestJust.getThirdPartyVendor()) {
                setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("is_3rd_party_vendor"), "Yes");
                //Third Party Vendor Justification in the GPC checklist is the same the app collect and print in the justification page
                //so reuse it here
                setPdfFieldValue((PDTextField) gpcChecklistForm.getField("gpc_third_party_justification"), requestJust.getThirdPartyVendorJust());
            }

            setPdfFieldValue((PDTextField) gpcChecklistForm.getField("gpc_bch_sign"), auditReport.getBhName());
            if (auditReport.getOrderDate() != null) {
                setPdfFieldValue((PDTextField) gpcChecklistForm.getField("gpc_bch_sign_date"), dateFormat.format(auditReport.getOrderDate()));
            }

            //date ordered is the route date with status = 8
            //date_received is the route date with status = 13
            Date dateOrdered = null;
            Date dateReceived = null;
            for (int step = routeMap.size(); step >= 0; step--) {
                route = routeMap.get(step);
                if (route != null && dateOrdered == null && route.getStatusId() == 8) {
                    dateOrdered = route.getRouteDate();
                    if (dateOrdered != null) {
                        setPdfFieldValue((PDTextField) gpcChecklistForm.getField("date_ordered_af_date"), dateFormat.format(dateOrdered));
                    }
                } else if (route != null && dateReceived == null && route.getStatusId() == 13) {
                    dateReceived = route.getRouteDate();
                    if (dateReceived != null) {
                        setPdfFieldValue((PDTextField) gpcChecklistForm.getField("date_received_af_date"), dateFormat.format(dateReceived));
                    }
                } else if (dateOrdered != null && dateReceived != null) {
                    break;
                }
            }

            //this checkbox is asking for a copy of GPC checklist, which is what is doc is, so always check it
            setPdfCheckBoxFieldValue((PDCheckBox) gpcChecklistForm.getField("copy_of_gpc_checklist"), "Yes");

            //add this page to the pdf file
            auditReportPdf.addPage(gpcChecklistPdf.getPage(0));

            //load the routes template file
            PDDocument routesPdf = PDDocument.load(new File(templateDirectory, "AuditReportRoutes.pdf"));
            PDAcroForm routeForm = routesPdf.getDocumentCatalog().getAcroForm();
            //merge fields from the route form to the main form
            form.getFields().addAll(routeForm.getFields());
            setPdfFieldValue((PDTextField) routeForm.getField("requisition_number"), auditReport.getRequisitionNumber());

            //set approver records in the routes form in this pdf template
            for (int i = 0; i < auditRouteArray.size(); i++) {
                uniqueFormFieldRenameIndex++;
                Approval approval = auditRouteArray.get(i);
                setApprovalFields(form, approval, uniqueFormFieldRenameIndex);
            }

            //setRouteFields(form, route, uniqueFormFieldRenameIndex, numOfReview, numOfApprove);
            //done with set values
            //add this page to the pdf file
            auditReportPdf.addPage(routesPdf.getPage(0));

            // Set all fields to read-only to secure the page
            //TODO: hold off to set all fields ready only becuase once read only, the long text that cause scrollbar in the field will only show
            //text that fit in the field and the rest of text won't show. so before find a way to resolve this, don't do it
            //an alternative is to make certain fields read only(short text that always fit such as project task, objcls, quantity,price, total, approver names
            //NOTE: projtsk and objcls may not fit after BAS arrives (they are longer than current format)
            //PdfUtils.setAllFieldsReadOnly(form);
            //secure the whole pdf file
            /**
             * very strange thing that citi manager only allow one file upload
             * so BCHs were add new pages to the audit report before uploading
             * it to citimanger. adding policy to make it readonly prevented
             * them to do that. so we need to roll back this change //make the
             * pdf file read only so after the user download it, the user cannot
             * edit it // Create an AccessPermission object AccessPermission ap
             * = new AccessPermission(); ap.setCanPrint(true); // Allow printing
             * ap.setCanModify(false); // Disallow modifications
             *
             * // Set the permissions to make the PDF read-only
             * StandardProtectionPolicy spp = new
             * StandardProtectionPolicy("empbcappgeneratedreadonlypdf", null,
             * ap); spp.setEncryptionKeyLength(128); // Use 128 or 256
             *
             * // Apply the protection policy auditReportPdf.protect(spp);
             *
             */
            auditReportPdf.save(new File(tempDirectory, outputFilename));
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return false;
        }

        return true;
    }

    private Approval setApproval(Route route, String RoleName, int approvedCount) {
        Approval appr = new Approval();
        appr.setRole(RoleName);
        appr.setName(route.getRouteByName());
        appr.setAction((approvedCount == 0) ? "Approved" : "Re-Approved");
        appr.setApprovedDate(route.getRouteDate());
        return appr;
    }

    private Approval findItsoApproval(Map map, Integer step, Integer routeTo, int approvedCount) {
        Route route = (Route) map.get(step);
        //ITSO routes were done in DR style so the second route is created without routeDate and once
        //the ITSO approves it, the routeDate is updated. If a backup ITSO approves it, the routeBy is also update
        //so we cannot match up the routeby in the second route with routeTo from previous route
        //we should check if the second route has 1,5 and iSDynamic=0
        //if (route != null && Objects.equals(route.getRouteBy(), routeTo)) {
        if (route != null && route.getTypeId() == 1 && route.getStatusId() == 5 && route.getIsDynamic() == 0) {
            return setApproval(route, "ITSO", approvedCount);
        } else {
            return null;
        }
    }

    private Approval findBaoApproval(Map map, Integer step, Integer routeTo, int approvedCount) {
        Route route = (Route) map.get(step);
        //check next step to see if its routeBy = routeTo
        if (route != null && Objects.equals(route.getRouteBy(), routeTo)) {
            Approval appr = setApproval(route, "Bankcard Approving Official", approvedCount);
            return appr;
        } else {
            return null;
        }
    }

    private Approval findReviewerApproval(Map map, Integer step, Integer routeTo, int approvedCount) {
        Route route = (Route) map.get(step);
        //used to check for type=2 and status=6, a route sent to BAO, but since issue#619, reviewer route to DC (type=14 and status=17)
        if (route != null && route.getTypeId() == 14 && route.getStatusId() == 17 && Objects.equals(route.getRouteBy(), routeTo)) {
            return setApproval(route, "Reviewer (Bona Fide Need Certifier)", approvedCount);
        } else {
            return null;
        }
    }

    //before we added mission critical routes, reviewer approvals can be found by checking for type=2 and status=6
    //so requests routed before 3/5/2025 should use this to get reviewer approvals for audit report
    private Approval findReviewerApprovalBeforeMC(Map map, Integer step, Integer routeTo, int approvedCount) {
        Route route = (Route) map.get(step);
        if (route != null && route.getTypeId() == 2 && route.getStatusId() == 6 && Objects.equals(route.getRouteBy(), routeTo)) {
            return setApproval(route, "Reviewer (Bona Fide Need Certifier)", approvedCount);
        } else {
            return null;
        }
    }

    private Approval findFcoApproval(Map map, Integer step, Integer routeTo, int approvedCount) {
        Route route = (Route) map.get(step);
        if (route != null && Objects.equals(route.getRouteBy(), routeTo)) {
            return setApproval(route, "Fund Certifying Official", approvedCount);
        } else {
            return null;
        }
    }

    private void setApprovalFields(PDAcroForm form, Approval appr, int uniqueFormFieldRenameIndex) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String roleNameKey = "Approver RoleRow" + uniqueFormFieldRenameIndex;
        String approverNameKey = "Approver NameRow" + uniqueFormFieldRenameIndex;
        String actionNameKey = "ActionRow" + uniqueFormFieldRenameIndex;
        String approvalDateKey = "DateRow" + uniqueFormFieldRenameIndex;

        setPdfFieldValue((PDTextField) form.getField(roleNameKey), appr.getRole());
        setPdfFieldValue((PDTextField) form.getField(approverNameKey), appr.getName());
        setPdfFieldValue((PDTextField) form.getField(actionNameKey), appr.getAction());
        if (appr.getApprovedDate() != null) {
            setPdfFieldValue((PDTextField) form.getField(approvalDateKey), dateFormat.format(appr.getApprovedDate()));
        }

    }

    private void setRouteFields(PDAcroForm form, Route route, int uniqueFormFieldRenameIndex, int numOfReview, int numOfApprove) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String roleNameKey = "Approver RoleRow" + uniqueFormFieldRenameIndex;
        String approverNameKey = "Approver NameRow" + uniqueFormFieldRenameIndex;
        String actionNameKey = "ActionRow" + uniqueFormFieldRenameIndex;
        String approvalDateKey = "DateRow" + uniqueFormFieldRenameIndex;

        //use type to figure out the role name
        int type = route.getTypeId();
        //for ITSO approval, since the route is dynamic, we cannot use type; instead, use the SP returned "ITSO" value in the dynamic_type column
        String dynamicType = route.getDynamicType();

        if ("ITSO".equals(dynamicType)) {
            setPdfFieldValue((PDTextField) form.getField(roleNameKey), "ITSO");
        } else {
            setPdfFieldValue((PDTextField) form.getField(roleNameKey), (type == 2 ? "Reviewer (Bona Fide Need Certifier)" : "Bankcard Approving Official and Fund Certifying Official"));
        }
        //name of the approver
        if (route.getRouteByDelegate() != null) {
            setPdfFieldValue((PDTextField) form.getField(approverNameKey), route.getRouteByDelegateName() + " (Delegated Approval)");
        } else {
            setPdfFieldValue((PDTextField) form.getField(approverNameKey), route.getRouteByName());
        }
        //action
        if ("ITSO".equals(dynamicType)) {
            setPdfFieldValue((PDTextField) form.getField(actionNameKey), "Approved");
        } else if ((type == 2 && numOfReview == 1) || (type == 3 && numOfApprove == 1)) {
            setPdfFieldValue((PDTextField) form.getField(actionNameKey), "Approved");
        } else {
            setPdfFieldValue((PDTextField) form.getField(actionNameKey), "Re-Approved");
        }

        //approval date
        if (route.getRouteDate() != null) {
            setPdfFieldValue((PDTextField) form.getField(approvalDateKey), dateFormat.format(route.getRouteDate()));
        }

    }

    private void setItemFields(PDAcroForm form, Item item, int rowIndex, int uniqueFormFieldRenameIndex) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        String itemNameKey = "item_name_row_" + rowIndex + uniqueFieldNameVariable;
        String projectTaskKey = "project_task_row_" + rowIndex + uniqueFieldNameVariable;
        String objectClassKey = "object_class_row_" + rowIndex + uniqueFieldNameVariable;
        String quantityKey = "quantity_row_" + rowIndex + uniqueFieldNameVariable;
        String priceKey = "actual_price_row_" + rowIndex + uniqueFieldNameVariable;
        String costKey = "cost_row_" + rowIndex + uniqueFieldNameVariable;
        String stmtDateKey = "statement_date_row_" + rowIndex + uniqueFieldNameVariable;
        String descriptionKey = "description_row_" + rowIndex + uniqueFieldNameVariable;

        setPdfFieldValue((PDTextField) form.getField(itemNameKey), item.getItemName());
        setPdfFieldValue((PDTextField) form.getField(projectTaskKey), item.getProjectTask());
        setPdfFieldValue((PDTextField) form.getField(objectClassKey), item.getObjectClass());
        int quantity = -1;
        if (item.getQuantity() != null) {
            quantity = item.getQuantity();
            setPdfFieldValue((PDTextField) form.getField(quantityKey), "" + quantity);
        }
        double price = -1.0;
        if (item.getPrice() != null) {
            price = item.getPrice();
            setPdfFieldValue((PDTextField) form.getField(priceKey), String.format("%.2f", price));
        }
        if (quantity * price > 0) {
            setPdfFieldValue((PDTextField) form.getField(costKey), String.format("%.2f", quantity * price));
        }

        if (item.getStatementDate() != null) {
            setPdfFieldValue((PDTextField) form.getField(stmtDateKey), "" + dateFormat.format(item.getStatementDate()));
        }
        if (rowIndex != 16) { // No 16th row.
            if (item.getVendorId() != null && item.getVendorId() == -99) {
                String description = "";
                if (item.getDescription() != null) {
                    description = ": " + item.getDescription();
                }
                setPdfFieldValue((PDTextField) form.getField(descriptionKey), "IT Buying Service" + description);
            } else {
                setPdfFieldValue((PDTextField) form.getField(descriptionKey), item.getDescription());
            }
        }
    }

    private void renameFormFields(PDAcroForm form, int uniqueFormFieldRenameIndex) throws IOException {
        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        for (int i = 1; i <= NUM_ITEMS_IN_ADDITIONAL_PAGE_AUDIT_REPORT; i++) {
            String itemNameKey = String.format("item_name_row_%d", i);
            String projectTaskKey = String.format("project_task_row_%d", i);
            String objectClassKey = String.format("object_class_row_%d", i);
            String quantityKey = String.format("quantity_row_%d", i);
            String priceKey = String.format("actual_price_row_%d", i);
            String costKey = String.format("cost_row_%d", i);
            String descriptionKey = String.format("description_row_%d", i);
            form.getField(itemNameKey).setPartialName(itemNameKey + uniqueFieldNameVariable);
            form.getField(projectTaskKey).setPartialName(projectTaskKey + uniqueFieldNameVariable);
            form.getField(objectClassKey).setPartialName(objectClassKey + uniqueFieldNameVariable);
            form.getField(quantityKey).setPartialName(quantityKey + uniqueFieldNameVariable);
            form.getField(priceKey).setPartialName(priceKey + uniqueFieldNameVariable);
            form.getField(costKey).setPartialName(costKey + uniqueFieldNameVariable);
            if (i != 16) { // No 16th row.
                form.getField(descriptionKey).setPartialName(descriptionKey + uniqueFieldNameVariable);
            }
        }
    }

    private boolean generateRequestSummaryReportPdf(String outputFilename, File webInfDirectory, RequestSummaryReport requestSummaryReport) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        // Update total cost and shipping cost.
        double actualShippingCost = 0;
        double actualSubtotalCost = 0;
        double actualTotalCost = 0;
        double estimatedShippingCost = 0;
        double estimatedSubtotalCost = 0;
        double estimatedTotalCost = 0;
        String shippingPtc = null;
        String shippingOcc = null;
        for (Item item : requestSummaryReport.getItems()) {
            //since we no longer need to have vendor ID for item, it could be null
            if (item.getVendorId() != null && item.getItemName().equalsIgnoreCase("Shipping & Handling")) { // && item.getVendorId() == -999) {
                if (item.getActualPrice() != null) {
                    actualShippingCost = item.getActualPrice();
                    actualTotalCost += actualShippingCost;
                }
                if (item.getPrice() != null) {
                    estimatedShippingCost = item.getPrice();
                    estimatedTotalCost += estimatedShippingCost;
                }
                shippingPtc = item.getProjectTask();
                shippingOcc = item.getObjectClass();
            } else {
                if (item.getActualPrice() != null && item.getActualQuantity() != null) {
                    actualTotalCost += item.getActualPrice() * item.getActualQuantity();
                    actualSubtotalCost += item.getActualPrice() * item.getActualQuantity();
                }
                if (item.getPrice() != null && item.getQuantity() != null) {
                    estimatedTotalCost += item.getPrice() * item.getQuantity();
                    estimatedSubtotalCost += item.getPrice() * item.getQuantity();
                }
            }
        }

        File templateDirectory = new File(webInfDirectory, "template");
        File tempDirectory = new File(webInfDirectory, "temp");
        if (!tempDirectory.exists()) {
            tempDirectory.mkdirs();
        }
        try ( PDDocument requestSummaryReportPdf = PDDocument.load(new File(templateDirectory, "RequestSummaryMain.pdf"));) {
            PDAcroForm form = requestSummaryReportPdf.getDocumentCatalog().getAcroForm();

            setPdfFieldValue((PDTextField) form.getField("requisition_number"), requestSummaryReport.getRequisitionNumber());

            setPdfFieldValue((PDTextField) form.getField("request_id"), requestSummaryReport.getRequestId().toString());

            setPdfFieldValue((PDTextField) form.getField("shipping_ptc"), shippingPtc);
            setPdfFieldValue((PDTextField) form.getField("shipping_occ"), shippingOcc);
            if (actualShippingCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("shipping_actual_cost"), String.format("%.2f", actualShippingCost));
            }
            if (estimatedShippingCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("shipping_estimate_cost"), String.format("%.2f", estimatedShippingCost));
            }
            if (actualSubtotalCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("subtotal_actual_cost"), String.format("%.2f", actualSubtotalCost));
            }
            if (estimatedSubtotalCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("subtotal_estimate_cost"), String.format("%.2f", estimatedSubtotalCost));
            }
            if (actualTotalCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("grandtotal_actual_cost"), String.format("%.2f", actualTotalCost));
            }
            if (estimatedTotalCost != 0) {
                setPdfFieldValue((PDTextField) form.getField("grandtotal_estimate_cost"), String.format("%.2f", estimatedTotalCost));
            }

            // Update requester information.
            setPdfFieldValue((PDTextField) form.getField("created_by"), requestSummaryReport.getCreatedByName());
            setPdfFieldValue((PDTextField) form.getField("official_requester"), requestSummaryReport.getRequesterName());
            if (requestSummaryReport.getRequestDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("request_date"), dateFormat.format(requestSummaryReport.getRequestDate()));

                // Calculate the fiscal year.
                /*LocalDate localDate = requestSummaryReport.getRequestDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                int year = localDate.getYear();
                if (localDate.getMonthValue() >= 10) {
                    year++;
                }
                setPdfFieldValue((PDTextField) form.getField("fiscal_year"), "" + year);
                 */
                //issue 589; since we added fy to request, instead of calculate it based on current date, use it directly 
                setPdfFieldValue((PDTextField) form.getField("fiscal_year"), "" + requestSummaryReport.getFy());

            }
            if (requestSummaryReport.getNeededByDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("needed_by_date"), dateFormat.format(requestSummaryReport.getNeededByDate()));
            }
            setPdfFieldValue((PDTextField) form.getField("delivery_address"), requestSummaryReport.getDeliverAddress());

            //MB-418
            Double approvalAmount = requestSummaryReport.getApprovalAmount();
            if (approvalAmount != null) {
                setPdfFieldValue((PDTextField) form.getField("approval_amount"), String.format("%.2f", approvalAmount));
            }

            // Reviewer.
            setPdfFieldValue((PDTextField) form.getField("reviewer_name"), requestSummaryReport.getReviewerName());
            if (requestSummaryReport.getReviewerDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("reviewer_date_of_approval"), dateFormat.format(requestSummaryReport.getReviewerDate()));
            }

            //DC
            setPdfFieldValue((PDTextField) form.getField("mission_critical_approver_name"), requestSummaryReport.getDcName());
            if (requestSummaryReport.getDcDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("mission_critical_date_of_approval"), dateFormat.format(requestSummaryReport.getDcDate()));
            }

            //FCO
            setPdfFieldValue((PDTextField) form.getField("fund_certifying_official_name"), requestSummaryReport.getFcoName());
            if (requestSummaryReport.getFcoDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("fund_certifying_official_date_of_approval"), dateFormat.format(requestSummaryReport.getFcoDate()));
            }

            // Bankcard holder.
            setPdfFieldValue((PDTextField) form.getField("bch_name"), requestSummaryReport.getBhName());
            if (requestSummaryReport.getOrderDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("bch_date_of_approval"), dateFormat.format(requestSummaryReport.getOrderDate()));
            }

            // Bankcard approving official.
            setPdfFieldValue((PDTextField) form.getField("reviewer_approved_by_name"), requestSummaryReport.getBaoName());
            if (requestSummaryReport.getBaoDate() != null) {
                setPdfFieldValue((PDTextField) form.getField("approving_official_date_of_approval"), dateFormat.format(requestSummaryReport.getBaoDate()));
            }

            // Update vendor related stuff.
            RequestVendor requestVendor = requestSummaryReport.getRequestVendor();
            if (requestVendor != null && requestVendor.getVendor() != null) {
                Vendor vendor = requestVendor.getVendor();
                if (vendor != null) {
                    setPdfFieldValue((PDTextField) form.getField("vendor_name"), vendor.getName());
                    setPdfFieldValue((PDTextField) form.getField("vendor_website"), vendor.getWebUrl());
                    setPdfFieldValue((PDTextField) form.getField("vendor_phone"), vendor.getPhoneNumber());
                    setPdfFieldValue((PDTextField) form.getField("vendor_contact"), vendor.getContactName());
                    setPdfFieldValue((PDTextField) form.getField("vendor_email"), vendor.getEmail());
                    setPdfFieldValue((PDTextField) form.getField("vendor_contact"), vendor.getContactName());
                }
            }

            // Notes.
            setPdfFieldValue((PDTextField) form.getField("additional_comments"), requestSummaryReport.getNotes());

            // Now do item stuff.
            //339 make sure transaction number date and statement date are output.
            int nonShippingItemIndex = 0;
            int uniqueFormFieldRenameIndex = 0;
            if (requestSummaryReport.getItems() != null) {
                for (int i = 0; i < requestSummaryReport.getItems().size(); i++) {
                    Item item = requestSummaryReport.getItems().get(i);
                    if (item.getVendorId() != null && item.getItemName().equalsIgnoreCase("Shipping & Handling")) { // && item.getVendorId() == -999) {item
                        continue;
                    }
                    nonShippingItemIndex++;
                    int itemRowIndex = 1;
                    if (nonShippingItemIndex <= NUM_ITEMS_IN_FIRST_PAGE_SUMMARY_REPORT) {
                        itemRowIndex = nonShippingItemIndex;
                    } else {
                        itemRowIndex = ((nonShippingItemIndex - (NUM_ITEMS_IN_FIRST_PAGE_SUMMARY_REPORT + 1)) % NUM_ITEMS_IN_ADDITIONAL_PAGE_SUMMARY_REPORT) + 1; // 6 rows per page.
                        if (itemRowIndex == 1) {
                            PDDocument itemsPdf = PDDocument.load(new File(templateDirectory, "RequestSummaryItemsList.pdf"));
                            form = itemsPdf.getDocumentCatalog().getAcroForm();
                            uniqueFormFieldRenameIndex++;
                            renameRequestSummaryItemsFormFields(form, uniqueFormFieldRenameIndex);
                            requestSummaryReportPdf.addPage(itemsPdf.getPage(0));
                        }
                    }
                    setRequestSummaryItemFields(form, item, itemRowIndex, uniqueFormFieldRenameIndex);
                }
            }

            // Now do file attachment stuff.
            uniqueFormFieldRenameIndex = 0;
            if (requestSummaryReport.getFileAttachments() != null) {
                for (int i = 0; i < requestSummaryReport.getFileAttachments().size(); i++) {
                    FileAttachment fileAttachment = requestSummaryReport.getFileAttachments().get(i);
                    if (i % NUM_FILE_ATTACHMENTS_SUMMARY_REPORT == 0) {
                        PDDocument fileAttachmentsPdf = PDDocument.load(new File(templateDirectory, "RequestSummaryAttachments.pdf"));
                        form = fileAttachmentsPdf.getDocumentCatalog().getAcroForm();
                        uniqueFormFieldRenameIndex++;
                        renameRequestSummaryFileAttachmentsFormFields(form, uniqueFormFieldRenameIndex);
                        requestSummaryReportPdf.addPage(fileAttachmentsPdf.getPage(0));
                    }
                    setRequestSummaryFileAttachmentFields(form, fileAttachment, i + 1, uniqueFormFieldRenameIndex);
                }
            }
            requestSummaryReportPdf.save(new File(tempDirectory, outputFilename));
        }
        return true;
    }

    private void renameRequestSummaryItemsFormFields(PDAcroForm form, int uniqueFormFieldRenameIndex) throws IOException {
        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        for (int i = 1; i <= NUM_ITEMS_IN_ADDITIONAL_PAGE_SUMMARY_REPORT; i++) {
            String itemNameKey = String.format("item_name_row_%d", i);
            String projectTaskKey = String.format("project_task_row_%d", i);
            String objectClassKey = String.format("object_class_row_%d", i);
            String actualQuantityKey = String.format("actual_quantity_row_%d", i);
            String actualPriceKey = String.format("actual_price_row_%d", i);
            String actualCostKey = String.format("actual_cost_row_%d", i);
            String estimateCostKey = String.format("estimate_cost_row_%d", i);
            //String extractedKey = String.format("extracted_row_%d", i);
            //String reconciledKey = String.format("reconciled_row_%d", i);
            String reallocatedKey = String.format("reallocated_row_%d", i);
            String descriptionKey = String.format("description_row_%d", i);
            //339
            String catalogNumberKey = String.format("item_catalog_number_row_%d", i);
            String transactionNumberKey = String.format("transaction_number_row_%d", i);
            String statementDateKey = String.format("statement_date_row_%d", i);

            form.getField(itemNameKey).setPartialName(itemNameKey + uniqueFieldNameVariable);
            form.getField(projectTaskKey).setPartialName(projectTaskKey + uniqueFieldNameVariable);
            form.getField(objectClassKey).setPartialName(objectClassKey + uniqueFieldNameVariable);
            form.getField(actualQuantityKey).setPartialName(actualQuantityKey + uniqueFieldNameVariable);
            form.getField(actualPriceKey).setPartialName(actualPriceKey + uniqueFieldNameVariable);
            form.getField(actualCostKey).setPartialName(actualCostKey + uniqueFieldNameVariable);
            form.getField(estimateCostKey).setPartialName(estimateCostKey + uniqueFieldNameVariable);
            //form.getField(extractedKey).setPartialName(extractedKey + uniqueFieldNameVariable);
            //form.getField(reconciledKey).setPartialName(reconciledKey + uniqueFieldNameVariable);
            form.getField(reallocatedKey).setPartialName(reallocatedKey + uniqueFieldNameVariable);
            form.getField(descriptionKey).setPartialName(descriptionKey + uniqueFieldNameVariable);
            //339
            form.getField(catalogNumberKey).setPartialName(catalogNumberKey + uniqueFieldNameVariable);
            form.getField(transactionNumberKey).setPartialName(transactionNumberKey + uniqueFieldNameVariable);
            form.getField(statementDateKey).setPartialName(statementDateKey + uniqueFieldNameVariable);
        }
    }

    private void setRequestSummaryItemFields(PDAcroForm form, Item item, int rowIndex, int uniqueFormFieldRenameIndex) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        String itemNameKey = "item_name_row_" + rowIndex + uniqueFieldNameVariable;
        String projectTaskKey = "project_task_row_" + rowIndex + uniqueFieldNameVariable;
        String objectClassKey = "object_class_row_" + rowIndex + uniqueFieldNameVariable;
        String actualQuantityKey = "actual_quantity_row_" + rowIndex + uniqueFieldNameVariable;
        String actualPriceKey = "actual_price_row_" + rowIndex + uniqueFieldNameVariable;
        String actualCostKey = "actual_cost_row_" + rowIndex + uniqueFieldNameVariable;
        String estimateCostKey = "estimate_cost_row_" + rowIndex + uniqueFieldNameVariable;
        //String extractedKey = "extracted_row_" + rowIndex + uniqueFieldNameVariable;
        //String reconciledKey = "reconciled_row_" + rowIndex + uniqueFieldNameVariable;
        String reallocatedKey = "reallocated_row_" + rowIndex + uniqueFieldNameVariable;
        String descriptionKey = "description_row_" + rowIndex + uniqueFieldNameVariable;
        //339
        String catalogNumberKey = "item_catalog_number_row_" + rowIndex + uniqueFieldNameVariable;
        String transactionNumberKey = "transaction_number_row_" + rowIndex + uniqueFieldNameVariable;
        String statementDateKey = "statement_date_row_" + rowIndex + uniqueFieldNameVariable;

        setPdfFieldValue((PDTextField) form.getField(itemNameKey), item.getItemName());
        setPdfFieldValue((PDTextField) form.getField(projectTaskKey), item.getProjectTask());
        setPdfFieldValue((PDTextField) form.getField(objectClassKey), item.getObjectClass());
        //339
        setPdfFieldValue((PDTextField) form.getField(catalogNumberKey), item.getCatalogNumber());
        String transNum = item.getTransactionNumber();
        if (transNum != null && transNum.length() > 25) {
            transNum = transNum.substring(0, 25);
        }
        setPdfFieldValue((PDTextField) form.getField(transactionNumberKey), transNum);
        if (item.getStatementDate() != null) {
            setPdfFieldValue((PDTextField) form.getField(statementDateKey), dateFormat.format(item.getStatementDate()));
        }

        int quantity = -1;
        if (item.getActualQuantity() != null) {
            quantity = item.getActualQuantity();
            setPdfFieldValue((PDTextField) form.getField(actualQuantityKey), "" + quantity);
        }
        double price = -1.0;
        if (item.getActualPrice() != null) {
            price = item.getActualPrice();
            setPdfFieldValue((PDTextField) form.getField(actualPriceKey), String.format("%.2f", price));
        }
        if (quantity * price > 0) {
            setPdfFieldValue((PDTextField) form.getField(actualCostKey), String.format("%.2f", quantity * price));
        }

        int estimatedQuantity = -1;
        if (item.getQuantity() != null) {
            estimatedQuantity = item.getQuantity();
            if (quantity == -1.0) {
                setPdfFieldValue((PDTextField) form.getField(actualQuantityKey), "" + estimatedQuantity);
            }
        }
        double estimatedPrice = -1.0;
        if (item.getPrice() != null) {
            estimatedPrice = item.getPrice();
            if (price == -1.0) {
                setPdfFieldValue((PDTextField) form.getField(actualPriceKey), String.format("%.2f", estimatedPrice));
            }
        }
        if (estimatedQuantity * estimatedPrice > 0) {
            setPdfFieldValue((PDTextField) form.getField(estimateCostKey), String.format("%.2f", estimatedQuantity * estimatedPrice));
        }

        if (item.getVendorId() != null && item.getVendorId() == -99) {
            String description = "";
            if (item.getDescription() != null) {
                description = ": " + item.getDescription();
            }
            setPdfFieldValue((PDTextField) form.getField(descriptionKey), "IT Buying Service" + description);
        } else {
            setPdfFieldValue((PDTextField) form.getField(descriptionKey), item.getDescription());
        }
    }

    private void renameRequestSummaryFileAttachmentsFormFields(PDAcroForm form, int uniqueFormFieldRenameIndex) throws IOException {
        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        for (int i = 1; i <= NUM_FILE_ATTACHMENTS_SUMMARY_REPORT; i++) {
            String cateogoryKey = String.format("category_row_%d", i);
            String fileNameKey = String.format("file_name_row_%d", i);
            String fileTypeKey = String.format("file_type_row_%d", i);
            String fileSizeKey = String.format("file_size_row_%d", i);
            form.getField(cateogoryKey).setPartialName(cateogoryKey + uniqueFieldNameVariable);
            form.getField(fileNameKey).setPartialName(fileNameKey + uniqueFieldNameVariable);
            form.getField(fileTypeKey).setPartialName(fileTypeKey + uniqueFieldNameVariable);
            form.getField(fileSizeKey).setPartialName(fileSizeKey + uniqueFieldNameVariable);
        }
    }

    private void setRequestSummaryFileAttachmentFields(PDAcroForm form, FileAttachment fileAttachment, int rowIndex, int uniqueFormFieldRenameIndex) throws IOException {
        String uniqueFieldNameVariable = uniqueFormFieldRenameIndex != 0 ? "_" + uniqueFormFieldRenameIndex : "";
        String categoryKey = "category_row_" + rowIndex + uniqueFieldNameVariable;
        String fileNameKey = "file_name_row_" + rowIndex + uniqueFieldNameVariable;
        String fileTypeKey = "file_type_row_" + rowIndex + uniqueFieldNameVariable;
        String fileSizeKey = "file_size_row_" + rowIndex + uniqueFieldNameVariable;

        setPdfFieldValue((PDTextField) form.getField(categoryKey), fileAttachment.getCategoryName());
        setPdfFieldValue((PDTextField) form.getField(fileNameKey), fileAttachment.getName());
        setPdfFieldValue((PDTextField) form.getField(fileTypeKey), fileAttachment.getTypeCode());
        if (fileAttachment.getSize() != null) {
            String fileSize = "0B";
            if (fileAttachment.getSize() < 1000) {
                fileSize = fileAttachment.getSize() + " B";
            } else if (fileAttachment.getSize() < 1000000) {
                fileSize = (fileAttachment.getSize() / 1000) + " KB";
            } else {
                fileSize = (fileAttachment.getSize() / 1000000) + " MB";
            }
            setPdfFieldValue((PDTextField) form.getField(fileSizeKey), fileSize);
        }
    }

    private void setPdfFieldValue(PDTextField field, String value) {
        try {
            if (value != null && !value.isEmpty() && field != null) {
                field.setDefaultAppearance("/Helv 8 Tf 0 g");
                field.setMultiline(true);
                field.setValue(value);
            }
        } catch (Exception caught) {
            LOG.log(Level.WARNING, caught.getMessage(), caught);
        }
    }

    private void setPdfCheckBoxFieldValue(PDField field, String value) {
        try {
            if (value != null && !value.isEmpty() && field != null) {
                field.setValue(value);
            }
        } catch (Exception caught) {
            LOG.log(Level.WARNING, caught.getMessage(), caught);

        }
    }

    public static class GetRequestsByCriteriaResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetRequestResponse extends JsonStatus {

        @Getter
        @Setter
        private RequestDeepCopy data;

    }

    public static class GetSavedRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetSubmittedRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetPendingRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetPreparedRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetProcessedRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetArchivedRequestsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<RequestDeepCopy> data;

    }

    public static class GetRequestCountsResponse extends JsonStatus {

        @Getter
        @Setter
        public static class Count {

            private Integer savedRequests;
            private Integer activeRequests;
            private Integer inboxRequests;

        }

        @Getter
        @Setter
        private GetRequestCountsResponse.Count data;

    }

    public static class GetItemsResponse extends JsonStatus {

        @Getter
        @Setter
        public static class Item {

            private Integer id;
            private Integer requestId;
            //private String mType;
            private Integer vendorId;
            private String catalogNumber;
            private String itemName;
            private String description;
            private Double price;
            private Integer quantity;
            private Double actualPrice;
            private Integer actualQuantity;
            private String purpose;
            private Boolean chemical;
            public Boolean isTaggableEquipment;
            private String projectTask;
            private Integer shoppingCartFileId;
            private Integer statusId;
            private String objectClass;
            private Integer latestItemStatusTypeId;
            private String latestItemStatusTypeName;
            private Boolean isShippingCost;
            private String itemNotes;
            private String unitIssue;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date dateReceived;
            private String transactionNumber;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date statementDate;

        }

        @Getter
        @Setter
        List<GetItemsResponse.Item> data;

    }

    public static class GetAttachmentsResponse extends JsonStatus {

        @Getter
        @Setter
        public static class Attachment {

            private Integer id;
            private Integer requestId;
            private Integer categoryId;
            private String categoryName;
            private String name;
            private String typeCode;
            private Integer size;
            private Integer createdBy;
            private String createdByName;
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
            private Date createdDate;

        }
        @Getter
        @Setter
        List<GetAttachmentsResponse.Attachment> data;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostRequest {

        private Integer creatorId;
        private Integer requesterId;
        private Integer requestedForId;
        private String delivAddr;
        private String comments;
        private String description;
        private Boolean generateRequisitionNumber;
        private String neededByDate;
        private Integer reviewerId;
        private Integer bankcardApprovingOfficialId;
        private Integer bankcardHolderId;
        private Integer fy;
        private String isItPurchase;
        private Integer purchaseTypeId;
        private Integer groupId;
        private Integer ouId;
        private Integer divisionId;
        //the request is created for a different group than the requester's group
        private Boolean isDetailReq;
        private Integer missionCriticalCategoryId;
        private String missionCriticalJustification;
    }

    public static class PostRequestResponse extends JsonStatus {

        @Getter
        @Setter
        private RequestDeepCopy data;

    }

    @Data
    public static class PutDescriptionRequest {

        private String description;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PutApproverRequest extends Request {

        private String approverType;
        private Integer approverId;

    }

    public static class PutApproverResponse extends JsonStatus {
    }

    public static class PutDescriptionResponse extends JsonStatus {
    }

    public static class PutRequisitionNumberResponse extends JsonStatus {

        public String requisitionNumber;
    }

    @Data
    public static class PutRequestBchCommentsRequest {

        private String bchComments;

    }

    public static class PutRequestBchCommentsResponse extends JsonStatus {
    }

    public static class PostRequestCopyResponse extends JsonStatus {

        @Getter
        @Setter
        private Integer requestId;

    }

    @Data
    public static class PostItemStatusTypesForRequestRequest {

        private Integer[] itemIds;
        private Integer statusTypeId;

    }

    public static class PostItemStatusTypesForRequestResponse extends JsonStatus {
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutRequest {

        private Integer requesterId;
        private Integer requestedForId;
        private String delivAddr;
        private Boolean delivToHome;
        private String comments;
        private Boolean generateRequisitionNumber;
        private String neededByDate;
        private Integer reviewerId;
        private Integer divisionChiefId;
        private Integer fundsCertifyingOfficialId;
        private Integer bankcardApprovingOfficialId;
        private Integer bankcardHolderId;
        private String estimatedTimeOfArrival;
        private String orderNumber;
        private String gsaSessionNumber;
        private String purchaseOrderNumber;
        private Double approvalAmount;
        private String description;
        private Integer fy;
        private String isItPurchase;
        private Integer purchaseTypeId;
        private String fcoName;
        private Integer missionCriticalCategoryId;
        private String missionCriticalJustification;

    }

    public static class PutRequestResponse extends JsonStatus {

        @Getter
        @Setter
        private RequestDeepCopy data;

    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DeleteRequestResponse extends JsonStatus {

        private Integer requestId;
        private Integer rowCount;

    }

    @Data
    public static class PostAttachmentResponse {

        private boolean success;
        private Integer fileId;
        private Integer code;
        private String description;

        @Override
        public String toString() {
            StringBuilder output = new StringBuilder("{ ");
            output.append(String.format("\"success\": %s, ", success ? "true" : "false"));
            if (success) {
                if (fileId != null) {
                    output.append(String.format("\"fileId\": %d", fileId));
                }
            } else if (code != null && description != null) {
                output.append(String.format("\"code\": %d, \"description\": \"%s\"", code, description));
            }
            output.append(" }");
            return output.toString();
        }
    }

    public static class PutProjectTaskForRequestItemsResponse extends JsonStatus {

        @Getter
        @Setter
        private Integer rowsUpdated;

    }

    public static class PutObjectClassForRequestItemsResponse extends JsonStatus {

        @Getter
        @Setter
        private Integer rowsUpdated;

    }

    public static class PutPullBackRouteResponse extends JsonStatus {
    }

    @Data
    public static class RequestDeepCopy {

        private Integer requestId;
        private String requisitionNumber;
        private String comments;
        private Integer requesterId;
        private String requesterName;
        private Integer creatorId;
        private String creatorName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date reqDate;
        private Integer requestedForId;
        private String requestedForName;
        private Boolean isShoppingCart;
        private Integer referenceId;
        private Integer updatedBy;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date updatedDate;
        private String delivAddr;
        private Boolean delivToHome;
        private Double totalCost;
        private Double actualTotalCost;
        private Integer statusCode;
        private String statusText;
        private String approverNote;
        private Integer routeId;
        private Integer routeFrom;
        private Integer routeByDelegate;
        private Integer routeTo;
        private String routeFromName;
        private String routeByDelegateName;
        private String routeToName;
        private String routeTypeName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date routeDate;
        private Integer routeTypeId;
        private Integer ouId;
        private Integer divisionId;
        private Integer groupId;
        private String vendors;
        private String items;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date neededByDate;
        private Integer reviewerId;
        private Integer divisionChiefId;
        private Integer bankcardApprovingOfficialId;
        private Integer bankcardHolderId;
        private String dcName;
        private String reviewerName;
        private String baoName;
        private String bhName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date estimatedTimeOfArrival;
        private String orderNumber;
        private String gsaSessionNumber;
        private String purchaseOrderNumber;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private Date submittedDate;
        private String bchComments;
        private String description;
        private Double approvalAmount;
        private Integer isDynamic;
        private Integer rerouteStack;
        private Integer isDynamicReroute;
        private Integer fy;
        private String isItPurchase;
        private Integer itsoApproved;
        private Integer routeStep;
        private String dynamicType;
        private Integer fundsCertifyingOfficialId;
        private String fcoName;
        private Integer purchaseTypeId;
        private Integer missionCriticalCategoryId;
        private String missionCriticalJustification;

    }
}
