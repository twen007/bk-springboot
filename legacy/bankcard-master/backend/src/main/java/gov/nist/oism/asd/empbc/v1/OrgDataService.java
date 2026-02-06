package gov.nist.oism.asd.empbc.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.nist.oism.asd.empbc.db.OrgDataDao;
import gov.nist.oism.asd.empbc.model.BchInitial;
import gov.nist.oism.asd.empbc.model.DivisionPreference;
import gov.nist.oism.asd.empbc.model.NistOrg;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.NistOrgData;
import gov.nist.oism.asd.empbc.util.StatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

@Path("/nistOrgs")
public class OrgDataService extends SsoService {

    private static final Logger LOG = Logger.getLogger(OrgDataService.class.getSimpleName());

    private Response getOrgData(HttpServletRequest servletRequest, Function<OrgDataDao, Map<String, Object>> daoMethod) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = daoMethod.apply(dao);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetOrgDataResponse getOrgDataResponse = new GetOrgDataResponse();
        if (statusCode == StatusCode.OK) {
            List<NistOrg> nistOrgs = (List<NistOrg>) results.get(OrgDataDao.NIST_ORG_LIST_KEY);
            List<NistOrgData> dataList = new ArrayList<>();
            nistOrgs.stream().map((nistOrg) -> {
                NistOrgData nistOrgData = new NistOrgData();
                nistOrgData.setOuId(nistOrg.getOuId());
                nistOrgData.setDivisionId(nistOrg.getDivisionId());
                nistOrgData.setGroupId(nistOrg.getGroupId());
                nistOrgData.setCode(nistOrg.getCode());
                nistOrgData.setAcronym(nistOrg.getAcronym());
                nistOrgData.setName(nistOrg.getName());
                nistOrgData.setShortName(nistOrg.getShortName());
                return nistOrgData;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getOrgDataResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getOrgDataResponse, statusCode)).build();
    }

    @GET
    @Path("/ou")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOus(@Context HttpServletRequest servletRequest) {
        return getOrgData(servletRequest, OrgDataDao::selectOrganizations);
    }

    @GET
    @Path("/division")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDivisions(@Context HttpServletRequest servletRequest) {
        return getOrgData(servletRequest, OrgDataDao::selectDivisions);
    }

    @GET
    @Path("/group")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroups(@Context HttpServletRequest servletRequest) {
        return getOrgData(servletRequest, OrgDataDao::selectGroups);
    }

    //BANK-488
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getDivPrefs/{divId}")
    public Response getDivPrefs(@Context HttpServletRequest servletRequest, @PathParam("divId") Integer divId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        //if no div passed in, just return all. since we are dealing issues like one div staff create or approve request for another div or even ou 
        //sometimes, it's easier to get all divs prefs and client app can find the right one to use whenever needed
        //client can pass a negative number to indicate to get all (divId <= 0)
        if (divId == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.IncompleteData)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = dao.getDivPrefs(divId);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetDivPrefsResponse getDivPrefsResponse = new GetDivPrefsResponse();
        if (statusCode == StatusCode.OK) {
            List<DivisionPreference> dataList = (List<DivisionPreference>) results.get(OrgDataDao.NIST_ORG_LIST_KEY);
            getDivPrefsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getDivPrefsResponse, statusCode)).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/setDivPrefs")
    public Response postDivPrefs(@Context HttpServletRequest request, PostDivPref postDivPref) {

        User authenticatedUser = getSsoAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        if (postDivPref == null || postDivPref.getDivId() < 1) {
            LOG.info("incomplete division preference data");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.IncompleteData)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = dao.setDivPrefs(postDivPref);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetDivPrefsResponse getDivPrefsResponse = new GetDivPrefsResponse();

        return Response.ok().entity(serializeResponseWithStatus(getDivPrefsResponse, statusCode)).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostDivPref extends DivisionPreference {
    }

    //BANK-501
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getBchInitPrefs/{divId}")
    public Response getBchInitPrefs(@Context HttpServletRequest servletRequest, @PathParam("divId") Integer divId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        if (divId == null || divId < 1) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.IncompleteData)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = dao.getBchInitPrefs(divId);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetBchInitPrefsResponse getBchInitPrefsResponse = new GetBchInitPrefsResponse();
        if (statusCode == StatusCode.OK) {
            List<BchInitial> dataList = (List<BchInitial>) results.get(OrgDataDao.NIST_ORG_LIST_KEY);
            getBchInitPrefsResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getBchInitPrefsResponse, statusCode)).build();

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/setBchInitPrefs")
    public Response postBchInitPrefs(@Context HttpServletRequest request, PostBchInitPref postBchInitPref) {

        User authenticatedUser = getSsoAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        if (postBchInitPref == null || postBchInitPref.getDivId() < 1) {
            LOG.info("incomplete division preference data");
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.IncompleteData)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = dao.setBchInitPrefs(postBchInitPref);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetBchInitPrefsResponse getBchInitPrefsResponse = new GetBchInitPrefsResponse();

        return Response.ok().entity(serializeResponseWithStatus(getBchInitPrefsResponse, statusCode)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/bchInitPrefs/{id}")
    public Response deleteSharedVendor(@Context HttpServletRequest servletRequest, @PathParam("id") Integer id) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        OrgDataDao dao = new OrgDataDao();
        Map<String, Object> results = dao.deleteBchInitPref(id);
        StatusCode statusCode = (StatusCode) results.get(OrgDataDao.STATUS_CODE_KEY);
        GetBchInitPrefsResponse getBchInitPrefsResponse = new GetBchInitPrefsResponse();

        return Response.ok().entity(serializeResponseWithStatus(getBchInitPrefsResponse, statusCode)).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PostBchInitPref extends BchInitial {
    }

    public static class GetOrgDataResponse extends JsonStatus {

        @Getter
        @Setter
        private List<NistOrgData> data;
    }

    public static class GetDivPrefsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<DivisionPreference> data;
    }

    public static class GetBchInitPrefsResponse extends JsonStatus {

        @Getter
        @Setter
        private List<BchInitial> data;

    }

}
