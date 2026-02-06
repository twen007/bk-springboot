package gov.nist.oism.asd.empbc.util;

public enum StatusCode {

    OK(true),
    BadRequest(false, 400, "Bad Request"),
    ResourceNotFound(false, 404, "Resource not found"),
    UnAuthorized(false, 401, "The user is not authorized or session timed out. Please reload the application and try again."),
    IbbrPostFailed(true, 600, "Post to IBBR inventory failed"), // the description will be appendeded with the error code and message from the WS call
    IncompleteData(false, 1000, "The input data is incomplete"),
    DatabaseError(false, 1001, "There was an error in the database"),
    ServerError(false, 1500, "There was an error in the server"),
    InsertFailed(false, 1002, "Did not insert row into database"),
    UserNotFound(false, 1003, "Cannot find the MML Bankcard user"),
    DataIntegrityError(false, 1004, "There is an issue with the data integrity"),
    FailedToGenerateRequisitionNumber(false, 1005, "Unable to generate requisition number"),
    UpdateFailed(false, 1006, "Did not update in the database"),
    InvalidRoute(false, 1007, "The route is not valid"),
    RouteValidationFailed(false, 1008, "The validation of route has failed"),
    NistOrgServiceCallFailed(false, 1009, "The NIST Org web service call failed"),
    UserNotBankcardHolder(false, 1010, "The user is not an official bankcard holder"),
    InsufficienPrivileges(false, 1011, "The user doesn't have sufficient privileges for the operation"),
    RequestNotInCorrectRouteStateForOperation(false, 1012, "The request is not in the proper stage to complete the operation"),
    InvalidRequestState(false, 1013, "The request is in an invalid state"),
    DatabaseNotRespondingError(false, 1014, "The database is not responding"),
    DatabaseConnectionError(false, 1015, "The system cannot connect to the database"),
    DelegationError(false, 1016, "Delegation and delegation status do not align"),
    DetailedPrivilegeError(false, 1017, "Error getting user detailed privilege"),
    DetaileeError(false, 1018, "Error getting a detailee user data"),
    DetaileeDelegationError(false, 1019, "While in delegation mode, detailee mode is not allowed"),
    CacheManagementError(false, 1032, "There was an error when loading data from cache"),
    RequestNotFound(false, 1404, "Request not found"),
    RecordNotFound(false, 2404, "Record not found");
    
    private final boolean mSuccess;
    private final int mCode;
    private final String mDescription;

    StatusCode(boolean success) {
        mSuccess = success;
        mCode = -1;
        mDescription = null;
    }

    StatusCode(boolean success, int code, String description) {
        mSuccess = success;
        mCode = code;
        mDescription = description;
    }

    public boolean getSuccess() {
        return mSuccess;
    }

    public int getCode() {
        return mCode;
    }

    public String getDescription() {
        return mDescription;
    }
}
