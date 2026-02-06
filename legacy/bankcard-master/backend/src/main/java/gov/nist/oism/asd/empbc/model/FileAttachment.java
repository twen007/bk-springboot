package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class FileAttachment implements Serializable {
    
    private Integer mId;
    private Integer mRequestId;
    private Integer mCategoryId;
    private String mName;
    private String mTypeCode;
    private Integer mSize;
    private byte[] mContent;
    private Integer mCreatedBy;
    private String mCreatedByName;
    private Date mCreatedDate;
    private String mCategoryName; // Joined.

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

    public Integer getCategoryId() {
        return mCategoryId;
    }

    public void setCategoryId(Integer categoryId) {
        mCategoryId = categoryId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getTypeCode() {
        return mTypeCode;
    }

    public void setTypeCode(String typeCode) {
        mTypeCode = typeCode;
    }

    public Integer getSize() {
        return mSize;
    }

    public void setSize(Integer size) {
        mSize = size;
    }

    public byte[] getContent() {
        return mContent;
    }

    public void setContent(byte[] content) {
        mContent = content;
    }

    public Integer getCreatedBy() {
        return mCreatedBy;
    }

    public void setCreatedBy(Integer createdBy) {
        mCreatedBy = createdBy;
    }
    
    public String getCreatedByName() {
        return mCreatedByName;
    }

    public void setCreatedByName(String createdByName) {
        mCreatedByName = createdByName;
    }
     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Date createdDate) {
        mCreatedDate = createdDate;
    }
    
    public String getCategoryName() {
        return mCategoryName;
    }

    public void setCategoryName(String categoryName) {
        mCategoryName = categoryName;
    }
}
