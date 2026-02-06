/**
 * Assuming your model class uses camel case (e.g. requestId) and your oracle column name uses the 
 * underscore naming convention (e.g. REQUEST_ID), this helper class reduce the code  to set value
 * for each variable after a resultSet is returned from execution of a SQL statement
 **/
package gov.nist.oism.asd.empbc.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.text.CaseUtils;

public class ResultSetMapper<T> {

    private static final Logger LOG = Logger.getLogger(ResultSetMapper.class.getSimpleName());

    @SuppressWarnings("unchecked")
    public List<T> mapResultSetToObject(ResultSet rs, Class outputClass) {
        List<T> outputList = null;
        //ConvertUtils
       //         .register(new MyDateConverter(), java.util.Date.class);
        try {
            // make sure resultset is not null
            if (rs != null) {
                // get the resultset metadata
                ResultSetMetaData rsmd = rs.getMetaData();
                // get all the attributes of outputClass
                Field[] fields = outputClass.getDeclaredFields();
                while (rs.next()) {
                    T bean;
                    bean = (T) outputClass.getDeclaredConstructor().newInstance();
                    for (int _iterator = 0; _iterator < rsmd
                            .getColumnCount(); _iterator++) {
                        // getting the SQL column name
                        String columnName = rsmd
                                .getColumnName(_iterator + 1);
                        int columnType = rsmd.getColumnType(_iterator + 1);
                        // reading the value of the SQL column
                        Object columnValue = rs.getObject(_iterator + 1);
                        //convert oracle column name to camelCase
                        columnName = CaseUtils.toCamelCase(columnName, false, new char[]{'_'});
                        // iterating over outputClass attributes with matching 'name' value
                        for (Field field : fields) {
                            if (field.getName().equalsIgnoreCase(
                                    columnName)
                                    && columnValue != null) {
                                //handles timestamp type
                                if (columnType == Types.TIMESTAMP) {
                                    java.util.Date date;
                                    Timestamp timestamp = rs.getTimestamp(_iterator + 1);
                                    if (timestamp != null) {
                                        date = new java.util.Date(timestamp.getTime());
                                        BeanUtils.setProperty(bean, field
                                                .getName(), date);
                                    }
                                } else {
                                    BeanUtils.setProperty(bean, field
                                            .getName(), columnValue);
                                }
                                break;
                            }
                        }
                    }
                    if (outputList == null) {
                        outputList = new ArrayList<T>();
                    }
                    outputList.add(bean);
                }

            } else {
                return null;
            }
        } catch (IllegalAccessException | SQLException | InstantiationException | InvocationTargetException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ResultSetMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return outputList;
    }
}
