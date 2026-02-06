package gov.nist.oism.asd.empbc.v1;

import com.google.gson.Gson;
import gov.nist.oism.asd.empbc.db.WsCallFailedRecordDao;
import gov.nist.oism.asd.empbc.model.IbbrChemicalItem;
import gov.nist.oism.asd.empbc.model.WsCallFailedRecord;
import gov.nist.oism.asd.empbc.util.IbbrWSCalls;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.WsCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author ynz25
 */
@Path("/records")
public class WsCallFailedRecordService extends SsoService {

    private static final Logger LOG = Logger.getLogger(WsCallFailedRecordService.class.getSimpleName());
    // this marks the beginning of the customized status code which will be embedded in the response body
    public static final int CUSTOMIZED_ERROR_CODE_START = StatusCode.IbbrPostFailed.getCode();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ibbr")
    public Response getAllFailedIbbrRecords() {
        WsCallFailedRecordDao dao = new WsCallFailedRecordDao();
        Map<String, Object> results = dao.selectAllRecordsForWsCategory(WsCategory.IBBR);
        GetFailedRecordResponse response = new GetFailedRecordResponse();
        StatusCode statusCode = (StatusCode) results.get(dao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            List<WsCallFailedRecord> records = (List<WsCallFailedRecord>) results.get(WsCallFailedRecordDao.WS_CALL_FAILED_RECORDS_KEY);
            response.setData(records);
        }
        return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ibbr/{recordId}")
    public Response getFailedIbbrRecord(@NotNull @PathParam("recordId") Integer recordId) {
        WsCallFailedRecordDao dao = new WsCallFailedRecordDao();
        Map<String, Object> results = dao.selectRecordForWsCategoryAndId(WsCategory.IBBR, recordId);
        GetFailedRecordResponse response = new GetFailedRecordResponse();
        StatusCode statusCode = (StatusCode) results.get(dao.STATUS_CODE_KEY);
        if (statusCode == StatusCode.OK) {
            List<WsCallFailedRecord> dataList = new ArrayList<>();
            WsCallFailedRecord record = (WsCallFailedRecord) results.get(WsCallFailedRecordDao.WS_CALL_FAILED_RECORDS_KEY);
            if (record != null) {
                dataList.add(record);
            }
            response.setData(dataList);
            return Response.ok().entity(serializeResponseWithStatus(response, statusCode)).build();
        } else if (statusCode.getCode() < CUSTOMIZED_ERROR_CODE_START) { // use http response status code
            return Response.status(statusCode.getCode()).entity(serializeStatusWithCustomizedErrorString(statusCode, statusCode.getDescription() + " for IBBR record ID " + recordId)).build();
        } else { // use customized status code in the body
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(serializeStatusWithCustomizedErrorString(statusCode, "Failed to get IBBR record " + recordId)).build();
        }
    }

    private ResyncFailedRecordResponse.ResyncStatus processResyncIbbrRecord(HttpServletRequest servletRequest, Integer recordId, String jsonString) {
        ResyncFailedRecordResponse.ResyncStatus taskStatus = null;

        // make the web service call
        SsoService.Error error = IbbrWSCalls.resyncIbbrRecord(servletRequest, jsonString);
        if (error != null) {
            String msg = error.getDescription();
            taskStatus = new ResyncFailedRecordResponse.ResyncStatus(recordId, error.getCode(), "");

            WsCallFailedRecordDao dao = new WsCallFailedRecordDao();
            StatusCode daoCode = dao.updateFailedRecord(recordId, error.getCode(), error.getDescription());

            if (daoCode != StatusCode.OK) {
                msg += " Update IBBR record failed with errorCode: " + daoCode.getCode() + ", " + daoCode.getDescription();

            }
            taskStatus.setDescription(msg);

        } else {
            // WS call went through, thus remove the failed record from the Database
            String msg = "IBBR resubmit succeeded.";
            taskStatus = new ResyncFailedRecordResponse.ResyncStatus(recordId, StatusCode.OK.getCode(), msg);
            LOG.info(String.format("ResyncStatus statusCode = %d", taskStatus.getStatusCode()));

            WsCallFailedRecordDao dao = new WsCallFailedRecordDao();
            StatusCode daoCode = dao.deleteFailedRecord(recordId);

            if (daoCode != StatusCode.OK) {
                msg += " But deleting IBBR record failed with errorCode: " + daoCode.getCode() + ". " + daoCode.getDescription();
                taskStatus.setDescription(msg);
            }

        }
        return taskStatus;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/ibbr/{recordId}")
    public Response resyncFailedIbbrRecord(@Context HttpServletRequest servletRequest, @NotNull
            @PathParam("recordId") Integer recordId) {
        WsCallFailedRecordDao dao = new WsCallFailedRecordDao();
        Map<String, Object> results = dao.selectRecordForWsCategoryAndId(WsCategory.IBBR, recordId);
        StatusCode statusCode = (StatusCode) results.get(dao.STATUS_CODE_KEY);

        if (statusCode == StatusCode.OK) {
            ResyncFailedRecordResponse response = new ResyncFailedRecordResponse();
            WsCallFailedRecord record = (WsCallFailedRecord) results.get(WsCallFailedRecordDao.WS_CALL_FAILED_RECORDS_KEY);;

            Gson gson = new Gson();
            ResyncFailedRecordResponse.ResyncStatus taskStatus = processResyncIbbrRecord(servletRequest, recordId, gson.toJson(record.getIbbrRecord()));

            List<ResyncFailedRecordResponse.ResyncStatus> dataList = new ArrayList<>();
            dataList.add(taskStatus);
            response.setData(dataList);
            return Response.ok().entity(serializeResponseWithResyncStatus(response, taskStatus.getStatusCode())).build();
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity(serializeStatusWithCustomizedErrorString(statusCode, statusCode.getDescription() + " for IBBR record ID " + recordId)).build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ibbr")
    public Response resyncAllSelectedFailedIbbrRecord(@Context HttpServletRequest servletRequest, List<WsCallFailedRecord> records
    ) {
        ResyncFailedRecordResponse response = new ResyncFailedRecordResponse();
        List<ResyncFailedRecordResponse.ResyncStatus> dataList = new ArrayList<>();
        for (WsCallFailedRecord record : records) {
            if (record != null) {
                IbbrChemicalItem ibbrRecord = record.getIbbrRecord();
                LOG.info(String.format("Processing record %d, IBBR item %s ", record.getId(), ibbrRecord.toString()));
                // make the web service call
                Gson gson = new Gson();
                ResyncFailedRecordResponse.ResyncStatus taskStatus = processResyncIbbrRecord(servletRequest, record.getId(), gson.toJson(ibbrRecord));

                dataList.add(taskStatus);

            }
        }
        response.setData(dataList);
        return Response.ok().entity(serializeResponseWithResyncStatus(response, StatusCode.OK.getCode())).build();

    }

    public static class GetFailedRecordResponse extends JsonStatus {

        private List<WsCallFailedRecord> mData;

        public List<WsCallFailedRecord> getData() {
            return mData;
        }

        public void setData(List<WsCallFailedRecord> data) {
            mData = data;
        }
    }

    public static class ResyncFailedRecordResponse extends JsonStatus {

        public static class ResyncStatus {

            private Integer recordId;
            private Integer statusCode;
            private String description;

            public ResyncStatus(Integer recordId, Integer statusCode, String description) {
                this.recordId = recordId;
                this.statusCode = statusCode;
                this.description = description;
            }

            public Integer getRecordId() {
                return recordId;
            }

            public void setRecordId(Integer recordId) {
                this.recordId = recordId;
            }

            public void setStatusCode(Integer statusCode) {
                this.statusCode = statusCode;
            }

            public Integer getStatusCode() {
                return statusCode;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getDescription() {
                return description;
            }

        }
        private List<ResyncStatus> mData;

        public List<ResyncStatus> getData() {
            return mData;
        }

        public void setData(List<ResyncStatus> data) {
            mData = data;
        }
    }
}
