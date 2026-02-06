package gov.nist.oism.asd.empbc.v1;

import gov.nist.oism.asd.empbc.db.ObjectClassDao;
import gov.nist.oism.asd.empbc.model.ObjectClass;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/objectClasses")
public class ObjectClassService extends SsoService {
    
    private static final Logger LOG = Logger.getLogger(ObjectClassService.class.getSimpleName());
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObjectClasses(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        
        ObjectClassDao dao = new ObjectClassDao();
        Map<String, Object> results = dao.selectObjectClassesWithFilter(filter);
        StatusCode statusCode = (StatusCode) results.get(ObjectClassDao.STATUS_CODE_KEY);
        GetObjectClassesResponse getObjectClassesResponse = new GetObjectClassesResponse();
        if (statusCode == StatusCode.OK) {
            List<ObjectClass> objectClasses = (List<ObjectClass>) results.get(ObjectClassDao.OBJECT_CLASS_LIST_KEY);
            List<GetObjectClassesResponse.ObjectClass> dataList = new ArrayList<>();
            objectClasses.stream().map((objectClass) -> {
                GetObjectClassesResponse.ObjectClass data = new GetObjectClassesResponse.ObjectClass();
                data.setCode(objectClass.getCode());
                data.setDescription(objectClass.getDescription());
                
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });
            getObjectClassesResponse.setData(dataList);
        }

        return Response.ok().entity(serializeResponseWithStatus(getObjectClassesResponse, statusCode)).build();
    }
    
    public static class GetObjectClassesResponse extends JsonStatus {

        public static class ObjectClass {

            private String mCode;
            private String mDescription;

            public String getCode() {
                return mCode;
            }

            public void setCode(String code) {
                mCode = code;
            }

            public String getDescription() {
                return mDescription;
            }

            public void setDescription(String description) {
                mDescription = description;
            }
        }
        
        List<GetObjectClassesResponse.ObjectClass> mData;

        public List<GetObjectClassesResponse.ObjectClass> getData() {
            return mData;
        }

        public void setData(List<GetObjectClassesResponse.ObjectClass> data) {
            mData = data;
        }
    }
}
