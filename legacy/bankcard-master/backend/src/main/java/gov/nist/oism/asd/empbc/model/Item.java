package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Item implements Serializable {
    
    private static final int VARCHAR = 0;
    private static final int INTEGER = 1;
    private static final int DOUBLE = 2;
    
    private enum CsvFilePosition {
        RequestId(0, INTEGER),
        //deprecated
        //Type(1, VARCHAR),
        VendorId(2, INTEGER),
        CatalogNumber(3, VARCHAR),
        Name(4, VARCHAR),
        Description(5, VARCHAR),
        Price(6, DOUBLE),
        Quantity(7, INTEGER),
        Purpose(8, VARCHAR),
        IsChemical(9, VARCHAR),
        ProjectTask(10, VARCHAR),
        ObjectClass(11, VARCHAR);
        
        private int mPosition;
        private int mDataType;
        
        CsvFilePosition(int position, int dataType) {
            mPosition = position;
            mDataType = dataType;
        }
        
        private int getPosition() {
            return mPosition;
        }
        
        private int getDataType() {
            return mDataType;
        }
    }
    
    private Integer mId;
    private Integer mRequestId;
    //deprecated
    //private String mType;
    private Integer mVendorId;
	private String vendorName;
	private String requisitionNumber;
    private String mCatalogNumber;
    private String mItemName;
    private String mDescription;
    private Double mPrice;
    private Integer mQuantity;
    private Double mActualPrice;
    private Integer mActualQuantity;
    private String mPurpose;
    private Boolean mChemical;
    private Boolean isTaggableEquipment;
    private String mProjectTask;
    private Integer mShoppingCartFileId;
    private Integer mStatusId;
    private String mObjectClass;
    private Integer mLatestStatusTypeId;
    private String mLatestStatusTypeName;
    private Boolean mIsShipping;
    private String mItemNotes;
    private String mUnitIssue;
    private Date mDateReceived;
    private String mTransactionNumber;
    private Date mStatementDate;

    public Boolean getIsTaggableEquipment() {
        return isTaggableEquipment;
    }

    public void setIsTaggableEquipment(Boolean isTaggableEquipment) {
        this.isTaggableEquipment = isTaggableEquipment;
    }

    
    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public Integer getRequestId() {
        return mRequestId;
    }

    public void setRequestId(Integer requestId) {
        mRequestId = requestId;
    }
    
    public String getUnitIssue() {
        return mUnitIssue;
    }

    public void setUnitIssue(String unitIssue) {
        mUnitIssue = unitIssue;
    }

    /*public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }*/

    public Integer getVendorId() {
        return mVendorId;
    }

    public void setVendorId(Integer vendorId) {
        mVendorId = vendorId;
    }
	
	public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String s) {
        vendorName = s;
    }
	
	public String getRequisitionNumber() {
        return requisitionNumber;
    }

    public void setRequisitionNumber(String s) {
        requisitionNumber = s;
    }

    public String getCatalogNumber() {
        return mCatalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        mCatalogNumber = catalogNumber;
    }

    public String getItemName() {
        return mItemName;
    }

    public void setItemName(String itemName) {
        mItemName = itemName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public Double getPrice() {
        return mPrice;
    }

    public void setPrice(Double price) {
        mPrice = price;
    }

    public Integer getQuantity() {
        return mQuantity;
    }

    public void setQuantity(Integer quantity) {
        this.mQuantity = quantity;
    }

    public Double getActualPrice() {
        return mActualPrice;
    }

    public void setActualPrice(Double actualPrice) {
        mActualPrice = actualPrice;
    }

    public Integer getActualQuantity() {
        return mActualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        mActualQuantity = actualQuantity;
    }
    
    public String getPurpose() {
        return mPurpose;
    }

    public void setPurpose(String purpose) {
        mPurpose = purpose;
    }

    public Boolean getChemical() {
        return mChemical;
    }

    public void setChemical(Boolean chemical) {
        mChemical = chemical;
    }

    public String getProjectTask() {
        return mProjectTask;
    }

    public void setProjectTask(String projectTask) {
        mProjectTask = projectTask;
    }

    public Integer getShoppingCartFileId() {
        return mShoppingCartFileId;
    }

    public void setShoppingCartFileId(Integer shoppingCartFileId) {
        mShoppingCartFileId = shoppingCartFileId;
    }

    public Integer getStatusId() {
        return mStatusId;
    }

    public void setStatusId(Integer statusId) {
        mStatusId = statusId;
    }

    public String getObjectClass() {
        return mObjectClass;
    }

    public void setObjectClass(String objectClass) {
        mObjectClass = objectClass;
    }

    public Integer getLatestStatusTypeId() {
        return mLatestStatusTypeId;
    }

    public void setLatestStatusTypeId(Integer latestStatusTypeId) {
        mLatestStatusTypeId = latestStatusTypeId;
    }

    public String getLatestStatusTypeName() {
        return mLatestStatusTypeName;
    }

    public void setLatestStatusTypeName(String latestStatusTypeName) {
        mLatestStatusTypeName = latestStatusTypeName;
    }

    public Boolean getIsShipping() {
        return mIsShipping;
    }

    public void setIsShipping(Boolean isShipping) {
        mIsShipping = isShipping;
    }

    public String getItemNotes() {
        return mItemNotes;
    }

    public void setItemNotes(String itemNotes) {
        mItemNotes = itemNotes;
    }
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getDateReceived() {
        return mDateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        mDateReceived = dateReceived;
    }

    public String getTransactionNumber() {
        return mTransactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        mTransactionNumber = transactionNumber;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getStatementDate() {
        return mStatementDate;
    }

    public void setStatementDate(Date statementDate) {
        mStatementDate = statementDate;
    }
    
    public static List<Item> parseCsvFile(String fileContents) {
        List<Item> items = new ArrayList<>();
        String[] lines;
        if (fileContents != null && !fileContents.isEmpty()) {
            lines = fileContents.split("\n");
            for (String line : lines) {
                items.add(parseItem(line));
            }
        }
        return items;
    }
    
    private static Item parseItem(String line) {
        if (line == null || line.isEmpty()) {
            return new Item();
        }
        System.out.println(line);

        List<String> columnValues = new ArrayList<>();
        StringBuilder columnValue = new StringBuilder();
        boolean evenNumberedQuote = false;
        boolean appendAnyCharacterMode = false; // In the middle of a quoted string.
        for (char ch : line.toCharArray()) {
            if (evenNumberedQuote) {
                appendAnyCharacterMode = true;
                if (ch != '"') { // '"' gets added in the else/switch statement. "" inside a quoted column is a single ".
                    columnValue.append(ch);
                }
            }
            else {
                switch (ch) {
                    case '"' :
                        if (appendAnyCharacterMode) {
                            columnValue.append('"');
                        }
                        break;
                        
                    case ',' :
                        columnValues.add(columnValue.toString());
                        System.out.println(columnValue.toString());
                        columnValue = new StringBuilder();
                        appendAnyCharacterMode = false;
                        break;
                    
                    case '\r' :
                    case '\n' :
                        break; // Ignore.
                        
                    default:
                        columnValue.append(ch);
                }
            }
            if (ch == '"') {
                evenNumberedQuote = !evenNumberedQuote;
            }
        }
        columnValues.add(columnValue.toString());
        System.out.println(columnValue.toString());

        return columnValuesToItem(columnValues);
    }
    
    private static Item columnValuesToItem(List<String> columnValues) {
        Item item = new Item();
        for (int i = 0; i < columnValues.size(); i++) {
            String columnValue = columnValues.get(i);
            if (i == CsvFilePosition.RequestId.getPosition()) {
                if (columnValue != null && columnValue.length() > 0) {
                    item.setRequestId(Integer.parseInt(columnValue));
                }
            }
            /*else if (i == CsvFilePosition.Type.getPosition()) {
                item.setType(columnValue);
            }*/
            else if (i == CsvFilePosition.VendorId.getPosition()) {
                if (columnValue != null && columnValue.length() > 0) {
                    item.setVendorId(Integer.parseInt(columnValue));
                }
            }
            else if (i == CsvFilePosition.CatalogNumber.getPosition()) {
                item.setCatalogNumber(columnValue);
            }
            else if (i == CsvFilePosition.Name.getPosition()) {
                item.setItemName(columnValue);
            }
            else if (i == CsvFilePosition.Description.getPosition()) {
                item.setDescription(columnValue);
            }
            else if (i == CsvFilePosition.Price.getPosition()) {
                if (columnValue != null && columnValue.length() > 0) {
                    item.setPrice(Double.parseDouble(columnValue));
                }
            }
            else if (i == CsvFilePosition.Quantity.getPosition()) {
                if (columnValue != null && columnValue.length() > 0) {
                    item.setQuantity(Integer.parseInt(columnValue));
                }
            }
            else if (i == CsvFilePosition.Purpose.getPosition()) {
                item.setPurpose(columnValue);
            }
            else if (i == CsvFilePosition.IsChemical.getPosition()) {
                item.setChemical("Y".equals(columnValue));
            }
            else if (i == CsvFilePosition.ProjectTask.getPosition()) {
                item.setProjectTask(columnValue);
            }
            else if (i == CsvFilePosition.ObjectClass.getPosition()) {
                item.setObjectClass(columnValue);
            }
        }
        return item;
    }
}
