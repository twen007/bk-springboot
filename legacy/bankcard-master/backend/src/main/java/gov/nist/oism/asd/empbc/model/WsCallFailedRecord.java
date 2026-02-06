package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import gov.nist.oism.asd.empbc.util.WsCategory;
import java.io.Serializable;
import java.util.Date;

public class WsCallFailedRecord implements Serializable {
    private Integer id;
    private Integer referenceId;
    private Integer wsCategory;
    private String wsMethod;
    private Integer statusCode; // 100-5xx status code from the standard web service call response, or 600 for exception
    private String errorMessage;
    private Date dateCreated;
    private Date lastSubmitted;
    private IbbrChemicalItem ibbrRecord;
    
    public WsCallFailedRecord() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getReferenceId() {
        return referenceId;
    }

    public Integer getWsCategory() {
        return wsCategory;
    }

    public void setWsCategory(Integer wsCategory) {
        this.wsCategory = wsCategory;
    }
    

    public String getWsMethod() {
        return wsMethod;
    }

    public void setWsMethod(String wsMethod) {
        this.wsMethod = wsMethod;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public Date getLastSubmitted() {
        return lastSubmitted;
    }

    public void setLastSubmitted(Date lastSubmitted) {
        this.lastSubmitted = lastSubmitted;
    }

    public IbbrChemicalItem getIbbrRecord() {
        return ibbrRecord;
    }

    public void setIbbrRecordFromJson(Integer refId, String recordDetails) {
        this.referenceId = refId;
        this.wsCategory = WsCategory.IBBR.getValue();
        this.wsMethod="POST";
        Gson gson = new Gson();
        IbbrChemicalItem ibbrRecord = gson.fromJson(recordDetails, IbbrChemicalItem.class);

        this.ibbrRecord = ibbrRecord;
    }

    // setIbbrRecord is called when the record is initially created and 
    // setRecordDetail is called to construct IbbrChemicalItem during resync
    public void setIbbrRecord(Integer refId, IbbrChemicalItem ibbrItem) {
        this.referenceId = refId;
         this.wsCategory = WsCategory.IBBR.getValue();
         this.wsMethod="POST";
        this.ibbrRecord = ibbrItem;
    }
    
    public String toString()
    {
        return "id:" + id + ", statusCode:" + statusCode + ", errorMessage: " + errorMessage + ", ibbrRecord:{" + ibbrRecord.toString() + "}";
    }
}
