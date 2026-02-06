package gov.nist.oism.asd.empbc.v1;

import gov.nist.oism.asd.empbc.db.ProjectTaskDao;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/projectTasks")
public class ProjectTaskService extends SsoService {

    private static final Logger LOG = Logger.getLogger(ProjectTaskService.class.getSimpleName());

    //For the Finance View
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{ou_code}")
    public Response selectProjectTasksForOu(@Context HttpServletRequest servletRequest, @PathParam("ou_code") String ouCode, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ProjectTaskDao dao = new ProjectTaskDao();
        Map<String, Object> results = dao.selectProjectTasksForOu(ouCode);
        StatusCode statusCode = (StatusCode) results.get(ProjectTaskDao.STATUS_CODE_KEY);
        GetPtcsResponse getProjectTasksResponse = new GetPtcsResponse();
        if (statusCode == StatusCode.OK) {
            getProjectTasksResponse.setData((List<Ptc>) results.get(ProjectTaskDao.PROJECT_TASK_LIST_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(getProjectTasksResponse, statusCode)).build();
    }

    //For Request Search view
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allOU")
    public Response selectProjectTasksSearch(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        ProjectTaskDao dao = new ProjectTaskDao();
        Map<String, Object> results = dao.selectProjectTasksSearch(filter);
        StatusCode statusCode = (StatusCode) results.get(ProjectTaskDao.STATUS_CODE_KEY);
        GetPtcsResponse getProjectTasksResponse = new GetPtcsResponse();
        if (statusCode == StatusCode.OK) {
            getProjectTasksResponse.setData((List<Ptc>) results.get(ProjectTaskDao.PROJECT_TASK_LIST_KEY));
        }

        return Response.ok().entity(serializeResponseWithStatus(getProjectTasksResponse, statusCode)).build();
    }

  

    public static class Ptc {

        public String code;
        public String display;
    }

    public static class GetPtcsResponse extends JsonStatus {

        List<Ptc> data;

        public List<Ptc> getData() {
            return data;
        }

        public void setData(List<Ptc> data) {
            this.data = data;
        }
    }
}
