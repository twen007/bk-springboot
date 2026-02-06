package gov.nist.oism.asd.empbc.db;

import gov.nist.oism.asd.empbc.model.ItemQueryParam;
import gov.nist.oism.asd.empbc.model.PcItem;
import gov.nist.oism.asd.empbc.model.Item;
import gov.nist.oism.asd.empbc.model.ChemicalItem;
import gov.nist.oism.asd.empbc.model.EaItem;
import gov.nist.oism.asd.empbc.model.FileAttachment;
import gov.nist.oism.asd.empbc.model.IbbrChemicalItem;
import gov.nist.oism.asd.empbc.model.ItemStatus;
import gov.nist.oism.asd.empbc.model.Lookup;
import gov.nist.oism.asd.empbc.util.StatusCode;
import gov.nist.oism.asd.empbc.util.PreparedStatementUtil;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ItemDao extends OracleDao {

    private static final Logger LOG = Logger.getLogger(ItemDao.class.getSimpleName());

    public static final String ITEMS_LIST_KEY = "items_list_key";
    public static final String ITEM_KEY = "item_key";
    public static final String CHEMICAL_ITEM_KEY = "chemical_item_key";
    public static final String ID_KEY = "id_key";
    public static final String PROCESSED_STATE_KEY = "processed_state_key";
    public static final String SHOPPING_CART_KEY = FileAttachmentDao.FILE_ATTACHMENT_KEY;
    public static final String SHOPPING_CART_FILE_ID_KEY = FileAttachmentDao.FILE_ID_KEY;
    public static final String IBBR_CHEMICAL_ITEMS_KEY = "ibbr_chemical_items_key";
    public static final String IBBR_CHEMICAL_ITEM_ID_KEY = "ibbr_chemical_item_id_key";
    public static final String ITEM_STATUS_TYPE_KEY = "item_status_type_key";
    public static final String ERROR_CODE_KEY = "error_code_key";
    public static final String ERROR_MESSAGE_KEY = "error_message_key";
    public static String PC_ITEMS_QUERY = "select * from V_RPT_PROPERTY_CUSTODIAN_ITEMS where 1=1 ";

    @Data
    @NoArgsConstructor
    public static class PcItemCriteria implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer ouId;
        private Integer divId;
        private Integer grpId;
        private Integer fy;
        private String fromDate;
        private String toDate;
    }

    public Map<String, Object> selectPropertyCustodianItemsReport(PcItemCriteria criteria) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        StringBuilder sql = new StringBuilder(PC_ITEMS_QUERY);
        List<PcItem> pcItems = new ArrayList<>();

        List<Object> params = new ArrayList<>();

        if (Objects.nonNull(criteria.getOuId())) {
            sql.append(" AND OU_ID = ?");
            params.add(criteria.getOuId());
        }
        if (Objects.nonNull(criteria.getDivId())) {
            sql.append(" AND DIV_ID = ?");
            params.add(criteria.getDivId());
        }
        if (Objects.nonNull(criteria.getGrpId())) {
            sql.append(" AND GRP_ID = ?");
            params.add(criteria.getGrpId());
        }
        if (Objects.nonNull(criteria.getFy())) {
            sql.append(" AND FY = ?");
            params.add(criteria.getFy());
        }
        // Date range filtering
        if (Objects.nonNull(criteria.getFromDate())) {
            sql.append(" and CREATED_DATE >= to_date (?,'yyyy-mm-dd')");
            params.add(criteria.getFromDate());
        }
        if (Objects.nonNull(criteria.getToDate())) {
            sql.append(" AND CREATED_DATE <= to_date (?,'yyyy-mm-dd')");
            params.add(criteria.getToDate());
        }

        LOG.info(String.format("sql: %s", sql.toString()));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql.toString());) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try ( ResultSet resultSet = pstmt.executeQuery();) {
                while (resultSet.next()) {
                    PcItem pcItem = new PcItem();
                    pcItem.setRequestId(resultSet.getInt("REQUEST_ID"));
                    pcItem.setFy(resultSet.getInt("FY"));
                    pcItem.setCreatedDate(resultSet.getTimestamp("CREATED_DATE"));
                    pcItem.setRequisitionNumber(resultSet.getString("REQUISITION_NUMBER"));
                    pcItem.setOuId(resultSet.getInt("OU_ID"));
                    pcItem.setDivId(resultSet.getInt("DIV_ID"));
                    pcItem.setGrpId(resultSet.getInt("GRP_ID"));
                    pcItem.setOu(resultSet.getString("OU"));
                    pcItem.setDivision(resultSet.getString("DIVISION"));
                    pcItem.setGroup(resultSet.getString("Group"));
                    pcItem.setVendor(resultSet.getString("Vendor"));
                    pcItem.setItemId(resultSet.getInt("ITEM_ID"));
                    pcItem.setCatelogNumber(resultSet.getString("CATELOG_NUMBER"));
                    pcItem.setItemName(resultSet.getString("ITEM_NAME"));
                    pcItem.setItemDescription(resultSet.getString("ITEM_DESCRIPTION"));
                    pcItem.setPrice(resultSet.getDouble("PRICE"));
                    pcItem.setQuantity(resultSet.getInt("QUANTITY"));
                    pcItem.setPurpose(resultSet.getString("PURPOSE"));
                    pcItem.setIsChemical(resultSet.getString("IS_CHEMICAL"));
                    pcItem.setShoppingCartFileId(resultSet.getInt("SHOPPING_CART_FILE_ID"));
                    pcItem.setItemStatus(resultSet.getString("ITEM_STATUS"));
                    pcItem.setItemStatusId(resultSet.getInt("ITEM_STATUS_ID"));
                    pcItem.setProjectTask(resultSet.getString("PROJECT_TASK"));
                    pcItem.setObjectClass(resultSet.getString("OBJECT_CLASS"));
                    pcItem.setIsTaggableEquipment(resultSet.getString("IS_TAGGABLE_EQUIPMENT"));
                    pcItem.setPriceOrdered(resultSet.getDouble("PRICE_ORDERED"));
                    pcItem.setQuantityOrdered(resultSet.getInt("QUANTITY_ORDERED"));
                    pcItem.setItemNotes(resultSet.getString("ITEM_NOTES"));
                    pcItem.setDateReceived(resultSet.getTimestamp("DATE_RECEIVED"));
                    pcItem.setTransactionNumber(resultSet.getString("TRANSACTION_NUMBER"));
                    pcItem.setStatementDate(resultSet.getTimestamp("STATEMENT_DATE"));
                    pcItem.setUnitIssue(resultSet.getString("UNIT_ISSUE"));
                    pcItem.setPurchaseTypeId(resultSet.getInt("PURCHASE_TYPE_ID"));
                    pcItems.add(pcItem);
                }
            }

            results.put(ITEMS_LIST_KEY, pcItems);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;

    }

    public Map<String, Object> selectEAItemsWithParams(ItemQueryParam param) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (param == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "select i.*, "
                + " r.REQUISITION_NUMBER, "
                + " (select o.org_cd from nist_OU o where o.org_id= r.OU_ID) ou, "
                + " (select d.org_cd from nist_DIVISION d where d.org_id= r.DIV_ID) division, "
                + " get_user_name(r.bankcard_holder_id) bch,"
                + " rv.VENDOR_NAME "
                + " from item i, item_status s, request r, nist_user u, request_vendor_t rv "
                + " where i.ITEM_ID = s.ITEM_ID "
                + " and s.ITEM_STATUS_TYPE_ID = 5 " // 5 means "Estimated Accrual"
                + " and i.REQUEST_ID = r.REQUEST_ID and "
                + " r.REQUESTER_ID = u.PEOPLE_ID and r.REQUEST_ID = rv.REQUEST_ID "
                + " and s.ITEM_STATUS_ID = (select max(s1.ITEM_STATUS_ID) from item_status s1 where i.ITEM_ID = s1.ITEM_ID) ";

        String sqlOrderby = " order by i.project_task, i.request_id, i.item_id";

        //System.out.println ("ouId = " + param.getOuId());
        //System.out.println ("divId = " + param.getDivisionId());
        //System.out.println ("param.getOrgCodes () = " + param.getOrgCodes ());
        //if (param.getDivisionId () != null) sql += " and div_org_id = " + param.getDivisionId ();
        String orgCode = param.getDivCode();
        /*String orgCodes = param.getOrgCodes();
        if (orgCode != null && orgCode.length() > 1) {
            orgCodes += "'" + orgCode + "'";
        }*/

        //System.out.println ("orgCodes = " + orgCodes);
        //MB-425 since other ou, div could use ptc from the AO's support divs, using orgcodes against request's division no longer works
        //sql += " and u.OU_ORG_ID in (select d.OU_ORG_ID from nist_division d where d.ORG_CD in (" + orgCodes + "))";
        //instead, use orgcodes that matches item's ptc(first 3 chars)
        if (orgCode != null && orgCode.trim().length() >= 3) {
            sql += " and substr(trim(i.project_task),0,3) in (" + orgCode + ")";
        }

        if (param.getFromDate() != null) {
            sql += " and r.created_date >= to_date ('" + param.getFromDate() + "','yyyy-mm-dd')";
        }
        if (param.getToDate() != null) {
            sql += " and r.created_date <= to_date ('" + param.getToDate() + "','yyyy-mm-dd')";
        }

        sql += sqlOrderby;

        LOG.info(String.format("sql: %s", sql));

        try {
            Connection conn = getConnection(true);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rset = pstmt.executeQuery();

            List<Item> items = new ArrayList<>();
            while (rset.next()) {
                EaItem item = new EaItem();
                item.setRequestId(rset.getInt("request_id"));
                item.setOu(rset.getString("ou"));
                item.setProjectTask(rset.getString("project_task"));
                item.setObjectClass(rset.getString("object_class"));
                item.setDivision(rset.getString("division"));
                item.setBch(rset.getString("bch"));
                item.setId(rset.getInt("item_id"));
                item.setItemName(rset.getString("item_name"));
                item.setDescription(rset.getString("item_description"));
                item.setVendorId(rset.getInt("vendor_id"));
                item.setVendorName(rset.getString("vendor_name"));
                item.setRequisitionNumber(rset.getString("requisition_number"));
                item.setCatalogNumber(rset.getString("catelog_number"));
                item.setQuantity(rset.getInt("quantity"));
                item.setPrice(rset.getDouble("price"));
                item.setActualQuantity(rset.getInt("quantity_ordered"));
                item.setActualPrice(rset.getDouble("price_ordered"));
                Timestamp statementDate = rset.getTimestamp("statement_date");
                if (!rset.wasNull()) {
                    item.setStatementDate(statementDate);
                }
                String chemical = rset.getString("chemical");
                if (!rset.wasNull()) {
                    item.setChemical("Y".equals(chemical));
                } else {
                    item.setChemical(Boolean.FALSE);
                }
                item.setShoppingCartFileId(rset.getInt("shopping_cart_file_id"));

                items.add(item);
            }

            results.put(ITEMS_LIST_KEY, items);

        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> getItemStatusType() {
        LOG.info("getItemStatusType");
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;

        List<Lookup> lookups = new ArrayList<>();
        String sql = "select * from LKUP_ITEM_STATUS_TYPE order by ITEM_STATUS_TYPE_ID";

        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    Lookup lookup = new Lookup();
                    lookup.setId(rset.getInt("ITEM_STATUS_TYPE_ID"));
                    lookup.setName(rset.getString("ITEM_STATUS_TYPE_NAME"));
                    lookups.add(lookup);
                    //System.out.println ("id=" + lookup.getId() + ", name=" + lookup.getName());
                }
            }
            results.put(ITEM_STATUS_TYPE_KEY, lookups);
        } catch (Exception ex) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectItemsForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        try ( Connection connection = getConnection(true);) {
            results = selectItemsForRequestWithConnection(connection, requestId);
            statusCode = (StatusCode) results.get(STATUS_CODE_KEY);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectIbbrChemicalItemsForRequest(Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        List<IbbrChemicalItem> ibbrChemicalItems = new ArrayList<>();
        String sql = "SELECT "
                + "ic.item_id, "
                + "i.item_name, "
                + "ic.cas_number, "
                + "ic.ibbr_room_name, "
                + "ic.location, "
                + "ic.catalog_number, "
                + "(SELECT vendor_name FROM request_vendor_t WHERE request_id = r.request_id) AS vendor_name, "
                + "i.price, "
                + "(SELECT last_name FROM nist_user_active WHERE people_id = r.requester_id) AS last_name, "
                + "(SELECT first_name FROM nist_user_active WHERE people_id = r.requester_id) AS first_name, "
                + "ic.amount_per_container, "
                //MB-467
                + "(SELECT email FROM nist_user_active WHERE people_id = r.requester_id) AS email, "
                + "i.quantity "
                + " FROM "
                + "item_chemical ic, item i, request r"
                + " WHERE "
                + "r.request_id = ?"
                + " AND "
                + "r.request_id = i.request_id"
                + " AND "
                + "ic.item_id = i.item_id"
                + " AND "
                + "ic.ibbr_room_id is NOT NULL";
        LOG.info(String.format("%s - requestId: %d", sql, requestId));
        try ( Connection connection = getConnection(true);  PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, requestId);
            int itemId = 0;
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    itemId = rset.getInt("item_id");
                    IbbrChemicalItem ibbrChemicalItem = new IbbrChemicalItem();

                    // Item name.
                    ibbrChemicalItem.setName(rset.getString("item_name"));

                    // Cas number
                    ibbrChemicalItem.setCas(rset.getString("cas_number"));

                    // Room name.
                    ibbrChemicalItem.setRoom(rset.getString("ibbr_room_name"));

                    // Location.
                    ibbrChemicalItem.setLocation(rset.getString("location"));

                    // Catalog number.
                    ibbrChemicalItem.setCatalog(rset.getString("catalog_number"));

                    // Vendor name.
                    ibbrChemicalItem.setSupplier(rset.getString("vendor_name"));

                    // Price.
                    double price = rset.getDouble("price");
                    if (!rset.wasNull()) {
                        ibbrChemicalItem.setCost(String.format("%.2f", price));
                    }

                    // Last name.
                    String lastName = rset.getString("last_name");
                    if (lastName != null) {
                        lastName = lastName.toLowerCase();
                    }
                    ibbrChemicalItem.setOwner_sn(lastName);

                    // First name.
                    String firstName = rset.getString("first_name");
                    if (firstName != null) {
                        firstName = firstName.toLowerCase();
                    }
                    ibbrChemicalItem.setOwner_given(firstName);

                    // Amount.
                    ibbrChemicalItem.setAmount(rset.getString("amount_per_container"));

                    //email
                    ibbrChemicalItem.setOwner_Email(rset.getString("email"));

                    //quantity
                    ibbrChemicalItem.setQuantity(rset.getInt("quantity"));

                    ibbrChemicalItems.add(ibbrChemicalItem);
                }
            }

            results.put(IBBR_CHEMICAL_ITEMS_KEY, ibbrChemicalItems);
            results.put(IBBR_CHEMICAL_ITEM_ID_KEY, itemId);

        } catch (SQLException caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> selectItem(Integer itemId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (itemId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        try ( Connection connection = getConnection(true);) {

            // Item.
            Item item = selectItemByIdWithConnection(connection, itemId);

            // Chemical item.
            ChemicalItem chemicalItem = selectChemicalItem(connection, itemId);

            // Shopping cart.
            FileAttachment fileAttachment = null;
            if (item != null && item.getShoppingCartFileId() != null) {
                FileAttachmentDao dao = new FileAttachmentDao();
                Map<String, Object> fileAttachmentResults = dao.selectFileAttachment(item.getShoppingCartFileId(), false);
                if ((StatusCode) fileAttachmentResults.get(STATUS_CODE_KEY) == StatusCode.OK) {
                    fileAttachment = (FileAttachment) fileAttachmentResults.get(FileAttachmentDao.FILE_ID_KEY);
                }
            }

            results.put(ITEM_KEY, item);
            results.put(CHEMICAL_ITEM_KEY, chemicalItem);
            results.put(SHOPPING_CART_KEY, fileAttachment);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertItem(Item item, Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getItemName() == null || item.getItemName().isEmpty() || item.getRequestId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "INSERT INTO"
                + " item "
                + "("
                + "request_id, "
                //+ "item_type, " +
                + "vendor_id, "
                + "catelog_number, "
                + "item_name, "
                + "item_description, "
                + "price, "
                + "quantity, "
                + "price_ordered, "
                + "quantity_ordered, "
                + "purpose, "
                + "chemical, "
                + "project_task, "
                + "object_class, "
                + "shopping_cart_file_id, "
                + "item_status_id, "
                + "is_shipping, "
                + "item_notes, "
                + "unit_issue, "
                + "date_received, "
                + "is_taggable_equipment"
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"item_id"});) {

            insertItem(item, pstmt);

            // Do the insert and get back the generated key.
            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.InsertFailed;
            }

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                Integer id = generatedKeys.getInt(1);
                results.put(ID_KEY, id);
                generatedKeys.close();

                // Create an item status.
                ItemStatus itemStatus = new ItemStatus();
                itemStatus.setItemId(id);
                itemStatus.setTypeId(1);
                itemStatus.setCreatedBy(createdBy);
                Map<String, Object> resultsForItemStatusInsert = insertItemStatusWithConnection(connection, itemStatus);
                statusCode = (StatusCode) resultsForItemStatusInsert.get(STATUS_CODE_KEY);
            } else {
                statusCode = StatusCode.InsertFailed;
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertChemicaItem(Item item, ChemicalItem chemicalItem, Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getItemName() == null || item.getItemName().isEmpty() || item.getRequestId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "INSERT INTO"
                + " item "
                + "("
                + "request_id, "
                //+ "item_type, " +
                + "vendor_id, "
                + "catelog_number, "
                + "item_name, "
                + "item_description, "
                + "price, "
                + "quantity, "
                + "price_ordered, "
                + "quantity_ordered, "
                + "purpose, "
                + "chemical, "
                + "project_task, "
                + "object_class, "
                + "shopping_cart_file_id, "
                + "item_status_id, "
                + "is_shipping, "
                + "item_notes, "
                + "unit_issue, "
                + "date_received, "
                + "is_taggable_equipment"
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"item_id"});) {

            insertItem(item, pstmt);

            // Do the insert and get back the generated key.
            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.InsertFailed;
            }

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys != null && generatedKeys.next()) {
                Integer id = generatedKeys.getInt(1);
                results.put(ID_KEY, id);
                generatedKeys.close();

                sql = "INSERT INTO"
                        + " item_chemical "
                        + "("
                        + "item_id, "
                        + "owner_id, "
                        + "location, "
                        + "sub_location, "
                        + "cas_number, "
                        + "chemical_form, "
                        + "chemical_grade, "
                        + "manufacturer_name, "
                        + "catalog_number, "
                        + "catalog_number_quantity, "
                        + "containers_per_package, "
                        + "amount_per_container, "
                        + "lables_needed, "
                        + "container_type, "
                        + "expiration_date, "
                        + "health_nfpa_value, "
                        + "flammability_nfpa_value, "
                        + "reactivity_nfpa_value, "
                        + "special_code_nfpa_value, "
                        + "is_radioactive_material, "
                        + "biohazard_registration_req, "
                        + "special_instruction, "
                        + "ibbr_room_id, "
                        + "ibbr_room_name, "
                        + "primary_user_id, "
                        + "cispro_remarks, "
                        + "container_total, "
                        + "product_url"
                        + ")"
                        + " VALUES "
                        + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                LOG.info(String.format("sql: %s", sql));
                try ( PreparedStatement pstmt2 = connection.prepareStatement(sql);) {

                    chemicalItem.setId(id);
                    insertChemicalItem(chemicalItem, pstmt2);

                    // Do the insert for chemical.
                    rowCount = pstmt2.executeUpdate();
                    if (rowCount == 0) {
                        statusCode = StatusCode.InsertFailed;
                    }

                    // Create an item status.
                    ItemStatus itemStatus = new ItemStatus();
                    itemStatus.setItemId(id);
                    itemStatus.setTypeId(1);
                    itemStatus.setCreatedBy(createdBy);
                    Map<String, Object> resultsForItemStatusInsert = insertItemStatusWithConnection(connection, itemStatus);
                    statusCode = (StatusCode) resultsForItemStatusInsert.get(STATUS_CODE_KEY);
                }
            } else {
                statusCode = StatusCode.InsertFailed;
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertShoppingCartItem(Item item, FileAttachment fileAttachment, Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getRequestId() == null
                //|| item.getVendorId() == null
                || fileAttachment.getRequestId() == null
                || fileAttachment.getContent() == null
                || fileAttachment.getCreatedBy() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        try ( Connection connection = getConnection(false);) {
            FileAttachmentDao dao = new FileAttachmentDao();
            results = dao.insertAttachmentWithConnection(fileAttachment, connection);
            statusCode = (StatusCode) results.get(STATUS_CODE_KEY);
            if (statusCode == StatusCode.OK) {
                String sql = "INSERT INTO"
                        + " item "
                        + "("
                        + "request_id, "
                        //+ "vendor_id, "
                        + "item_name, "
                        + "price, "
                        + "shopping_cart_file_id, "
                        + "quantity, "
                        //MB-401, for shopping cart, set actual price and quantity to be the same  as estimated just like normal items
                        + "price_ordered, "
                        + "quantity_ordered, "
                        + "chemical"
                        + ")"
                        + " VALUES "
                        + "(?, 'Shopping Cart Item', ?, ?, 1, ?, 1, ?)";
                LOG.info(String.format("sql: %s", sql));
                try ( PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"item_id"});) {

                    // Request id.
                    if (item.getRequestId() != null) {
                        pstmt.setInt(1, item.getRequestId());
                    } else {
                        pstmt.setNull(1, Types.INTEGER);
                    }

                    // Vendor id.
                    //pstmt.setInt(2, item.getVendorId());
                    // Price.
                    if (item.getPrice() != null) {
                        pstmt.setDouble(2, item.getPrice());
                    } else {
                        pstmt.setNull(2, Types.DOUBLE);
                    }

                    // Shopping cart file id.
                    pstmt.setInt(3, (Integer) results.get(FileAttachmentDao.FILE_ID_KEY));

                    // actual Price.
                    if (item.getPrice() != null) {
                        pstmt.setDouble(4, item.getPrice());
                    } else {
                        pstmt.setNull(4, Types.DOUBLE);
                    }

                    pstmt.setString(5, item.getChemical() ? "Y" : "N");

                    int rowCount = pstmt.executeUpdate();
                    if (rowCount == 0) {
                        statusCode = StatusCode.InsertFailed;
                    }

                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys != null && generatedKeys.next()) {
                        Integer id = generatedKeys.getInt(1);
                        results.put(ID_KEY, id);
                        generatedKeys.close();

                        // Now set the is shopping cart to yes in the request table.
                        sql = "UPDATE request"
                                + " SET "
                                + "is_shopping_cart = 'Y'"
                                + " WHERE "
                                + "request_id = ?";
                        LOG.info(String.format("sql: %s", sql));
                        try ( PreparedStatement pstmt2 = connection.prepareStatement(sql);) {
                            pstmt2.setInt(1, item.getRequestId());

                            rowCount = pstmt2.executeUpdate();
                            if (rowCount == 0) {
                                statusCode = StatusCode.InsertFailed;
                            }
                        }

                        // Create an item status.
                        ItemStatus itemStatus = new ItemStatus();
                        itemStatus.setItemId(id);
                        itemStatus.setTypeId(1);
                        itemStatus.setCreatedBy(createdBy);
                        Map<String, Object> resultsForItemStatusInsert = insertItemStatusWithConnection(connection, itemStatus);
                        statusCode = (StatusCode) resultsForItemStatusInsert.get(STATUS_CODE_KEY);
                    } else {
                        statusCode = StatusCode.InsertFailed;
                    }
                }
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> insertItemStatusTypes(Integer[] itemIds, Integer requestId, Integer createdById, Integer statusTypeId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (itemIds == null || itemIds.length == 0 || requestId == null || createdById == null || statusTypeId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_insert_item_status_types(?, ?, ?, ?) }";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {

            //OracleConnection oracleConnection = connection.unwrap(OracleConnection.class);
            //cstmt.setArray(1, oracleConnection.createOracleArray("ARRAY_INT", itemIds));
            //there seems have a lib conflict related error creating oracleArray, so changed it to pass in a string
            //and updated the SP to parse the string to array
            cstmt.setString(1, Arrays.toString(itemIds));
            cstmt.setInt(2, requestId);
            cstmt.setInt(3, createdById);
            cstmt.setInt(4, statusTypeId);
            cstmt.executeUpdate();
            connection.commit();
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateItem(Item item) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getItemName() == null || item.getItemName().isEmpty() || item.getId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                // + "item_type = ?, " +
                + "vendor_id = ?, "
                + "catelog_number = ?, "
                + "item_name = ?, "
                + "item_description = ?, "
                + "price = ?, "
                + "quantity = ?, "
                + "purpose = ?, "
                + "chemical = ?, "
                + "project_task = ?, "
                + "shopping_cart_file_id = ?, "
                + "item_status_id = ?, "
                + "object_class = ?, "
                + "is_shipping = ?, "
                + "item_notes = ?, "
                + "unit_issue = ?, "
                + "date_received = ?, "
                + "price_ordered = ?, "
                + "quantity_ordered = ?, "
                + "is_taggable_equipment = ?"
                + " WHERE "
                + "item_id = ?";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            updateItem(item, pstmt);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.UpdateFailed;
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateItemForProcessedRequest(Item item, Integer itemStatusTypeId, Integer createdBy) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        if (item.getQuantity() == null && item.getPrice() == null && item.getActualQuantity() == null && item.getActualPrice() == null && item.getItemNotes() == null && item.getDateReceived() == null && itemStatusTypeId == null) {
            results.put(ROW_COUNT_KEY, 0);
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_update_item_process(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {

            // Item id.
            cstmt.setInt("p_item_id", item.getId());

            // Item price.
            if (item.getPrice() != null) {
                cstmt.setDouble("p_price", item.getPrice());
            } else {
                cstmt.setNull("p_price", Types.DOUBLE);
            }

            // Item quantity.
            if (item.getQuantity() != null) {
                cstmt.setInt("p_quantity", item.getQuantity());
            } else {
                cstmt.setNull("p_quantity", Types.INTEGER);
            }

            // Item price ordered.
            if (item.getActualPrice() != null) {
                cstmt.setDouble("p_price_ordered", item.getActualPrice());
            } else {
                cstmt.setNull("p_price_ordered", Types.DOUBLE);
            }

            // Item quantity ordered.
            if (item.getActualQuantity() != null) {
                cstmt.setInt("p_quantity_ordered", item.getActualQuantity());
            } else {
                cstmt.setNull("p_quantity_ordered", Types.INTEGER);
            }

            // Item notes.
            if (item.getItemNotes() != null && !item.getItemNotes().isEmpty()) {
                cstmt.setString("p_item_notes", item.getItemNotes().trim());
            } else {
                cstmt.setNull("p_item_notes", Types.VARCHAR);
            }

            // Date received.
            if (item.getDateReceived() != null) {
                cstmt.setTimestamp("p_date_received", new Timestamp(item.getDateReceived().getTime()));
            } else {
                cstmt.setNull("p_date_received", Types.TIMESTAMP);
            }

            // Item status type id.
            if (itemStatusTypeId != null) {
                cstmt.setInt("p_item_status_type_id", itemStatusTypeId);
            } else {
                cstmt.setNull("p_item_status_type_id", Types.INTEGER);
            }

            // Created by.
            if (createdBy != null) {
                cstmt.setInt("p_created_by", createdBy);
            } else {
                cstmt.setNull("p_created_by", Types.INTEGER);
            }

            cstmt.registerOutParameter("o_route_type_id", Types.INTEGER);
            cstmt.registerOutParameter("o_row_count", Types.INTEGER);
            cstmt.execute();

            results.put(PROCESSED_STATE_KEY, cstmt.getInt("o_route_type_id") == 4);
            results.put(ROW_COUNT_KEY, cstmt.getInt("o_row_count"));

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateItemForBilledRequest(Item item) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        if (item.getTransactionNumber() == null && item.getStatementDate() == null) {
            results.put(ROW_COUNT_KEY, 0);
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_update_billed(?, ?, ?, ?) }";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  CallableStatement cstmt = connection.prepareCall(sql);) {

            // Item id.
            cstmt.setInt("p_item_id", item.getId());

            // Transaction number.
            if (item.getTransactionNumber() != null) {
                cstmt.setString("p_transaction_number", item.getTransactionNumber().trim());
            } else {
                cstmt.setNull("p_transaction_number", Types.VARCHAR);
            }

            // Statement date.
            if (item.getStatementDate() != null) {
                cstmt.setTimestamp("p_statement_date", new Timestamp(item.getStatementDate().getTime()));
            } else {
                cstmt.setNull("p_statement_date", Types.TIMESTAMP);
            }

            cstmt.registerOutParameter("o_row_count", Types.INTEGER);
            cstmt.execute();

            results.put(ROW_COUNT_KEY, cstmt.getInt("o_row_count"));

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateChemicaItem(Item item, ChemicalItem chemicalItem) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (item.getItemName() == null || item.getItemName().isEmpty() || item.getId() == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                //+ "item_type = ?, " +
                + "vendor_id = ?, "
                + "catelog_number = ?, "
                + "item_name = ?, "
                + "item_description = ?, "
                + "price = ?, "
                + "quantity = ?, "
                + "purpose = ?, "
                + "chemical = ?, "
                + "project_task = ?, "
                + "shopping_cart_file_id = ?, "
                + "item_status_id = ?, "
                + "object_class = ?, "
                + "is_shipping = ?, "
                + "item_notes = ?, "
                + "unit_issue = ?, "
                + "date_received = ?, "
                + "price_ordered = ?, "
                + "quantity_ordered = ?, "
                + "is_taggable_equipment = ?"
                + " WHERE "
                + "item_id = ?";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            updateItem(item, pstmt);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.UpdateFailed;
            } else {

                //added this to handle the situation when a user tries to update a non chem item to a chem item
                //in this case, there's no record in the item_chemical table and we need to insert instead of update
                //because the update will always return 0 updated row and resulted in returning an error to the client app
                sql = "select count(*) from item_chemical where item_id = ?";

                LOG.info(String.format("sql: %s", sql));
                try ( PreparedStatement pstmt2 = connection.prepareStatement(sql);) {
                    int count = 0;
                    pstmt2.setInt(1, item.getId());
                    // check if an chem record exists
                    ResultSet rs = pstmt2.executeQuery();
                    if (rs != null && rs.next()) {
                        count = rs.getInt(1);
                    }
                    if (count == 0) {
                        sql = "INSERT INTO"
                                + " item_chemical "
                                + "("
                                + "item_id, "
                                + "owner_id, "
                                + "location, "
                                + "sub_location, "
                                + "cas_number, "
                                + "chemical_form, "
                                + "chemical_grade, "
                                + "manufacturer_name, "
                                + "catalog_number, "
                                + "catalog_number_quantity, "
                                + "containers_per_package, "
                                + "amount_per_container, "
                                + "lables_needed, "
                                + "container_type, "
                                + "expiration_date, "
                                + "health_nfpa_value, "
                                + "flammability_nfpa_value, "
                                + "reactivity_nfpa_value, "
                                + "special_code_nfpa_value, "
                                + "is_radioactive_material, "
                                + "biohazard_registration_req, "
                                + "special_instruction, "
                                + "ibbr_room_id, "
                                + "ibbr_room_name, "
                                + "primary_user_id, "
                                + "cispro_remarks, "
                                + "container_total, "
                                + "product_url"
                                + ")"
                                + " VALUES "
                                + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        LOG.info(String.format("sql: %s", sql));
                        try ( PreparedStatement pstmt3 = connection.prepareStatement(sql);) {

                            chemicalItem.setId(item.getId());
                            insertChemicalItem(chemicalItem, pstmt3);

                            // Do the insert for chemical.
                            rowCount = pstmt3.executeUpdate();
                            if (rowCount == 0) {
                                statusCode = StatusCode.InsertFailed;
                            }
                        }

                    } else {
                        chemicalItem.setId(item.getId());
                        sql = "UPDATE item_chemical"
                                + " SET "
                                + "owner_id = ?, "
                                + "location = ?, "
                                + "sub_location = ?, "
                                + "cas_number = ?, "
                                + "chemical_form = ?, "
                                + "chemical_grade = ?, "
                                + "manufacturer_name = ?, "
                                + "catalog_number = ?, "
                                + "catalog_number_quantity = ?, "
                                + "containers_per_package = ?, "
                                + "amount_per_container = ?, "
                                + "lables_needed = ?, "
                                + "container_type = ?, "
                                + "expiration_date = ?, "
                                + "health_nfpa_value = ?, "
                                + "flammability_nfpa_value = ?, "
                                + "reactivity_nfpa_value = ?, "
                                + "special_code_nfpa_value= ?, "
                                + "is_radioactive_material = ?, "
                                + "biohazard_registration_req = ?, "
                                + "special_instruction = ?, "
                                + "ibbr_room_id = ?, "
                                + "ibbr_room_name = ?, "
                                + "primary_user_id = ?, "
                                + "cispro_remarks = ?, "
                                + "container_total= ?, "
                                + "product_url=?"
                                + " WHERE "
                                + "item_id = ?";

                        LOG.info(String.format("sql: %s", sql));
                        try ( PreparedStatement pstmt4 = connection.prepareStatement(sql);) {

                            updateChemicalItem(chemicalItem, pstmt4);

                            // Do the update.
                            rowCount = pstmt4.executeUpdate();
                            if (rowCount == 0) {
                                statusCode = StatusCode.UpdateFailed;
                            }
                        }

                    }
                }

            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateProjectTaskForRequestItems(String projectTask, Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (projectTask == null || projectTask.isEmpty() || requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                + "project_task = ?"
                + " WHERE "
                + "request_id = ?";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            pstmt.setString(1, projectTask);
            pstmt.setInt(2, requestId);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateProjectTaskForItem(String projectTask, Integer itemId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (projectTask == null || projectTask.isEmpty() || itemId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                + "project_task = ?"
                + " WHERE "
                + "item_id = ?";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            pstmt.setString(1, projectTask);
            pstmt.setInt(2, itemId);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateObjectClassForRequestItems(String objectClass, Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (objectClass == null || objectClass.isEmpty() || requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                + "object_class = ?"
                + " WHERE "
                + "request_id = ?"
                + " AND "
                + "is_shipping = 'N'";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            pstmt.setString(1, objectClass);
            pstmt.setInt(2, requestId);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> updateObjectClassForItem(String objectClass, Integer itemId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (objectClass == null || objectClass.isEmpty() || itemId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "UPDATE item"
                + " SET "
                + "object_class = ?"
                + " WHERE "
                + "item_id = ?"
                + " AND "
                + "is_shipping = 'N'";

        LOG.info(String.format("sql: %s", sql));
        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            pstmt.setString(1, objectClass);
            pstmt.setInt(2, itemId);

            // Do the update and get back the number of affected rows.
            int rowCount = pstmt.executeUpdate();
            results.put(ROW_COUNT_KEY, rowCount);

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> deleteItem(Integer itemId, Integer fileId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (itemId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }

        String sql = "select request_id from item where item_id = " + itemId;

        try ( Connection connection = getConnection(false);  PreparedStatement pstmt = connection.prepareStatement(sql);) {

            ResultSet rset = pstmt.executeQuery();
            Integer requestId = 0;
            if (rset.next()) {
                requestId = rset.getInt("request_id");
            }

            sql = "DELETE FROM item WHERE item_id = ?";
            LOG.info(String.format("sql: %s, id:%d", sql, itemId));
            try ( PreparedStatement pstmt1 = connection.prepareStatement(sql);) {

                pstmt1.setInt(1, itemId);
                pstmt1.executeUpdate();
            }

            // Chemical item.
            sql = "DELETE FROM item_chemical WHERE item_id = ?";
            LOG.info(String.format("sql: %s, id:%d", sql, itemId));
            try ( PreparedStatement pstmt2 = connection.prepareStatement(sql);) {
                pstmt2.setInt(1, itemId);
                pstmt2.executeUpdate();
            }

            // Attachment.
            if (fileId != 0) {
                sql = "UPDATE request"
                        + " SET "
                        + "is_shopping_cart = 'N'"
                        + " WHERE "
                        + "request_id = (SELECT request_id FROM file_attachment WHERE file_id = ?)";
                LOG.info(String.format("sql: %s, file_id:%d", sql, fileId));
                try ( PreparedStatement pstmt3 = connection.prepareStatement(sql);) {
                    pstmt3.setInt(1, fileId);
                    pstmt3.executeUpdate();
                }

                sql = "DELETE FROM file_attachment WHERE file_id = ?";
                LOG.info(String.format("sql: %s, file_id:%d", sql, fileId));
                try ( PreparedStatement pstmt4 = connection.prepareStatement(sql);) {
                    pstmt4.setInt(1, fileId);
                    pstmt4.executeUpdate();
                }
            }

            //Convenience check fee
            if (requestId > 0) {
                sql = "{ call sp_convenience_check_fee (?) }";
                LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
                try ( CallableStatement cstmt = connection.prepareCall(sql);) {
                    cstmt.setInt("p_request_id", requestId);
                    cstmt.execute();
                }
            }

            if (statusCode == StatusCode.OK) {
                connection.commit();
            } else {
                connection.rollback();
            }
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    public Map<String, Object> callPartialDelivery(Integer itemId, Integer quantityDelivered, Integer peopleId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (itemId == null || quantityDelivered == null || peopleId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "{ call sp_partial_delivery(?, ?, ?) }";
        LOG.info(String.format("sql: %s, itemId: %d", sql, itemId));
        try ( Connection connection = getConnection(true);  CallableStatement cstmt = connection.prepareCall(sql);) {

            // Item id.
            cstmt.setInt("p_item_id", itemId);

            // Quantity delivered.
            cstmt.setInt("p_delivered_quantity", quantityDelivered);

            // User id.
            cstmt.setInt("p_user_id", peopleId);

            cstmt.execute();
        } catch (SQLException caught) {
            Integer errorCode = caught.getErrorCode();
            results.put(ERROR_CODE_KEY, errorCode);
            String errorMessage = null;
            switch (errorCode) {
                case 20201:
                    errorMessage = "partial delivery cannot be applied on shipping cost";
                    break;

                case 20202:
                    errorMessage = "partial delivery cannot be applied on Shopping Cart Item";
                    break;

                case 20203:
                    errorMessage = "delivered quantity need to be less than the quantity of the original item";
                    break;

                default:
                    errorMessage = "There was an error during partial delivery";
            }
            results.put(ERROR_MESSAGE_KEY, errorMessage);
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    private Map<String, Object> selectItemsForRequestWithConnection(Connection connection, Integer requestId) {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (requestId == null) {
            statusCode = StatusCode.IncompleteData;
            results.put(STATUS_CODE_KEY, statusCode);
            return results;
        }
        String sql = "SELECT "
                + "i.item_id, "
                + "i.request_id, "
                //deprecated + "i.item_type, " +
                + "i.vendor_id, "
                + "i.catelog_number, "
                + "i.item_name, "
                + "i.item_description, "
                + "i.price, "
                + "i.quantity, "
                + "i.price_ordered, "
                + "i.quantity_ordered, "
                + "i.purpose, "
                + "i.chemical, "
                + "i.is_taggable_equipment, "
                + "i.project_task, "
                + "i.shopping_cart_file_id, "
                + "i.item_status_id, "
                + "i.object_class, "
                + "i.is_shipping, "
                + "i.item_notes, "
                + "i.unit_issue, "
                + "i.date_received, "
                + "i.transaction_number, "
                + "i.statement_date, "
                + "ist.item_status_type_id, "
                + "(SELECT item_status_type_name FROM lkup_item_status_type WHERE item_status_type_id = ist.item_status_type_id) AS item_status_type_name"
                + " FROM "
                + "item i"
                + " LEFT JOIN "
                + "item_status ist"
                + " ON "
                + "i.item_id = ist.item_id"
                + " AND "
                + "ist.created_date = (SELECT MAX(created_date) FROM item_status WHERE item_id = i.item_id)"
                + " WHERE "
                + "i.request_id = ?";

        LOG.info(String.format("sql: %s, request_id: %d", sql, requestId));
        try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
            List<Item> items = new ArrayList<>();
            pstmt.setInt(1, requestId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                while (rset.next()) {
                    Item item = new Item();

                    // Item id.
                    int itemId = rset.getInt("item_id");
                    if (!rset.wasNull()) {
                        item.setId(itemId);
                    }

                    // Request id.
                    requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        item.setRequestId(requestId);
                    }

                    // Type.
                    //item.setType(rset.getString("item_type"));
                    // Vendor id.
                    int vendorId = rset.getInt("vendor_id");
                    if (!rset.wasNull()) {
                        item.setVendorId(vendorId);
                    }

                    // Catalog number.
                    item.setCatalogNumber(rset.getString("catelog_number"));

                    // Name.
                    item.setItemName(rset.getString("item_name"));

                    // Description.
                    item.setDescription(rset.getString("item_description"));

                    // Price.
                    double price = rset.getDouble("price");
                    if (!rset.wasNull()) {
                        item.setPrice(price);
                    }

                    // Quantity.
                    int quantity = rset.getInt("quantity");
                    if (!rset.wasNull()) {
                        item.setQuantity(quantity);
                    }

                    // Actual price.
                    double actualPrice = rset.getDouble("price_ordered");
                    if (!rset.wasNull()) {
                        item.setActualPrice(actualPrice);
                    }

                    // Actual quantity.
                    int actualQuantity = rset.getInt("quantity_ordered");
                    if (!rset.wasNull()) {
                        item.setActualQuantity(actualQuantity);
                    }

                    // Purpose.
                    item.setPurpose(rset.getString("purpose"));

                    // Chemical.
                    String chemical = rset.getString("chemical");
                    if (!rset.wasNull()) {
                        item.setChemical("Y".equals(chemical));
                    } else {
                        item.setChemical(Boolean.FALSE);
                    }

                    item.setIsTaggableEquipment(StringToBool(rset.getString("is_taggable_equipment")));

                    // Project task.
                    item.setProjectTask(rset.getString("project_task"));

                    // Shopping cart file id.
                    int shoppingCartFileId = rset.getInt("shopping_cart_file_id");
                    if (!rset.wasNull()) {
                        item.setShoppingCartFileId(shoppingCartFileId);
                    }

                    // Status id.
                    int statusId = rset.getInt("item_status_id");
                    if (!rset.wasNull()) {
                        item.setStatusId(statusId);
                    }

                    // Object class.
                    item.setObjectClass(rset.getString("object_class"));

                    // Is shipping.
                    String isShipping = rset.getString("is_shipping");
                    if (!rset.wasNull()) {
                        item.setIsShipping("Y".equals(isShipping));
                    }

                    // Item notes.
                    item.setItemNotes(rset.getString("item_notes"));

                    // Unit Issue.
                    item.setUnitIssue(rset.getString("unit_issue"));

                    // Date received.
                    Timestamp dateReceived = rset.getTimestamp("date_received");
                    if (!rset.wasNull()) {
                        item.setDateReceived(dateReceived);
                    }

                    // Transaction number.
                    item.setTransactionNumber(rset.getString("transaction_number"));

                    // Statement date.
                    Timestamp statementDate = rset.getTimestamp("statement_date");
                    if (!rset.wasNull()) {
                        item.setStatementDate(statementDate);
                    }

                    // Item status type id.
                    int itemStatusTypeId = rset.getInt("item_status_type_id");
                    if (!rset.wasNull()) {
                        item.setLatestStatusTypeId(itemStatusTypeId);
                    }

                    // Item status type name.
                    item.setLatestStatusTypeName(rset.getString("item_status_type_name"));

                    items.add(item);
                }
            }

            results.put(ITEMS_LIST_KEY, items);
        } catch (Exception caught) {
            statusCode = StatusCode.DatabaseError;
            LOG.log(Level.SEVERE, caught.getMessage(), caught);
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    private Item selectItemByIdWithConnection(Connection connection, int itemId) throws SQLException {
        Item item = null;
        String sql = "SELECT "
                + "i.item_id, "
                + "i.request_id, "
                // + "i.item_type, " +
                + "i.vendor_id, "
                + "i.catelog_number, "
                + "i.item_name, "
                + "i.item_description, "
                + "i.price, "
                + "i.quantity, "
                + "i.price_ordered, "
                + "i.quantity_ordered, "
                + "i.purpose, "
                + "i.chemical, "
                + "i.is_taggable_equipment, "
                + "i.project_task, "
                + "i.shopping_cart_file_id, "
                + "i.item_status_id, "
                + "i.object_class, "
                + "i.is_shipping, "
                + "i.item_notes, "
                + "i.unit_issue, "
                + "i.date_received, "
                + "i.transaction_number, "
                + "i.statement_date, "
                + "ist.item_status_type_id, "
                + "(SELECT item_status_type_name FROM lkup_item_status_type WHERE item_status_type_id = ist.item_status_type_id) AS item_status_type_name"
                + " FROM "
                + "item i"
                + " LEFT JOIN "
                + "item_status ist"
                + " ON "
                + "i.item_id = ist.item_id"
                + " AND "
                + "i.item_type = 'R'"
                + " AND "
                + "ist.created_date = (SELECT MAX(created_date) FROM item_status WHERE item_id = i.item_id)"
                + " WHERE "
                + "i.item_id = ?";
        LOG.info(String.format("sql: %s, item_id: %d", sql, itemId));
        try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, itemId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    item = new Item();

                    // Item id.
                    Integer id = rset.getInt("item_id");
                    if (!rset.wasNull()) {
                        item.setId(id);
                    }

                    // Request id.
                    int requestId = rset.getInt("request_id");
                    if (!rset.wasNull()) {
                        item.setRequestId(requestId);
                    }

                    // Type.
                    //item.setType(rset.getString("item_type"));
                    // Vendor id.
                    int vendorId = rset.getInt("vendor_id");
                    if (!rset.wasNull()) {
                        item.setVendorId(vendorId);
                    }

                    // Catalog number.
                    item.setCatalogNumber(rset.getString("catelog_number"));

                    // Name.
                    item.setItemName(rset.getString("item_name"));

                    // Description.
                    item.setDescription(rset.getString("item_description"));

                    // Price.
                    double price = rset.getDouble("price");
                    if (!rset.wasNull()) {
                        item.setPrice(price);
                    }

                    // Quantity.
                    int quantity = rset.getInt("quantity");
                    if (!rset.wasNull()) {
                        item.setQuantity(quantity);
                    }

                    // Actual price.
                    double actualPrice = rset.getDouble("price_ordered");
                    if (!rset.wasNull()) {
                        item.setActualPrice(actualPrice);
                    }

                    // Actual quantity.
                    int actualQuantity = rset.getInt("quantity_ordered");
                    if (!rset.wasNull()) {
                        item.setActualQuantity(actualQuantity);
                    }

                    // Purpose.
                    item.setPurpose(rset.getString("purpose"));

                    // Chemical.
                    String chemical = rset.getString("chemical");
                    if (!rset.wasNull()) {
                        item.setChemical("Y".equals(chemical));
                    } else {
                        item.setChemical(Boolean.FALSE);
                    }

                    item.setIsTaggableEquipment(StringToBool(rset.getString("is_taggable_equipment")));

                    // Project task.
                    item.setProjectTask(rset.getString("project_task"));

                    // Shopping cart file id.
                    int shoppingCartFileId = rset.getInt("shopping_cart_file_id");
                    if (!rset.wasNull()) {
                        item.setShoppingCartFileId(shoppingCartFileId);
                    }

                    // Status id.
                    int statusId = rset.getInt("item_status_id");
                    if (!rset.wasNull()) {
                        item.setStatusId(statusId);
                    }

                    // Object class.
                    item.setObjectClass(rset.getString("object_class"));

                    // Is shipping.
                    String isShipping = rset.getString("is_shipping");
                    if (!rset.wasNull()) {
                        item.setIsShipping("Y".equals(isShipping));
                    }

                    // Item notes.
                    item.setItemNotes(rset.getString("item_notes"));

                    // Item notes.
                    item.setUnitIssue(rset.getString("unit_issue"));

                    // Date received.
                    Timestamp dateReceived = rset.getTimestamp("date_received");
                    if (!rset.wasNull()) {
                        item.setDateReceived(dateReceived);
                    }

                    // Transaction number.
                    item.setTransactionNumber(rset.getString("transaction_number"));

                    // Statement date.
                    Timestamp statementDate = rset.getTimestamp("statement_date");
                    if (!rset.wasNull()) {
                        item.setStatementDate(statementDate);
                    }

                    // Item status type id.
                    int itemStatusTypeId = rset.getInt("item_status_type_id");
                    if (!rset.wasNull()) {
                        item.setLatestStatusTypeId(itemStatusTypeId);
                    }

                    // Item status type name.
                    item.setLatestStatusTypeName(rset.getString("item_status_type_name"));
                }
            }
        }
        return item;
    }

    private ChemicalItem selectChemicalItem(Connection connection, int itemId) throws SQLException {
        ChemicalItem chemicalItem = null;
        String sql = "SELECT "
                + "item_id, "
                + "owner_id, "
                + "location, "
                + "sub_location, "
                + "cas_number, "
                + "chemical_form, "
                + "chemical_grade, "
                + "manufacturer_name, "
                + "catalog_number, "
                + "catalog_number_quantity, "
                + "containers_per_package, "
                + "amount_per_container, "
                + "lables_needed, "
                + "container_type, "
                + "expiration_date, "
                + "health_nfpa_value, "
                + "flammability_nfpa_value, "
                + "reactivity_nfpa_value, "
                + "special_code_nfpa_value, "
                + "is_radioactive_material, "
                + "biohazard_registration_req, "
                + "special_instruction, "
                + "ibbr_room_id, "
                + "ibbr_room_name, "
                + "primary_user_id, "
                + "cispro_remarks, "
                + "container_total, "
                + "product_url"
                + " FROM "
                + "item_chemical"
                + " WHERE "
                + "item_id = ?";
        LOG.info(String.format("sql: %s, item_id: %d", sql, itemId));
        try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {
            pstmt.setInt(1, itemId);
            try ( ResultSet rset = pstmt.executeQuery();) {
                if (rset.next()) {
                    chemicalItem = new ChemicalItem();

                    // Item id.
                    int id = rset.getInt("item_id");
                    if (!rset.wasNull()) {
                        chemicalItem.setId(id);
                    }

                    // Owner id.
                    int ownerId = rset.getInt("owner_id");
                    if (!rset.wasNull()) {
                        chemicalItem.setOwnerId(ownerId);
                    }

                    // Location.
                    chemicalItem.setLocation(rset.getString("location"));

                    // Sub location.
                    chemicalItem.setSubLocation(rset.getString("sub_location"));

                    // Cas number.
                    chemicalItem.setCasNumber(rset.getString("cas_number"));

                    // Chemical name.
                    chemicalItem.setChemicalForm(rset.getString("chemical_form"));

                    // Chemical grade.
                    chemicalItem.setChemicalGrade(rset.getString("chemical_grade"));

                    // Manufacturer name.
                    chemicalItem.setManufacturerName(rset.getString("manufacturer_name"));

                    // Catalog number.
                    chemicalItem.setChemicalCatalogNumber(rset.getString("catalog_number"));

                    // Catalog number quantity.
                    chemicalItem.setCatalogNumberQuantity(rset.getString("catalog_number_quantity"));

                    // Containers per package.
                    chemicalItem.setContainersPerPackage(rset.getString("containers_per_package"));

                    // Amount per container.
                    chemicalItem.setAmountPerContainer(rset.getString("amount_per_container"));

                    // Labels needed.
                    int labelsNeeded = rset.getInt("lables_needed");
                    if (!rset.wasNull()) {
                        chemicalItem.setLabelsNeeded(labelsNeeded);
                    }

                    // Container type.
                    chemicalItem.setContainerType(rset.getString("container_type"));

                    // Expiration date.
                    chemicalItem.setExpirationDate(rset.getTimestamp("expiration_date"));

                    // Health npfa value.
                    chemicalItem.setHealthNfpaValue(rset.getString("health_nfpa_value"));

                    // Flammability npfa value.
                    chemicalItem.setFlammabilityNpfaValue(rset.getString("flammability_nfpa_value"));

                    // Ractivity npfa value.
                    chemicalItem.setReactivityNpfaValue(rset.getString("reactivity_nfpa_value"));

                    // Special code npfa value.
                    chemicalItem.setSpecialCodeNpfaValue(rset.getString("special_code_nfpa_value"));

                    // Is radioactive material.
                    String isRadioactiveMaterial = rset.getString("is_radioactive_material");
                    if (!rset.wasNull()) {
                        chemicalItem.setIsRadioactiveMaterial("Y".equals(isRadioactiveMaterial));
                    }

                    // Biohazard registration required.
                    String biohazardRegistrationRequired = rset.getString("biohazard_registration_req");
                    if (!rset.wasNull()) {
                        chemicalItem.setBiohazardRegistrationRequired("Y".equals(biohazardRegistrationRequired));
                    }

                    // Special instructions.
                    chemicalItem.setSpecialInstructions(rset.getString("special_instruction"));

                    // IBBR Room Id.
                    int ibbrRoomId = rset.getInt("ibbr_room_id");
                    if (!rset.wasNull()) {
                        chemicalItem.setIbbrRoomId(ibbrRoomId);
                    }

                    // IBBR Room Name.
                    String ibbrRoomName = rset.getString("ibbr_room_name");
                    if (!rset.wasNull()) {
                        chemicalItem.setIbbrRoomName(ibbrRoomName);
                    }

                    // Primary user id.
                    int primaryUserId = rset.getInt("primary_user_id");
                    if (!rset.wasNull()) {
                        chemicalItem.setPrimaryUserId(primaryUserId);
                    }

                    // Cispro remarks.
                    chemicalItem.setCisproRemarks(rset.getString("cispro_remarks"));

                    // container total
                    chemicalItem.setContainerTotal(rset.getInt("container_total"));

                    // production url
                    chemicalItem.setProductUrl(rset.getString("product_url"));
                }
            }
        }
        return chemicalItem;
    }

    private void insertItem(Item item, PreparedStatement pstmt) throws SQLException {

        // Request id.
        PreparedStatementUtil.setIntegerValue(pstmt, 1, item.getRequestId());

        // Vendor id.
        PreparedStatementUtil.setIntegerValue(pstmt, 2, item.getVendorId());

        // Catalog number.
        PreparedStatementUtil.setStringValue(pstmt, 3, item.getCatalogNumber());

        // Name.
        PreparedStatementUtil.setStringValue(pstmt, 4, item.getItemName());

        // Description.
        PreparedStatementUtil.setStringValue(pstmt, 5, item.getDescription());

        // Price.
        PreparedStatementUtil.setDoubleValue(pstmt, 6, item.getPrice());

        // Quantity.
        PreparedStatementUtil.setIntegerValue(pstmt, 7, item.getQuantity());

        // Actual Price.
        PreparedStatementUtil.setDoubleValue(pstmt, 8, item.getActualPrice());

        // Actual Quantity.
        PreparedStatementUtil.setIntegerValue(pstmt, 9, item.getActualQuantity());

        // Purpose.
        PreparedStatementUtil.setStringValue(pstmt, 10, item.getPurpose());

        // Chemical.
        setString(pstmt, 11, boolToString(item.getChemical()));

        // Project task.
        PreparedStatementUtil.setStringValue(pstmt, 12, item.getProjectTask());

        // Object class.
        PreparedStatementUtil.setStringValue(pstmt, 13, item.getObjectClass());

        // Shopping cart file id.
        PreparedStatementUtil.setIntegerValue(pstmt, 14, item.getShoppingCartFileId());

        // Status id.
        PreparedStatementUtil.setIntegerValue(pstmt, 15, item.getStatusId());

        // Is shipping.
        setString(pstmt, 16, boolToString(item.getIsShipping()));

        // Item notes.
        PreparedStatementUtil.setStringValue(pstmt, 17, item.getItemNotes());

        // Unit issue.
        PreparedStatementUtil.setStringValue(pstmt, 18, item.getUnitIssue());

        // Date received.
        PreparedStatementUtil.setTimestampValue(pstmt, 19, item.getDateReceived());

        // is taggable equipment. 
        setString(pstmt, 20, boolToString(item.getIsTaggableEquipment()));

    }

    private void insertChemicalItem(ChemicalItem chemicalItem, PreparedStatement pstmt) throws SQLException {

        // Item id.
        PreparedStatementUtil.setIntegerValue(pstmt, 1, chemicalItem.getId());
        //pstmt.setInt(1, chemicalItem.getId());

        // Owner id.
        PreparedStatementUtil.setIntegerValue(pstmt, 2, chemicalItem.getOwnerId());

        // Location.
        PreparedStatementUtil.setStringValue(pstmt, 3, chemicalItem.getLocation());

        // Sub location.
        PreparedStatementUtil.setStringValue(pstmt, 4, chemicalItem.getSubLocation());

        // Cas number.
        PreparedStatementUtil.setStringValue(pstmt, 5, chemicalItem.getCasNumber());

        // Chemical name.
        PreparedStatementUtil.setStringValue(pstmt, 6, chemicalItem.getChemicalForm());

        // Chemical grade.
        PreparedStatementUtil.setStringValue(pstmt, 7, chemicalItem.getChemicalGrade());

        // Manufacturer name.
        PreparedStatementUtil.setStringValue(pstmt, 8, chemicalItem.getManufacturerName());

        // Catalog number.
        PreparedStatementUtil.setStringValue(pstmt, 9, chemicalItem.getChemicalCatalogNumber());

        // Catalog number quantity.
        PreparedStatementUtil.setStringValue(pstmt, 10, chemicalItem.getCatalogNumberQuantity());

        // Containers per package.
        PreparedStatementUtil.setStringValue(pstmt, 11, chemicalItem.getContainersPerPackage());

        // Amount per container.
        PreparedStatementUtil.setStringValue(pstmt, 12, chemicalItem.getAmountPerContainer());

        // Labels needed.
        PreparedStatementUtil.setIntegerValue(pstmt, 13, chemicalItem.getLabelsNeeded());

        // Container type.
        PreparedStatementUtil.setStringValue(pstmt, 14, chemicalItem.getContainerType());

        // Expiration date.
        PreparedStatementUtil.setTimestampValue(pstmt, 15, chemicalItem.getExpirationDate());

        // Health NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 16, chemicalItem.getHealthNfpaValue());

        // Flammability NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 17, chemicalItem.getFlammabilityNpfaValue());

        // Reactivity NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 18, chemicalItem.getReactivityNpfaValue());

        // Special code NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 19, chemicalItem.getSpecialCodeNpfaValue());

        // Is radioactive material.
        pstmt.setString(20, boolToString(chemicalItem.getIsRadioactiveMaterial()));

        // Biohazard registration required.
        pstmt.setString(21, boolToString(chemicalItem.getBiohazardRegistrationRequired()));

        // Special instructions.
        PreparedStatementUtil.setStringValue(pstmt, 22, chemicalItem.getSpecialInstructions());

        // IBBR Room ID.
        PreparedStatementUtil.setIntegerValue(pstmt, 23, chemicalItem.getIbbrRoomId());

        // IBBR Room Name.
        PreparedStatementUtil.setStringValue(pstmt, 24, chemicalItem.getIbbrRoomName());

        // Primary user id.
        PreparedStatementUtil.setIntegerValue(pstmt, 25, chemicalItem.getPrimaryUserId());

        // Cispro remarks.
        PreparedStatementUtil.setStringValue(pstmt, 26, chemicalItem.getCisproRemarks());

        // Container Total.
        PreparedStatementUtil.setIntegerValue(pstmt, 27, chemicalItem.getContainerTotal());

        // Production URL.
        PreparedStatementUtil.setStringValue(pstmt, 28, chemicalItem.getProductUrl());
    }

    private Map<String, Object> insertItemStatusWithConnection(Connection connection, ItemStatus itemStatus) throws SQLException {
        Map<String, Object> results = new HashMap<>();
        StatusCode statusCode = StatusCode.OK;
        if (itemStatus == null || itemStatus.getCreatedBy() == null || itemStatus.getTypeId() == null || itemStatus.getItemId() == null) {
            results.put(STATUS_CODE_KEY, StatusCode.IncompleteData);
            return results;
        }
        String sql = "INSERT INTO"
                + " item_status "
                + "("
                + "item_id, "
                + "item_status_type_id, "
                + "item_status_notes, "
                + "created_by, "
                + "created_date"
                + ")"
                + " VALUES "
                + "(?, ?, ?, ?, SYSDATE)";
        LOG.info(sql);
        try ( PreparedStatement pstmt = connection.prepareStatement(sql);) {

            // Item id.
            PreparedStatementUtil.setIntegerValue(pstmt, 1, itemStatus.getItemId());

            // Item status type id.
            PreparedStatementUtil.setIntegerValue(pstmt, 2, itemStatus.getTypeId());

            // Item status notes.
            PreparedStatementUtil.setStringValue(pstmt, 3, itemStatus.getNotes());

            // Created by.
            PreparedStatementUtil.setIntegerValue(pstmt, 4, itemStatus.getCreatedBy());

            int rowCount = pstmt.executeUpdate();
            if (rowCount == 0) {
                statusCode = StatusCode.InsertFailed;
            }
        }

        results.put(STATUS_CODE_KEY, statusCode);
        return results;
    }

    private void updateItem(Item item, PreparedStatement pstmt) throws SQLException {

        // Vendor id.
        PreparedStatementUtil.setIntegerValue(pstmt, 1, item.getVendorId());

        // Catalog number.
        PreparedStatementUtil.setStringValue(pstmt, 2, item.getCatalogNumber());

        // Name.
        PreparedStatementUtil.setStringValue(pstmt, 3, item.getItemName());

        // Description.
        PreparedStatementUtil.setStringValue(pstmt, 4, item.getDescription());

        // Price.
        PreparedStatementUtil.setDoubleValue(pstmt, 5, item.getPrice());

        // Quantity.
        PreparedStatementUtil.setIntegerValue(pstmt, 6, item.getQuantity());

        // Purpose.
        PreparedStatementUtil.setStringValue(pstmt, 7, item.getPurpose());

        // Chemical.
        pstmt.setString(8, boolToString(item.getChemical()));

        // Project task.
        PreparedStatementUtil.setStringValue(pstmt, 9, item.getProjectTask());

        // Shopping cart file id.
        PreparedStatementUtil.setIntegerValue(pstmt, 10, item.getShoppingCartFileId());

        // Status id.
        PreparedStatementUtil.setIntegerValue(pstmt, 11, item.getStatusId());

        // Object class.
        PreparedStatementUtil.setStringValue(pstmt, 12, item.getObjectClass());

        // Is shipping.
        pstmt.setString(13, boolToString(item.getIsShipping()));

        // Item notes.
        PreparedStatementUtil.setStringValue(pstmt, 14, item.getItemNotes());

        // Unit issue.
        PreparedStatementUtil.setStringValue(pstmt, 15, item.getUnitIssue());

        // Date received.
        PreparedStatementUtil.setTimestampValue(pstmt, 16, item.getDateReceived());

        // Price ordered.
        PreparedStatementUtil.setDoubleValue(pstmt, 17, item.getPrice());

        // Quantity ordered.
        PreparedStatementUtil.setIntegerValue(pstmt, 18, item.getQuantity());

        // Is taggable equipment.
        pstmt.setString(19, boolToString(item.getIsTaggableEquipment()));

        // Item id.
        PreparedStatementUtil.setIntegerValue(pstmt, 20, item.getId());
        //pstmt.setInt(20, item.getId());
    }

    private void updateChemicalItem(ChemicalItem chemicalItem, PreparedStatement pstmt) throws SQLException {
        // Owner id.
        PreparedStatementUtil.setIntegerValue(pstmt, 1, chemicalItem.getOwnerId());

        // Location.
        PreparedStatementUtil.setStringValue(pstmt, 2, chemicalItem.getLocation());

        // Sub location.
        PreparedStatementUtil.setStringValue(pstmt, 3, chemicalItem.getSubLocation());

        // Cas number.
        PreparedStatementUtil.setStringValue(pstmt, 4, chemicalItem.getCasNumber());

        // Chemical name.
        PreparedStatementUtil.setStringValue(pstmt, 5, chemicalItem.getChemicalForm());

        // Chemical grade.
        PreparedStatementUtil.setStringValue(pstmt, 6, chemicalItem.getChemicalGrade());

        // Manufacturer name.
        PreparedStatementUtil.setStringValue(pstmt, 7, chemicalItem.getManufacturerName());

        // Catalog number.
        PreparedStatementUtil.setStringValue(pstmt, 8, chemicalItem.getChemicalCatalogNumber());

        // Catalog number quantity.
        PreparedStatementUtil.setStringValue(pstmt, 9, chemicalItem.getCatalogNumberQuantity());

        // Containers per package.
        PreparedStatementUtil.setStringValue(pstmt, 10, chemicalItem.getContainersPerPackage());

        // Amount per container.
        PreparedStatementUtil.setStringValue(pstmt, 11, chemicalItem.getAmountPerContainer());

        // Labels needed.
        PreparedStatementUtil.setIntegerValue(pstmt, 12, chemicalItem.getLabelsNeeded());

        // Container type.
        PreparedStatementUtil.setStringValue(pstmt, 13, chemicalItem.getContainerType());

        // Expiration date.
        PreparedStatementUtil.setTimestampValue(pstmt, 14, chemicalItem.getExpirationDate());

        // Health NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 15, chemicalItem.getHealthNfpaValue());

        // Flammability NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 16, chemicalItem.getFlammabilityNpfaValue());

        // Reactivity NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 17, chemicalItem.getReactivityNpfaValue());

        // Special code NPFA value.
        PreparedStatementUtil.setStringValue(pstmt, 18, chemicalItem.getSpecialCodeNpfaValue());

        // Is radioactive material.
        pstmt.setString(19, boolToString(chemicalItem.getIsRadioactiveMaterial()));

        // Biohazard registration required.
        pstmt.setString(20, boolToString(chemicalItem.getBiohazardRegistrationRequired()));

        // Special instructions.
        PreparedStatementUtil.setStringValue(pstmt, 21, chemicalItem.getSpecialInstructions());

        // IBBR Room ID.
        PreparedStatementUtil.setIntegerValue(pstmt, 22, chemicalItem.getIbbrRoomId());

        // IBBR Room Name.
        PreparedStatementUtil.setStringValue(pstmt, 23, chemicalItem.getIbbrRoomName());

        // Primary user ID.
        PreparedStatementUtil.setIntegerValue(pstmt, 24, chemicalItem.getPrimaryUserId());

        // Cispro remarks.
        PreparedStatementUtil.setStringValue(pstmt, 25, chemicalItem.getCisproRemarks());

        // Container Total.
        PreparedStatementUtil.setIntegerValue(pstmt, 26, chemicalItem.getContainerTotal());
        //pstmt.setInt(26, chemicalItem.getContainerTotal());

        // Production URL.
        PreparedStatementUtil.setStringValue(pstmt, 27, chemicalItem.getProductUrl());

        // Item ID.
        PreparedStatementUtil.setIntegerValue(pstmt, 28, chemicalItem.getId());
        //pstmt.setInt(28, chemicalItem.getId());
    }

}
