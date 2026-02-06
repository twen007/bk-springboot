package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class ItemStatus implements Serializable {
    
    private Integer mId;
    private Integer mItemId;
    private Integer mTypeId;
    private String mNotes;
    private Integer mCreatedBy;
    private Date mCreatedDate;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public Integer getItemId() {
        return mItemId;
    }

    public void setItemId(Integer itemId) {
        mItemId = itemId;
    }

    public Integer getTypeId() {
        return mTypeId;
    }

    public void setTypeId(Integer typeId) {
        mTypeId = typeId;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public Integer getCreatedBy() {
        return mCreatedBy;
    }

    public void setCreatedBy(Integer createdBy) {
        mCreatedBy = createdBy;
    }
     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    public Date getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(Date createdDate) {
        mCreatedDate = createdDate;
    }
}
