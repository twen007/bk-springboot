package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileAttachmentDao extends OracleDao {
    
    private static final Logger LOG = Logger.getLogger(FileAttachmentDao.class.getSimpleName());
    private static final Integer SHOPPING_CART_CATEGORY_ID = 5;
    
    public static final String FILE_ATTACHMENT_KEY = "file_attachment_key";
    public static final String FILE_ID_KEY = "file_id_key";
    public static final String FILE_ATTACHMENT_LIST_KEY = "file_attachment_list_key";
    
    public Map<String, Object> selectFileAttachment(Integer fileId, boolean withContents) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (fileId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        try (Connection connection = getConnection(true);) {
            FileAttachment fileAttachment;
            if (withContents) {
                fileAttachment = selectFileAttachmentWithContents(connection, fileId);
            }
            else {
                fileAttachment = selectFileAttachmentWithoutContents(connection, fileId);
            }
            
            results.put(FILE_ATTACHMENT_KEY, fileAttachment);
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    public Map<String, Object> selectFileAttachmentsWithContentForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        
        String sql = "SELECT " +
                     "file_id, " +
                     "request_id, " +
                     "file_category_id, " +
                     "file_name, " +
                     "file_type_code, " +
                     "file_size, " +
                     "file_content " +
                     " FROM " +
                     "file_attachment " +
                     " WHERE " +
                     "request_id = ?";
        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        List<FileAttachment> fileAttachments = new ArrayList<>();
        try (Connection connection = getConnection(true);
             PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            try (ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    FileAttachment fileAttachment = new FileAttachment();
                    
                    // File id.
                    fileAttachment.setId(rset.getInt("file_id"));

                    // Request id.
                    requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setRequestId(requestId);
                    }

                    // File category id.
                    int categoryId = rset.getInt("file_category_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setCategoryId(categoryId);
                    }

                    // File name.
                    fileAttachment.setName(rset.getString("file_name"));

                    // File type code.
                    fileAttachment.setTypeCode(rset.getString("file_type_code"));

                    // File size.
                    int size = rset.getInt("file_size");
                    if (!rset.wasNull()) {
                        fileAttachment.setSize(size);
                    }
                    
                    // File content.
                    Blob blob = rset.getBlob("file_content");
                    if (blob != null) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        BufferedInputStream in = new BufferedInputStream(blob.getBinaryStream());
                        byte[] buffer = new byte[1024];
                        int length = 0;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        in.close();
                        blob.free();
                        fileAttachment.setContent(out.toByteArray());
                    }
                    
                    fileAttachments.add(fileAttachment);
                }
            }
            results.put(FILE_ATTACHMENT_LIST_KEY, fileAttachments);
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
     public Map<String, Object> selectFileAttachmentsWithContentByIds(List<Integer> fileIds) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        if (fileIds == null || fileIds.isEmpty()) {
            LOG.warning("selectFileAttachmentsWithContentByIds called with null or empty fileIds list.");
            statusCode = StatusCode.IncompleteData; // Or another appropriate status code
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        // Build the SQL query with a dynamic IN clause
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        sqlBuilder.append("file_id, ");
        sqlBuilder.append("request_id, ");
        sqlBuilder.append("file_category_id, ");
        sqlBuilder.append("file_name, ");
        sqlBuilder.append("file_type_code, ");
        sqlBuilder.append("file_size, ");
        sqlBuilder.append("file_content ");
        sqlBuilder.append(" FROM ");
        sqlBuilder.append("file_attachment ");
        sqlBuilder.append(" WHERE ");
        sqlBuilder.append("file_id IN (");

        // Add '?' placeholders for each file ID
        for (int i = 0; i < fileIds.size(); i++) {
            sqlBuilder.append("?");
            if (i < fileIds.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();

        // Log the SQL and parameters (be cautious with logging sensitive data in production)
        LOG.info(String.format("sql: %s, file_ids: %s", sql, fileIds.toString()));

        List<FileAttachment> fileAttachments = new ArrayList<>();
        try (Connection connection = getConnection(true);
             PreparedStatement pstmt = connection.prepareStatement(sql);) {

            // Set the file ID parameters in the prepared statement
            for (int i = 0; i < fileIds.size(); i++) {
                pstmt.setInt(i + 1, fileIds.get(i));
            }

            try (ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    FileAttachment fileAttachment = new FileAttachment();

                    // File id.
                    fileAttachment.setId(rset.getInt("file_id"));

                    // Request id.
                    // Note: This will get the request_id associated with *each* file.
                    // If you only need the list of files, you might not need the request_id here.
                    // Keeping it for now as it was in the original method.
                    Integer requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setRequestId(requestId);
                    }

                    // File category id.
                    int categoryId = rset.getInt("file_category_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setCategoryId(categoryId);
                    }

                    // File name.
                    fileAttachment.setName(rset.getString("file_name"));

                    // File type code.
                    fileAttachment.setTypeCode(rset.getString("file_type_code"));

                    // File size.
                    int size = rset.getInt("file_size");
                    if (!rset.wasNull()) {
                        fileAttachment.setSize(size);
                    }

                    // File content.
                    Blob blob = rset.getBlob("file_content");
                    if (blob != null) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        BufferedInputStream in = new BufferedInputStream(blob.getBinaryStream());
                        byte[] buffer = new byte[1024];
                        int length = 0;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        // It's good practice to close streams
                        in.close();
                        // Free the blob resource
                        blob.free();
                        fileAttachment.setContent(out.toByteArray());
                    }

                    fileAttachments.add(fileAttachment);
                }
            }
            results.put(FILE_ATTACHMENT_LIST_KEY, fileAttachments);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, "Error retrieving file attachments by IDs: " + fileIds.toString(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    
    public Map<String, Object> selectFileAttachmentsForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        
        String sql = "SELECT " +
                     "file_id, " +
                     "request_id, " +
                     "fa.file_category_id AS file_category_id, " +
                     "file_description, " +
                     "lfc.file_category_name AS file_category_name, " +
                     "file_name, " +
                     "file_type_code, " +
                     "file_size, " +
                     "created_by, " +
                     "get_user_name(created_by) AS created_by_name, " +
                     "created_date" +
                     " FROM " +
                     "file_attachment fa, lkup_file_category lfc" +
                     " WHERE " +
                     "request_id = ?" +
                     " AND " +
                     "fa.file_category_id = lfc.file_category_id";
        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        List<FileAttachment> fileAttachments = new ArrayList<>();
        try (Connection connection = getConnection(true);
             PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            try (ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    FileAttachment fileAttachment = new FileAttachment();
                    
                    // File id.
                    fileAttachment.setId(rset.getInt("file_id"));

                    // Request id.
                    requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setRequestId(requestId);
                    }

                    // File category id.
                    int categoryId = rset.getInt("file_category_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setCategoryId(categoryId);
                    }
                    
                     if(categoryId==3 && rset.getString("file_description")!=null && rset.getString("file_description").trim().length()>0){
                        fileAttachment.setCategoryName(rset.getString("file_description"));
                    }else{
                        // File category name.
                        fileAttachment.setCategoryName(rset.getString("file_category_name"));
                    }

                    // File name.
                    fileAttachment.setName(rset.getString("file_name"));

                    // File type code.
                    fileAttachment.setTypeCode(rset.getString("file_type_code"));

                    // File size.
                    int size = rset.getInt("file_size");
                    if (!rset.wasNull()) {
                        fileAttachment.setSize(size);
                    }

                    // Created by.
                    int createdBy = rset.getInt("created_by");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedBy(createdBy);
                    }
                    
                    // Created by name.
                    fileAttachment.setCreatedByName(rset.getString("created_by_name"));

                    // Created date.
                    Timestamp createdDate = rset.getTimestamp("created_date");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedDate(createdDate);
                    }
                    
                    fileAttachments.add(fileAttachment);
                }
            }
            results.put(FILE_ATTACHMENT_LIST_KEY, fileAttachments);
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    public Map<String, Object> insertAttachment(FileAttachment fileAttachment) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (fileAttachment.getRequestId() == null || fileAttachment.getContent() == null || fileAttachment.getCreatedBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        
        try (Connection connection = getConnection(false);) {
            results = insertAttachmentWithConnection(fileAttachment, connection);
            statusCode = (StatusCode) results.get(STATUS_CODE_KEY);
            if (statusCode == StatusCode.OK) {
                connection.commit();
            }
            else {
                connection.rollback();
            }
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    public Map<String, Object> deleteAttachment(Integer fileId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (fileId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql;
        try (Connection connection = getConnection(false);) {
            FileAttachment shoppingCart = selectFileAttachmentWithoutContents(connection, fileId);
            
            // It is assumed there is only 1 shopping cart per request.
            if (shoppingCart != null && SHOPPING_CART_CATEGORY_ID.equals(shoppingCart.getCategoryId())) {
                if (shoppingCart.getRequestId() == null || shoppingCart.getId() == null) {
                    statusCode = StatusCode.IncompleteData;
                }
                
                // Delete from item.
                if (statusCode == StatusCode.OK) {
                    sql = "DELETE FROM item WHERE request_id = ?";
                    LOG.info(String.format("sql: %s, id:%d", sql, shoppingCart.getRequestId()));
                    try (PreparedStatement pstmt = connection.prepareStatement(sql);) {
                        pstmt.setInt(1, shoppingCart.getRequestId());
                        pstmt.executeUpdate();
                    }
                    catch (Exception caught) {
                        statusCode = StatusCode.DatabaseError;
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }
                }
                
                // Update request.
                if (statusCode == StatusCode.OK) {
                    sql = "UPDATE request" +
                          " SET " +
                          "is_shopping_cart = 'N'" +
                          " WHERE " +
                          "request_id = ?";
                    LOG.info(String.format("sql: %s, request_id:%d", sql, shoppingCart.getRequestId()));
                    try (PreparedStatement pstmt = connection.prepareStatement(sql);) {
                        pstmt.setInt(1, shoppingCart.getRequestId());
                        pstmt.executeUpdate();
                    }
                    catch (Exception caught) {
                        statusCode = StatusCode.DatabaseError;
                        LOG.log(Level.SEVERE, caught.getMessage(), caught);
                    }
                }
            }
            
            // Now delete, after taking care of a shopping cart.
            if (statusCode == StatusCode.OK) {
                sql = "DELETE FROM" +
                      " file_attachment " +
                      " WHERE " +
                      "file_id = ?";
                LOG.info(String.format("sql: %s, fileId: %d", sql, fileId));
                try (PreparedStatement pstmt = connection.prepareStatement(sql);) {
                    pstmt.setInt(1, fileId);
                    results.put(ROW_COUNT_KEY, pstmt.executeUpdate());
                }
                catch (Exception caught) {
                    statusCode = StatusCode.DatabaseError;
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }
            }
            
            if (statusCode == StatusCode.OK) {
                connection.commit();
            }
            else {
                connection.rollback();
            }
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
    
    private FileAttachment selectFileAttachmentWithoutContents(Connection connection, int fileId) throws SQLException, IOException {
        FileAttachment fileAttachment = null;
        String sql = "SELECT " +
                     "file_id, " +
                     "request_id, " +
                     "fa.file_category_id AS file_category_id, " +
                     "file_description, " +
                     "lfc.file_category_name AS file_category_name, " +
                     "file_name, " +
                     "file_type_code, " +
                     "file_size, " +
                     "created_by, " +
                     "get_user_name(created_by) AS created_by_name, " +
                     "created_date" +
                     " FROM " +
                     "file_attachment fa, lkup_file_category lfc" +
                     " WHERE " +
                     "file_id = ?" +
                     " AND " +
                     "fa.file_category_id = lfc.file_category_id";
        LOG.info(String.format("sql: %s, file_id: %d", sql, fileId));
        try (PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, fileId);
            try (ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    fileAttachment = new FileAttachment();
                    
                    // File id.
                    fileAttachment.setId(rset.getInt("file_id"));

                    // Request id.
                    int requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setRequestId(requestId);
                    }

                    // File category id.
                    int categoryId = rset.getInt("file_category_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setCategoryId(categoryId);
                    }
                    
                    if(categoryId==3 && rset.getString("file_description")!=null && rset.getString("file_description").trim().length()>0){
                        fileAttachment.setCategoryName(rset.getString("file_description"));
                    }else{
                        // File category name.
                        fileAttachment.setCategoryName(rset.getString("file_category_name"));
                    }

                    // File name.
                    fileAttachment.setName(rset.getString("file_name"));

                    // File type code.
                    fileAttachment.setTypeCode(rset.getString("file_type_code"));

                    // File size.
                    int size = rset.getInt("file_size");
                    if (!rset.wasNull()) {
                        fileAttachment.setSize(size);
                    }
                    
                    // Created by.
                    int createdBy = rset.getInt("created_by");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedBy(createdBy);
                    }
                    
                    // Created by name.
                    fileAttachment.setCreatedByName(rset.getString("created_by_name"));

                    // Created date.
                    Timestamp createdDate = rset.getTimestamp("created_date");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedDate(createdDate);
                    }
                }
            }
        }
        return fileAttachment;
    }
    
    private FileAttachment selectFileAttachmentWithContents(Connection connection, int fileId) throws SQLException, IOException {
        FileAttachment fileAttachment = null;
        String sql = "SELECT " +
                     "file_id, " +
                     "request_id, " +
                     "fa.file_category_id AS file_category_id, " +
                     "file_description, " +
                     "lfc.file_category_name AS file_category_name, " +
                     "file_name, " +
                     "file_type_code, " +
                     "file_size, " +
                     "file_content, " +
                     "created_by, " +
                     "get_user_name(created_by) AS created_by_name, " +
                     "created_date" +
                     " FROM " +
                     "file_attachment fa, lkup_file_category lfc" +
                     " WHERE " +
                     "file_id = ?" +
                     " AND " +
                     "fa.file_category_id = lfc.file_category_id";
        LOG.info(String.format("sql: %s, file_id: %d", sql, fileId));
        try (PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, fileId);
            try (ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    fileAttachment = new FileAttachment();
                    
                    // File id.
                    fileAttachment.setId(rset.getInt("file_id"));

                    // Request id.
                    int requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setRequestId(requestId);
                    }

                    // File category id.
                    int categoryId = rset.getInt("file_category_id");
                    if (!rset.wasNull()) {
                        fileAttachment.setCategoryId(categoryId);
                    }
                    
                    if(categoryId==3 && rset.getString("file_description")!=null && rset.getString("file_description").trim().length()>0){
                        fileAttachment.setCategoryName(rset.getString("file_description"));
                    }else{
                        // File category name.
                        fileAttachment.setCategoryName(rset.getString("file_category_name"));
                    }

                    // File name.
                    fileAttachment.setName(rset.getString("file_name"));

                    // File type code.
                    fileAttachment.setTypeCode(rset.getString("file_type_code"));

                    // File size.
                    int size = rset.getInt("file_size");
                    if (!rset.wasNull()) {
                        fileAttachment.setSize(size);
                    }

                    // File content.
                    Blob blob = rset.getBlob("file_content");
                    if (blob != null) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        BufferedInputStream in = new BufferedInputStream(blob.getBinaryStream());
                        byte[] buffer = new byte[1024];
                        int length = 0;
                        while ((length = in.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        in.close();
                        blob.free();
                        fileAttachment.setContent(out.toByteArray());
                    }

                    // Created by.
                    int createdBy = rset.getInt("created_by");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedBy(createdBy);
                    }
                    
                    // Created by name.
                    fileAttachment.setCreatedByName(rset.getString("created_by_name"));

                    // Created date.
                    Timestamp createdDate = rset.getTimestamp("created_date");
                    if (!rset.wasNull()) {
                        fileAttachment.setCreatedDate(createdDate);
                    }
                }
            }
        }
        return fileAttachment;
    }
    
    
    protected Map<String, Object> insertAttachmentWithConnection(FileAttachment fileAttachment, Connection connection) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (fileAttachment.getRequestId() == null || fileAttachment.getContent() == null || fileAttachment.getCreatedBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        
        String sql = "INSERT INTO" +
                     " file_attachment " +
                     "(" +
                     "request_id, " +
                     "file_category_id, " +
                     "file_name, " +
                     "file_type_code, " +
                     "file_size, " +
                     "file_content, " +
                     "created_by, " +
                     "created_date, " +
                     "file_description" +
                     ")" +
                     " VALUES " +
                     "(?, ?, ?, ?, ?, ?, ?, SYSDATE, ?)";
        LOG.info(String.format("sql: %s", sql));
        try (PreparedStatement pstmt = connection.prepareStatement(sql, new String[] { "file_id" });) {
            
            // Request id.
            if (fileAttachment.getRequestId() != null) {
                pstmt.setInt(1, fileAttachment.getRequestId());
            }
            else {
                pstmt.setNull(1, Types.INTEGER);
            }

            // File category id.
            if (fileAttachment.getCategoryId() != null ) {
                pstmt.setInt(2, fileAttachment.getCategoryId());
            }
            else {
                pstmt.setNull(2, Types.INTEGER);
            }

            // File name.
            if (fileAttachment.getName() != null && !fileAttachment.getName().isEmpty()) {
                pstmt.setString(3, fileAttachment.getName().trim());
            }
            else {
                pstmt.setNull(3, Types.VARCHAR);
            }

            // File type code.
            if (fileAttachment.getTypeCode() != null && !fileAttachment.getTypeCode().isEmpty()) {
                pstmt.setString(4, fileAttachment.getTypeCode().trim());
            }
            else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // File size.
            if (fileAttachment.getSize() != null) {
                pstmt.setInt(5, fileAttachment.getSize());
            }
            else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            // File content.
            if (fileAttachment.getContent() != null) {
                ByteArrayInputStream in = new ByteArrayInputStream(fileAttachment.getContent());
                pstmt.setBinaryStream(6, in, fileAttachment.getContent().length);
            }
            else {
                pstmt.setNull(6, Types.BLOB);
            }
            
            // Created by.
            if (fileAttachment.getCreatedBy() != null) {
                pstmt.setInt(7, fileAttachment.getCreatedBy());
            }
            else {
                pstmt.setNull(7, Types.INTEGER);
            }
            
             if (fileAttachment.getCategoryName() != null && !fileAttachment.getCategoryName().isEmpty()) {
                pstmt.setString(8, fileAttachment.getCategoryName().trim());
            }
            else {
                pstmt.setNull(8, Types.VARCHAR);
            }
            
            // Do the insert and get back the generated key.
            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.InsertFailed;
            }
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                Integer fileId = generatedKeys.getInt(1);
                results.put(FILE_ID_KEY, fileId);
                generatedKeys.close();
            }
        }
        catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }
        
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }
}
