package gov.nist.oism.asd.empbc.model;

import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
public class AuditReport {
    
    private Integer requestId;
    private String requisitionNumber;
    private String requesterName;
    private String createdByName;
    private String reviewerName;
    private String baoName;
    private String bhName;
    private String fcoName;
    private Date requestDate;
    private Date reviewerDate;
    private Date baoDate;
    private Date fcoDate;
    private Date orderDate;
    private Date deliverDate;
    private Double approvalAmount;
    private RequestVendor requestVendor;
    private RequestJustification requestJustification;
    private List<Item> items;
    private List<Route> routes;
    private String isItPurchase;
    private String missionCriticalCategoryName;
    private String missionCriticalJustification;
    
}
