/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.db;

import com.google.gson.Gson;
import static gov.nist.oism.asd.empbc.db.OracleDao.STATUS_CODE_KEY;
import gov.nist.oism.asd.empbc.model.WsCallFailedRecord;
import gov.nist.oism.asd.empbc.util.IbbrWSCalls;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.WsCategory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ynz25
 */
public class WsCallFailedRecordDao extends OracleDao {

    public static final String WS_CALL_FAILED_RECORDS_KEY = "ws_call_failed_records_key";

    private static final Logger LOG = Logger.getLogger(WsCallFailedRecordDao.class.getSimpleName());

    public Map<String, Object> selectAllRecordsForWsCategory(WsCategory category) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        List<WsCallFailedRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM web_service_call_failed_record WHERE ws_category = ?";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, category.getValue());
            ResultSet rset = pstmt.executeQuery();

            while (rset.next()) {
                WsCallFailedRecord record = new WsCallFailedRecord();

                //id
                record.setId(rset.getInt("id"));
                record.setWsCategory(category.getValue());

                //status_code
                record.setStatusCode(rset.getInt("status_code"));

                // string types
                record.setWsMethod(rset.getString("ws_method"));
                record.setErrorMessage(rset.getString("error_message"));
                LOG.info(String.format("recordId: %d", record.getId()));

                Integer refId = rset.getInt("reference_id");
                String recordDetail = rset.getString("record_detail");
                if (!rset.wasNull()) {
                    LOG.info(String.format("recordDetails: %s", recordDetail));
                    record.setIbbrRecordFromJson(refId, recordDetail);
                }

                // Date created
                Date dateCreated = rset.getDate("created");
                if (!rset.wasNull()) {
                    record.setDateCreated(dateCreated);
                }

                // Date lastSubmitted
                Date lastSubmitted = rset.getDate("last_submitted");
                if (!rset.wasNull()) {
                    record.setLastSubmitted(lastSubmitted);
                }
                LOG.info(String.format("Add record: %s", record.toString()));
                records.add(record);
            }
            results.put(WS_CALL_FAILED_RECORDS_KEY, records);

        } catch (SQLException caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectRecordForWsCategoryAndId(WsCategory category, Integer recordId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * FROM web_service_call_failed_record WHERE ws_category = ? AND ID = ? ";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, category.getValue());
            pstmt.setInt(2, recordId);
            ResultSet rset = pstmt.executeQuery();
            WsCallFailedRecord record = new WsCallFailedRecord();
            if (rset.next()) {
                //id
                record.setId(rset.getInt("id"));

                // ws_category
                record.setWsCategory(category.getValue());

                //status_code
                record.setStatusCode(rset.getInt("status_code"));

                // string types
                record.setWsMethod(rset.getString("ws_method"));
                record.setErrorMessage(rset.getString("error_message"));
                 Integer refId = rset.getInt("reference_id");
                String recordDetail = rset.getString("record_detail");
                if (!rset.wasNull()) {
                    LOG.info(String.format("recordDetails: %s", recordDetail));
                    record.setIbbrRecordFromJson(refId, recordDetail);
                }

                // Date created
                Date dateCreated = rset.getDate("created");
                if (!rset.wasNull()) {
                    record.setDateCreated(dateCreated);
                }

                // Date lastSubmitted
                Date lastSubmitted = rset.getDate("last_submitted");
                if (!rset.wasNull()) {
                    record.setLastSubmitted(lastSubmitted);
                }
                LOG.info(String.format("Found record: %s", record.toString()));
                results.put(WS_CALL_FAILED_RECORDS_KEY, record);
            } else {
                LOG.info(String.format("Cannot find record with id %s", recordId));
                statusCode = StatusCode.ResourceNotFound;
            }
        } catch (SQLException caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
            statusCode = StatusCode.DatabaseError;
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public StatusCode deleteFailedRecord(Integer recordId) {
        StatusCode statusCode = StatusCode.OK;
        String sql = "DELETE FROM web_service_call_failed_record WHERE ID = ? ";
        LOG.info(String.format("sql: %s, recordId = %d", sql, recordId));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, recordId);
            int rowCount = pstmt.executeUpdate();

            if (rowCount == 0) {
                statusCode = StatusCode.UpdateFailed;
                LOG.info(String.format("Failed delete IBBR record for recordId %d", recordId));
            }
            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            statusCode = StatusCode.DatabaseError;
        }
        return statusCode;
    }

    public StatusCode updateFailedRecord(Integer recordId, int errorCode, String errorMsg) {
        StatusCode statusCode = StatusCode.OK;
        String sql = "UPDATE web_service_call_failed_record SET last_submitted=SYSDATE, status_code = ?, error_message = ? WHERE ID = ? ";
        LOG.info(String.format("sql: %s, recordId = %d", sql, recordId));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, errorCode);
            pstmt.setString(2, errorMsg);
            pstmt.setInt(3, recordId);
            int rowCount = pstmt.executeUpdate();

            if (rowCount == 0) {
                statusCode = StatusCode.UpdateFailed;
                LOG.info(String.format("Failed to update record for Id %d", recordId));
            }
            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            statusCode = StatusCode.UpdateFailed;
        }
        return statusCode;
    }

    public Map<String, Object> insertWsCallFailedRecord(WsCallFailedRecord record) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "INSERT INTO"
                + " web_service_call_failed_record "
                + "("
                + "ws_category, "
                + "reference_id, "
                + "created, "
                + "last_submitted, "
                + "ws_method, "
                + "status_code, "
                + "error_message, "
                + "record_detail"
                + ")"
                + " VALUES "
                + "(?, ?, SYSDATE, SYSDATE, ?, ?, ?, ?)";
        Connection connection = null;
        PreparedStatement pstmt = null;
        try {
            connection = getConnection(false);
            pstmt = connection.prepareStatement(sql);

            LOG.info(String.format("sql: %s", sql));
            int index = 1;
            pstmt.setInt(index++, record.getWsCategory());
            pstmt.setInt(index++, record.getReferenceId());
            pstmt.setString(index++, record.getWsMethod());
            pstmt.setInt(index++, record.getStatusCode());
            pstmt.setString(index++, record.getErrorMessage());
            Gson gson = new Gson();
            String recordDetail = gson.toJson(record.getIbbrRecord());
            pstmt.setString(index++, recordDetail);

            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.InsertFailed;
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }

        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
            statusCode = StatusCode.DatabaseError;
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

}
