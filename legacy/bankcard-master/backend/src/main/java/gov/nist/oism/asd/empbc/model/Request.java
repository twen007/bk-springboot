package gov.nist.oism.asd.empbc.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
public class Request implements Serializable {
    
    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    private Integer id;
    private String requisitionNumber;
    private String notes;
    private Integer requesterId;
    private Integer createdBy;
    private Integer createdFor;
    private Date createdDate;
    private String shoppingCart;
    private Integer referenceId;
    private Integer updatedBy;
    private Date updatedDate;
    private String deliveryAddress;
    private Boolean delivToHome;
    private Double shippingCost;
    private Integer routeStatusId; // Joined.
    private Date neededByDate;
    private Integer reviewerId;
    private Integer divisionChiefId;
    private Integer bankcardApprovingOfficialId;
    private Integer fundsCertifyingOfficialId;
    private String fcoName;
    private Integer bankcardHolderId;
    private String reviewerName;
    private String dcName;
    private String baoName;
    private String bhName;
    private Date estimatedTimeOfArrival;
    private String orderNumber;
    private String gsaSessionNumber;
    private String purchaseOrderNumber;
    private Date submittedDate;
    private String bchComments;
    private String description;
    private Double approvalAmount;
    private Integer isDynamic;
    private Integer rerouteStack;
    private Integer isDynamicReroute;
    private Integer fy;
    private String isItPurchase;
    private Integer itsoApproved;
    private Integer ouId;
    private Integer divisionId;
    private Integer groupId;
    private Integer purchaseTypeId;
    private Integer missionCriticalCategoryId;
    private String missionCriticalJustification;

}
