package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.ObjectClass;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectClassDao extends OracleDao {
    
    private static final Logger LOG = Logger.getLogger(ObjectClassDao.class.getSimpleName());
    
    public static final String OBJECT_CLASS_LIST_KEY = "object_class_list_key";
    
    public Map<String, Object> selectObjectClassesWithFilter(String filter) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        boolean useFilter = filter != null && !filter.isEmpty();
        String sql;
        if (useFilter) {
            sql = "SELECT " +
                   "code, " +
                   "description" +
                   " FROM " +
                   "v_objclass" +
                   " WHERE " +
                   "code LIKE ?";
        }
        else {
            sql = "SELECT " +
                   "code, " +
                   "description" +
                   " FROM " +
                   "v_objclass";
        }
        sql=sql+" order by code";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
             PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<ObjectClass> objectClasses = new ArrayList<>();
            if (useFilter) {
                pstmt.setString(1, filter + "%");
            }
            try (ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    ObjectClass objectClass = new ObjectClass();

                    // Code.
                    objectClass.setCode(rset.getString("code"));

                    // Description.
                    objectClass.setDescription(rset.getString("description"));

                    objectClasses.add(objectClass);
                }
            }
            
            results.put(OBJECT_CLASS_LIST_KEY, objectClasses);
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
}
