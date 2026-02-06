package gov.nist.oism.asd.empbc.db;

/* File: UserPrefsDao.java
 * Author: PPG
 * Create Date: October 2020
 * Purpose: Allow user to set the preferences.
 */
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import gov.nist.oism.asd.empbc.model.UserPrefs;

public class UserPrefsDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(UserPrefsDao.class.getSimpleName());

    public static final String PERMISSION_STATE_KEY = "permission_state_key";
    public static final String ERROR_CODE_KEY = "error_code_key";
    public static final String ERROR_MESSAGE_KEY = "error_message_key";
    public static final String USER_PREFS_KEY = "user_prefs_key";

    //type 1: reminder email; type 2: bk total < cbs total notification
    public Map<String, Object> getUserPrefs(Integer peopleId, Integer type) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (peopleId == null || peopleId < 1) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "select pref_value from preferences where pref_type_id = ? and people_id = ?";
        LOG.info(String.format("sql: %s, peopleId: %d", sql, peopleId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, type);
            pstmt.setInt(2, peopleId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    results.put(USER_PREFS_KEY, rset.getString("pref_value"));
                } else {
                    results.put(USER_PREFS_KEY, "");
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> setUserPrefs(UserPrefs userPrefs) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "select count (*) cnt from preferences where people_id = ? and pref_type_id = ?";
        String sql1 = "insert into preferences (pref_value, people_id, pref_type_id) values (?,?,?)";
        String sql2 = "update preferences set pref_value = ? where people_id = ? and pref_type_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        int cnt = 0;

        try {
            conn = getConnection(true);
            pstmt = conn.prepareStatement(sql); 
            pstmt.setInt(1, userPrefs.getPeopleId()); 
            pstmt.setInt(2, userPrefs.getPrefTypeId());
            rset = pstmt.executeQuery();

            if (rset.next()) {
                cnt = rset.getInt("cnt");
            }

            if (cnt < 1) {
                sql = sql1;
            } else {
                sql = sql2;
            }
            LOG.info(String.format("sql: %s, peopleId: %d", sql, userPrefs.getPeopleId()));

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userPrefs.getPrefValue());
            pstmt.setInt(2, userPrefs.getPeopleId());
            pstmt.setInt(3, userPrefs.getPrefTypeId());
            pstmt.executeUpdate();
        } catch (Exception caught) {
            System.out.println("ex occurs in user prefs dao");
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    
    public Map<String, Object> deleteUserPrefs(UserPrefs userPrefs) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        Integer peopleId= userPrefs.getPeopleId(); 
        Integer type= userPrefs.getPrefTypeId();
        if (peopleId == null || peopleId < 1) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "delete from preferences where pref_type_id = ? and people_id = ?";
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, type);
            pstmt.setInt(2, peopleId);
            rowCount = pstmt.executeUpdate();
            results.put(USER_PREFS_KEY, rowCount);
            
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
}
