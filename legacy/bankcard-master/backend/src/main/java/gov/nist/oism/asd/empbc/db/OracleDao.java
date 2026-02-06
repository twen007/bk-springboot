package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.util.CommonUtil;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class OracleDao {

    public static final String STATUS_CODE_KEY = "status_code_key";
    public static final String ROW_COUNT_KEY = "row_count_key";

    private static final Logger LOG = Logger.getLogger(OracleDao.class.getSimpleName());

    public static Connection getStaticConnection() {
        return null;
    }

    public StatusCode testConnection() {
        StatusCode statusCode = StatusCode.OK;
        String sql = "select sysdate from dual";

        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            try ( ResultSet rset = pstmt.executeQuery()) {
                if (!rset.next()) {
                    statusCode = StatusCode.DatabaseNotRespondingError;
                }
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseConnectionError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        return statusCode;
    }

    protected Connection getConnection(boolean autoCommit) {
        Connection connection = null;

        // First try a web application pooled connection.
        try {
            Context context = (Context) new InitialContext();
            DataSource dataSource;
            try {
                dataSource = (DataSource) context.lookup("java:comp/env/jdbc/bcws");
                connection = dataSource.getConnection();
                connection.setAutoCommit(autoCommit);
            } catch (NamingException ignore) {
            }
            return connection;
        } catch (SQLException | NamingException caught) {
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        // If that doesn't work, get one directly.
        return getStaticConnection();
    }

    protected String getString(ResultSet rset, String key) throws SQLException {
        String value = rset.getString(key);
        if (rset.wasNull()) {
            value = null;
        }
        return value;
    }

    protected void setString(PreparedStatement pstmt, int index, String value) throws SQLException {
        if (!CommonUtil.isStringNullOrEmpty(value)) {
            pstmt.setString(index, value.trim());
        } else {
            pstmt.setNull(index, Types.VARCHAR);
        }
    }

    protected Boolean intToBool(Integer value) {
        return value != null && value == 1;
    }

    protected Integer boolToInt(Boolean value) {
        return value != null && value ? 1 : 0;
    }

    //NOTE: we may need to update these two method to return null when value is null
    //which could be reflected in the UI as users hasn't select any value in the radio group
    protected Boolean StringToBool(String value) {
        return value != null && "Y".equals(value);
    }

    protected String boolToString(Boolean value) {
        return value != null && value ? "Y" : "N";
    }

    protected Integer getInt(ResultSet rset, String key) throws SQLException {
        Integer value = rset.getInt(key);
        if (rset.wasNull()) {
            value = null;
        }
        return value;
    }

    protected void setInt(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value != null) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    protected Double getDouble(ResultSet rset, String key) throws SQLException {
        Double value = rset.getDouble(key);
        if (rset.wasNull()) {
            value = null;
        }
        return value;
    }

    protected void setDouble(PreparedStatement pstmt, int index, Double value) throws SQLException {
        if (value != null) {
            pstmt.setDouble(index, value);
        } else {
            pstmt.setNull(index, Types.DOUBLE);
        }
    }

    protected Timestamp getTimestamp(ResultSet rset, String key) throws SQLException {
        Timestamp value = rset.getTimestamp(key);
        if (rset.wasNull()) {
            value = null;
        }
        return value;
    }

    protected void setTimestamp(PreparedStatement pstmt, int index, Date value) throws SQLException {
        if (value != null) {
            pstmt.setTimestamp(index, new Timestamp(value.getTime()));
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }

    protected void setBooleanYNOrDefaultParam(PreparedStatement pstmt, int index, Boolean value, String defaultStringIfNull) throws SQLException {
        if (value != null) {
            pstmt.setString(index, value ? "Y" : "N");
        } else {
            pstmt.setString(index, defaultStringIfNull);
        }
    }

    protected void setStringParam(CallableStatement cstmt, String paramName, String value) throws SQLException {
        if (value != null && !value.trim().isEmpty()) { // Consider if empty string after trim should be NULL
            cstmt.setString(paramName, value.trim());
        } else {
            cstmt.setNull(paramName, Types.VARCHAR);
        }
    }

    protected void setIntParam(CallableStatement cstmt, String paramName, Integer value) throws SQLException {
        if (value != null) {
            cstmt.setInt(paramName, value);
        } else {
            cstmt.setNull(paramName, Types.INTEGER);
        }
    }

    protected void setDoubleParam(CallableStatement cstmt, String paramName, Double value) throws SQLException {
        if (value != null) {
            cstmt.setDouble(paramName, value);
        } else {
            cstmt.setNull(paramName, Types.DOUBLE);
        }
    }

    protected void setTimestampParam(CallableStatement cstmt, String paramName, Date value) throws SQLException {
        if (value != null) {
            cstmt.setTimestamp(paramName, new Timestamp(value.getTime()));
        } else {
            cstmt.setNull(paramName, Types.TIMESTAMP);
        }
    }

    protected void setBooleanYNParam(CallableStatement cstmt, String paramName, Boolean value) throws SQLException {
        if (value != null) {
            cstmt.setString(paramName, value ? "Y" : "N");
        } else {
            cstmt.setNull(paramName, Types.VARCHAR); // Or set to "N" by default if preferred
        }
    }

    protected void setBooleanYNOrDefaultParam(CallableStatement cstmt, String paramName, Boolean value, String defaultStringIfNull) throws SQLException {
        if (value != null) {
            cstmt.setString(paramName, value ? "Y" : "N");
        } else {
            cstmt.setString(paramName, defaultStringIfNull);
        }
    }

    protected void setStringOrDefaultParam(CallableStatement cstmt, String paramName, String value, String defaultStringIfNullOrEmpty) throws SQLException {
        if (value != null && !value.trim().isEmpty()) {
            cstmt.setString(paramName, value.trim());
        } else {
            cstmt.setString(paramName, defaultStringIfNullOrEmpty);
        }
    }
}
