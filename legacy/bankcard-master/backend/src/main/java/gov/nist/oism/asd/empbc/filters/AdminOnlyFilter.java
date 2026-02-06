package gov.nist.oism.asd.empbc.filters;

import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.security.annotations.AdminOnly;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.v1.SsoService;
import static gov.nist.oism.asd.empbc.v1.SsoService.serializeStatus;
import java.io.IOException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
/**
 *
 * @author xinweiw
 */

@Provider
@AdminOnly
public class AdminOnlyFilter extends SsoService implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AdminOnlyFilter.class.getName());

    @Context
    private HttpServletRequest servletRequest;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (!authenticatedUser.getAccessAdmin()) {
            LOG.log(Level.INFO, "User with Id {0} is unauthorized to access admin function", authenticatedUser.getPeopleId());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build());
        }
    }
}