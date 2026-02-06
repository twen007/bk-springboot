package gov.nist.oism.asd.empbc.model;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class RequestSummaryReport {
    
    private Integer requestId;
    private String requisitionNumber;
    private String requesterName;
    private String createdByName;
    private String reviewerName;
    private String dcName;
    private String fcoName;
    private String baoName;
    private String bhName;
    private Date requestDate;
    private Date reviewerDate;
    private Date dcDate;
    private Date fcoDate;
    private Date baoDate;
    private Date orderDate;
    private Date deliverDate;
    private String notes;
    private String deliverAddress;
    private Date neededByDate;
    private Double approvalAmount;
    private RequestVendor requestVendor;
    private List<Item> items;
    private List<FileAttachment> fileAttachments;
    private Integer fy;

}
