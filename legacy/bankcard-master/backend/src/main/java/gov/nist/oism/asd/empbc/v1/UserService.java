package gov.nist.oism.asd.empbc.v1;

import com.fasterxml.jackson.annotation.JsonFormat;

import gov.nist.oism.asd.empbc.db.UserDao;
import gov.nist.oism.asd.empbc.db.UserPrefsDao;
import gov.nist.oism.asd.empbc.model.UserPrivileges;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.UserDetailedPrivilege;
import gov.nist.oism.asd.empbc.util.NistOrgWSCalls;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import gov.nist.oism.asd.empbc.model.UserPrefs;
import gov.nist.oism.asd.empbc.util.ApiUtil;
import gov.nist.oism.asd.empbc.util.CacheManager;
import gov.nist.oism.asd.empbc.util.StatusCodeException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Path("/users")
public class UserService extends SsoService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getSimpleName());

    // Helper method to create a list of OuMember objects from a list of User objects
    private EmpMidFormat createUserMidFormatCopyFrom(User user) {

        EmpMidFormat data = new EmpMidFormat();
        if (user != null) {
            data.setStaffType(user.getStaffType());
            data.setPeopleId(user.getPeopleId());
            data.setFullName(user.toFullName());
            data.setEmpEmail(user.getEmail());
            data.setActive(user.getActive());
            data.setOuId(user.getOuId());
            data.setDivisionId(user.getDivisionId());
            data.setGroupId(user.getGroupId());
        }
        return data;

    }

    /**
     * all NIST staff (employee and associate) from the OU of the logged in user
     * @param servletRequest
     * @param filter
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ouMembers")
    public Response getOuMembers(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        //Map<String, Object> results = dao.selectUsersInOuByPeopleId(authenticatedUser.getPeopleId(), filter);
        Map<String, Object> results = dao.selectUsersInOuByPeopleId(getDetaileePeopleIdWhenApply(servletRequest), filter);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetOuMembersResponse getOuMembersResponse = new GetOuMembersResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<EmpMidFormat> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                return createUserMidFormatCopyFrom(user);
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });
            getOuMembersResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getOuMembersResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/supervisor/{requestedForId}")
    public Response getSupervisorProfile(@Context HttpServletRequest servletRequest, @PathParam("requestedForId") Integer requestedForId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        StatusCode statusCode = StatusCode.OK;
        GetProfileResponse getProfileResponse = new GetProfileResponse();

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectUserByPeopleId(requestedForId);
        User associateUser = (User) results.get(UserDao.USER_KEY);
        results = dao.selectUserByPeopleId(associateUser.getBossId());
        User supervisorUser = (User) results.get(UserDao.USER_KEY);
        GetProfileResponse.User data = new GetProfileResponse.User();

        data.setProfile(userToProfile(supervisorUser));
        getProfileResponse.setData(data);

        return Response.ok().entity(serializeResponseWithStatus(getProfileResponse, statusCode)).build();
    }

    /**
     * not used for now
     *
     * @param servletRequest
     * @param requestedForId peopleId
     * @return reviewer,bao and bch for the user with that peopleId
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{requestedForId}/defaultRoute")
    public Response getUserDefaultRoute(@Context HttpServletRequest servletRequest, @PathParam("requestedForId") Integer requestedForId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        GetDefaultRouteResponse getRouteResponse = new GetDefaultRouteResponse();
        StatusCode statusCode = StatusCode.OK;

        Route defaultRoute = this.getDefaultRoute(servletRequest, requestedForId);
        if (defaultRoute == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        getRouteResponse.defaultRoute = defaultRoute;
        return Response.ok().entity(serializeResponseWithStatus(getRouteResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logout")
    public Response logoutBankcard(@Context HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();
        StatusCode statusCode = StatusCode.OK;
        String sessionId = session.getId();

        try {
            session.invalidate();
            servletRequest.logout();
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            statusCode = StatusCode.BadRequest;
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(statusCode)).build();
        }
        return Response.status(Response.Status.OK).entity(serializeStatus(statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public Response getProfile(@Context HttpServletRequest servletRequest) {
        StatusCode statusCode = StatusCode.OK;

        statusCode = checkDbStatus();
        if (statusCode != StatusCode.OK) {
            return Response.status(Response.Status.OK).entity(serializeStatus(statusCode)).build();
        }

        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        GetProfileResponse getProfileResponse = new GetProfileResponse();

        //determin url used for getting user roles
        String employeeProfileWsUrl = ApiUtil.getMmlEmployeeRolesUrl(authenticatedUser.getPeopleId());

        //get user roles from NIST Org
        List<Role> dataRoles = new ArrayList<>();
        try {
            dataRoles = CacheManager.getInstance().getUserRoleCache().get(employeeProfileWsUrl);
        } catch (Exception e) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }

        //get default routes from NIST Org
        Route defaultRoute;

        //sampled detailee
        GetProfileResponse.User.Detailee detailee = new GetProfileResponse.User.Detailee();
        //if detailee mode is enabled
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            authenticatedUser.setDetaileeMode(Boolean.TRUE);
            User sampleDetaileeUser;
            try {
                //save true user data
                try {
                    sampleDetaileeUser = getSampleUserByGroupId((Integer) session.getAttribute(DETAILEE_GROUP));
                } catch (StatusCodeException ex) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(StatusCode.DatabaseError)).build();
                }

                detailee.setPeopleId(sampleDetaileeUser.getPeopleId());
                detailee.setOuId(sampleDetaileeUser.getOuId());
                detailee.setDivisionId(sampleDetaileeUser.getDivisionId());
                detailee.setGroupId(sampleDetaileeUser.getGroupId());
                detailee.setOuCode(sampleDetaileeUser.getOuCode());
                detailee.setDivisionCode(sampleDetaileeUser.getDivisionCode());
                detailee.setGroupCode(sampleDetaileeUser.getGroupCode());

            } catch (Exception e) {
                return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.DetaileeError)).build();
            }
            defaultRoute = this.getDefaultRoute(servletRequest, getDetaileePeopleIdWhenApply(servletRequest));
        } else {
            authenticatedUser.setDetaileeMode(Boolean.FALSE);
            defaultRoute = this.getDefaultRoute(servletRequest, authenticatedUser.getPeopleId());
        }

        //get default routes from NIST Org
        //Route defaultRoute = this.getDefaultRoute(servletRequest, authenticatedUser.getPeopleId());
        //if (defaultRoute == null) {
        //    return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        //}
        //Retrieve delegations MB-474
        GetProfileResponse.User.Delegation delegation = new GetProfileResponse.User.Delegation();

        if (session.getAttribute(DELEGATING_USERNAME)
                != null) {
            delegation.setDelegating(Boolean.TRUE);
            authenticatedUser.setIsDelegating(Boolean.TRUE);

            String trueUsername = (String) session.getAttribute(DELEGATING_TRUE_USER);

            User trueUser;
            try {
                //save true user data
                trueUser = getUserByUsername(trueUsername);
                GetProfileResponse.User.Delegation.DelegationUser trueUserInfo = new GetProfileResponse.User.Delegation.DelegationUser();
                trueUserInfo.setUsername(trueUser.getUsername());
                trueUserInfo.setFirstName(trueUser.getFirstName());
                trueUserInfo.setLastName(trueUser.getLastName());
                trueUserInfo.setMiddleName(trueUser.getMiddleName());
                delegation.setTrueUser(trueUserInfo);
            } catch (Exception e) {
                return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.DelegationError)).build();
            }

        } else {
            List<GetProfileResponse.User.Delegation.DelegationUser> delegationUsers = getDelegationByUsername(servletRequest, authenticatedUser.getUsername());
            delegation.setDelegating(Boolean.FALSE);
            authenticatedUser.setIsDelegating(Boolean.FALSE);
            delegation.setDelegateAs(delegationUsers);
        }

        //update user provilege table based on user roles
        UserPrivileges userPrivileges = new UserPrivileges();

        //MB-465 everyone has div access in search
        userPrivileges.setAccessGroup(Boolean.TRUE);

        userPrivileges.setAccessDiv(Boolean.TRUE);

        for (int i = 0;
                i < dataRoles.size();
                i++) {
            Role role = dataRoles.get(i);
            //this method set who can access what
            setUserPrivilege(userPrivileges, role.getName());
        }

        // Insert the permissions into the DB table
        UserDao dao = new UserDao();

        userPrivileges.setUsername(authenticatedUser.getUsername());
        statusCode = dao.inserPrivileges(userPrivileges);

        if (statusCode != StatusCode.OK) {
            return Response.ok().entity(serializeResponseWithStatus(getProfileResponse, statusCode)).build();
        }
        List<UserDetailedPrivilege> details;
        List<GetProfileResponse.User.UserDetailedPrivilege> dataList = new ArrayList<>();

        try {
            details = getUserDetailedPrivilegeById(authenticatedUser.getPeopleId());

            details.stream().map((udp) -> {
                GetProfileResponse.User.UserDetailedPrivilege data = new GetProfileResponse.User.UserDetailedPrivilege();
                data.setOuId(udp.getOuId());
                data.setPeopleId(udp.getPeopleId());
                data.setDivisionId(udp.getDivisionId());
                data.setGroupId(udp.getGroupId());
                data.setAccessOu(udp.getAccessOu());
                data.setAccessDiv(udp.getAccessDiv());
                data.setAccessGroup(udp.getAccessGroup());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });
        } catch (Exception e) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.DetailedPrivilegeError)).build();
        }

        //set all data to response before returning
        GetProfileResponse.User data = new GetProfileResponse.User();

        data.setProfile(userToProfile(authenticatedUser));
        data.setRoles(dataRoles);

        data.setDefaultRoute(defaultRoute);

        data.setDelegation(delegation);

        data.setDetailee(detailee);

        data.setDetailedPrivilege(dataList);

        getProfileResponse.setData(data);

        return Response.ok()
                .entity(serializeResponseWithStatus(getProfileResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/roles/{peopleId}")
    /**
     * get only the user roles using a user's peopleId
     */
    public Response getRolesByUserId(@Context HttpServletRequest servletRequest, @PathParam("peopleId") Integer peopleId) {
        StatusCode statusCode = StatusCode.OK;
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);

        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        GetUserRolesResponse getUserRolesResponse = new GetUserRolesResponse();
        //determin url used for getting user roles
        String employeeProfileWsUrl = ApiUtil.getMmlEmployeeRolesUrl(peopleId);

        //get user roles from NIST Org
        List<Role> dataRoles = new ArrayList<>();
        try {
            dataRoles = getUserRoles(employeeProfileWsUrl);
        } catch (Exception e) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        //set all data to response before returning

        getUserRolesResponse.setData(dataRoles);
        return Response.ok().entity(serializeResponseWithStatus(getUserRolesResponse, statusCode)).build();

    }

    /**
     *
     * @param username
     * @return
     * @throws StatusCodeException
     */
    public User getUserByUsername(String username) throws StatusCodeException {
        if (username == null || username.equals("")) {
            throw new StatusCodeException(StatusCode.DataIntegrityError.getCode(),
                    "getUserByUsername: username is null or empty");
        }

        User user;
        try {
            user = CacheManager.getInstance().getUserCache().get(username);
        } catch (ExecutionException ex) {
            throw new StatusCodeException(StatusCode.CacheManagementError.getCode(),
                    "Cannot find user name " + username);
        }
        return user;
    }

    /**
     * user from other OUs maybe granted access to see or do approvals for
     * requests belong to a OU. we store that info in the user_detailed table
     *
     * @param peopleId
     * @return
     * @throws StatusCodeException
     */
    public List getUserDetailedPrivilegeById(Integer peopleId) throws StatusCodeException {
        if (peopleId == null || peopleId == 0) {
            throw new StatusCodeException(StatusCode.DataIntegrityError.getCode(),
                    "getDetailedPrivilegeById: peopleId is null or 0");
        }

        List<UserDetailedPrivilege> details = new ArrayList<>();
        try {
            details = CacheManager.getInstance().getUserDetailedCache().get(peopleId);
        } catch (ExecutionException ex) {
            throw new StatusCodeException(StatusCode.CacheManagementError.getCode(),
                    "Cannot find userdetailed" + peopleId);
        }
        return details;
    }

    public List getUserDetailedPrivilegeByIdFromDatabase(Integer peopleId) throws StatusCodeException {
        UserDao dao = new UserDao();
        List<UserDetailedPrivilege> details = new ArrayList<>();
        Map<String, Object> results = dao.selectUserDetailedById(peopleId);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            details = (List<UserDetailedPrivilege>) results.get(UserDao.USER_LIST_KEY);
        } else {
            throw new StatusCodeException(StatusCode.DatabaseError);
        }
        return details;
    }

    public User getUserByUsernameFromDatabase(String username) throws StatusCodeException {
        UserDao dao = new UserDao();
        User user;
        Map<String, Object> results = dao.selectUserByUsername(username);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            user = (User) results.get(UserDao.USER_KEY);
            if (dao.isAdminUser(user.getUsername())) {
                user.setAccessAdmin(Boolean.TRUE);
            } else {
                user.setAccessAdmin(Boolean.FALSE);
            }
        } else {
            throw new StatusCodeException(StatusCode.DatabaseError);
        }
        return user;
    }

    /**
     * get NIST Org roles for a employee
     *
     * @param url
     * @return
     * @throws StatusCodeException
     */
    public List getUserRoles(String url) throws StatusCodeException {
        List<Role> dataRoles = new ArrayList<>();
        UserPrivileges userPrivileges = new UserPrivileges();
        NistOrgWSCalls.MmlEmployeeProfileCall mmlEmployeeProfileCall = null;
        try {
            mmlEmployeeProfileCall = NistOrgWSCalls.callMmlEmployeeProfileService(url);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            throw new StatusCodeException(StatusCode.NistOrgServiceCallFailed);
        }
        if (mmlEmployeeProfileCall == null) {
            throw new StatusCodeException(StatusCode.NistOrgServiceCallFailed);
        } else if ("success".equals(mmlEmployeeProfileCall.returnMessage)) {

            //MB-465 everyone has div access in search
            userPrivileges.setAccessGroup(Boolean.TRUE);
            userPrivileges.setAccessDiv(Boolean.TRUE);

            //go through roles
            mmlEmployeeProfileCall.roles.stream().map((mmlEmployeeProfileRole) -> {
                Role dataRole = new Role();
                dataRole.setId(mmlEmployeeProfileRole.id);
                dataRole.setName(mmlEmployeeProfileRole.name);
                dataRole.setCategory(mmlEmployeeProfileRole.category);
                return dataRole;
            }).forEachOrdered((dataRole) -> {
                dataRoles.add(dataRole);
            });
        }
        return dataRoles;
    }

    /**
     * all NIST employee from the OU of the logged in user
     * @param servletRequest
     * @param filter
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nistEmployees")
    public Response getNistEmployeesInOu(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectNistEmployeesInOu(authenticatedUser.getOuId(), filter);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetNistEmployeesResponse getNistEmployeesResponse = new GetNistEmployeesResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<NistEmployee> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                NistEmployee data = new NistEmployee();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                data.setEmpEmail(user.getEmail());
                data.setActive(user.getActive());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getNistEmployeesResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getNistEmployeesResponse, statusCode)).build();
    }
    
    /**
     * all NIST employee, no associates
     * @param servletRequest
     * @param filter - for filtering by employee name
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nistEmployeesAll")
    public Response getNistEmployees(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectNistEmployees(filter);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetNistEmployeesResponse getNistEmployeesResponse = new GetNistEmployeesResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<NistEmployee> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                NistEmployee data = new NistEmployee();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                data.setEmpEmail(user.getEmail());
                data.setActive(user.getActive());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getNistEmployeesResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getNistEmployeesResponse, statusCode)).build();
    }

    /**
     * all NIST employee from the division of the logged in user
     * @param servletRequest
     * @return 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/divEmployees")
    public Response getNistEmployeesInDiv(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectNistEmployeesInDiv(authenticatedUser.getDivisionId());
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetNistEmployeesResponse getNistEmployeesResponse = new GetNistEmployeesResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<NistEmployee> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                NistEmployee data = new NistEmployee();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                data.setEmpEmail(user.getEmail());
                data.setActive(user.getActive());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getNistEmployeesResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getNistEmployeesResponse, statusCode)).build();
    }

    // The logic of get a list of supervisors in CPR as reviewers is still valid.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/reviewers")
    public Response getReviewers(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results;
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            results = dao.selectReviewersInDivision((Integer) session.getAttribute(DETAILEE_OU), filter);
        } else {
            results = dao.selectReviewersInDivision(authenticatedUser.getOuId(), filter);
        }
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetReviewersResponse getReviewersResponse = new GetReviewersResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<Reviewer> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                Reviewer data = new Reviewer();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                data.setActive(user.getActive());
                data.setBossId(user.getBossId());
                data.setDivId(user.getDivisionId());
                return data;
            }).forEachOrdered((reviewer) -> {
                dataList.add(reviewer);
            });

            getReviewersResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getReviewersResponse, statusCode)).build();
    }

    //NOTE: we get BCHs, BAOs from NIST ORG. If a BCH is no longer an active NIST employee, we will not get the person from the list
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/bankcardHolders")
    public Response getBankcardHolders(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetApproversResponse getBankcardHoldersResponse = new GetApproversResponse();
        List<EmpShortFormat> bankcardHolders = new ArrayList<>();
        String divCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            divCode = (String) session.getAttribute(DETAILEE_DIV_CODE);
        } else {
            divCode = authenticatedUser.getDivisionCode();
        }

        String bankcardHoldersWsUrl = ApiUtil.getMmlBankcardHoldersUrl(divCode);
        NistOrgWSCalls.MmlBankcardHolderCall mmlBankcardHolderCall = null;
        try {
            mmlBankcardHolderCall = NistOrgWSCalls.callMmlBankcardHolderService(bankcardHoldersWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        if (mmlBankcardHolderCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(mmlBankcardHolderCall.returnMessage)) {
            mmlBankcardHolderCall.bankcard_holder.possible_holders.stream().map((possibleHolder) -> {
                EmpShortFormat bankcardHolder = new EmpShortFormat();
                bankcardHolder.setPeopleId(possibleHolder.employee_number);
                bankcardHolder.setFullName(firstAndLastNameToFullName(possibleHolder.first_name, possibleHolder.last_name));
                return bankcardHolder;
            }).forEachOrdered((bankcardHolder) -> {
                bankcardHolders.add(bankcardHolder);
            });
            mmlBankcardHolderCall.bankcard_holder.current_holders.stream().map((currentHolders) -> {
                EmpShortFormat bankcardHolder = new EmpShortFormat();
                bankcardHolder.setPeopleId(currentHolders.employee_number);
                bankcardHolder.setFullName(firstAndLastNameToFullName(currentHolders.first_name, currentHolders.last_name));
                return bankcardHolder;
            }).forEachOrdered((bankcardHolder) -> {
                bankcardHolders.add(bankcardHolder);
            });

            getBankcardHoldersResponse.setData(bankcardHolders);
        }

        return Response.ok().entity(serializeResponseWithStatus(getBankcardHoldersResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/bankcardApprovingOfficials")
    public Response getBankcardApprovingOfficials(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetApproversResponse getBankcardApprovingOfficialsResponse = new GetApproversResponse();
        List<EmpShortFormat> bankcardApprovingOfficials = new ArrayList<>();
        String divCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            divCode = (String) session.getAttribute(DETAILEE_DIV_CODE);
        } else {
            divCode = authenticatedUser.getDivisionCode();
        }

        String bankcardApprovingOfficialWsUrl = ApiUtil.getMmlBankcardApprovingOfficialUrl(divCode);
        NistOrgWSCalls.MmlBankcardApprovingOfficialCall mmlBankcardApprovingOfficialCall = null;
        try {
            mmlBankcardApprovingOfficialCall = NistOrgWSCalls.callMmlBankcardApprovingOfficialService(bankcardApprovingOfficialWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        if (mmlBankcardApprovingOfficialCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(mmlBankcardApprovingOfficialCall.returnMessage)) {
            mmlBankcardApprovingOfficialCall.bankcard_approving_official.possible_holders.stream().map((possibleHolder) -> {
                EmpShortFormat bankcardApprovingOfficial = new EmpShortFormat();
                bankcardApprovingOfficial.setPeopleId(possibleHolder.employee_number);
                bankcardApprovingOfficial.setFullName(firstAndLastNameToFullName(possibleHolder.first_name, possibleHolder.last_name));
                return bankcardApprovingOfficial;
            }).forEachOrdered((bankcardApprovingOfficial) -> {
                bankcardApprovingOfficials.add(bankcardApprovingOfficial);
            });
            
             mmlBankcardApprovingOfficialCall.bankcard_approving_official.current_holders.stream().map((currentHolders) -> {
                EmpShortFormat bankcardApprovingOfficial = new EmpShortFormat();
                bankcardApprovingOfficial.setPeopleId(currentHolders.employee_number);
                bankcardApprovingOfficial.setFullName(firstAndLastNameToFullName(currentHolders.first_name, currentHolders.last_name));
                return bankcardApprovingOfficial;
            }).forEachOrdered((bankcardApprovingOfficial) -> {
                bankcardApprovingOfficials.add(bankcardApprovingOfficial);
            });

            getBankcardApprovingOfficialsResponse.setData(bankcardApprovingOfficials);
        }

        return Response.ok().entity(serializeResponseWithStatus(getBankcardApprovingOfficialsResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/fundsCertifyingOfficials")
    public Response getFundsCertifyingOfficials(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetApproversResponse getFundsCertifyingOfficialsResponse = new GetApproversResponse();
        List<EmpShortFormat> fundsCertifyingOfficials = new ArrayList<>();
        String divCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            divCode = (String) session.getAttribute(DETAILEE_DIV_CODE);
        } else {
            divCode = authenticatedUser.getDivisionCode();
        }

        String fundsCertifyingOfficialsWsUrl = ApiUtil.getMmlFundsCertifyingOfficialUrl(divCode);
        NistOrgWSCalls.MmlFundsCertifyingOfficialCall mmlFundsCertifyingOfficialsCall = null;
        try {
            mmlFundsCertifyingOfficialsCall = NistOrgWSCalls.callFundsCertifyingOfficialService(fundsCertifyingOfficialsWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        if (mmlFundsCertifyingOfficialsCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(mmlFundsCertifyingOfficialsCall.returnMessage)) {
            if (mmlFundsCertifyingOfficialsCall.funds_Certifying_Official.current_holders != null) {
                mmlFundsCertifyingOfficialsCall.funds_Certifying_Official.current_holders.stream().map((current_holders) -> {
                    EmpShortFormat fundsCertifyingOfficial = new EmpShortFormat();
                    fundsCertifyingOfficial.setPeopleId(current_holders.employee_number);
                    fundsCertifyingOfficial.setFullName(firstAndLastNameToFullName(current_holders.first_name, current_holders.last_name));
                    return fundsCertifyingOfficial;
                }).forEachOrdered((fundsCertifyingOfficial) -> {
                    fundsCertifyingOfficials.add(fundsCertifyingOfficial);
                });
            }
            getFundsCertifyingOfficialsResponse.setData(fundsCertifyingOfficials);
        }

        return Response.ok().entity(serializeResponseWithStatus(getFundsCertifyingOfficialsResponse, statusCode)).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/ouFundsCertifyingOfficials")
    public Response getAllFundsCertifyingOfficialsInOu(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetApproversResponse getFundsCertifyingOfficialsResponse = new GetApproversResponse();
        List<EmpShortFormat> fundsCertifyingOfficials = new ArrayList<>();
        String divCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            divCode = (String) session.getAttribute(DETAILEE_DIV_CODE);
        } else {
            divCode = authenticatedUser.getDivisionCode();
        }

        String fundsCertifyingOfficialsWsUrl = ApiUtil.getMmlFundsCertifyingOfficialUrl(divCode);
        NistOrgWSCalls.MmlFundsCertifyingOfficialCall mmlFundsCertifyingOfficialsCall = null;
        try {
            mmlFundsCertifyingOfficialsCall = NistOrgWSCalls.callFundsCertifyingOfficialService(fundsCertifyingOfficialsWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
        if (mmlFundsCertifyingOfficialsCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(mmlFundsCertifyingOfficialsCall.returnMessage)) {
            if (mmlFundsCertifyingOfficialsCall.funds_Certifying_Official.possible_holders != null) {
                mmlFundsCertifyingOfficialsCall.funds_Certifying_Official.possible_holders.stream().map((possible_holders) -> {
                    EmpShortFormat fundsCertifyingOfficial = new EmpShortFormat();
                    fundsCertifyingOfficial.setPeopleId(possible_holders.employee_number);
                    fundsCertifyingOfficial.setFullName(firstAndLastNameToFullName(possible_holders.first_name, possible_holders.last_name));
                    return fundsCertifyingOfficial;
                }).forEachOrdered((fundsCertifyingOfficial) -> {
                    fundsCertifyingOfficials.add(fundsCertifyingOfficial);
                });
            }
            getFundsCertifyingOfficialsResponse.setData(fundsCertifyingOfficials);
        }

        return Response.ok().entity(serializeResponseWithStatus(getFundsCertifyingOfficialsResponse, statusCode)).build();
    }

    /**
     * API calls to get division chief data based on division of the logged user
     * or user in detailee mode
     *
     * @param servletRequest
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/divisionChiefs")
    public Response getDivisionChief(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        String divCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            divCode = (String) session.getAttribute(DETAILEE_DIV_CODE); 
        } else {
            divCode = authenticatedUser.getDivisionCode();
        }

        return getDivisionChiefByDivisionCode(servletRequest, divCode);

    }
    
    /**
     * return a list of itsos based on OU Code
     *
     * @param ouCode
     * @return
     */
    public List<Itso> getItsoHolders(String ouCode) {
        List<Itso> itsos = null;
        Map<Integer, Itso> itsoMap = new HashMap<Integer, Itso>();
        NistOrgWSCalls.itsoCall itsoCall = null;
        String itsoWsUrl = ApiUtil.getMmlOuItsoUrl(ouCode);

        try {
            itsoCall = NistOrgWSCalls.callItsoService(itsoWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return null;
        }
        //use id, not peopleId here becuase possible holders can have the same staff in current holder
        int id = 0;
        int ditsoPeopleId = 0;
        if ("success".equals(itsoCall.returnMessage)) {
            itsos = new ArrayList<>();
            if (itsoCall.roles != null) {
                if (!itsoCall.roles.isEmpty()) {

                    for (int i = 0; i < itsoCall.roles.get(0).current_holder.size(); i++) {
                        Itso itso = new Itso();
                        NistOrgWSCalls.itsoCall.Employee emp = itsoCall.roles.get(0).current_holder.get(i);
                        id++;
                        itso.setPeopleId(emp.employee_number);
                        ditsoPeopleId = emp.employee_number;
                        itso.setFullName(firstAndLastNameToFullName(emp.first_name, emp.last_name));
                        itso.setActive(true);//nist org doesn't return this for current_holders, so just set it to true
                        itso.setId(id);
                        //BANK-557 since multiple itso is allowed, we use the first one as the OU ITSO
                        itso.setDitso(id == 1);//current_holders is the division itso (Note: changed to use OU ITSO)
                        itsos.add(itso);
                        //used for checking duplicates later
                        itsoMap.put((Integer) ditsoPeopleId, itso);
                    }

                    for (int i = 0; i < itsoCall.roles.get(0).possible_holders.size(); i++) {
                        Itso itso = new Itso();
                        NistOrgWSCalls.itsoCall.Employee emp = itsoCall.roles.get(0).possible_holders.get(i);
                        if (itsoMap.get((Integer) emp.employee_number) != null) {
                            //skip the possible duplicate (the current holder) in the possible holders
                            continue;
                        }
                        id++;
                        itso.setPeopleId(emp.employee_number);
                        itso.setFullName(firstAndLastNameToFullName(emp.first_name, emp.last_name));
                        itso.setActive(emp.active);
                        itso.setId(id);
                        itso.setDitso(false); //possible holders are staff with the DITSO role but are not assigned to this division
                        itsos.add(itso);
                    }
                }
            } else {
                return null;
            }

        }
        return itsos;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/itsos")
    public Response getItsos(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetItsoResponse getItsoResponse = new GetItsoResponse();
        List<Itso> itsos = null;
        itsos = getItsoHolders(authenticatedUser.getOuCode());
        if (itsos != null) {
            getItsoResponse.setData(itsos);
            return Response.ok().entity(serializeResponseWithStatus(getItsoResponse, statusCode)).build();
        } else {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/ouRoles")
    public Response getOuRoles(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        HttpSession session = servletRequest.getSession();
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        StatusCode statusCode = StatusCode.OK;
        GetOuRolesResponse getOuRolesResponse = new GetOuRolesResponse();
        List<OuRole> roles = new ArrayList<>();
        String ouCode = "";
        //detailee mode
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            ouCode = (String) session.getAttribute(DETAILEE_OU_CODE);
        } else {
            ouCode = authenticatedUser.getOuCode();
        }
        //get all OU roles in one request and take the roles needed by the bankcard app
        String ouRolesWsUrl = ApiUtil.getMmlOuRolesUrl(ouCode);
        NistOrgWSCalls.OuRolesCall ouRolesCall = null;
        try {
            ouRolesCall = NistOrgWSCalls.callOuRolesService(ouRolesWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }

        if (ouRolesCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(ouRolesCall.returnMessage)) {

            if (ouRolesCall.roles != null) {
                if (!ouRolesCall.roles.isEmpty()) {

                    for (int i = 0; i < ouRolesCall.roles.size(); i++) {
                        String roleName = ouRolesCall.roles.get(i).role.name;
                        //for now, we only use Executive Officer as reviewer for DC request approval but get these roles in case we need them
                        if (roleName.equalsIgnoreCase("Director") || roleName.equalsIgnoreCase("Deputy Director")
                                || roleName.equalsIgnoreCase("Senior Management Advisor") || roleName.equalsIgnoreCase("Executive Officer")
                                || roleName.equalsIgnoreCase("Chief of Staff")) {
                            OuRole role = new OuRole();
                            if (!ouRolesCall.roles.get(i).current_holder.isEmpty()) {
                                NistOrgWSCalls.OuRolesCall.Employee emp = ouRolesCall.roles.get(i).current_holder.get(0);
                                if (emp != null) {
                                    role.setPeopleId(emp.employee_number);
                                    role.setFullName(firstAndLastNameToFullName(emp.first_name, emp.last_name));
                                    role.setActive(true);//nist org doesn't return this for current_holders, so just set it to true
                                }

                            }
                            role.setRoleName(roleName);
                            roles.add(role);
                        }
                    }
                }
            }

            getOuRolesResponse.setData(roles);
        }

        return Response.ok().entity(serializeResponseWithStatus(getOuRolesResponse, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/requestFor")
    public Response getRequestFor(@Context HttpServletRequest servletRequest, @QueryParam("filter") String filter) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserDao dao = new UserDao();
        Map<String, Object> results = dao.selectRequestForInOu(authenticatedUser.getOuId(), filter);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        GetRequestForResponse getRequestForResponse = new GetRequestForResponse();
        if (statusCode == StatusCode.OK) {
            List<User> users = (List<User>) results.get(UserDao.USER_LIST_KEY);
            List<EmpShortFormat> dataList = new ArrayList<>();
            users.stream().map((user) -> {
                EmpShortFormat data = new EmpShortFormat();
                data.setPeopleId(user.getPeopleId());
                data.setFullName(user.toFullName());
                data.setActive(user.getActive());
                return data;
            }).forEachOrdered((data) -> {
                dataList.add(data);
            });

            getRequestForResponse.setData(dataList);
        }
        return Response.ok().entity(serializeResponseWithStatus(getRequestForResponse, statusCode)).build();
    }

    //MB-301 two end points
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getprefs")
    public String getUserPrefs(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return "";
        }

        UserPrefsDao dao = new UserPrefsDao();
        //Map<String, Object> results = dao.getUserPrefs(authenticatedUser.getPeopleId(), REMINDER_EMAIL);
        Map<String, Object> results = dao.getUserPrefs(getDetaileePeopleIdWhenApply(servletRequest), REMINDER_EMAIL);

        String prefValue = results.get(UserPrefsDao.USER_PREFS_KEY).toString();

        return prefValue;
    }

    //BANK-514
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getNapCbsDiffPref")
    public String getNapCbsDiffPref(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return "";
        }

        UserPrefsDao dao = new UserPrefsDao();
        //Map<String, Object> results = dao.getUserPrefs(authenticatedUser.getPeopleId(), BK_CBS_DIFF_TOTAL_EMAIL);
        Map<String, Object> results = dao.getUserPrefs(getDetaileePeopleIdWhenApply(servletRequest), BK_CBS_DIFF_TOTAL_EMAIL);

        String prefValue = results.get(UserPrefsDao.USER_PREFS_KEY).toString();

        return prefValue;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/setprefs/{prefValue}")
    public Response postUserPrefs(@Context HttpServletRequest request, @PathParam("prefValue") String prefValue) {
        System.out.println("ckp 2 post pref Value = " + prefValue);

        User authenticatedUser = getSsoAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        if (prefValue == null || prefValue == "") {
            LOG.info("Pref Value is null.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setPeopleId(authenticatedUser.getPeopleId());
        userPrefs.setPrefTypeId(REMINDER_EMAIL);
        userPrefs.setPrefValue(prefValue);

        UserPrefsDao dao = new UserPrefsDao();
        Map<String, Object> results = dao.setUserPrefs(userPrefs);
        StatusCode statusCode = (StatusCode) results.get(UserPrefsDao.STATUS_CODE_KEY);
        //statusCode = StatusCode.OK;
        GetUserPrefsForResponse resp = new GetUserPrefsForResponse();

        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/setNapCbsDiffPref/{prefValue}")
    public Response setNapCbsDiffPref(@Context HttpServletRequest request, @PathParam("prefValue") String prefValue) {

        Map<String, Object> results;
        User authenticatedUser = getSsoAuthenticatedUser(request);
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        if (prefValue == null || prefValue == "") {
            LOG.info("Pref Value is null.");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }

        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setPeopleId(authenticatedUser.getPeopleId());
        userPrefs.setPrefTypeId(BK_CBS_DIFF_TOTAL_EMAIL);
        userPrefs.setPrefValue(prefValue);

        UserPrefsDao dao = new UserPrefsDao();
        if (prefValue.equalsIgnoreCase("never")) {
            results = dao.deleteUserPrefs(userPrefs);
        } else {
            results = dao.setUserPrefs(userPrefs);
        }
        StatusCode statusCode = (StatusCode) results.get(UserPrefsDao.STATUS_CODE_KEY);
        GetUserPrefsForResponse resp = new GetUserPrefsForResponse();

        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/delegation/set")
    public Response setDelegation(@Context HttpServletRequest servletRequest, @QueryParam("username") String username) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        HttpSession session = servletRequest.getSession();
        Boolean delegateIsValid = false;
        //Validate delegation user.
        StatusCode statusCode = StatusCode.OK;
        if (session.getAttribute(DELEGATING_USERNAME) != null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(StatusCode.DelegationError)).build();
        } else {
            UserDao dao = new UserDao();
            Map<String, Object> verifyUserCall = dao.selectUserByUsername(username);
            User verifyUser = (User) verifyUserCall.get(UserDao.USER_KEY);

            List<GetProfileResponse.User.Delegation.DelegationUser> verifiedDelegates = getDelegationByUsername(servletRequest, authenticatedUser.getUsername());
            if (verifiedDelegates != null && !verifiedDelegates.isEmpty()) {
                for (int i = 0; i < verifiedDelegates.size(); i++) {
                    if (username.equals(verifiedDelegates.get(i).getUsername())) {
                        delegateIsValid = true;
                    }
                }
            }

            if (!delegateIsValid || verifyUser == null || verifyUser.getPeopleId() == null) {
                statusCode = StatusCode.BadRequest;
            } else {
                try {
                    session.setAttribute(DELEGATING_USERNAME, username);
                    session.setAttribute(DELEGATING_TRUE_USER, authenticatedUser.getUsername());
                } catch (Exception caught) {
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(StatusCode.BadRequest)).build();
                }
            }

        }
        //statusCode = dao.insertDelegationLock(authenticatedUser.getUsername(), username);
        if (!statusCode.getSuccess()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(StatusCode.InsertFailed)).build();
        }

        GetProfileResponse resp = new GetProfileResponse();
        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    /*
     use a groupId to get a sample user from the group and set the sample user and true user's username
     in session. since a random sample user in the group is used, we cannot do delegation at the same time
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/detailee/set")
    public Response setDetailee(@Context HttpServletRequest servletRequest, @QueryParam("groupId") Integer groupId) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        
        HttpSession session = servletRequest.getSession();
        User sampleUser = null;

        //Validate detailee user.
        StatusCode statusCode = StatusCode.OK;
        //cannot set detailee if delegating
        if (session.getAttribute(DELEGATING_USERNAME) != null) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(StatusCode.DetaileeDelegationError)).build();
        } else {
            try {
                sampleUser = getSampleUserByGroupId(groupId);
            } catch (StatusCodeException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializeStatus(StatusCode.DetaileeError)).build();
            }

            try {
                session.setAttribute(DETAILEE_ID, sampleUser.getPeopleId());
                session.setAttribute(DETAILEE_USERNAME, sampleUser.getUsername());
                session.setAttribute(DETAILEE_GROUP, groupId);
                session.setAttribute(DETAILEE_GROUP_CODE, sampleUser.getGroupCode());
                session.setAttribute(DETAILEE_OU, sampleUser.getOuId());
                session.setAttribute(DETAILEE_DIV, sampleUser.getDivisionId());
                session.setAttribute(DETAILEE_DIV_CODE, sampleUser.getDivisionCode());
                session.setAttribute(DETAILEE_OU_CODE, sampleUser.getOuCode());
                session.setAttribute(DETAILEE_TRUE_USER, authenticatedUser.getUsername());
            } catch (Exception caught) {
                LOG.log(Level.SEVERE, caught.getMessage(), caught);
                return Response.status(Response.Status.BAD_REQUEST).entity(serializeStatus(StatusCode.BadRequest)).build();
            }

        }
        
        GetProfileResponse resp = new GetProfileResponse();
        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/detailee/clear")
    public Response clearDetailee(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        
        HttpSession session = servletRequest.getSession();
        StatusCode statusCode = StatusCode.OK;

        LOG.info("Exiting detailee mode for user " + authenticatedUser.getUsername());
        try {
            session.removeAttribute(DETAILEE_USERNAME);
            session.removeAttribute(DETAILEE_ID);
            session.removeAttribute(DETAILEE_GROUP);
            session.removeAttribute(DETAILEE_OU);
            session.removeAttribute(DETAILEE_DIV);
            session.removeAttribute(DETAILEE_DIV_CODE);
            session.removeAttribute(DETAILEE_OU_CODE);
            session.removeAttribute(DETAILEE_TRUE_USER);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            statusCode = StatusCode.BadRequest;
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(statusCode)).build();
        }

        GetUserPrefsForResponse resp = new GetUserPrefsForResponse();
        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/delegation/clear")
    public Response clearDelegation(@Context HttpServletRequest servletRequest) {
        User authenticatedUser = getSsoAuthenticatedUser(servletRequest);
        
        if (authenticatedUser == null || authenticatedUser.getPeopleId() == null) {
            LOG.info("Can't find SSO user");
            return Response.status(Response.Status.UNAUTHORIZED).entity(serializeStatus(StatusCode.UnAuthorized)).build();
        }
        
        HttpSession session = servletRequest.getSession();
        StatusCode statusCode = StatusCode.OK;
        if (session.getAttribute(DELEGATING_USERNAME) == null) {
            LOG.info("Not currently delegating");
            statusCode = StatusCode.BadRequest;
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(statusCode)).build();
        }

        LOG.info("Clearing delegation for user who was delegating as " + authenticatedUser.getUsername());
        try {
            session.removeAttribute(DELEGATING_USERNAME);
            session.removeAttribute(DELEGATING_TRUE_USER);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            statusCode = StatusCode.BadRequest;
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(serializeStatus(statusCode)).build();
        }

        GetUserPrefsForResponse resp = new GetUserPrefsForResponse();
        return Response.ok().entity(serializeResponseWithStatus(resp, statusCode)).build();
    }

    public List<GetProfileResponse.User.Delegation.DelegationUser> getDelegationByUsername(HttpServletRequest servletRequest, String username) {
        List<GetProfileResponse.User.Delegation.DelegationUser> outputDelegates = new ArrayList<>();
        NistOrgWSCalls.MmlDelegationsByUsernameCall mmlDelegationsByUsernameCall = null;

        String delegationWsUrl = ApiUtil.getMmlDelegationByUsernameUrl(username);
        try {
            mmlDelegationsByUsernameCall = NistOrgWSCalls.callMmlDelegationsByUsernameService(delegationWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        if (mmlDelegationsByUsernameCall == null) {
            return null;
        } else if ("success".equals(mmlDelegationsByUsernameCall.returnMessage)) {
            for (int i = 0; i < mmlDelegationsByUsernameCall.delegations.size(); i++) {
                NistOrgWSCalls.MmlEmployeeByNistOrgIdCall mmlEmployeeByNistOrgIdCall = null;
                String employeeWsUrl = ApiUtil.getMmlEmployeeByNistOrgIdUrl(mmlDelegationsByUsernameCall.delegations.get(i).delegator_id);
                try {
                    mmlEmployeeByNistOrgIdCall = NistOrgWSCalls.callMmlEmployeeByNistOrgIdService(employeeWsUrl);
                } catch (Exception caught) {
                    return null;
                }
                if (mmlEmployeeByNistOrgIdCall == null) {
                    return null;
                } else if ("success".equals(mmlEmployeeByNistOrgIdCall.returnMessage)) {
                    GetProfileResponse.User.Delegation.DelegationUser delegationUser = new GetProfileResponse.User.Delegation.DelegationUser();
                    delegationUser.setUsername(mmlEmployeeByNistOrgIdCall.employee.username);
                    delegationUser.setFirstName(mmlEmployeeByNistOrgIdCall.employee.first_name);
                    delegationUser.setMiddleName(mmlEmployeeByNistOrgIdCall.employee.middle_name);
                    delegationUser.setLastName(mmlEmployeeByNistOrgIdCall.employee.last_name);
                    outputDelegates.add(delegationUser);
                }
            }
        }

        return outputDelegates;
    }

    private GetProfileResponse.User.Profile userToProfile(User user) {
        GetProfileResponse.User.Profile profile = new GetProfileResponse.User.Profile();
        if (user != null) {
            profile.setPeopleId(user.getPeopleId());
            profile.setFirstName(user.getFirstName());
            profile.setLastName(user.getLastName());
            profile.setMiddleName(user.getMiddleName());
            profile.setOuId(user.getOuId());
            profile.setOuCode(user.getOuCode());
            profile.setDivisionId(user.getDivisionId());
            profile.setDivisionCode(user.getDivisionCode());
            profile.setGroupId(user.getGroupId());
            profile.setUsername(user.getUsername());
            profile.setEmail(user.getEmail());
            profile.setPhoneNumber(user.getPhoneNumber());
            profile.setSupervisor(user.getSupervisor());
            profile.setLastUpdateDate(user.getLastUpdateDate());
            profile.setStaffType(user.getStaffType());
            profile.setBossId(user.getBossId());
            profile.setDelegating(user.getIsDelegating());
            profile.setDetaileeMode(user.getDetaileeMode());

            UserDao dao = new UserDao();
            if (dao.isAdminUser(user.getUsername())) {
                profile.setAccessAdmin(Boolean.TRUE);
            } else {
                profile.setAccessAdmin(Boolean.FALSE);
            }

        }
        return profile;
    }

    private Map<Integer, String> transformToNames(NistOrgWSCalls.MmlBankcardApproversCall mmlBankcardApproversCall) {
        Map<Integer, String> names = new HashMap<>();
        if (mmlBankcardApproversCall.host != null) {
            names.put(mmlBankcardApproversCall.host.employee_number, approverToName(mmlBankcardApproversCall.host));
        }
        if (mmlBankcardApproversCall.supervisor != null) {
            names.put(mmlBankcardApproversCall.supervisor.employee_number, approverToName(mmlBankcardApproversCall.supervisor));
        }
        if (mmlBankcardApproversCall.group_leader != null) {
            names.put(mmlBankcardApproversCall.group_leader.employee_number, approverToName(mmlBankcardApproversCall.group_leader));
        }
        if (mmlBankcardApproversCall.division_chief != null) {
            names.put(mmlBankcardApproversCall.division_chief.employee_number, approverToName(mmlBankcardApproversCall.division_chief));
        }
        if (mmlBankcardApproversCall.bankcard_approving_official != null) {
            names.put(mmlBankcardApproversCall.bankcard_approving_official.employee_number, approverToName(mmlBankcardApproversCall.bankcard_approving_official));
        }
        if (mmlBankcardApproversCall.bankcard_holder != null) {
            names.put(mmlBankcardApproversCall.bankcard_holder.employee_number, approverToName(mmlBankcardApproversCall.bankcard_holder));
        }
        if (mmlBankcardApproversCall.administrative_officer != null) {
            names.put(mmlBankcardApproversCall.administrative_officer.employee_number, approverToName(mmlBankcardApproversCall.administrative_officer));
        }
        return names;
    }

    private String approverToName(NistOrgWSCalls.MmlBankcardApproversCall.Approver approver) {
        String name = null;
        if (approver != null) {
            name = firstAndLastNameToFullName(approver.first_name, approver.last_name);
        }
        return name;
    }

    protected Route getDefaultRoute(HttpServletRequest servletRequest, Integer peopleId) {
        Route defaultRoute = new Route();
        String bankcardApproversWsUrl = ApiUtil.getMmlEmployeeBankcardApproversUrl(peopleId);
        NistOrgWSCalls.MmlBankcardApproversCall mmlBankcardApproversCall = null;
        try {
            mmlBankcardApproversCall = NistOrgWSCalls.callMmlBankcardApproversService(bankcardApproversWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return null;
        }
        if (mmlBankcardApproversCall == null) {
            return null;
        } else if ("success".equals(mmlBankcardApproversCall.returnMessage)) {

            Map<Integer, String> names = transformToNames(mmlBankcardApproversCall);
            List<Integer> approvalChain = mmlBankcardApproversCall.approval_chain;
            if (approvalChain != null) {

                // host is no more for approval chain but we still need it for associate make request
                // which will use host as the official requester
                // Host.
                Integer hostId = approvalChain.get(0);
                if (hostId != null) {
                    Route.RouteStep hostRouteStep = new Route.RouteStep();
                    hostRouteStep.setPersonId(hostId);
                    hostRouteStep.setName(names.get(hostId));
                    defaultRoute.setHost(hostRouteStep);
                }
                // Reviewer.
                Integer reviewerId = approvalChain.get(1);
                if (reviewerId != null) {
                    Route.RouteStep reviewerRouteStep = new Route.RouteStep();
                    reviewerRouteStep.setPersonId(reviewerId);
                    reviewerRouteStep.setName(names.get(reviewerId));
                    defaultRoute.setReviewer(reviewerRouteStep);
                }

                // Bankcard approving official.
                Integer bankcardApprovingOfficialId = approvalChain.get(2);
                if (bankcardApprovingOfficialId != null) {
                    Route.RouteStep baoRouteStep = new Route.RouteStep();
                    baoRouteStep.setPersonId(bankcardApprovingOfficialId);
                    baoRouteStep.setName(names.get(bankcardApprovingOfficialId));
                    defaultRoute.setBankcardApprovingOfficial(baoRouteStep);
                }

                // Bankcard holder.
                Integer bankcardHolderId = approvalChain.get(3);
                if (bankcardHolderId != null) {
                    Route.RouteStep bhRouteStep = new Route.RouteStep();
                    bhRouteStep.setPersonId(bankcardHolderId);
                    bhRouteStep.setName(names.get(bankcardHolderId));
                    defaultRoute.setBankcardHolder(bhRouteStep);
                }
            }
        }
        return defaultRoute;
    }

    private void setUserPrivilege(UserPrivileges userPrivileges, String privilegeName) {
        if (privilegeName != null) {
            //this switch statement defines requests access at the search view
            switch (privilegeName) {
                //MB-465 everyone has at least division access
                /*case "Group Leader":
                    userPrivileges.setAccessGroup(Boolean.TRUE);
                    break;

                case "Division Chief":
                case "Deputy Chief":
                //case "Administrative Office Assistant": //MB-367
                case "Bankcard Holder":
                case "CIMS Power User":
                    //Casara's email on 3/12 clarified that Michele wants all OM to have division level access, so moved group office manager here from above
                    //case "Group Office Manager" :
                    //case "Division Office Manager" :
                    //this role can access division data; added 4/13/18    
                    userPrivileges.setAccessGroup(Boolean.TRUE);
                    userPrivileges.setAccessDiv(Boolean.TRUE);
                    break;
                 */
                case "Director":
                case "Deputy Director":
                case "Bankcard Approving Official":
                case "Senior Management Advisor":
                case "Laboratory Office Manager":
                //MB-334
                case "Group Office Manager":
                case "Division Office Manager":
                case "Administrative Officer":
                case "Administrative Office Assistant": //MB-367
                case "Property Custodian": //Michele email on 6/1/2021
                    //case "CIMS Power User": //Megan msg on 2/7/2024, also BANK-566
                    userPrivileges.setAccessGroup(Boolean.TRUE);
                    userPrivileges.setAccessDiv(Boolean.TRUE);
                    userPrivileges.setAccessOu(Boolean.TRUE);
                    break;
            }
            //added this new block of code because new req says these 3 roles can change PTC at any time 4/13/18
            switch (privilegeName) {
                case "Administrative Officer":
                case "Bankcard Approving Official":
                case "Bankcard Holder":
                    userPrivileges.setChangePtc(Boolean.TRUE);
                    break;

            }
        }

    }

    public static class GetDefaultRouteResponse extends JsonStatus {

        public Route defaultRoute;

    }

    public static class GetProfileResponse extends JsonStatus {

        @Data
        public static class User {

            @Data
            public static class Profile {

                private Integer peopleId;
                private String firstName;
                private String lastName;
                private String middleName;
                private Integer ouId;
                private String ouCode;
                private Integer divisionId;
                private String divisionCode;
                private Integer groupId;
                private String username;
                private String email;
                private String phoneNumber;
                private Boolean supervisor;
                @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
                private Date lastUpdateDate;
                private String staffType;
                private Integer bossId;
                private Boolean accessAdmin;
                private Boolean delegating;
                private Boolean detaileeMode;
            }

            @Data
            public static class UserDetailedPrivilege {

                private Integer peopleId;
                private Integer ouId;
                private Integer divisionId;
                private Integer groupId;
                private Boolean accessGroup;
                private Boolean accessDiv;
                private Boolean accessOu;
            }

            @Data
            public static class Detailee {

                private Integer peopleId;
                private Integer ouId;
                private Integer divisionId;
                private Integer groupId;
                private String ouCode;
                private String divisionCode;
                private String groupCode;
            }

            @Data
            public static class Delegation {

                @Data
                public static class DelegationUser {

                    private String username;
                    private String firstName;
                    private String middleName;
                    private String lastName;
                }

                private Boolean delegating;
                private DelegationUser trueUser;
                private List<DelegationUser> delegateAs;
            }

            private Profile profile;
            private List<Role> roles;
            private List<UserDetailedPrivilege> detailedPrivilege;
            private Route defaultRoute;
            private Delegation delegation;
            private Detailee detailee;
        }

        private User data;

        public User getData() {
            return data;
        }

        public void setData(User data) {
            this.data = data;
        }
    }

    @Data
    public static class Route {

        @Getter
        @Setter
        public static class RouteStep {

            private Integer personId;
            private String name;
        }

        private RouteStep host;
        private RouteStep reviewer;
        private RouteStep bankcardApprovingOfficial;
        private RouteStep bankcardHolder;
    }

    /**
     * basic data of an employee
     */
    @Data
    public static class EmpShortFormat {

        private Integer peopleId;
        private String fullName;
        private Boolean active;
        private String isCurrentHolder;
    }

    /**
     * key data of an employee
     */
    @Data
    public static class EmpMidFormat {

        private Integer peopleId;
        private Integer ouId;
        private Integer divisionId;
        private Integer groupId;
        private String fullName;
        private String staffType;
        private Boolean active;
        private String empEmail;

    }

    public static class GetOuMembersResponse extends JsonStatus {

        @Getter
        @Setter
        private List<EmpMidFormat> data;
    }

    @Data
    public static class NistEmployee {

        private Integer peopleId;
        private String fullName;
        private Boolean active;
        private String empEmail;
    }

    public static class GetNistEmployeesResponse extends JsonStatus {

        @Getter
        @Setter
        private List<NistEmployee> data;
    }

    @Data
    public static class Reviewer {

        private Integer peopleId;
        private String fullName;
        private Boolean active;
        private Integer divId;
        private Integer bossId;
    }

    public static class GetReviewersResponse extends JsonStatus {

        @Getter
        @Setter
        private List<Reviewer> data;
    }

    @Data
    public static class Role {

        private Integer id;
        private String name;
        private String category;
    }

    public static class GetUserRolesResponse extends JsonStatus {

        @Getter
        @Setter
        private List<Role> data;
    }

    public static class GetApproversResponse extends JsonStatus {

        @Getter
        @Setter
        private List<EmpShortFormat> data;
    }

    @Data
    public static class Itso {

        private int id;
        private Integer peopleId;
        private String fullName;
        private Boolean active;
        private Boolean ditso;
    }

    public static class GetItsoResponse extends JsonStatus {

        @Getter
        @Setter
        private List<Itso> data;
    }

    @Data
    public static class OuRole {

        private String roleName;
        private Integer peopleId;
        private String fullName;
        private Boolean active;
    }

    public static class GetOuRolesResponse extends JsonStatus {

        @Getter
        @Setter
        private List<OuRole> data;
    }

    public static class GetRequestForResponse extends JsonStatus {

        @Getter
        @Setter
        private List<EmpShortFormat> data;
    }

    public static class GetUserPrefsForResponse extends JsonStatus {

        @Getter
        @Setter
        private List<UserPrefs> data;
    }

}
