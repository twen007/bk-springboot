package gov.nist.oism.asd.empbc.v1;

import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/system")
public class SystemService extends SsoService {

    private static final Logger LOG = Logger.getLogger(SystemService.class.getSimpleName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/version")
    public Response getObjectClasses(@Context HttpServletRequest servletRequest) {
        StatusCode statusCode = StatusCode.OK;
         String appVersion = getAppVerison();

        if (appVersion == null) {
            LOG.log(Level.SEVERE, "Version property not found in application.properties");
            statusCode = StatusCode.ResourceNotFound;
            appVersion = "N/A"; // Or handle the error differently
        }

        GetVersionResponse getVersionResponse = new GetVersionResponse();
        getVersionResponse.data = appVersion;
        return Response.ok().entity(serializeResponseWithStatus(getVersionResponse, statusCode)).build();
    }

    public static class GetVersionResponse extends JsonStatus {

        public String data;

    }
}
