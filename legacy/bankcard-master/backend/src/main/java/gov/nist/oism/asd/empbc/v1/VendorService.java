package gov.nist.oism.asd.empbc.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import gov.nist.oism.asd.empbc.db.VendorDao;
import gov.nist.oism.asd.empbc.model.RequestJustification;
import gov.nist.oism.asd.empbc.model.RequestVendorT;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.VendorT;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/vendors")
public class VendorService extends SsoService {

    private static final Logger LOG = Logger.getLogger(VendorService.class.getSimpleName());

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getVendors(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results = dao.selectVendorsForRequester(authenticatedUser.getPeopleId(), authenticatedUser.getDivisionId(), filter);
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        GetVendorsResponse getVendorsResponse = new GetVendorsResponse();
//        if (statusCode == StatusCode.OK) {
//            List<VendorRequestBean> dataList = new ArrayList<>();
//            List<Vendor> vendors = (List<Vendor>) results.get(VendorDao.VENDOR_LIST_KEY);
//            vendors.stream().map((vendor) -> {
//                VendorRequestBean data = new VendorRequestBean();
//                data.setVendorId(vendor.getId());
//                data.setName(vendor.getName());
//                data.setStreetAddr(vendor.getStreet());
//                data.setCity(vendor.getCity());
//                data.setState(vendor.getState());
//                data.setZipCode(vendor.getZipCode());
//                data.setWebUrl(vendor.getWebUrl());
//                data.setContactName(vendor.getContactName());
//                data.setPhone(vendor.getPhoneNumber());
//                data.setFax(vendor.getFaxNumber());
//                data.setEmail(vendor.getEmail());
//                data.setAccountNumber(vendor.getAccountNumber());
//                data.setIsForeignAddress(vendor.getIsForeignAddress());
//                data.setForeignAddress(vendor.getForeignAddress());
//                data.setCreatedBy(vendor.getCreatedBy());
//                data.setDunsNumber(vendor.getDunsNumber());
//                data.setImportedFrom(vendor.getImportedFrom());
//
//                return data;
//            }).forEachOrdered((data) -> {
//                dataList.add(data);
//            });
//            getVendorsResponse.setData(dataList);
//        }
//        return Response.ok().entity(serializeResponseWithStatus(getVendorsResponse, StatusCode.OK)).build();
//    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{requestId}")
//    public Response getVendorsForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results = dao.selectVendorsForRequest(requestId);
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        GetVendorsForRequestResponse getVendorsForRequestResponse = new GetVendorsForRequestResponse();
//        if (statusCode == StatusCode.OK) {
//            List<VendorRequestBean> dataList = new ArrayList<>();
//            List<RequestVendor> requestVendors = (List<RequestVendor>) results.get(VendorDao.REQUEST_VENDOR_LIST_KEY);
//            requestVendors.stream().map((requestVendor) -> {
//                VendorRequestBean data = new VendorRequestBean();
//                Vendor vendor = requestVendor.getVendor();
//                if (vendor != null) {
//                    data.setVendorId(vendor.getId());
//                    data.setName(vendor.getName());
//                    data.setStreetAddr(vendor.getStreet());
//                    data.setCity(vendor.getCity());
//                    data.setState(vendor.getState());
//                    data.setZipCode(vendor.getZipCode());
//                    data.setWebUrl(vendor.getWebUrl());
//                    data.setContactName(vendor.getContactName());
//                    data.setPhone(vendor.getPhoneNumber());
//                    data.setFax(vendor.getFaxNumber());
//                    data.setEmail(vendor.getEmail());
//                    data.setAccountNumber(vendor.getAccountNumber());
//                    data.setIsForeignAddress(vendor.getIsForeignAddress());
//                    data.setForeignAddress(vendor.getForeignAddress());
//                    data.setDunsNumber(vendor.getDunsNumber());
//                    data.setImportedFrom(vendor.getImportedFrom());
//                }
//                data.setCreatedBy(requestVendor.getCreatedBy());
//                data.setCreatedDate(requestVendor.getCreatedDate());
//                data.setUpdatedBy(requestVendor.getUpdatedBy());
//                data.setUpdatedDate(requestVendor.getUpdatedDate());
//                data.setNonElectricJust(requestVendor.getConvenienceCheckJustification());
//                data.setNonGsaJust(requestVendor.getGsaScheduleJustification());
//                data.setNonSbJust(requestVendor.getSmallBusinessJustification());
//                data.setThirdPartyJust(requestVendor.getThirdPartyJustification());
//                data.setPriceJust(requestVendor.getPriceJustification());
//                if (requestVendor.getGsaSchedule() != null) {
//                    data.setIsNonGsa(!requestVendor.getGsaSchedule());
//                }
//                if (requestVendor.getSmallBusiness() != null) {
//                    data.setIsNonSb(!requestVendor.getSmallBusiness());
//                }
//                data.setIsNonElectric(requestVendor.getConvenienceCheck());
//                data.setIsThirdParty(requestVendor.getThirdPartyVendor());
//                data.setRequestId(requestVendor.getRequestId());
//                if (requestVendor.getProfessionalOrg() != null) {
//                    data.setIsProfessionalOrg(requestVendor.getProfessionalOrg());
//                }
//                
//                return data;
//            }).forEachOrdered((data) -> {
//                dataList.add(data);
//            });
//            getVendorsForRequestResponse.setData(dataList);
//        }
//        return Response.ok().entity(serializeResponseWithStatus(getVendorsForRequestResponse, StatusCode.OK)).build();
//    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/shared")
    public Response getSharedVendors(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results;
        
        HttpSession session = servletRequest.getSession();
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
             User sampleDetaileeUser;
            try {
                UserService userService=new UserService();
                sampleDetaileeUser = userService.getSampleUserByGroupId((Integer)session.getAttribute(DETAILEE_GROUP));
            }catch (Exception e) {
                return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.DetaileeError)).build();
            }
            results = dao.selectSharedVendors(sampleDetaileeUser);
        }else{
            results = dao.selectSharedVendors(authenticatedUser);
        }
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        GetSharedVendorsResponse response = new GetSharedVendorsResponse();
        if (statusCode == StatusCode.OK) {
            response.data = (List<VendorT>) results.get(VendorDao.VENDOR_LIST_KEY);
            return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
        }

        // Some kind of error has happened.
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestId}")
    public Response getVendorForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.selectVendorForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        GetVendorForRequestResponse response = new GetVendorForRequestResponse();
        if (statusCode == StatusCode.OK) {
            response.data = (List<RequestVendorT>) results.get(VendorDao.VENDOR_LIST_KEY);
        }

        // Some kind of error has happened.
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/justification/{requestId}")
    public Response getJustificationForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.selectJustificationForRequest(requestId);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        GetJustForRequestResponse response = new GetJustForRequestResponse();
        if (statusCode == StatusCode.OK) {
            response.data = (List<RequestJustification>) results.get(VendorDao.VENDOR_LIST_KEY);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/shared/{vendor_id}")
    public Response deleteSharedVendor(@Context HttpServletRequest servletRequest, @PathParam("vendor_id") Integer vendorId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.deleteVendorT(vendorId, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        DeleteVendorTResponse response = new DeleteVendorTResponse();
        if (statusCode == StatusCode.OK) {
            response.vendorRowCount = (Integer) results.get(VendorDao.ROW_COUNT_KEY);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/shared")
    public Response putVendorT(@Context HttpServletRequest servletRequest, PutVendorTRequest vendor) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user ");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.updateVendorT(vendor, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PutVendorTResponse response = new PutVendorTResponse();
        if (statusCode == StatusCode.OK) {
            response.vendorRowCount = (Integer) results.get(VendorDao.ROW_COUNT_KEY);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request/{requestId}")
    public Response putRequestVendorT(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PostRequestVendorTRequest vendor) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user ");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.updateRequestVendorT(vendor, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PutVendorTResponse response = new PutVendorTResponse();
        if (statusCode == StatusCode.OK) {
            response.requestVendorRowCount = (Integer) results.get(VendorDao.ROW_COUNT_KEY);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/justification/{requestId}")
    public Response putRequestJustification(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PostRequestJustificationRequest just) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user ");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.updateRequestJustification(just, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PutJustResponse response = new PutJustResponse();
        if (statusCode == StatusCode.OK) {
            response.justRowCount = (Integer) results.get(VendorDao.ROW_COUNT_KEY);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @POST
    @Path("/shared")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postVendorT(@Context HttpServletRequest servletRequest, PostVendorTRequest vendor) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results;
        results = dao.insertVendorT(vendor, authenticatedUser);

        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PostVendorResponse postVendorResponse = new PostVendorResponse();
        if (statusCode == StatusCode.OK) {
            postVendorResponse.setVendorId((Integer) results.get(VendorDao.VENDOR_ID_KEY));
        }
        return Response.ok().entity(serializeResponseWithStatus(postVendorResponse, statusCode)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request/{requestId}")
    public Response postVendorForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PostRequestVendorTRequest vendor) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.insertRequestVendorT(vendor, requestId, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PostRequestVendorResponse response = new PostRequestVendorResponse();
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/justification/{requestId}")
    public Response postJustificationForRequest(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId, PostRequestJustificationRequest just) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.insertRequestJustification(just, requestId, authenticatedUser);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        PostRequestJustResponse response = new PostRequestJustResponse();
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/justification/{requestId}")
    public Response deleteJustification(@Context HttpServletRequest servletRequest, @PathParam("requestId") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.deleteJustification(requestId);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        DeleteJustResponse deleteJustResponse = new DeleteJustResponse();

        return Response.ok().entity(serializeResponseWithStatus(deleteJustResponse, statusCode)).build();
    }
    
    public static class DeleteJustResponse extends JsonStatus {
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response postVendor(@Context HttpServletRequest servletRequest, PostVendorRequest postVendorRequest) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//
//        Vendor vendor = new Vendor();
//        vendor.setId(postVendorRequest.getVendorId());
//        vendor.setName(postVendorRequest.getName());
//        vendor.setStreet(postVendorRequest.getStreetAddr());
//        vendor.setCity(postVendorRequest.getCity());
//        vendor.setState(postVendorRequest.getState());
//        vendor.setZipCode(postVendorRequest.getZipCode());
//        vendor.setWebUrl(postVendorRequest.getWebUrl());
//        vendor.setContactName(postVendorRequest.getContactName());
//        vendor.setPhoneNumber(postVendorRequest.getPhone());
//        vendor.setFaxNumber(postVendorRequest.getFax());
//        vendor.setEmail(postVendorRequest.getEmail());
//        vendor.setAccountNumber(postVendorRequest.getAccountNumber());
//        vendor.setIsForeignAddress(postVendorRequest.getIsForeignAddress());
//        vendor.setForeignAddress(postVendorRequest.getForeignAddress());
//        vendor.setDunsNumber(postVendorRequest.getDunsNumber());
//        vendor.setImportedFrom(postVendorRequest.getImportedFrom());
//        vendor.setCreatedBy(authenticatedUser.getPeopleId());
//        vendor.setUpdatedBy(authenticatedUser.getPeopleId());
//
//        RequestVendor requestVendor = new RequestVendor();
//        requestVendor.setVendor(vendor);
//        requestVendor.setRequestId(postVendorRequest.getRequestId());
//        requestVendor.setVendorId(postVendorRequest.getVendorId());
//        requestVendor.setConvenienceCheck(postVendorRequest.getIsNonElectric());
//        requestVendor.setConvenienceCheckJustification(postVendorRequest.getNonElectricJust());
//        if (postVendorRequest.getIsNonGsa() != null) {
//            requestVendor.setGsaSchedule(!postVendorRequest.getIsNonGsa());
//        }
//        requestVendor.setGsaScheduleJustification(postVendorRequest.getNonGsaJust());
//        requestVendor.setThirdPartyVendor(postVendorRequest.getIsThirdParty());
//        requestVendor.setThirdPartyJustification(postVendorRequest.getThirdPartyJust());
//        requestVendor.setPriceJustification(postVendorRequest.getPriceJust());
//        if (postVendorRequest.getIsNonSb() != null) {
//            requestVendor.setSmallBusiness(!postVendorRequest.getIsNonSb());
//        }
//        requestVendor.setSmallBusinessJustification(postVendorRequest.getNonSbJust());
//        requestVendor.setConvenienceCheckNumber(postVendorRequest.getConvenienceCheckNumber());
//        requestVendor.setCreatorId(authenticatedUser.getPeopleId());
//        requestVendor.setUpdatedBy(authenticatedUser.getPeopleId());
//        requestVendor.setDivisionId(authenticatedUser.getDivisionId());
//        requestVendor.setProfessionalOrg(postVendorRequest.getIsProfessionalOrg());
//
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results;
//        if (postVendorRequest.getVendorId() == null || postVendorRequest.getVendorId() == 0) {
//            results = dao.insertNewRequestVendor(requestVendor);
//        } else {
//            // Also try to update the vendor if the vendor is created by the user.
//            dao.updateVendor(vendor);
//            results = dao.insertExistingRequestVendor(requestVendor);
//        }
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        PostVendorResponse postVendorResponse = new PostVendorResponse();
//        if (statusCode == StatusCode.OK) {
//            postVendorResponse.setVendorId((Integer) results.get(VendorDao.VENDOR_ID_KEY));
//        }
//
//        return Response.ok().entity(serializeResponseWithStatus(postVendorResponse, statusCode)).build();
//    }

//    @PUT
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response putVendor(@Context HttpServletRequest servletRequest, PutVendorRequest putVendorRequest) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//
//        Vendor vendor = new Vendor();
//        vendor.setId(putVendorRequest.getVendorId());
//        vendor.setName(putVendorRequest.getName());
//        vendor.setStreet(putVendorRequest.getStreetAddr());
//        vendor.setCity(putVendorRequest.getCity());
//        vendor.setState(putVendorRequest.getState());
//        vendor.setZipCode(putVendorRequest.getZipCode());
//        vendor.setWebUrl(putVendorRequest.getWebUrl());
//        vendor.setContactName(putVendorRequest.getContactName());
//        vendor.setPhoneNumber(putVendorRequest.getPhone());
//        vendor.setFaxNumber(putVendorRequest.getFax());
//        vendor.setEmail(putVendorRequest.getEmail());
//        vendor.setAccountNumber(putVendorRequest.getAccountNumber());
//        vendor.setIsForeignAddress(putVendorRequest.getIsForeignAddress());
//        vendor.setForeignAddress(putVendorRequest.getForeignAddress());
//        vendor.setDunsNumber(putVendorRequest.getDunsNumber());
//        vendor.setImportedFrom(putVendorRequest.getImportedFrom());
//        vendor.setUpdatedBy(authenticatedUser.getPeopleId());
//
//        RequestVendor requestVendor = new RequestVendor();
//        requestVendor.setRequestId(putVendorRequest.getRequestId());
//        requestVendor.setVendorId(putVendorRequest.getVendorId());
//        requestVendor.setConvenienceCheck(putVendorRequest.getIsNonElectric());
//        requestVendor.setConvenienceCheckJustification(putVendorRequest.getNonElectricJust());
//        if (putVendorRequest.getIsNonGsa() != null) {
//            requestVendor.setGsaSchedule(!putVendorRequest.getIsNonGsa());
//        }
//        requestVendor.setGsaScheduleJustification(putVendorRequest.getNonGsaJust());
//        requestVendor.setThirdPartyVendor(putVendorRequest.getIsThirdParty());
//        requestVendor.setThirdPartyJustification(putVendorRequest.getThirdPartyJust());
//        requestVendor.setPriceJustification(putVendorRequest.getPriceJust());
//        if (putVendorRequest.getIsNonSb() != null) {
//            requestVendor.setSmallBusiness(!putVendorRequest.getIsNonSb());
//        }
//        requestVendor.setSmallBusinessJustification(putVendorRequest.getNonSbJust());
//        requestVendor.setConvenienceCheckNumber(putVendorRequest.getConvenienceCheckNumber());
//        requestVendor.setDivisionId(authenticatedUser.getDivisionId());
//        requestVendor.setUpdatedBy(authenticatedUser.getPeopleId());
//        requestVendor.setProfessionalOrg(putVendorRequest.getIsProfessionalOrg());
//
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results = dao.updateVendor(vendor);
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        PutVendorResponse putVendorResponse = new PutVendorResponse();
//        if (statusCode == StatusCode.OK) {
//            putVendorResponse.setVendorRowCount((Integer) results.get(VendorDao.ROW_COUNT_KEY));
//            results = dao.updateRequestVendor(requestVendor);
//            if (statusCode == StatusCode.OK) {
//                putVendorResponse.setRequestVendorRowCount((Integer) results.get(VendorDao.ROW_COUNT_KEY));
//            }
//        }
//
//        return Response.ok().entity(serializeResponseWithStatus(putVendorResponse, statusCode)).build();
//    }

//    @DELETE
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{vendor_id}")
//    public Response deleteVendor(@Context HttpServletRequest servletRequest, @PathParam("vendor_id") Integer vendorId) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results = dao.deleteVendor(vendorId, authenticatedUser.getPeopleId());
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        DeleteVendorResponse deleteVendorResponse = new DeleteVendorResponse();
//        if (statusCode == StatusCode.OK) {
//            deleteVendorResponse.setRowCount((Integer) results.get(VendorDao.ROW_COUNT_KEY));
//        }
//
//        return Response.ok().entity(serializeResponseWithStatus(deleteVendorResponse, statusCode)).build();
//    }

//    @DELETE
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/{vendor_id}/{request_id}")
//    public Response deleteItem(@Context HttpServletRequest servletRequest, @PathParam("vendor_id") Integer vendorId, @PathParam("request_id") Integer requestId) {
//        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
//        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
//            LOG.info("Can't find SSO user");
//            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
//        }
//        
//        VendorDao dao = new VendorDao();
//        Map<String, Object> results = dao.deleteRequestVendor(vendorId, requestId);
//        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
//        DeleteItemResponse deleteItemResponse = new DeleteItemResponse();
//        
//        return Response.ok().entity(serializeResponseWithStatus(deleteItemResponse, statusCode)).build();
//    }
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request/{request_id}")
    public Response deleteRequestVendor(@Context HttpServletRequest servletRequest, @PathParam("vendor_id") Integer vendorId, @PathParam("request_id") Integer requestId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        VendorDao dao = new VendorDao();
        Map<String, Object> results = dao.deleteRequestVendorT(requestId);
        StatusCode statusCode = (StatusCode) results.get(VendorDao.STATUS_CODE_KEY);
        DeleteRequestVendorResponse response = new DeleteRequestVendorResponse();

        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    public static class GetVendorsResponse extends JsonStatus {

        private List<VendorRequestBean> mData;

        public List<VendorRequestBean> getData() {
            return mData;
        }

        public void setData(List<VendorRequestBean> data) {
            mData = data;
        }
    }

    public static class GetSharedVendorsResponse extends JsonStatus {

        public List<VendorT> data;

    }

    public static class GetVendorsForRequestResponse extends JsonStatus {

        private List<VendorRequestBean> mData;

        public List<VendorRequestBean> getData() {
            return mData;
        }

        public void setData(List<VendorRequestBean> data) {
            mData = data;
        }
    }

    public static class GetVendorForRequestResponse extends JsonStatus {

        public List<RequestVendorT> data;
    }
    
    public static class GetJustForRequestResponse extends JsonStatus {

        public List<RequestJustification> data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostVendorRequest extends VendorRequestBean {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostVendorTRequest extends VendorT {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostRequestVendorTRequest extends RequestVendorT {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostRequestJustificationRequest extends RequestJustification {
    }

    public static class PostVendorResponse extends JsonStatus {

        private Integer mVendorId;

        public Integer getVendorId() {
            return mVendorId;
        }

        public void setVendorId(Integer vendorId) {
            mVendorId = vendorId;
        }
    }

    public static class PostRequestVendorResponse extends JsonStatus {
    }

    public static class PostRequestJustResponse extends JsonStatus {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutVendorRequest extends VendorRequestBean {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PutVendorTRequest extends VendorT {
    }

    public static class PutVendorTResponse extends JsonStatus {

        public Integer vendorRowCount;
        public Integer requestVendorRowCount;
    }

    public static class PutJustResponse extends JsonStatus {

        public Integer justRowCount;
    }

    public static class PutVendorResponse extends JsonStatus {

        private Integer mVendorRowCount;
        private Integer mRequestVendorRowCount;

        public Integer getVendorRowCount() {
            return mVendorRowCount;
        }

        public void setVendorRowCount(Integer vendorRowCount) {
            mVendorRowCount = vendorRowCount;
        }

        public Integer getRequestVendorRowCount() {
            return mRequestVendorRowCount;
        }

        public void setRequestVendorRowCount(Integer requestVendorRowCount) {
            mRequestVendorRowCount = requestVendorRowCount;
        }
    }

    public static class DeleteVendorResponse extends JsonStatus {

        private int mRowCount;

        public int getRowCount() {
            return mRowCount;
        }

        public void setRowCount(int rowCount) {
            mRowCount = rowCount;
        }
    }

    public static class DeleteRequestVendorResponse extends JsonStatus {
    }

    public static class DeleteVendorTResponse extends JsonStatus {

        public Integer vendorRowCount;
        public Integer requestRowCount;
    }

    public static class VendorRequestBean {

        private Integer mVendorId;
        private String mName;
        private String mStreetAddr;
        private String mCity;
        private String mState;
        private String mZipCode;
        private String mWebUrl;
        private String mContactName;
        private String mPhone;
        private String mFax;
        private String mEmail;
        private String mAccountNumber;
        private Boolean mIsForeignAddress;
        private String mForeignAddress;
        private String mDunsNumber;
        private String mImportedFrom;
        private Integer mCreatedBy;
        private Date mCreatedDate;
        private Integer mUpdatedBy;
        private Date mUpdatedDate;
        private String mNonElectricJust;
        private String mNonGsaJust;
        private String mNonSbJust;
        private String mThirdPartyJust;
        private String mPriceJust;
        private Boolean mNoJustNeeded;
        private Boolean mIsNonGsa;
        private Boolean mIsNonSb;
        private Boolean mIsNonElectric;
        private Boolean mIsThirdParty;
        private Boolean mIsPriceReasonable;
        private Integer mRequestId;
        private String mConvenienceCheckNumber;
        private Boolean mIsProfessionalOrg;
        private Boolean mIsCommercialVendor;
        private String mCommercialVendorJust;

        public Integer getVendorId() {
            return mVendorId;
        }

        public void setVendorId(Integer vendorId) {
            mVendorId = vendorId;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public String getStreetAddr() {
            return mStreetAddr;
        }

        public void setStreetAddr(String streetAddr) {
            mStreetAddr = streetAddr;
        }

        public String getCity() {
            return mCity;
        }

        public void setCity(String city) {
            mCity = city;
        }

        public String getState() {
            return mState;
        }

        public void setState(String state) {
            mState = state;
        }

        public String getZipCode() {
            return mZipCode;
        }

        public void setZipCode(String zipCode) {
            mZipCode = zipCode;
        }

        public String getWebUrl() {
            return mWebUrl;
        }

        public void setWebUrl(String webUrl) {
            mWebUrl = webUrl;
        }

        public String getContactName() {
            return mContactName;
        }

        public void setContactName(String contactName) {
            mContactName = contactName;
        }

        public String getPhone() {
            return mPhone;
        }

        public void setPhone(String phone) {
            mPhone = phone;
        }

        public String getFax() {
            return mFax;
        }

        public void setFax(String fax) {
            mFax = fax;
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(String email) {
            mEmail = email;
        }

        public String getAccountNumber() {
            return mAccountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            mAccountNumber = accountNumber;
        }

        public Boolean getIsForeignAddress() {
            return mIsForeignAddress;
        }

        public void setIsForeignAddress(Boolean isForeignAddress) {
            mIsForeignAddress = isForeignAddress;
        }

        public String getForeignAddress() {
            return mForeignAddress;
        }

        public void setForeignAddress(String foreignAddress) {
            mForeignAddress = foreignAddress;
        }

        public String getDunsNumber() {
            return mDunsNumber;
        }

        public void setDunsNumber(String dunsNumber) {
            mDunsNumber = dunsNumber;
        }

        public String getImportedFrom() {
            return mImportedFrom;
        }

        public void setImportedFrom(String importedFrom) {
            mImportedFrom = importedFrom;
        }

        public Integer getCreatedBy() {
            return mCreatedBy;
        }

        public void setCreatedBy(Integer createdBy) {
            mCreatedBy = createdBy;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        public Date getCreatedDate() {
            return mCreatedDate;
        }

        public void setCreatedDate(Date createdDate) {
            mCreatedDate = createdDate;
        }

        public Integer getUpdatedBy() {
            return mUpdatedBy;
        }

        public void setUpdatedBy(Integer updatedBy) {
            mUpdatedBy = updatedBy;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        public Date getUpdatedDate() {
            return mUpdatedDate;
        }

        public void setUpdatedDate(Date updatedDate) {
            mUpdatedDate = updatedDate;
        }

        public String getNonElectricJust() {
            return mNonElectricJust;
        }

        public void setNonElectricJust(String nonElectricJust) {
            mNonElectricJust = nonElectricJust;
        }

        public String getNonGsaJust() {
            return mNonGsaJust;
        }

        public void setNonGsaJust(String nonGsaJust) {
            mNonGsaJust = nonGsaJust;
        }

        public String getNonSbJust() {
            return mNonSbJust;
        }

        public void setNonSbJust(String nonSbJust) {
            mNonSbJust = nonSbJust;
        }

        public String getThirdPartyJust() {
            return mThirdPartyJust;
        }

        public void setThirdPartyJust(String thirdPartyJust) {
            mThirdPartyJust = thirdPartyJust;
        }

        public String getPriceJust() {
            return mPriceJust;
        }

        public void setPriceJust(String priceJust) {
            mPriceJust = priceJust;
        }

        public Boolean getNoJustNeeded() {
            return mNoJustNeeded;
        }

        public void setNoJustNeeded(Boolean noJustNeeded) {
            mNoJustNeeded = noJustNeeded;
        }

        public Boolean getIsNonGsa() {
            return mIsNonGsa;
        }

        public void setIsNonGsa(Boolean isNonGsa) {
            mIsNonGsa = isNonGsa;
        }

        public Boolean getIsNonSb() {
            return mIsNonSb;
        }

        public void setIsNonSb(Boolean isNonSb) {
            mIsNonSb = isNonSb;
        }

        public Boolean getIsNonElectric() {
            return mIsNonElectric;
        }

        public void setIsNonElectric(Boolean isNonElectric) {
            mIsNonElectric = isNonElectric;
        }

        public Boolean getIsThirdParty() {
            return mIsThirdParty;
        }

        public void setIsThirdParty(Boolean isThirdParty) {
            mIsThirdParty = isThirdParty;
        }

        public Boolean getIsPriceReasonable() {
            return mIsPriceReasonable;
        }

        public void setIsPriceReasonable(Boolean isPriceReasonable) {
            mIsPriceReasonable = isPriceReasonable;
        }

        public Integer getRequestId() {
            return mRequestId;
        }

        public void setRequestId(Integer requestId) {
            mRequestId = requestId;
        }

        public String getConvenienceCheckNumber() {
            return mConvenienceCheckNumber;
        }

        public void setConvenienceCheckNumber(String convenienceCheckNumber) {
            mConvenienceCheckNumber = convenienceCheckNumber;
        }

        public Boolean getIsProfessionalOrg() {
            return mIsProfessionalOrg;
        }

        public void setIsProfessionalOrg(Boolean isProfessionalOrg) {
            mIsProfessionalOrg = isProfessionalOrg;
        }

        public Boolean getIsCommercialVendor() {
            return mIsCommercialVendor;
        }

        public void setIsCommercialVendor(Boolean isCommercialVendor) {
            mIsCommercialVendor = isCommercialVendor;
        }

        public String getCommercialVendorJust() {
            return mCommercialVendorJust;
        }

        public void setCommercialVendorJust(String commercialVendorJust) {
            mCommercialVendorJust = commercialVendorJust;
        }
        
        
    }
}
