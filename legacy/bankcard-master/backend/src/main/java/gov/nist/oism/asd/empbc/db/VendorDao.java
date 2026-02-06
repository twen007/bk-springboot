package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.RequestJustification;
import gov.nist.oism.asd.empbc.model.RequestVendorT;
import gov.nist.oism.asd.empbc.model.VendorT;
import gov.nist.oism.asd.empbc.model.User;
import gov.nist.oism.asd.empbc.util.CommonUtil;
import gov.nist.oism.asd.empbc.util.ResultSetMapper;
import gov.nist.oism.asd.empbc.util.StatusCode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VendorDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(VendorDao.class.getSimpleName());

    public static final String VENDOR_ID_KEY = "vendor_id_key";
    public static final String VENDOR_LIST_KEY = "vendor_list_key";
    public static final String REQUEST_VENDOR_LIST_KEY = "request_vendor_list_key";

    public Map<String, Object> insertVendorT(VendorT vendor, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        Integer vendorId = 0;
        if (CommonUtil.isStringNullOrEmpty(vendor.getVendorName())) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "INSERT INTO"
                + " vendor_t "
                + "("
                + "vendor_name, "
                + "street, "
                + "city, "
                + "state, "
                + "zip, "
                + "web_url, "
                + "contact_person, "
                + "phone, "
                + "fax, "
                + "email, "
                + "account_number, "
                + "created_by, "
                + "created_date, "
                + "updated_by, "
                + "updated_date, "
                + "is_foreign_address, "
                + "foreign_address, "
                + "is_active, "
                + "duns_number, "
                + "ou_id, "
                + "division_id, "
                + "group_id, "
                + "shared, "
                + "vendor_id "
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, 1, ?, ?, ?, ?, ?,SEQ_VENDOR_T_ID.nextVal)";

        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"vendor_id"});) {

            setString(pstmt, 1, vendor.getVendorName());
            setString(pstmt, 2, vendor.getStreet());
            setString(pstmt, 3, vendor.getCity());
            setString(pstmt, 4, vendor.getState());
            setString(pstmt, 5, vendor.getZip());
            setString(pstmt, 6, vendor.getWebUrl());
            setString(pstmt, 7, vendor.getContactPerson());
            setString(pstmt, 8, vendor.getPhone());
            setString(pstmt, 9, vendor.getFax());
            setString(pstmt, 10, vendor.getEmail());
            setString(pstmt, 11, vendor.getAccountNumber());
            setInt(pstmt, 12, user.getPeopleId());
            setInt(pstmt, 13, user.getPeopleId());
            setString(pstmt, 14, boolToString(vendor.getIsForeignAddress()));
            setString(pstmt, 15, vendor.getForeignAddress());
            setString(pstmt, 16, vendor.getDunsNumber());
            setInt(pstmt, 17, user.getOuId());
            setInt(pstmt, 18, user.getDivisionId());
            setInt(pstmt, 19, vendor.getGroupId());
            //setInt(pstmt, 19, user.getGroupId());
            setInt(pstmt, 20, vendor.getShared());

            pstmt.execute();

            try (ResultSet rset = pstmt.getGeneratedKeys();) {

                if (rset.next()) {
                    vendorId = rset.getInt(1);
                    results.put(VENDOR_ID_KEY, vendorId);
                }
            } catch (Exception caught) {
                statusCode = StatusCode.InsertFailed;
                LOG.log(Level.SEVERE, caught.getMessage(), caught);
            }

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertRequestJustification(RequestJustification just, int requestId, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (requestId == 0) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "INSERT INTO REQUEST_JUSTIFICATION ("
                + "   REQUEST_ID, CONVENIENCE_CHECK, CONVENIENCE_CHECK_JUST, "
                + "   GSA_SCHEDULE, GSA_SCHEDULE_JUST, THIRD_PARTY_VENDOR, "
                + "   THIRD_PARTY_VENDOR_JUST, PRICE_IS_REASONABLE_JUST, SMALL_BUSINESS, "
                + "   SMALL_BUSINESS_JUST, CREATED_BY, CREATED_DATE, "
                + "   UPDATED_BY, UPDATED_DATE, DIVISION_ORG_ID, "
                + "   PROFESSIONAL_ORG, BUILT_IN_VENDOR, "
                + "   COMMERCIAL_VENDOR, COMMERCIAL_VENDOR_JUST ) "
                + "   VALUES (?,?,?,?,?,?,?,?,?,?,?,SYSDATE,?,SYSDATE,?,?,?,?,?) ";

        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {

            setInt(pstmt, 1, just.getRequestId());
            setString(pstmt, 2, boolToString(just.getConvenienceCheck()));
            setString(pstmt, 3, just.getConvenienceCheckJust());
            setString(pstmt, 4, boolToString(just.getGsaSchedule()));
            setString(pstmt, 5, just.getGsaScheduleJust());
            setString(pstmt, 6, boolToString(just.getThirdPartyVendor()));
            setString(pstmt, 7, just.getThirdPartyVendorJust());
            setString(pstmt, 8, just.getPriceIsReasonableJust());
            setString(pstmt, 9, boolToString(just.getSmallBusiness()));
            setString(pstmt, 10, just.getSmallBusinessJust());
            setInt(pstmt, 11, user.getPeopleId());
            setInt(pstmt, 12, user.getPeopleId());
            setInt(pstmt, 13, user.getDivisionId());
            setString(pstmt, 14, boolToString(just.getProfessionalOrg()));
            setInt(pstmt, 15, just.getBuiltInVendor());
            setString(pstmt, 16, boolToString(just.getCommercialVendor()));
            setString(pstmt, 17, just.getCommercialVendorJust());

            rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertRequestVendorT(RequestVendorT vendor, int requestId, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (requestId == 0 || CommonUtil.isStringNullOrEmpty(vendor.getVendorName())) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "INSERT INTO"
                + " request_vendor_t "
                + "("
                + "vendor_name, "
                + "street, "
                + "city, "
                + "state, "
                + "zip, "
                + "web_url, "
                + "contact_person, "
                + "phone, "
                + "fax, "
                + "email, "
                + "account_number, "
                + "created_by, "
                + "created_date, "
                + "updated_by, "
                + "updated_date, "
                + "is_foreign_address, "
                + "foreign_address, "
                + "duns_number, "
                + "ref_vendor_id, "
                + "CONVENIENCE_CHECK, "
                + "ADDITIONAL_INFO, "
                + "request_id "
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, ?, ?, ?, ?, ?)";

        String sql2 = "INSERT INTO"
                + " request_justification "
                + "("
                + "request_id, "
                + "convenience_check, "
                + "convenience_check_just, "
                + "created_by, "
                + "created_date, "
                + "updated_by, "
                + "updated_date, "
                + "division_org_id "
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?)";

        String sql3 = "INSERT INTO"
                + " request_justification "
                + "("
                + "request_id, "
                + "built_in_vendor, "
                + "created_by, "
                + "created_date, "
                + "updated_by, "
                + "updated_date, "
                + "division_org_id "
                + ")"
                + " VALUES "
                + "(?, ?, ?, SYSDATE, ?, SYSDATE, ?)";

        String sql4 = "Delete from request_justification WHERE request_id = ?";

        //if user create a convenience check vendor, we also need to create a justification record
        Boolean needExecSecQuery = vendor.getConvenienceCheck() == null ? false : vendor.getConvenienceCheck();
        Boolean needExecThirdQuery = vendor.getVendorId() == null ? false : vendor.getVendorId() < 0;
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(!(needExecSecQuery || needExecThirdQuery));
                PreparedStatement pstmt = connection.prepareStatement(sql);) {

            setString(pstmt, 1, vendor.getVendorName());
            setString(pstmt, 2, vendor.getStreet());
            setString(pstmt, 3, vendor.getCity());
            setString(pstmt, 4, vendor.getState());
            setString(pstmt, 5, vendor.getZip());
            setString(pstmt, 6, vendor.getWebUrl());
            setString(pstmt, 7, vendor.getContactPerson());
            setString(pstmt, 8, vendor.getPhone());
            setString(pstmt, 9, vendor.getFax());
            setString(pstmt, 10, vendor.getEmail());
            setString(pstmt, 11, vendor.getAccountNumber());
            setInt(pstmt, 12, user.getPeopleId());
            setInt(pstmt, 13, user.getPeopleId());
            setString(pstmt, 14, boolToString(vendor.getIsForeignAddress()));
            setString(pstmt, 15, vendor.getForeignAddress());
            setString(pstmt, 16, vendor.getDunsNumber());
            setInt(pstmt, 17, vendor.getVendorId());
            setString(pstmt, 18, boolToString(vendor.getConvenienceCheck()));
            setString(pstmt, 19, vendor.getAdditionalInfo());
            setInt(pstmt, 20, requestId);

            rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

            if (needExecSecQuery || needExecThirdQuery) {
                //for these conditions, since the insert auto creates justification record, we need to 
                //make sure no existing jsutification record by delete any first
                PreparedStatement pstmt4 = connection.prepareStatement(sql4);
                pstmt4.setInt(1, requestId);
                rowCount = pstmt4.executeUpdate();
            }
            if (needExecSecQuery) {
                try {
                    PreparedStatement pstmt2 = connection.prepareStatement(sql2);
                    setInt(pstmt2, 1, requestId);
                    setString(pstmt2, 2, boolToString(vendor.getConvenienceCheck()));
                    setString(pstmt2, 3, vendor.getConvenienceCheckJust());
                    setInt(pstmt2, 4, user.getPeopleId());
                    setInt(pstmt2, 5, user.getPeopleId());
                    setInt(pstmt2, 6, user.getDivisionId());
                    rowCount = pstmt2.executeUpdate();
                } catch (Exception caught) {
                    statusCode = StatusCode.DatabaseError;
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }

                if (statusCode == StatusCode.OK) {
                    results.put(ROW_COUNT_KEY, rowCount++);
                    connection.commit();
                } else {
                    connection.rollback();
                    results.put(ROW_COUNT_KEY, 0);
                }

            } else if (needExecThirdQuery) {
                try {
                    PreparedStatement pstmt3 = connection.prepareStatement(sql3);
                    setInt(pstmt3, 1, requestId);
                    setInt(pstmt3, 2, vendor.getVendorId());
                    setInt(pstmt3, 3, user.getPeopleId());
                    setInt(pstmt3, 4, user.getPeopleId());
                    setInt(pstmt3, 5, user.getDivisionId());
                    rowCount = pstmt3.executeUpdate();
                } catch (Exception caught) {
                    statusCode = StatusCode.DatabaseError;
                    LOG.log(Level.SEVERE, caught.getMessage(), caught);
                }

                if (statusCode == StatusCode.OK) {
                    results.put(ROW_COUNT_KEY, rowCount++);
                    connection.commit();
                } else {
                    connection.rollback();
                    results.put(ROW_COUNT_KEY, 0);
                }

            } else {
                results.put(ROW_COUNT_KEY, rowCount);
            }

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    /**
     * Not Used (we take the whole record of vendor_t from UI and use the insertRequestVendorT method to insert it as request_vendor_t 
     * with vendor_id set to ref_vendor_id column)
     * 
     * When users decide to use a vendor from the shared list of vendors, we will copy the data from vendor_t and insert it to request_vendor_t
     * This separation allow the users to update vendor info in the request without effecting the vendor data in the shared list
     * @param vendorId
     * @param requestId
     * @param user
     * @return 
     */
    public Map<String, Object> CopyVendorDataToRequest(Integer vendorId, Integer requestId, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "INSERT INTO REQUEST_VENDOR_T (REQUEST_ID, "
                + "                              REF_VENDOR_ID, "
                + "                              VENDOR_NAME, "
                + "                              STREET, "
                + "                              CITY, "
                + "                              STATE, "
                + "                              ZIP, "
                + "                              WEB_URL, "
                + "                              CONTACT_PERSON, "
                + "                              PHONE, "
                + "                              FAX, "
                + "                              EMAIL, "
                + "                              ACCOUNT_NUMBER, "
                + "                              CREATED_BY, "
                + "                              CREATED_DATE, "
                + "                              UPDATED_BY, "
                + "                              UPDATED_DATE, "
                + "                              IS_FOREIGN_ADDRESS, "
                + "                              FOREIGN_ADDRESS, "
                + "                              DUNS_NUMBER) "
                + "    (SELECT ?, "
                + "            ?, "
                + "            VENDOR_NAME, "
                + "            STREET, "
                + "            CITY, "
                + "            STATE, "
                + "            ZIP, "
                + "            WEB_URL, "
                + "            CONTACT_PERSON, "
                + "            PHONE, "
                + "            FAX, "
                + "            EMAIL, "
                + "            ACCOUNT_NUMBER, "
                + "            ?, "
                + "            SYSDATE, "
                + "            ?, "
                + "            SYSDATE, "
                + "            IS_FOREIGN_ADDRESS, "
                + "            FOREIGN_ADDRESS, "
                + "            DUNS_NUMBER "
                + "       FROM VENDOR_T) ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(false);
                PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"vendor_id"});) {

            setInt(pstmt, 1, requestId);
            setInt(pstmt, 2, vendorId);
            setInt(pstmt, 3, user.getPeopleId());
            setInt(pstmt, 4, user.getPeopleId());
            pstmt.execute();

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateVendorT(VendorT vendor, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (vendor == null || vendor.getVendorId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE vendor_t"
                + " SET "
                + "vendor_name = ?, "
                + "street = ?, "
                + "city = ?, "
                + "state = ?, "
                + "zip = ?, "
                + "web_url = ?, "
                + "contact_person = ?, "
                + "phone = ?, "
                + "fax = ?, "
                + "email = ?, "
                + "account_number = ?, "
                + "updated_by = ?, "
                + "updated_date = SYSDATE, "
                + "is_foreign_address = ?, "
                + "foreign_address = ?, "
                + "duns_number = ?, "
                + "shared= ?, "
                + "additional_info= ?, "
                + "group_id = ? "
                + " WHERE "
                + "vendor_id = ? ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {

            setString(pstmt, 1, vendor.getVendorName());
            setString(pstmt, 2, vendor.getStreet());
            setString(pstmt, 3, vendor.getCity());
            setString(pstmt, 4, vendor.getState());
            setString(pstmt, 5, vendor.getZip());
            setString(pstmt, 6, vendor.getWebUrl());
            setString(pstmt, 7, vendor.getContactPerson());
            setString(pstmt, 8, vendor.getPhone());
            setString(pstmt, 9, vendor.getFax());
            setString(pstmt, 10, vendor.getEmail());
            setString(pstmt, 11, vendor.getAccountNumber());
            setInt(pstmt, 12, user.getPeopleId());
            setString(pstmt, 13, boolToString(vendor.getIsForeignAddress()));
            setString(pstmt, 14, vendor.getForeignAddress());
            setString(pstmt, 15, vendor.getDunsNumber());
            setInt(pstmt, 16, vendor.getShared());
            setString(pstmt, 17, vendor.getAdditionalInfo());
            setInt(pstmt, 18, vendor.getGroupId());
            setInt(pstmt, 19, vendor.getVendorId());

            rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRequestVendorT(RequestVendorT vendor, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;

        if (vendor == null || vendor.getRequestId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE request_vendor_t"
                + " SET "
                + "vendor_name = ?, "
                + "street = ?, "
                + "city = ?, "
                + "state = ?, "
                + "zip = ?, "
                + "web_url = ?, "
                + "contact_person = ?, "
                + "phone = ?, "
                + "fax = ?, "
                + "email = ?, "
                + "account_number = ?, "
                + "updated_by = ?, "
                + "updated_date = SYSDATE, "
                + "is_foreign_address = ?, "
                + "foreign_address = ?, "
                + "duns_number = ?, "
                + "CONVENIENCE_CHECK = ?, "
                + "ADDITIONAL_INFO = ? "
                + " WHERE "
                + "request_id = ? ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {

            setString(pstmt, 1, vendor.getVendorName());
            setString(pstmt, 2, vendor.getStreet());
            setString(pstmt, 3, vendor.getCity());
            setString(pstmt, 4, vendor.getState());
            setString(pstmt, 5, vendor.getZip());
            setString(pstmt, 6, vendor.getWebUrl());
            setString(pstmt, 7, vendor.getContactPerson());
            setString(pstmt, 8, vendor.getPhone());
            setString(pstmt, 9, vendor.getFax());
            setString(pstmt, 10, vendor.getEmail());
            setString(pstmt, 11, vendor.getAccountNumber());
            setInt(pstmt, 12, user.getPeopleId());
            setString(pstmt, 13, boolToString(vendor.getIsForeignAddress()));
            setString(pstmt, 14, vendor.getForeignAddress());
            setString(pstmt, 15, vendor.getDunsNumber());
            setString(pstmt, 16, boolToString(vendor.getConvenienceCheck()));
            setString(pstmt, 17, vendor.getAdditionalInfo());
            setInt(pstmt, 18, vendor.getRequestId());

            rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateRequestJustification(RequestJustification just, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;

        if (just == null || just.getRequestId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE REQUEST_JUSTIFICATION"
                + " SET   CONVENIENCE_CHECK        = ?,"
                + "       CONVENIENCE_CHECK_JUST   = ?,"
                + "       GSA_SCHEDULE             = ?,"
                + "       GSA_SCHEDULE_JUST        = ?,"
                + "       THIRD_PARTY_VENDOR       = ?,"
                + "       THIRD_PARTY_VENDOR_JUST  = ?,"
                + "       PRICE_IS_REASONABLE_JUST = ?,"
                + "       SMALL_BUSINESS           = ?,"
                + "       SMALL_BUSINESS_JUST      = ?,"
                + "       UPDATED_BY               = ?,"
                + "       UPDATED_DATE             = sysdate,"
                + "       PROFESSIONAL_ORG         = ?,"
                + "       COMMERCIAL_VENDOR       = ?,"
                + "       COMMERCIAL_VENDOR_JUST  = ?"
                + " WHERE "
                + "request_id = ? ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {

            setString(pstmt, 1, boolToString(just.getConvenienceCheck()));
            setString(pstmt, 2, just.getConvenienceCheckJust());
            setString(pstmt, 3, boolToString(just.getGsaSchedule()));
            setString(pstmt, 4, just.getGsaScheduleJust());
            setString(pstmt, 5, boolToString(just.getThirdPartyVendor()));
            setString(pstmt, 6, just.getThirdPartyVendorJust());
            setString(pstmt, 7, just.getPriceIsReasonableJust());
            setString(pstmt, 8, boolToString(just.getSmallBusiness()));
            setString(pstmt, 9, just.getSmallBusinessJust());
            setInt(pstmt, 10, user.getPeopleId());
            setString(pstmt, 11, boolToString(just.getProfessionalOrg()));
            setString(pstmt, 12, boolToString(just.getCommercialVendor()));
            setString(pstmt, 13, just.getCommercialVendorJust());
            setInt(pstmt, 14, just.getRequestId());

            rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> deleteVendorT(Integer vendorId, User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (vendorId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql1 = "select request_id from request_vendor_t where ref_vendor_id=?";
        String sql2 = "UPDATE vendor_t SET is_active = 0, updated_by = ?, updated_date = SYSDATE WHERE vendor_id = ?";
        String sql3 = "Delete from vendor_t WHERE vendor_id = ?";

        LOG.info(String.format("sql: %s, id:%d", sql1, vendorId));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql1);) {

            setInt(pstmt, 1, vendorId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PreparedStatement pstmt2 = connection.prepareStatement(sql2);
                LOG.info(String.format("sql: %s, id:%d", sql2, vendorId));
                setInt(pstmt2, 1, user.getPeopleId());
                setInt(pstmt2, 2, vendorId);
                rowCount = pstmt2.executeUpdate();
            } else {
                PreparedStatement pstmt3 = connection.prepareStatement(sql3);
                LOG.info(String.format("sql: %s, id:%d", sql3, vendorId));
                setInt(pstmt3, 1, vendorId);
                rowCount = pstmt3.executeUpdate();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(ROW_COUNT_KEY, rowCount);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    /**
     *
     * @param requestId
     * @return
     */
    public Map<String, Object> deleteRequestVendorT(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        int rowCount = 0;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "select request_id from request_vendor_t where request_id = ? and ( ref_vendor_id < 0 or CONVENIENCE_CHECK = 'Y' )";

        String sql1 = "Delete from request_vendor_t WHERE request_id = ?";

        String sql2 = "Delete from request_justification WHERE request_id = ?";

        LOG.info(String.format("sql: %s, id:%d", sql, requestId));

        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            ResultSet rs = pstmt.executeQuery();
            //if user delete a built-in vendor or ConvenienceCheck vendor, we also need to delete the auto inserted justification record for these conditions
            if (rs.next()) {
                PreparedStatement pstmt1 = connection.prepareStatement(sql1);
                pstmt1.setInt(1, requestId);
                rowCount = pstmt1.executeUpdate();
                PreparedStatement pstmt2 = connection.prepareStatement(sql2);
                pstmt2.setInt(1, requestId);
                rowCount = pstmt2.executeUpdate();
            } else {
                //if it's a regular vendor, just need to delete the request vendor; 
                //NOTE: we can either 1. force users to create a vendor record before creating a justification record, which users may not like
                //OR 2. we allow user to enter justification without a vendor so if a vendor is removed, we don't remove the justification, which saves user re-entering time
                //for #2, it only applies to regular vendor becuase we don't want the use case of user use built-in vendor first, which creates a no justificatio needed record;
                //then, user removes the built-in vendor and add a regular vendor but the no justificatio needed record is still there!
                PreparedStatement pstmt3 = connection.prepareStatement(sql1);
                pstmt3.setInt(1, requestId);
                rowCount = pstmt3.executeUpdate();
            }

            results.put(ROW_COUNT_KEY, rowCount);

			//Convenience check fee
			if (requestId > 0) {
				sql = "{ call sp_convenience_check_fee (?) }";
				LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
				try (CallableStatement cstmt = connection.prepareCall(sql);) {
					cstmt.setInt("p_request_id", requestId);
					cstmt.execute();
				}
			}

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(ROW_COUNT_KEY, rowCount);
        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectSharedVendors(User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * "
                + " FROM "
                + " vendor_t "
                + " WHERE "
                + " 1 = 1  "
                + " AND "
                + " is_active = 1 "
                + " and vendor_id > 0 " //do not get built-in vendors that have negative number as id
                + " and ( (ou_id = ? and shared = 2) or "
                + " (division_id= ? and shared = 3) or "
                + " (group_id= ? and shared = 4) or "
                + " shared = 1 ) "
                //+ " ( (ou_id = ? and shared_to_ou = 1) or "
                //+ " (division_id=? and shared_to_division=1) or "
                //+ " (group_id=? and shared_to_group=1) or "
                //+ " (shared_to_all=1) ) " 
                + " ORDER BY "
                //+ " (CASE WHEN vendor_id = -99 THEN 1 WHEN vendor_id = -100 THEN 2 ELSE 3 END), "
                + "  lower(vendor_name) ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, user.getOuId());
            pstmt.setInt(2, user.getDivisionId());
            pstmt.setInt(3, user.getGroupId());

            ResultSetMapper<VendorT> resultSetMapper = new ResultSetMapper<>();
            List<VendorT> vendors = null;
            ResultSet rs = pstmt.executeQuery();
            vendors = resultSetMapper.mapResultSetToObject(rs, VendorT.class);

            results.put(VENDOR_LIST_KEY, vendors);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectVendorForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * "
                + " FROM "
                + " request_vendor_t "
                + " WHERE "
                + " request_id = ? ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);

            ResultSetMapper<RequestVendorT> resultSetMapper = new ResultSetMapper<>();
            List<RequestVendorT> vendors = null;
            ResultSet rs = pstmt.executeQuery();
            vendors = resultSetMapper.mapResultSetToObject(rs, RequestVendorT.class);

            results.put(VENDOR_LIST_KEY, vendors);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectJustificationForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * "
                + " FROM "
                + " request_justification "
                + " WHERE "
                + " request_id = ? ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);

            ResultSetMapper<RequestJustification> resultSetMapper = new ResultSetMapper<>();
            List<RequestJustification> justs = null;
            ResultSet rs = pstmt.executeQuery();
            justs = resultSetMapper.mapResultSetToObject(rs, RequestJustification.class);

            results.put(VENDOR_LIST_KEY, justs);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    /**
     * NOT USED (we currently did not implement UI for requesters to save private vendors)
     * 
     * return a list of vendors created by requesters for their private use (separated from the shared list, which is maintained by authorized users such as AO, AA, BCH)
     * @param user
     * @return 
     */
    public Map<String, Object> selectMyVendors(User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * "
                + " FROM "
                + " vendor_t "
                + " WHERE "
                + " 1 = 1  "
                + " AND "
                + " is_active = 1 and "
                + " created_by = ? and shared = 0 "
                + " ORDER BY "
                + " lower(vendor_name) ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, user.getPeopleId());
            ResultSetMapper<VendorT> resultSetMapper = new ResultSetMapper<>();
            List<VendorT> vendors;
            try (ResultSet rs = pstmt.executeQuery();) {
                vendors = resultSetMapper.mapResultSetToObject(rs, VendorT.class);
            }
            results.put(VENDOR_LIST_KEY, vendors);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    /**
     * NOT USED (we currently did not implement UI for authorized user to check on private vendors created by requesters and decide whether to make them a vendor in the shared list)
     * 
     * return a list of vendors created by requesters of a OU for their private use (separated from the shared list, which is maintained by authorized users such as AO, AA, BCH)
     * The authorized users can select private vendors and make them shared so other requesters can use them
     * @param user
     * @return 
     */
    public Map<String, Object> selectMyOrgPrivateVendors(User user) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        String sql = "SELECT * "
                + " FROM "
                + " vendor_t "
                + " WHERE "
                + " 1 = 1  "
                + " AND "
                + " is_active = 1 and "
                + " ou_id = ? and shared = 0"
                + " ORDER BY "
                + " lower(vendor_name) ";
        LOG.info(String.format("sql: %s", sql));
        try (Connection connection = getConnection(true);
                PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, user.getOuId());

            ResultSetMapper<VendorT> resultSetMapper = new ResultSetMapper<>();
            List<VendorT> vendors;
            try (ResultSet rs = pstmt.executeQuery();) {
                vendors = resultSetMapper.mapResultSetToObject(rs, VendorT.class);
            }
            results.put(VENDOR_LIST_KEY, vendors);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> deleteJustification(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "DELETE FROM request_justification WHERE request_id = ?";

        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

                pstmt.setInt(1, requestId);
                pstmt.executeUpdate();

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

//    public Map<String, Object> selectVendorsForRequester(Integer requesterId, Integer divisionId, String filter) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        boolean useFilter = filter != null && !filter.isEmpty();
//        String sql;
//        if (useFilter) {
//            sql = "SELECT "
//                    + "vendor_id, "
//                    + "vendor_name, "
//                    + "street, "
//                    + "city, "
//                    + "state, "
//                    + "zip, "
//                    + "web_url, "
//                    + "contact_person, "
//                    + "phone, "
//                    + "fax, "
//                    + "email, "
//                    + "account_number, "
//                    + "is_foreign_address, "
//                    + "foreign_address,"
//                    + "created_by, "
//                    + "duns_number, "
//                    + "imported_from"
//                    + " FROM "
//                    + "vendor"
//                    + " WHERE "
//                    + "is_active = 1 "
//                    + "AND "
//                    + "vendor_id"
//                    + " IN "
//                    + "("
//                    + "SELECT "
//                    + "DISTINCT(vendor_id)"
//                    + " FROM "
//                    + "request_vendor, request"
//                    + " WHERE "
//                    + "request_vendor.request_id = request.request_id"
//                    + " AND "
//                    + "(requester_id = ? OR division_org_id = ?)"
//                    + ")"
//                    + " AND "
//                    + "(INSTR(LOWER(vendor_name), ?) > 0)";
//        } else {
//            sql = "SELECT "
//                    + "vendor_id, "
//                    + "vendor_name, "
//                    + "street, "
//                    + "city, "
//                    + "state, "
//                    + "zip, "
//                    + "web_url, "
//                    + "contact_person, "
//                    + "phone, "
//                    + "fax, "
//                    + "email, "
//                    + "account_number, "
//                    + "is_foreign_address, "
//                    + "foreign_address,"
//                    + "created_by, "
//                    + "duns_number, "
//                    + "imported_from"
//                    + " FROM "
//                    + "vendor"
//                    + " WHERE "
//                    + "is_active = 1 "
//                    + "AND "
//                    + "vendor_id"
//                    + " IN "
//                    + "("
//                    + "SELECT "
//                    + "DISTINCT(vendor_id)"
//                    + " FROM "
//                    + "request_vendor, request"
//                    + " WHERE "
//                    + "request_vendor.request_id = request.request_id"
//                    + " AND "
//                    + "(requester_id = ? OR division_org_id = ?)"
//                    + ")";
//        }
//        LOG.info(String.format("sql: %s, requester_id: %d", sql, requesterId));
//        try (Connection connection = getConnection(true);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//            pstmt.setInt(1, requesterId);
//            pstmt.setInt(2, divisionId);
//            if (useFilter) {
//                pstmt.setString(3, filter);
//            }
//            List<Vendor> vendors = new ArrayList<>();
//            try (ResultSet rset = pstmt.executeQuery()) {
//                while (rset.next()) {
//                    Vendor vendor = new Vendor();
//
//                    // Id.
//                    Integer id = rset.getInt("vendor_id");
//                    if (!rset.wasNull()) {
//                        vendor.setId(id);
//                    }
//
//                    // Name.
//                    String name = rset.getString("vendor_name");
//                    if (!rset.wasNull()) {
//                        vendor.setName(name);
//                    }
//
//                    // Street.
//                    String street = rset.getString("street");
//                    if (!rset.wasNull()) {
//                        vendor.setStreet(street);
//                    }
//
//                    // City.
//                    String city = rset.getString("city");
//                    if (!rset.wasNull()) {
//                        vendor.setCity(city);
//                    }
//
//                    // State.
//                    String state = rset.getString("state");
//                    if (!rset.wasNull()) {
//                        vendor.setState(state);
//                    }
//
//                    // Zip code.
//                    String zipCode = rset.getString("zip");
//                    if (!rset.wasNull()) {
//                        vendor.setZipCode(zipCode);
//                    }
//
//                    // Web url.
//                    String webUrl = rset.getString("web_url");
//                    if (!rset.wasNull()) {
//                        vendor.setWebUrl(webUrl);
//                    }
//
//                    // Contact name.
//                    String contactName = rset.getString("contact_person");
//                    if (!rset.wasNull()) {
//                        vendor.setContactName(contactName);
//                    }
//
//                    // Phone number.
//                    String phoneNumber = rset.getString("phone");
//                    if (!rset.wasNull()) {
//                        vendor.setPhoneNumber(phoneNumber);
//                    }
//
//                    // Fax number.
//                    String faxNumber = rset.getString("fax");
//                    if (!rset.wasNull()) {
//                        vendor.setFaxNumber(faxNumber);
//                    }
//
//                    // Email.
//                    String email = rset.getString("email");
//                    if (!rset.wasNull()) {
//                        vendor.setEmail(email);
//                    }
//
//                    // Account number.
//                    String accountNumber = rset.getString("account_number");
//                    if (!rset.wasNull()) {
//                        vendor.setAccountNumber(accountNumber);
//                    }
//
//                    // Is foreign address.
//                    String isForeignAddress = rset.getString("is_foreign_address");
//                    if (!rset.wasNull()) {
//                        vendor.setIsForeignAddress("Y".equals(isForeignAddress));
//                    }
//
//                    // Foreign address.
//                    String foreignAddress = rset.getString("foreign_address");
//                    if (!rset.wasNull()) {
//                        vendor.setForeignAddress(foreignAddress);
//                    }
//
//                    // created_by.
//                    Integer createdBy = rset.getInt("created_by");
//                    if (!rset.wasNull()) {
//                        vendor.setCreatedBy(createdBy);
//                    }
//
//                    // Duns number.
//                    vendor.setDunsNumber(rset.getString("duns_number"));
//
//                    // Imported from.
//                    vendor.setImportedFrom(rset.getString("imported_from"));
//
//                    vendors.add(vendor);
//                }
//            }
//
//            results.put(VENDOR_LIST_KEY, vendors);
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

    /**
     * Deprecated
     *
     * @param requestId
     * @return
     */
//    public Map<String, Object> selectVendorsForRequest(Integer requestId) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (requestId == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//        String sql = "SELECT "
//                + "v.vendor_id, "
//                + "v.vendor_name, "
//                + "v.street, "
//                + "v.city, "
//                + "v.state, "
//                + "v.zip, "
//                + "v.web_url, "
//                + "v.contact_person, "
//                + "v.phone, "
//                + "v.fax, "
//                + "v.email, "
//                + "v.account_number, "
//                + "v.is_foreign_address, "
//                + "v.foreign_address,"
//                + "v.created_by AS v_created_by, "
//                + "v.created_date AS v_created_date, "
//                + "v.updated_by AS v_updated_by, "
//                + "v.updated_date AS v_updated_date, "
//                + "v.is_active, "
//                + "v.duns_number, "
//                + "v.imported_from, "
//                + "rv.request_id, "
//                + "rv.convenience_check, "
//                + "rv.convenience_check_just, "
//                + "rv.gsa_schedule, "
//                + "rv.gsa_schedule_just, "
//                + "rv.third_party_vendor, "
//                + "rv.third_party_vendor_just, "
//                + "rv.price_is_reasonable_just, "
//                + "rv.small_business, "
//                + "rv.small_business_just, "
//                + "rv.created_by AS rv_created_by, "
//                + "rv.created_date AS rv_created_date, "
//                + "rv.updated_by AS rv_updated_by, "
//                + "rv.updated_date AS rv_updated_date, "
//                + "rv.convenience_check_number, "
//                + "rv.division_org_id, "
//                + "rv.professional_org"
//                + " FROM "
//                + "vendor v, request_vendor rv"
//                + " WHERE "
//                + "v.vendor_id = rv.vendor_id"
//                + " AND "
//                + "rv.request_id = ?";
//        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
//        try (Connection connection = getConnection(true);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//            pstmt.setInt(1, requestId);
//            List<RequestVendor> requestVendors = new ArrayList<>();
//            try (ResultSet rset = pstmt.executeQuery()) {
//                while (rset.next()) {
//                    RequestVendor requestVendor = new RequestVendor();
//                    Vendor vendor = new Vendor();
//
//                    // Id.
//                    Integer id = rset.getInt("vendor_id");
//                    if (!rset.wasNull()) {
//                        vendor.setId(id);
//                    }
//
//                    // Name.
//                    String name = rset.getString("vendor_name");
//                    if (!rset.wasNull()) {
//                        vendor.setName(name);
//                    }
//
//                    // Street.
//                    String street = rset.getString("street");
//                    if (!rset.wasNull()) {
//                        vendor.setStreet(street);
//                    }
//
//                    // City.
//                    String city = rset.getString("city");
//                    if (!rset.wasNull()) {
//                        vendor.setCity(city);
//                    }
//
//                    // State.
//                    String state = rset.getString("state");
//                    if (!rset.wasNull()) {
//                        vendor.setState(state);
//                    }
//
//                    // Zip code.
//                    String zipCode = rset.getString("zip");
//                    if (!rset.wasNull()) {
//                        vendor.setZipCode(zipCode);
//                    }
//
//                    // Web url.
//                    String webUrl = rset.getString("web_url");
//                    if (!rset.wasNull()) {
//                        vendor.setWebUrl(webUrl);
//                    }
//
//                    // Contact name.
//                    String contactName = rset.getString("contact_person");
//                    if (!rset.wasNull()) {
//                        vendor.setContactName(contactName);
//                    }
//
//                    // Phone number.
//                    String phoneNumber = rset.getString("phone");
//                    if (!rset.wasNull()) {
//                        vendor.setPhoneNumber(phoneNumber);
//                    }
//
//                    // Fax number.
//                    String faxNumber = rset.getString("fax");
//                    if (!rset.wasNull()) {
//                        vendor.setFaxNumber(faxNumber);
//                    }
//
//                    // Email.
//                    String email = rset.getString("email");
//                    if (!rset.wasNull()) {
//                        vendor.setEmail(email);
//                    }
//
//                    // Account number.
//                    String accountNumber = rset.getString("account_number");
//                    if (!rset.wasNull()) {
//                        vendor.setAccountNumber(accountNumber);
//                    }
//
//                    // Is foreign address.
//                    String isForeignAddress = rset.getString("is_foreign_address");
//                    if (!rset.wasNull()) {
//                        vendor.setIsForeignAddress("Y".equals(isForeignAddress));
//                    }
//
//                    // Foreign address.
//                    String foreignAddress = rset.getString("foreign_address");
//                    if (!rset.wasNull()) {
//                        vendor.setForeignAddress(foreignAddress);
//                    }
//
//                    // Created by..
//                    Integer createdBy = rset.getInt("v_created_by");
//                    if (!rset.wasNull()) {
//                        vendor.setCreatedBy(createdBy);
//                    }
//
//                    // Created date.
//                    Timestamp createdDate = rset.getTimestamp("v_created_date");
//                    if (!rset.wasNull()) {
//                        vendor.setCreatedDate(createdDate);
//                    }
//
//                    // Updated by.
//                    Integer updatedBy = rset.getInt("v_updated_by");
//                    if (!rset.wasNull()) {
//                        vendor.setUpdatedBy(updatedBy);
//                    }
//
//                    // Updated date.
//                    Timestamp updatedDate = rset.getTimestamp("v_updated_date");
//                    if (!rset.wasNull()) {
//                        vendor.setUpdatedDate(updatedDate);
//                    }
//
//                    // Is active.
//                    Integer isActive = rset.getInt("is_active");
//                    if (!rset.wasNull()) {
//                        vendor.setIsActive(1 == isActive);
//                    }
//
//                    // Duns number.
//                    String dunsNumber = rset.getString("duns_number");
//                    if (!rset.wasNull()) {
//                        vendor.setDunsNumber(dunsNumber);
//                    }
//
//                    // Imported from.
//                    String importedFrom = rset.getString("imported_from");
//                    if (!rset.wasNull()) {
//                        vendor.setImportedFrom(importedFrom);
//                    }
//
//                    // Request id.
//                    requestVendor.setRequestId(requestId);
//
//                    // Convenience check.
//                    String convenienceCheck = rset.getString("convenience_check");
//                    if (!rset.wasNull()) {
//                        requestVendor.setConvenienceCheck("Y".equals(convenienceCheck));
//                    }
//
//                    // Convenience check justification.
//                    String convenienceCheckJustificaion = rset.getString("convenience_check_just");
//                    if (!rset.wasNull()) {
//                        requestVendor.setConvenienceCheckJustification(convenienceCheckJustificaion);
//                    }
//
//                    // GSA schedule.
//                    String gsaSchedule = rset.getString("gsa_schedule");
//                    if (!rset.wasNull()) {
//                        requestVendor.setGsaSchedule("Y".equals(gsaSchedule));
//                    }
//
//                    // GSA schedule justification.
//                    String gsaScheduleJustification = rset.getString("gsa_schedule_just");
//                    if (!rset.wasNull()) {
//                        requestVendor.setGsaScheduleJustification(gsaScheduleJustification);
//                    }
//
//                    // Third party vendor.
//                    String thirdPartyVendor = rset.getString("third_party_vendor");
//                    if (!rset.wasNull()) {
//                        requestVendor.setThirdPartyVendor("Y".equals(thirdPartyVendor));
//                    }
//
//                    // Third party vendor justification.
//                    String thirdPartyJustification = rset.getString("third_party_vendor_just");
//                    if (!rset.wasNull()) {
//                        requestVendor.setThirdPartyJustification(thirdPartyJustification);
//                    }
//
//                    // Price is reasonable justification.
//                    String priceIsReasonableJustification = rset.getString("price_is_reasonable_just");
//                    if (!rset.wasNull()) {
//                        requestVendor.setPriceJustification(priceIsReasonableJustification);
//                    }
//
//                    // Small business.
//                    String smallBusiness = rset.getString("small_business");
//                    if (!rset.wasNull()) {
//                        requestVendor.setSmallBusiness("Y".equals(smallBusiness));
//                    }
//
//                    // Small business justification.
//                    String smallBusinessJustification = rset.getString("small_business_just");
//                    if (!rset.wasNull()) {
//                        requestVendor.setSmallBusinessJustification(smallBusinessJustification);
//                    }
//
//                    // Created by..
//                    createdBy = rset.getInt("rv_created_by");
//                    if (!rset.wasNull()) {
//                        requestVendor.setCreatedBy(createdBy);
//                    }
//
//                    // Created date.
//                    createdDate = rset.getTimestamp("rv_created_date");
//                    if (!rset.wasNull()) {
//                        requestVendor.setCreatedDate(createdDate);
//                    }
//
//                    // Updated by.
//                    updatedBy = rset.getInt("rv_updated_by");
//                    if (!rset.wasNull()) {
//                        requestVendor.setUpdatedBy(updatedBy);
//                    }
//
//                    // Updated date.
//                    updatedDate = rset.getTimestamp("rv_updated_date");
//                    if (!rset.wasNull()) {
//                        requestVendor.setUpdatedDate(updatedDate);
//                    }
//
//                    // Convenience check number.
//                    String convenienceCheckNumber = rset.getString("convenience_check_number");
//                    if (!rset.wasNull()) {
//                        requestVendor.setConvenienceCheckNumber(convenienceCheckNumber);
//                    }
//
//                    // Division org id.
//                    Integer divisionOrgId = rset.getInt("division_org_id");
//                    if (!rset.wasNull()) {
//                        requestVendor.setDivisionId(divisionOrgId);
//                    }
//
//                    // Professional org.
//                    String professionalOrg = rset.getString("professional_org");
//                    if (!rset.wasNull()) {
//                        requestVendor.setProfessionalOrg("Y".equals(professionalOrg));
//                    }
//
//                    requestVendor.setVendor(vendor);
//                    requestVendors.add(requestVendor);
//                }
//            }
//
//            results.put(REQUEST_VENDOR_LIST_KEY, requestVendors);
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

//    
//    public Map<String, Object> insertNewRequestVendor(RequestVendor requestVendor) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (requestVendor.getVendor() == null
//                || requestVendor.getVendor().getName() == null
//                || requestVendor.getVendor().getName().isEmpty()
//                || requestVendor.getRequestId() == null
//                || requestVendor.getCreatorId() == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//        String sql = "INSERT INTO"
//                + " vendor "
//                + "("
//                + "vendor_name, "
//                + "street, "
//                + "city, "
//                + "state, "
//                + "zip, "
//                + "web_url, "
//                + "contact_person, "
//                + "phone, "
//                + "fax, "
//                + "email, "
//                + "account_number, "
//                + "created_by, "
//                + "created_date, "
//                + "updated_by, "
//                + "updated_date, "
//                + "is_foreign_address, "
//                + "foreign_address, "
//                + "is_active, "
//                + "duns_number, "
//                + "imported_from"
//                + ")"
//                + " VALUES "
//                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, 1, ?, ?)";
//
//        LOG.info(String.format("sql: %s", sql));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"vendor_id"});) {
//
//            // Vendor name.
//            if (requestVendor.getVendor().getName() != null && !requestVendor.getVendor().getName().isEmpty()) {
//                pstmt.setString(1, requestVendor.getVendor().getName().trim());
//            } else {
//                pstmt.setNull(1, Types.VARCHAR);
//            }
//
//            // Street.
//            if (requestVendor.getVendor().getStreet() != null && !requestVendor.getVendor().getStreet().isEmpty()) {
//                pstmt.setString(2, requestVendor.getVendor().getStreet().trim());
//            } else {
//                pstmt.setNull(2, Types.VARCHAR);
//            }
//
//            // City.
//            if (requestVendor.getVendor().getCity() != null && !requestVendor.getVendor().getCity().isEmpty()) {
//                pstmt.setString(3, requestVendor.getVendor().getCity().trim());
//            } else {
//                pstmt.setNull(3, Types.VARCHAR);
//            }
//
//            // State.
//            if (requestVendor.getVendor().getState() != null && !requestVendor.getVendor().getState().isEmpty()) {
//                pstmt.setString(4, requestVendor.getVendor().getState().trim());
//            } else {
//                pstmt.setNull(4, Types.VARCHAR);
//            }
//
//            // Zip code.
//            if (requestVendor.getVendor().getZipCode() != null && !requestVendor.getVendor().getZipCode().isEmpty()) {
//                pstmt.setString(5, requestVendor.getVendor().getZipCode().trim());
//            } else {
//                pstmt.setNull(5, Types.VARCHAR);
//            }
//
//            // Web url.
//            if (requestVendor.getVendor().getWebUrl() != null && !requestVendor.getVendor().getWebUrl().isEmpty()) {
//                pstmt.setString(6, requestVendor.getVendor().getWebUrl().trim());
//            } else {
//                pstmt.setNull(6, Types.VARCHAR);
//            }
//
//            // Contact name.
//            if (requestVendor.getVendor().getContactName() != null && !requestVendor.getVendor().getContactName().isEmpty()) {
//                pstmt.setString(7, requestVendor.getVendor().getContactName().trim());
//            } else {
//                pstmt.setNull(7, Types.VARCHAR);
//            }
//
//            // Phone number.
//            if (requestVendor.getVendor().getPhoneNumber() != null && !requestVendor.getVendor().getPhoneNumber().isEmpty()) {
//                pstmt.setString(8, requestVendor.getVendor().getPhoneNumber().trim());
//            } else {
//                pstmt.setNull(8, Types.VARCHAR);
//            }
//
//            // Fax number.
//            if (requestVendor.getVendor().getFaxNumber() != null && !requestVendor.getVendor().getFaxNumber().isEmpty()) {
//                pstmt.setString(9, requestVendor.getVendor().getFaxNumber().trim());
//            } else {
//                pstmt.setNull(9, Types.VARCHAR);
//            }
//
//            // Email.
//            if (requestVendor.getVendor().getEmail() != null && !requestVendor.getVendor().getEmail().isEmpty()) {
//                pstmt.setString(10, requestVendor.getVendor().getEmail().trim());
//            } else {
//                pstmt.setNull(10, Types.VARCHAR);
//            }
//
//            // Account number.
//            if (requestVendor.getVendor().getAccountNumber() != null && !requestVendor.getVendor().getAccountNumber().isEmpty()) {
//                pstmt.setString(11, requestVendor.getVendor().getAccountNumber().trim());
//            } else {
//                pstmt.setNull(11, Types.VARCHAR);
//            }
//
//            // Created by.
//            pstmt.setInt(12, requestVendor.getCreatorId());
//
//            // Updated by.
//            pstmt.setInt(13, requestVendor.getCreatorId());
//
//            // Is foreign address.
//            if (requestVendor.getVendor().getIsForeignAddress() != null) {
//                if (requestVendor.getVendor().getIsForeignAddress()) {
//                    pstmt.setString(14, "Y");
//                } else {
//                    pstmt.setString(14, "N");
//                }
//            } else {
//                pstmt.setNull(14, Types.VARCHAR);
//            }
//
//            // Freign address.
//            if (requestVendor.getVendor().getForeignAddress() != null && !requestVendor.getVendor().getForeignAddress().isEmpty()) {
//                pstmt.setString(15, requestVendor.getVendor().getForeignAddress().trim());
//            } else {
//                pstmt.setNull(15, Types.VARCHAR);
//            }
//
//            // Duns number.
//            if (requestVendor.getVendor().getDunsNumber() != null && !requestVendor.getVendor().getDunsNumber().isEmpty()) {
//                pstmt.setString(16, requestVendor.getVendor().getDunsNumber().trim());
//            } else {
//                pstmt.setNull(16, Types.VARCHAR);
//            }
//
//            // Imported from.
//            if (requestVendor.getVendor().getImportedFrom() != null && !requestVendor.getVendor().getImportedFrom().isEmpty()) {
//                pstmt.setString(17, requestVendor.getVendor().getImportedFrom().trim());
//            } else {
//                pstmt.setNull(17, Types.VARCHAR);
//            }
//
//            // Do the insert and get back the generated key.
//            int rowCount = pstmt.executeUpdate();
//            if (rowCount == 0) {
//                statusCode = StatusCode.InsertFailed;
//            }
//
//            ResultSet generatedKeys = pstmt.getGeneratedKeys();
//            if (generatedKeys != null && generatedKeys.next()) {
//                Integer vendorId = generatedKeys.getInt(1);
//                results.put(VENDOR_ID_KEY, vendorId);
//                generatedKeys.close();
//
//                // Now insert into request_venor.
//                sql = "INSERT INTO"
//                        + " request_vendor "
//                        + "("
//                        + "request_id, "
//                        + "vendor_id, "
//                        + "convenience_check, "
//                        + "convenience_check_just, "
//                        + "gsa_schedule, "
//                        + "gsa_schedule_just, "
//                        + "third_party_vendor, "
//                        + "third_party_vendor_just, "
//                        + "price_is_reasonable_just, "
//                        + "small_business, "
//                        + "small_business_just, "
//                        + "created_by, "
//                        + "created_date, "
//                        + "updated_by, "
//                        + "updated_date, "
//                        + "convenience_check_number, "
//                        + "division_org_id, "
//                        + "professional_org"
//                        + ")"
//                        + " VALUES "
//                        + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, ?)";
//
//                try (PreparedStatement pstmt2 = connection.prepareStatement(sql)) {
//
//                    // Request id.
//                    pstmt2.setInt(1, requestVendor.getRequestId());
//
//                    // Vendor id.
//                    pstmt2.setInt(2, vendorId);
//
//                    // Convenience check.
//                    if (requestVendor.getConvenienceCheck() != null) {
//                        if (requestVendor.getConvenienceCheck()) {
//                            pstmt2.setString(3, "Y");
//                        } else {
//                            pstmt2.setString(3, "N");
//                        }
//                    } else {
//                        pstmt2.setString(3, "N");
//                    }
//
//                    // Convenience check justification.
//                    if (requestVendor.getConvenienceCheckJustification() != null && !requestVendor.getConvenienceCheckJustification().isEmpty()) {
//                        pstmt2.setString(4, requestVendor.getConvenienceCheckJustification().trim());
//                    } else {
//                        pstmt2.setNull(4, Types.VARCHAR);
//                    }
//
//                    // Gsa schedule.
//                    if (requestVendor.getGsaSchedule() != null) {
//                        if (requestVendor.getGsaSchedule()) {
//                            pstmt2.setString(5, "Y");
//                        } else {
//                            pstmt2.setString(5, "N");
//                        }
//                    } else {
//                        pstmt2.setString(5, "N");
//                    }
//
//                    // Gsa schedule justification.
//                    if (requestVendor.getGsaScheduleJustification() != null && !requestVendor.getGsaScheduleJustification().isEmpty()) {
//                        pstmt2.setString(6, requestVendor.getGsaScheduleJustification().trim());
//                    } else {
//                        pstmt2.setNull(6, Types.VARCHAR);
//                    }
//
//                    // Third party vendor.
//                    if (requestVendor.getThirdPartyVendor() != null) {
//                        if (requestVendor.getThirdPartyVendor()) {
//                            pstmt2.setString(7, "Y");
//                        } else {
//                            pstmt2.setString(7, "N");
//                        }
//                    } else {
//                        pstmt2.setString(7, "N");
//                    }
//
//                    // Third party justification.
//                    if (requestVendor.getThirdPartyJustification() != null && !requestVendor.getThirdPartyJustification().isEmpty()) {
//                        pstmt2.setString(8, requestVendor.getThirdPartyJustification().trim());
//                    } else {
//                        pstmt2.setNull(8, Types.VARCHAR);
//                    }
//
//                    // Price justification.
//                    if (requestVendor.getPriceJustification() != null && !requestVendor.getPriceJustification().isEmpty()) {
//                        pstmt2.setString(9, requestVendor.getPriceJustification().trim());
//                    } else {
//                        pstmt2.setNull(9, Types.VARCHAR);
//                    }
//
//                    // Small business.
//                    if (requestVendor.getSmallBusiness() != null) {
//                        if (requestVendor.getSmallBusiness()) {
//                            pstmt2.setString(10, "Y");
//                        } else {
//                            pstmt2.setString(10, "N");
//                        }
//                    } else {
//                        pstmt2.setNull(10, Types.VARCHAR);
//                    }
//
//                    // Small business justification.
//                    if (requestVendor.getSmallBusinessJustification() != null && !requestVendor.getSmallBusinessJustification().isEmpty()) {
//                        pstmt2.setString(11, requestVendor.getSmallBusinessJustification().trim());
//                    } else {
//                        pstmt2.setNull(11, Types.VARCHAR);
//                    }
//
//                    // Created by.
//                    pstmt2.setInt(12, requestVendor.getCreatorId());
//
//                    // Updated by.
//                    pstmt2.setInt(13, requestVendor.getCreatorId());
//
//                    // Convenience check number.
//                    if (requestVendor.getConvenienceCheckNumber() != null && !requestVendor.getConvenienceCheckNumber().isEmpty()) {
//                        pstmt2.setString(14, requestVendor.getConvenienceCheckNumber().trim());
//                    } else {
//                        pstmt2.setNull(14, Types.VARCHAR);
//                    }
//
//                    // Division id.
//                    if (requestVendor.getDivisionId() != null) {
//                        pstmt2.setInt(15, requestVendor.getDivisionId());
//                    } else {
//                        pstmt2.setNull(15, Types.INTEGER);
//                    }
//
//                    // Professional org.
//                    if (requestVendor.getProfessionalOrg() != null) {
//                        if (requestVendor.getProfessionalOrg()) {
//                            pstmt2.setString(16, "Y");
//                        } else {
//                            pstmt2.setString(16, "N");
//                        }
//                    } else {
//                        pstmt2.setString(16, "N"); // Default of NO.
//                    }
//
//                    // Do the insert and get back the generated key.
//                    rowCount = pstmt2.executeUpdate();
//                    if (rowCount == 0) {
//                        statusCode = StatusCode.InsertFailed;
//                    }
//                }
//            } else {
//                statusCode = StatusCode.InsertFailed;
//            }
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

//    public Map<String, Object> insertExistingRequestVendor(RequestVendor requestVendor) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (requestVendor.getRequestId() == null || requestVendor.getVendorId() == null || requestVendor.getCreatorId() == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//        String sql = "INSERT INTO"
//                + " request_vendor "
//                + "("
//                + "request_id, "
//                + "vendor_id, "
//                + "convenience_check, "
//                + "convenience_check_just, "
//                + "gsa_schedule, "
//                + "gsa_schedule_just, "
//                + "third_party_vendor, "
//                + "third_party_vendor_just, "
//                + "price_is_reasonable_just, "
//                + "small_business, "
//                + "small_business_just, "
//                + "created_by, "
//                + "created_date, "
//                + "updated_by, "
//                + "updated_date, "
//                + "convenience_check_number, "
//                + "division_org_id, "
//                + "professional_org"
//                + ")"
//                + " VALUES "
//                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE, ?, ?, ?)";
//
//        LOG.info(String.format("sql: %s", sql));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//
//            // Request id.
//            pstmt.setInt(1, requestVendor.getRequestId());
//
//            // Vendor id.
//            pstmt.setInt(2, requestVendor.getVendorId());
//
//            // Convenience check.
//            if (requestVendor.getConvenienceCheck() != null) {
//                if (requestVendor.getConvenienceCheck()) {
//                    pstmt.setString(3, "Y");
//                } else {
//                    pstmt.setString(3, "N");
//                }
//            } else {
//                pstmt.setNull(3, Types.VARCHAR);
//            }
//
//            // Convenience check justification.
//            if (requestVendor.getConvenienceCheckJustification() != null && !requestVendor.getConvenienceCheckJustification().isEmpty()) {
//                pstmt.setString(4, requestVendor.getConvenienceCheckJustification().trim());
//            } else {
//                pstmt.setNull(4, Types.VARCHAR);
//            }
//
//            // Gsa schedule.
//            if (requestVendor.getGsaSchedule() != null) {
//                if (requestVendor.getGsaSchedule()) {
//                    pstmt.setString(5, "Y");
//                } else {
//                    pstmt.setString(5, "N");
//                }
//            } else {
//                pstmt.setNull(5, Types.VARCHAR);
//            }
//
//            // Gsa schedule justification.
//            if (requestVendor.getGsaScheduleJustification() != null && !requestVendor.getGsaScheduleJustification().isEmpty()) {
//                pstmt.setString(6, requestVendor.getGsaScheduleJustification().trim());
//            } else {
//                pstmt.setNull(6, Types.VARCHAR);
//            }
//
//            // Third party vendor.
//            if (requestVendor.getThirdPartyVendor() != null) {
//                if (requestVendor.getThirdPartyVendor()) {
//                    pstmt.setString(7, "Y");
//                } else {
//                    pstmt.setString(7, "N");
//                }
//            } else {
//                pstmt.setNull(7, Types.VARCHAR);
//            }
//
//            // Third party justification.
//            if (requestVendor.getThirdPartyJustification() != null && !requestVendor.getThirdPartyJustification().isEmpty()) {
//                pstmt.setString(8, requestVendor.getThirdPartyJustification().trim());
//            } else {
//                pstmt.setNull(8, Types.VARCHAR);
//            }
//
//            // Price justification.
//            if (requestVendor.getPriceJustification() != null && !requestVendor.getPriceJustification().isEmpty()) {
//                pstmt.setString(9, requestVendor.getPriceJustification().trim());
//            } else {
//                pstmt.setNull(9, Types.VARCHAR);
//            }
//
//            // Small business.
//            if (requestVendor.getSmallBusiness() != null) {
//                if (requestVendor.getSmallBusiness()) {
//                    pstmt.setString(10, "Y");
//                } else {
//                    pstmt.setString(10, "N");
//                }
//            } else {
//                pstmt.setNull(10, Types.VARCHAR);
//            }
//
//            // Small business justification.
//            if (requestVendor.getSmallBusinessJustification() != null && !requestVendor.getSmallBusinessJustification().isEmpty()) {
//                pstmt.setString(11, requestVendor.getSmallBusinessJustification().trim());
//            } else {
//                pstmt.setNull(11, Types.VARCHAR);
//            }
//
//            // Created by.
//            pstmt.setInt(12, requestVendor.getCreatorId());
//
//            // Updated by.
//            pstmt.setInt(13, requestVendor.getCreatorId());
//
//            // Convenience check number.
//            if (requestVendor.getConvenienceCheckNumber() != null && !requestVendor.getConvenienceCheckNumber().isEmpty()) {
//                pstmt.setString(14, requestVendor.getConvenienceCheckNumber().trim());
//            } else {
//                pstmt.setNull(14, Types.VARCHAR);
//            }
//
//            // Division id.
//            if (requestVendor.getDivisionId() != null) {
//                pstmt.setInt(15, requestVendor.getDivisionId());
//            } else {
//                pstmt.setNull(15, Types.INTEGER);
//            }
//
//            // Professional org.
//            if (requestVendor.getProfessionalOrg() != null) {
//                if (requestVendor.getProfessionalOrg()) {
//                    pstmt.setString(16, "Y");
//                } else {
//                    pstmt.setString(16, "N");
//                }
//            } else {
//                pstmt.setNull(16, Types.VARCHAR);
//            }
//
//            // Do the insert and get back the generated key.
//            int rowCount = pstmt.executeUpdate();
//            if (rowCount == 0) {
//                statusCode = StatusCode.InsertFailed;
//            }
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

//    public Map<String, Object> updateVendor(Vendor vendor) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (vendor == null
//                || vendor.getName() == null
//                || vendor.getName().isEmpty()
//                || vendor.getId() == null
//                || vendor.getUpdatedBy() == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//        String sql = "UPDATE vendor"
//                + " SET "
//                + "vendor_name = ?, "
//                + "street = ?, "
//                + "city = ?, "
//                + "state = ?, "
//                + "zip = ?, "
//                + "web_url = ?, "
//                + "contact_person = ?, "
//                + "phone = ?, "
//                + "fax = ?, "
//                + "email = ?, "
//                + "account_number = ?, "
//                + "updated_by = ?, "
//                + "updated_date = SYSDATE, "
//                + "is_foreign_address = ?, "
//                + "foreign_address = ?, "
//                + "duns_number = ?, "
//                + "imported_from = ?"
//                + " WHERE "
//                + "vendor_id = ?"
//                + " AND "
//                + "created_by = ?"; // Only the creator can update the vendor.
//        LOG.info(String.format("sql: %s", sql));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//
//            // Vendor name.
//            if (vendor.getName() != null && !vendor.getName().isEmpty()) {
//                pstmt.setString(1, vendor.getName().trim());
//            } else {
//                pstmt.setNull(1, Types.VARCHAR);
//            }
//
//            // Street.
//            if (vendor.getStreet() != null && !vendor.getStreet().isEmpty()) {
//                pstmt.setString(2, vendor.getStreet().trim());
//            } else {
//                pstmt.setNull(2, Types.VARCHAR);
//            }
//
//            // City.
//            if (vendor.getCity() != null && !vendor.getCity().isEmpty()) {
//                pstmt.setString(3, vendor.getCity().trim());
//            } else {
//                pstmt.setNull(3, Types.VARCHAR);
//            }
//
//            // State.
//            if (vendor.getState() != null && !vendor.getState().isEmpty()) {
//                pstmt.setString(4, vendor.getState().trim());
//            } else {
//                pstmt.setNull(4, Types.VARCHAR);
//            }
//
//            // Zip code.
//            if (vendor.getZipCode() != null && !vendor.getZipCode().isEmpty()) {
//                pstmt.setString(5, vendor.getZipCode().trim());
//            } else {
//                pstmt.setNull(5, Types.VARCHAR);
//            }
//
//            // Web url.
//            if (vendor.getWebUrl() != null && !vendor.getWebUrl().isEmpty()) {
//                pstmt.setString(6, vendor.getWebUrl().trim());
//            } else {
//                pstmt.setNull(6, Types.VARCHAR);
//            }
//
//            // Contact name.
//            if (vendor.getContactName() != null && !vendor.getContactName().isEmpty()) {
//                pstmt.setString(7, vendor.getContactName().trim());
//            } else {
//                pstmt.setNull(7, Types.VARCHAR);
//            }
//
//            // Phone number.
//            if (vendor.getPhoneNumber() != null && !vendor.getPhoneNumber().isEmpty()) {
//                pstmt.setString(8, vendor.getPhoneNumber().trim());
//            } else {
//                pstmt.setNull(8, Types.VARCHAR);
//            }
//
//            // Fax number.
//            if (vendor.getFaxNumber() != null && !vendor.getFaxNumber().isEmpty()) {
//                pstmt.setString(9, vendor.getFaxNumber().trim());
//            } else {
//                pstmt.setNull(9, Types.VARCHAR);
//            }
//
//            // Email.
//            if (vendor.getEmail() != null && !vendor.getEmail().isEmpty()) {
//                pstmt.setString(10, vendor.getEmail().trim());
//            } else {
//                pstmt.setNull(10, Types.VARCHAR);
//            }
//
//            // Account number.
//            if (vendor.getAccountNumber() != null && !vendor.getAccountNumber().isEmpty()) {
//                pstmt.setString(11, vendor.getAccountNumber().trim());
//            } else {
//                pstmt.setNull(11, Types.VARCHAR);
//            }
//
//            // Updated by.
//            pstmt.setInt(12, vendor.getUpdatedBy());
//
//            // Is foreign address.
//            if (vendor.getIsForeignAddress() != null) {
//                if (vendor.getIsForeignAddress()) {
//                    pstmt.setString(13, "Y");
//                } else {
//                    pstmt.setString(13, "N");
//                }
//            } else {
//                pstmt.setNull(13, Types.VARCHAR);
//            }
//
//            // Freign address.
//            if (vendor.getForeignAddress() != null && !vendor.getForeignAddress().isEmpty()) {
//                pstmt.setString(14, vendor.getForeignAddress().trim());
//            } else {
//                pstmt.setNull(14, Types.VARCHAR);
//            }
//
//            // Duns number.
//            if (vendor.getDunsNumber() != null && !vendor.getDunsNumber().isEmpty()) {
//                pstmt.setString(15, vendor.getDunsNumber().trim());
//            } else {
//                pstmt.setNull(15, Types.VARCHAR);
//            }
//
//            // Imported from.
//            if (vendor.getImportedFrom() != null && !vendor.getImportedFrom().isEmpty()) {
//                pstmt.setString(16, vendor.getImportedFrom().trim());
//            } else {
//                pstmt.setNull(16, Types.VARCHAR);
//            }
//
//            // Vendor id.
//            pstmt.setInt(17, vendor.getId());
//
//            // Created by. Only the creator can update the vendor.
//            pstmt.setInt(18, vendor.getUpdatedBy());
//
//            // Do the update.
//            int rowCount = pstmt.executeUpdate();
//            results.put(ROW_COUNT_KEY, rowCount);
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

//    public Map<String, Object> updateRequestVendor(RequestVendor requestVendor) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (requestVendor == null
//                || requestVendor.getRequestId() == null
//                || requestVendor.getVendorId() == null
//                || requestVendor.getUpdatedBy() == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//        String sql = "UPDATE request_vendor"
//                + " SET "
//                + "convenience_check = ?, "
//                + "convenience_check_just = ?, "
//                + "gsa_schedule = ?, "
//                + "gsa_schedule_just = ?, "
//                + "third_party_vendor = ?, "
//                + "third_party_vendor_just = ?, "
//                + "price_is_reasonable_just = ?, "
//                + "small_business = ?, "
//                + "small_business_just = ?, "
//                + "updated_by = ?, "
//                + "updated_date = SYSDATE, "
//                + "convenience_check_number = ?, "
//                + "division_org_id = ?, "
//                + "professional_org = ?"
//                + " WHERE "
//                + "vendor_id = ?"
//                + " AND "
//                + "request_id = ?"
//                + " AND "
//                + "( created_by = ? " // Only the creator can update the request vendor.
//                + " OR request_id = (select request_id from request where request_id= ? and "
//                + "(bankcard_holder_id = ? OR reviewer_id= ? OR bankcard_approving_official_id = ?) "
//                + " ) )";
//
//        LOG.info(String.format("sql: %s", sql));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//
//            // Convenience check.
//            if (requestVendor.getConvenienceCheck() != null) {
//                if (requestVendor.getConvenienceCheck()) {
//                    pstmt.setString(1, "Y");
//                } else {
//                    pstmt.setString(1, "N");
//                }
//            } else {
//                pstmt.setString(1, "N");
//            }
//
//            // Convenience check justification.
//            if (requestVendor.getConvenienceCheckJustification() != null && !requestVendor.getConvenienceCheckJustification().isEmpty()) {
//                pstmt.setString(2, requestVendor.getConvenienceCheckJustification().trim());
//            } else {
//                pstmt.setNull(2, Types.VARCHAR);
//            }
//
//            // Gsa schedule.
//            if (requestVendor.getGsaSchedule() != null) {
//                if (requestVendor.getGsaSchedule()) {
//                    pstmt.setString(3, "Y");
//                } else {
//                    pstmt.setString(3, "N");
//                }
//            } else {
//                pstmt.setString(3, "N");
//            }
//
//            // Gsa schedule justification.
//            if (requestVendor.getGsaScheduleJustification() != null && !requestVendor.getGsaScheduleJustification().isEmpty()) {
//                pstmt.setString(4, requestVendor.getGsaScheduleJustification().trim());
//            } else {
//                pstmt.setNull(4, Types.VARCHAR);
//            }
//
//            // Third party vendor.
//            if (requestVendor.getThirdPartyVendor() != null) {
//                if (requestVendor.getThirdPartyVendor()) {
//                    pstmt.setString(5, "Y");
//                } else {
//                    pstmt.setString(5, "N");
//                }
//            } else {
//                pstmt.setString(5, "N");
//            }
//
//            // Third party justification.
//            if (requestVendor.getThirdPartyJustification() != null && !requestVendor.getThirdPartyJustification().isEmpty()) {
//                pstmt.setString(6, requestVendor.getThirdPartyJustification().trim());
//            } else {
//                pstmt.setNull(6, Types.VARCHAR);
//            }
//
//            // Price justification.
//            if (requestVendor.getPriceJustification() != null && !requestVendor.getPriceJustification().isEmpty()) {
//                pstmt.setString(7, requestVendor.getPriceJustification().trim());
//            } else {
//                pstmt.setNull(7, Types.VARCHAR);
//            }
//
//            // Small business.
//            if (requestVendor.getSmallBusiness() != null) {
//                if (requestVendor.getSmallBusiness()) {
//                    pstmt.setString(8, "Y");
//                } else {
//                    pstmt.setString(8, "N");
//                }
//            } else {
//                pstmt.setNull(8, Types.VARCHAR);
//            }
//
//            // Small business justification.
//            if (requestVendor.getSmallBusinessJustification() != null && !requestVendor.getSmallBusinessJustification().isEmpty()) {
//                pstmt.setString(9, requestVendor.getSmallBusinessJustification().trim());
//            } else {
//                pstmt.setNull(9, Types.VARCHAR);
//            }
//
//            // Updated by.
//            pstmt.setInt(10, requestVendor.getUpdatedBy());
//
//            // Convenience check number.
//            if (requestVendor.getConvenienceCheckNumber() != null && !requestVendor.getConvenienceCheckNumber().isEmpty()) {
//                pstmt.setString(11, requestVendor.getConvenienceCheckNumber().trim());
//            } else {
//                pstmt.setNull(11, Types.VARCHAR);
//            }
//
//            // Division id.
//            if (requestVendor.getDivisionId() != null) {
//                pstmt.setInt(12, requestVendor.getDivisionId());
//            } else {
//                pstmt.setNull(12, Types.INTEGER);
//            }
//
//            // Professional org.
//            if (requestVendor.getProfessionalOrg() != null) {
//                if (requestVendor.getProfessionalOrg()) {
//                    pstmt.setString(13, "Y");
//                } else {
//                    pstmt.setString(13, "N");
//                }
//            } else {
//                pstmt.setString(13, "N"); // Default of NO.
//            }
//
//            // Vendor id.
//            pstmt.setInt(14, requestVendor.getVendorId());
//
//            // Request id.
//            pstmt.setInt(15, requestVendor.getRequestId());
//
//            // Updated by. Only the creator can update the request vendor.
//            pstmt.setInt(16, requestVendor.getUpdatedBy());
//
//            //since we are making vendor editable in the approval chain and do validation 
//            //when BCH ordered the request, reviewer, BAO and BCH odf the request can also update the vendor
//            // Request id again.
//            pstmt.setInt(17, requestVendor.getRequestId());
//            //BCH
//            pstmt.setInt(18, requestVendor.getUpdatedBy());
//            //Reviewer
//            pstmt.setInt(19, requestVendor.getUpdatedBy());
//            //BAO
//            pstmt.setInt(20, requestVendor.getUpdatedBy());
//
//            // Do the update.
//            int rowCount = pstmt.executeUpdate();
//            results.put(ROW_COUNT_KEY, rowCount);
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

//    public Map<String, Object> deleteVendor(Integer vendorId, Integer creatorId) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (vendorId == null || creatorId == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//
//        String sql = "UPDATE vendor SET is_active = 0, updated_by = ?, updated_date = SYSDATE WHERE vendor_id = ? AND created_by = ?";
//        LOG.info(String.format("sql: %s, id:%d", sql, vendorId));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//
//            // Updated by.
//            pstmt.setInt(1, creatorId);
//
//            // Vendor id.
//            pstmt.setInt(2, vendorId);
//
//            // Creator id.
//            pstmt.setInt(3, creatorId);
//
//            int rowCount = pstmt.executeUpdate();
//            results.put(ROW_COUNT_KEY, rowCount);
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

    /**
     * Deprecated
     *
     * @param vendorId
     * @param requestId
     * @return
     */
//    public Map<String, Object> deleteRequestVendor(Integer vendorId, Integer requestId) {
//        Map<String, Object> results = new HashMap<>();
//        StatusCode statusCode = StatusCode.OK;
//        if (vendorId == null) {
//            statusCode = StatusCode.IncompleteData;
//            results.put(STATUS_CODE_KEY, statusCode);
//            return results;
//        }
//
//        String sql = "DELETE FROM request_vendor WHERE request_id = ? and vendor_id = ?";
//        LOG.info(String.format("sql: %s, id:%d", sql, vendorId));
//        try (Connection connection = getConnection(false);
//                PreparedStatement pstmt = connection.prepareStatement(sql);) {
//
//            pstmt.setInt(1, requestId);
//            pstmt.setInt(2, vendorId);
//            pstmt.executeUpdate();
//
//            if (statusCode == StatusCode.OK) {
//                connection.commit();
//            } else {
//                connection.rollback();
//            }
//        } catch (Exception caught) {
//            statusCode = StatusCode.DatabaseError;
//            LOG.log(Level.SEVERE, caught.getMessage(), caught);
//        }
//
//        results.put(STATUS_CODE_KEY, statusCode);
//        return results;
//    }

}
