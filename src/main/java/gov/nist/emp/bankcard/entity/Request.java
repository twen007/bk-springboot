package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * Request entity representing bankcard purchase requests.
 * Maps to the REQUEST table in Oracle database.
 */
@Entity
@Table(name = "REQUEST")
@Data
@NoArgsConstructor
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "REQUISITION_NUMBER")
    private String requisitionNumber;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "REQUESTER_ID")
    private Integer requesterId;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "CREATED_FOR")
    private Integer createdFor;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "SHOPPING_CART")
    private String shoppingCart;

    @Column(name = "REFERENCE_ID")
    private Integer referenceId;

    @Column(name = "UPDATED_BY")
    private Integer updatedBy;

    @Column(name = "UPDATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @Column(name = "DELIVERY_ADDRESS")
    private String deliveryAddress;

    @Column(name = "DELIV_TO_HOME")
    private Boolean delivToHome;

    @Column(name = "SHIPPING_COST")
    private Double shippingCost;

    @Column(name = "ROUTE_STATUS_ID")
    private Integer routeStatusId;

    @Column(name = "NEEDED_BY_DATE")
    @Temporal(TemporalType.DATE)
    private Date neededByDate;

    @Column(name = "REVIEWER_ID")
    private Integer reviewerId;

    @Column(name = "DIVISION_CHIEF_ID")
    private Integer divisionChiefId;

    @Column(name = "BANKCARD_APPROVING_OFFICIAL_ID")
    private Integer bankcardApprovingOfficialId;

    @Column(name = "FUNDS_CERTIFYING_OFFICIAL_ID")
    private Integer fundsCertifyingOfficialId;

    @Column(name = "BANKCARD_HOLDER_ID")
    private Integer bankcardHolderId;

    @Column(name = "ESTIMATED_TIME_OF_ARRIVAL")
    @Temporal(TemporalType.DATE)
    private Date estimatedTimeOfArrival;

    @Column(name = "ORDER_NUMBER")
    private String orderNumber;

    @Column(name = "GSA_SESSION_NUMBER")
    private String gsaSessionNumber;

    @Column(name = "PURCHASE_ORDER_NUMBER")
    private String purchaseOrderNumber;

    @Column(name = "SUBMITTED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedDate;

    @Column(name = "BCH_COMMENTS")
    private String bchComments;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "APPROVAL_AMOUNT")
    private Double approvalAmount;

    @Column(name = "IS_DYNAMIC")
    private Integer isDynamic;

    @Column(name = "REROUTE_STACK")
    private Integer rerouteStack;

    @Column(name = "IS_DYNAMIC_REROUTE")
    private Integer isDynamicReroute;

    @Column(name = "FY")
    private Integer fy;

    @Column(name = "IS_IT_PURCHASE")
    private String isItPurchase;

    @Column(name = "ITSO_APPROVED")
    private Integer itsoApproved;

    @Column(name = "OU_ID")
    private Integer ouId;

    @Column(name = "DIVISION_ID")
    private Integer divisionId;

    @Column(name = "GROUP_ID")
    private Integer groupId;

    @Column(name = "PURCHASE_TYPE_ID")
    private Integer purchaseTypeId;

    @Column(name = "MISSION_CRITICAL_CATEGORY_ID")
    private Integer missionCriticalCategoryId;

    @Column(name = "MISSION_CRITICAL_JUSTIFICATION")
    private String missionCriticalJustification;
}
