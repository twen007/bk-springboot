package gov.nist.oism.asd.empbc.v1;

import gov.nist.oism.asd.empbc.config.PropertyLoader;
import gov.nist.oism.asd.empbc.db.UserDao;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.model.UserDetailedPrivilege;
import gov.nist.oism.asd.empbc.util.ApiUtil;
import gov.nist.oism.asd.empbc.util.NistOrgWSCalls;
import gov.nist.oism.asd.empbc.util.RequisitionNumberRequest;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.StatusCodeException;

import java.security.Principal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SsoService {

    private static final Logger LOG = Logger.getLogger(UserService.class.getSimpleName());
    public static final String DELEGATING_USERNAME = "delegating_username";
    public static final String DELEGATING_TRUE_USER = "delegating_true_user";
    public static final String DETAILEE_USERNAME = "detailee_username";
    public static final String DETAILEE_ID = "detailee_id";
    public static final String DETAILEE_GROUP = "detailee_group";
    public static final String DETAILEE_GROUP_CODE = "detailee_group_code";
    public static final String DETAILEE_DIV_CODE = "detailee_div_code";
    public static final String DETAILEE_DIV = "detailee_div";
    public static final String DETAILEE_OU = "detailee_ou";
    public static final String DETAILEE_OU_CODE = "detailee_ou_code";
    public static final String DETAILEE_TRUE_USER = "detailee_true_user";
    public static final Integer REMINDER_EMAIL = 1;
    public static final Integer BK_CBS_DIFF_TOTAL_EMAIL = 2;

    public static JsonStatus serializeStatus(StatusCode statusCode) {
        JsonStatus callStatus = new JsonStatus();
        return serializeResponseWithStatus(callStatus, statusCode);
    }

    public static JsonStatus serializeStatusWithCustomizedErrorString(StatusCode statusCode, String customizedErrorString) {
        JsonStatus jsonStatus = new JsonStatus();
        jsonStatus.setSuccess(statusCode.getSuccess());
        Error error = new Error();
        error.setCode(statusCode.getCode());
        error.setDescription(customizedErrorString);
        jsonStatus.setError(error);
        return jsonStatus;
    }

    public static <T extends JsonStatus> T serializeResponseWithStatus(T t, StatusCode statusCode) {
        t.setSuccess(statusCode.getSuccess());
        if (!statusCode.getSuccess()) {
            Error error = new Error();
            error.setCode(statusCode.getCode());
            error.setDescription(statusCode.getDescription());
            t.setError(error);
        }
        return t;
    }

    public static <T extends JsonStatus> T serializeResponseWithResyncStatus(T t, Integer statusCode) {
        if (statusCode == StatusCode.OK.getCode() || statusCode == StatusCode.IbbrPostFailed.getCode()) {
            t.setSuccess(true);
        } else {
            t.setSuccess(false);
        }
        return t;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JsonStatus {

        private boolean success;
        private Error error;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Error {

        private int code;
        private String description;
    }

    @Data
    public class ApiResponse<T> {

        private T data;
        private Error error;
        private boolean success;

        public ApiResponse(T data, StatusCode statusCode) {
            this.data = data;
            if (statusCode == null) {
                this.success = true;
                this.error = null;
            } else {
                this.error = new Error(statusCode.getCode(), statusCode.getDescription());
                this.success = false;
            }
        }
    }

    /**
     * Handles common exception logic for database errors and server errors.
     *
     * @param e The exception that occurred.
     * @param isSqlException Boolean whether or not the exception is a
     * SQLException
     * @return An ApiResponse representing the error.
     */
    protected <T> ApiResponse<T> handleException(Exception e, boolean isSqlException) {
        LOG.log(Level.SEVERE, e.getMessage(), e); // Include the exception itself in the log
        if (isSqlException) {
            return new ApiResponse<>(null, StatusCode.DatabaseError);
        } else {
            return new ApiResponse<>(null, StatusCode.ServerError);
        }

    }

    /**
     * Executes a database operation and handles exceptions.
     *
     * @param <T> The type of the result.
     * @param operation The database operation to execute.
     * @return An ApiResponse containing the result or an error status.
     */
    protected <T> ApiResponse<T> executeDbOperation(DbOperation<T> operation) {
        try {
            return operation.execute();
        } catch (SQLException e) {
            return handleException(e, true);
        } catch (Exception e) {
            return handleException(e, false);
        }
    }

    /**
     * Functional interface for database operations.
     *
     * @param <T> The type of the result.
     */
    @FunctionalInterface
    protected interface DbOperation<T> {

        ApiResponse<T> execute() throws SQLException, Exception;
    }

    protected StatusCode checkDbStatus() {
        UserDao dao = new UserDao();
        return dao.testConnection();
    }

    protected boolean shouldCallIbbr() {
        return Boolean.parseBoolean(PropertyLoader.getProperty("are.ibbr.ws.calls.active"));
    }

    protected String getAppVerison() {
        return PropertyLoader.getProperty("version");
    }

    /**
     * return either the true auth user or a delegating user if in delegation
     * mode note: if in detailee mode, this still return the true user, not the
     * sampled detailee user
     *
     * @param servletRequest
     * @return
     */
    protected User getSsoAuthenticatedUser(HttpServletRequest servletRequest) {
        String username;
        HttpSession session = servletRequest.getSession();

        boolean useSsoProxy = Boolean.parseBoolean(PropertyLoader.getProperty("use.sso.proxy")); // Get from application.properties

        if (session.getAttribute(UserService.DELEGATING_USERNAME) != null) {
            username = (String) session.getAttribute(UserService.DELEGATING_USERNAME);
        } else if (useSsoProxy) {
            username = PropertyLoader.getProperty("sso.proxy.username"); // Get from application.properties
        } else {
            Principal principal = servletRequest.getUserPrincipal();
            if (principal == null || principal.getName().isEmpty()) {
                return null;
            }
            username = principal.getName();
        }
        try {
            UserService userService = new UserService();
            User user = userService.getUserByUsername(username);
            return user;
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return null;
        }
    }

    /**
     * since we have delegating and detailee modes which allow a user to perform
     * functions as the delegating user or a detailee user (a non supervisor
     * sample user for a group that the user selected), we need to use proper
     * peopleId in different conditions. For delegation mode, we use
     * getSsoAuthenticatedUser's user which is the delegating user; so the usual
     * authenticatedUser.getPeopleId() would work. However, for detailee mode,
     * sometimes we need to use detailee to get the associated org data and
     * default routes. sometimes, we need to use true user to record who created
     * something. This method would return detailee peopleId if in detailee mode
     * or it will return getSsoAuthenticatedUser method's user's peopleId
     *
     * @param servletRequest
     * @return
     */
    protected Integer getDetaileePeopleIdWhenApply(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession();
        //if detailee mode is enabled
        if (session.getAttribute(DETAILEE_USERNAME) != null) {
            return (Integer) session.getAttribute(DETAILEE_ID);
        } else {
            User user = getSsoAuthenticatedUser(servletRequest);
            return user.getPeopleId();
        }
    }

    /**
     * Check user's detailed privilege to see if the user is granted access to a
     * different OU other than the user's OU
     *
     * @param user
     * @param accessToOuId
     * @return
     */
    protected Boolean isUserAllowedOuAccessWithDetailedPrivilege(User user, int accessToOuId) {
        Boolean allowAccess = false;
        try {
            UserService userService = new UserService();
            List<UserDetailedPrivilege> list = userService.getUserDetailedPrivilegeById(user.getPeopleId());
            for (int i = 0; i < list.size(); i++) {
                UserDetailedPrivilege udp = (UserDetailedPrivilege) list.get(i);
                //changed to check for record with ou_id but any of the access ou, div, or grp would allow
                if (udp.getOuId() == accessToOuId && (udp.getAccessOu() || udp.getAccessDiv() || udp.getAccessGroup())) {
                    allowAccess = true;
                    break;
                }
            }
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        return allowAccess;
    }

    /**
     * Check user's detailed privilege to see if the user is granted access to a
     * different division other than the user's division
     *
     * @param user
     * @param accessToDivId
     * @return
     */
    protected Boolean isUserAllowedDivAccessWithDetailedPrivilege(User user, int accessToDivId) {
        Boolean allowAccess = false;
        try {
            UserService userService = new UserService();
            List<UserDetailedPrivilege> list = userService.getUserDetailedPrivilegeById(user.getOuId());
            for (int i = 0; i < list.size(); i++) {
                UserDetailedPrivilege udp = (UserDetailedPrivilege) list.get(i);
                if (udp.getDivisionId() == accessToDivId && udp.getAccessDiv()) {
                    allowAccess = true;
                    break;
                }
            }
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        return allowAccess;
    }

    /**
     * get a sample, non supervisor user belong to a group. This will be used to
     * get bankcard approvers for a staff in that group
     *
     * @param groupId
     * @return
     * @throws StatusCodeException
     */
    public User getSampleUserByGroupId(Integer groupId) throws StatusCodeException {
        UserDao dao = new UserDao();
        User user;
        Map<String, Object> results = dao.selectSampleUserByGroup(groupId, false);
        StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
        if (null == statusCode) {

            throw new StatusCodeException(StatusCode.DatabaseError);
        } else {
            switch (statusCode) {
                case OK ->
                    user = (User) results.get(UserDao.USER_KEY);
                case UserNotFound -> {
                    //try to find feds who are supervisor, DC or GL because in rare
                    //cases, a group only have 1 non supervisor staff, so return the staff is better
                    //than return nobody
                    results = dao.selectSampleUserByGroup(groupId, true);
                    statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
                    if (statusCode == StatusCode.OK) {
                        user = (User) results.get(UserDao.USER_KEY);
                    } else {
                        //this group is empty, there's nothing we can here
                        throw new StatusCodeException(StatusCode.UserNotFound);
                    }
                }
                default ->
                    throw new StatusCodeException(StatusCode.DatabaseError);
            }
        }
        return user;

    }

    /**
     * This method is used to generate requisition number for the request. If
     * the requester request a purchase used for temporary assignment or detail
     * job in a different org, we need to know the division and group and the
     * bankcard holder to generate the correct number that can be used in NAP
     *
     * @param servletRequest
     * @param fy
     * @param bankcardHolderId
     * @param requesterId
     * @param detailDivCode
     * @param detailGrpCode
     * @return requisitionNumber
     */
    protected String generateRequisitionNumber(HttpServletRequest servletRequest, String fy, Integer bankcardHolderId, Integer requesterId, String detailDivCode, String detailGrpCode) {
        String requisitionNumber = "";
        String groupCode = "00";
        String divisionCode = null;

        String requisitionNumberUrl = PropertyLoader.getProperty("requisition.number.url");
        String authorizationCode = PropertyLoader.getProperty("requisition.number.authorization.code");

        String initials = null;
        //get BCH initials
        if (bankcardHolderId != null) {
            UserDao dao = new UserDao();
            Map<String, Object> results = dao.selectUserByPeopleId(bankcardHolderId);
            StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
            if (statusCode == StatusCode.OK) {
                User bankcardHolder = (User) results.get(UserDao.USER_KEY);
                if (bankcardHolder != null && bankcardHolder.getFirstName() != null && !bankcardHolder.getFirstName().isEmpty() && bankcardHolder.getLastName() != null && !bankcardHolder.getLastName().isEmpty()) {
                    initials = bankcardHolder.getFirstName().substring(0, 1) + bankcardHolder.getLastName().substring(0, 1);
                }
            }
        }

        //get div and grp codes using requester's detailed div and grp
        if (detailDivCode != null && detailGrpCode != null) {
            //MB-209 detailee
            groupCode = detailGrpCode.substring(3, 5);
            divisionCode = detailDivCode;
        } else {
            //get div and grp codes using requester's div and grp
            if (requesterId != null) {
                UserDao dao = new UserDao();
                Map<String, Object> results = dao.selectUserByPeopleId(requesterId);
                StatusCode statusCode = (StatusCode) results.get(UserDao.STATUS_CODE_KEY);
                if (statusCode == StatusCode.OK) {
                    User requester = (User) results.get(UserDao.USER_KEY);
                    if (requester != null && requester.getGroupCode() != null && requester.getGroupCode().length() == 5) {
                        groupCode = requester.getGroupCode().substring(3, 5);
                        divisionCode = requester.getDivisionCode();
                    }
                }
            }
        }
        //call NAP API to generate the requsition number
        RequisitionNumberRequest.Builder builder = new RequisitionNumberRequest.Builder();

        builder.url(requisitionNumberUrl)
                .code(authorizationCode)
                .divCd(divisionCode)
                .grpCd(groupCode)
                .bankCardIni(initials)
                .fy(fy);
        try {
            requisitionNumber = builder.build().processRequest();
        } catch (StatusCodeException ex) {
            Logger.getLogger(SsoService.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.info(String.format("requisitionNumber is %s", requisitionNumber));
        if (requisitionNumber == null || requisitionNumber.isEmpty()) {
            LOG.info("Unable to generate requisition number");
            return null;
        }
        return requisitionNumber;
    }

    protected String firstAndLastNameToFullName(String firstName, String lastName) {
        String name = null;
        if (lastName != null && lastName.trim().length() > 0) {
            if (firstName != null & firstName.trim().length() > 0) {
                name = lastName.trim() + ", " + firstName.trim();
            } else {
                name = lastName.trim();
            }
        } else if (firstName != null & firstName.trim().length() > 0) {
            name = firstName.trim();
        }
        return name;
    }

    //helper method to get DC based on divCode
    protected Response getDivisionChiefByDivisionCode(@javax.ws.rs.core.Context HttpServletRequest servletRequest, String divCode) {
        StatusCode statusCode = StatusCode.OK;
        UserService.GetApproversResponse getDivisionChiefResponse = new UserService.GetApproversResponse();
        List<UserService.EmpShortFormat> divisionChiefs = new ArrayList<>();

        String divisionChiefWsUrl = ApiUtil.getMmlDivisionChiefUrl(divCode);
        NistOrgWSCalls.DivChiefCall divChiefCall = null;
        try {
            divChiefCall = NistOrgWSCalls.callDcService(divisionChiefWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }

        if (divChiefCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(divChiefCall.returnMessage)) {
            if (divChiefCall.division_chief.current_holders != null) {
                divisionChiefs = divChiefCall.division_chief.current_holders.stream()
                        .map(currentHolder -> {
                            UserService.EmpShortFormat divisionChief = new UserService.EmpShortFormat();
                            divisionChief.setPeopleId(currentHolder.employee_number);
                            divisionChief.setFullName(firstAndLastNameToFullName(currentHolder.first_name, currentHolder.last_name));
                            return divisionChief;
                        })
                        .collect(Collectors.toList());
            }
            getDivisionChiefResponse.setData(divisionChiefs);
        }
        return Response.ok().entity(serializeResponseWithStatus(getDivisionChiefResponse, statusCode)).build();
    }

    protected Response getDrApproverByOuCode(@javax.ws.rs.core.Context HttpServletRequest servletRequest, String ouCode) {
        StatusCode statusCode = StatusCode.OK;
        UserService.GetApproversResponse getDrResponse = new UserService.GetApproversResponse();
        List<UserService.EmpShortFormat> drs = new ArrayList<>();

        String drWsUrl = ApiUtil.getDrUrl(ouCode);
        NistOrgWSCalls.DrCall drCall = null;
        try {
            drCall = NistOrgWSCalls.callDrService(drWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        }

        if (drCall == null) {
            return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
        } else if ("success".equals(drCall.returnMessage)) {
            if (drCall.roles != null) {
                if (!drCall.roles.isEmpty()) {

                    for (int i = 0; i < drCall.roles.get(0).current_holder.size(); i++) {
                        NistOrgWSCalls.DrCall.Employee emp = drCall.roles.get(0).current_holder.get(i);
                        UserService.EmpShortFormat dr = new UserService.EmpShortFormat();
                        dr.setPeopleId(emp.employee_number);
                        dr.setFullName(firstAndLastNameToFullName(emp.first_name, emp.last_name));
                        dr.setActive(true);
                        drs.add(dr);
                    }

                    getDrResponse.setData(drs);
                }
            } else {
                LOG.log(Level.SEVERE, drCall.error);
                return Response.status(Response.Status.OK).entity(serializeStatus(StatusCode.NistOrgServiceCallFailed)).build();
            }

        }

        return Response.ok().entity(serializeResponseWithStatus(getDrResponse, statusCode)).build();
    }

    /**
     * call NIST ORG API to get a list of roles for a user and then check if the
     * user has any of the roles
     *
     * @param roleNames
     * @param peopleId
     * @return
     */
    public boolean isUserInRole(String[] roleNames, Integer peopleId) {
        List<String> userRoles = getUserRoles(peopleId);
        return isUserInRole(roleNames, userRoles);
    }

    /**
     * use the list passed in to check if there's a match
     *
     * @param roleNames
     * @param userRoles
     * @return
     */
    public boolean isUserInRole(String[] roleNames, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }

        List<String> roleNameList = Arrays.asList(roleNames);
        for (String userRole : userRoles) {
            if (roleNameList.contains(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Call NIST ORG API to get a list of roles for a user using the user's
     * peopleId
     *
     * @param peopleId
     * @return
     */
    public List<String> getUserRoles(Integer peopleId) {
        String employeeProfileWsUrl = ApiUtil.getMmlEmployeeRolesUrl(peopleId);
        NistOrgWSCalls.MmlEmployeeProfileCall mmlEmployeeProfileCall = null;
        try {
            mmlEmployeeProfileCall = NistOrgWSCalls.callMmlEmployeeProfileService(employeeProfileWsUrl);
        } catch (Exception caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            return null;
        }

        if (mmlEmployeeProfileCall == null || !"success".equals(mmlEmployeeProfileCall.returnMessage) || mmlEmployeeProfileCall.roles == null) {
            return null;
        }

        return mmlEmployeeProfileCall.roles.stream()
                .map(role -> role.name)
                .toList();
    }

}
