package gov.nist.oism.asd.empbc.v1;

import gov.nist.oism.asd.empbc.db.DetailedUserDao;
import gov.nist.oism.asd.empbc.model.DetailedUser;
import gov.nist.oism.asd.empbc.security.annotations.AdminOnly;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author xinweiw
 */
@Path("/detailedusers")
public class DetailedUserService extends SsoService {

    private static final Logger LOG = Logger.getLogger(DetailedUserService.class.getSimpleName());
    private final DetailedUserDao detailedUserDao;

    public DetailedUserService() {
        this.detailedUserDao = new DetailedUserDao();
    }

    public ApiResponse<List<DetailedUser>> getAllUsers() {
       return executeDbOperation(() -> {
            List<DetailedUser> users = detailedUserDao.getAllUsers();
            return new ApiResponse<>(users, null);
         });
    }

    public ApiResponse<DetailedUser> getUserById(Integer id) {
        return executeDbOperation(() -> {
            DetailedUser user = detailedUserDao.getUserDetailed(id);
            if (user != null) {
                return new ApiResponse<>(user, null);
            } else {
                LOG.log(Level.INFO, "cannot find detailed user with id: {0}", id);
                return new ApiResponse<>(null, StatusCode.RecordNotFound);
            }
        });
    }

    public ApiResponse<Void> createUser(DetailedUser user) {
        return executeDbOperation(() -> {
            detailedUserDao.insertUserDetailed(user);
            return new ApiResponse<>(null, null);
        });
    }

    public ApiResponse<Void> updateUser(DetailedUser user) {
        return executeDbOperation(() -> {
            detailedUserDao.updateUserDetailed(user);
            return new ApiResponse<>(null, null);
        });
    }

    public ApiResponse<Void> deleteUser(Integer id) {
        return executeDbOperation(() -> {
            detailedUserDao.deleteUserDetailed(id);
            return new ApiResponse<>(null, null);
        });
    }

    @AdminOnly
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDetailedUsers(@Context HttpServletRequest servletRequest) {
        ApiResponse<List<DetailedUser>> response = this.getAllUsers();
        return Response.ok(response).build();
    }

    @AdminOnly
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDetailedUserById(@PathParam("id") Integer id) {
        ApiResponse<DetailedUser> response = this.getUserById(id);
        return Response.ok(response).build();
    }

    @AdminOnly
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDetailedUser(DetailedUser user) {
        ApiResponse<Void> response = this.createUser(user);
        return Response.status(response.isSuccess() ? Response.Status.CREATED : Response.Status.BAD_REQUEST)
                .entity(response)
                .build();
    }

    @AdminOnly
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDetailedUser(DetailedUser user) {
        ApiResponse<Void> response = this.updateUser(user);
        return Response.ok(response).build();
    }

    @AdminOnly
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDetailedUser(@PathParam("id") Integer id) {
        ApiResponse<Void> response = this.deleteUser(id);
        return Response.ok(response).build();
    }
}
