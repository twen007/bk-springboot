/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
/**
 *
 * @author xinweiw
 */
public class PreparedStatementUtil {
     /**
     * Sets an Integer value to the PreparedStatement at the specified index.
     * If the Integer is null, it sets the parameter to null.
     *
     * @param pstmt the PreparedStatement
     * @param index the index of the parameter to set (1-based)
     * @param intValue the Integer value to set
     * @throws SQLException if a database access error occurs
     */
    public static void setIntegerValue(PreparedStatement pstmt, int index, Integer intValue) throws SQLException {
        if (intValue != null) {
            pstmt.setInt(index, intValue);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }
    
     /**
     * Sets a string value to the PreparedStatement at the specified index.
     * If the string is null or empty, it sets the parameter to null.
     *
     * @param pstmt the PreparedStatement
     * @param index the index of the parameter to set (1-based)
     * @param strVal the string value to set
     * @throws SQLException if a database access error occurs
     */
    public static void setStringValue(PreparedStatement pstmt, int index, String strVal) throws SQLException {
        if (strVal != null && !strVal.isEmpty()) {
            pstmt.setString(index, strVal.trim());
        } else {
            pstmt.setNull(index, Types.VARCHAR);
        }
    }
    
   /**
     * Sets a Timestamp value to the PreparedStatement at the specified index.
     * If the Timestamp is null, it sets the parameter to null.
     *
     * @param pstmt the PreparedStatement
     * @param index the index of the parameter to set (1-based)
     * @param timestamp the Timestamp value to set
     * @throws SQLException if a database access error occurs
     */
    public static void setTimestampValue(PreparedStatement pstmt, int index, java.util.Date timestamp) throws SQLException {
        if (timestamp != null) {
            pstmt.setTimestamp(index, new Timestamp(timestamp.getTime()));
        } else {
            pstmt.setNull(index, Types.TIMESTAMP);
        }
    }

    /**
     * Sets a Double value to the PreparedStatement at the specified index.
     * If the Double is null, it sets the parameter to null.
     *
     * @param pstmt the PreparedStatement
     * @param index the index of the parameter to set (1-based)
     * @param doubleValue the Double value to set
     * @throws SQLException if a database access error occurs
     */
    public static void setDoubleValue(PreparedStatement pstmt, int index, Double doubleValue) throws SQLException {
        if (doubleValue != null) {
            pstmt.setDouble(index, doubleValue);
        } else {
            pstmt.setNull(index, Types.DOUBLE);
        }
    }
}
