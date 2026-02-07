package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * Item entity representing individual items within a purchase request.
 * Maps to the ITEM table in Oracle database.
 */
@Entity
@Table(name = "ITEM")
@Data
@NoArgsConstructor
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @Column(name = "VENDOR_ID")
    private Integer vendorId;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATALOG_NUMBER")
    private String catalogNumber;

    @Column(name = "UNIT_ISSUE")
    private String unitIssue;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "ACTUAL_PRICE")
    private Double actualPrice;

    @Column(name = "ACTUAL_QUANTITY")
    private Integer actualQuantity;

    @Column(name = "PURPOSE")
    private String purpose;

    @Column(name = "PROJECT_TASK")
    private String projectTask;

    @Column(name = "OBJECT_CLASS")
    private String objectClass;

    @Column(name = "IS_TAGGABLE_EQUIPMENT")
    private Boolean isTaggableEquipment;

    @Column(name = "IS_CHEMICAL")
    private Boolean chemical;

    @Column(name = "IS_BIOLOGICAL")
    private Boolean biological;

    @Column(name = "ITEM_STATUS_ID")
    private Integer itemStatusId;

    @Column(name = "TRANSACTION_NUMBER")
    private String transactionNumber;

    @Column(name = "STATEMENT_DATE")
    @Temporal(TemporalType.DATE)
    private Date statementDate;

    @Column(name = "PARTIAL_DELIVERY")
    private Boolean partialDelivery;

    @Column(name = "PARTIAL_DELIVERY_DATE")
    @Temporal(TemporalType.DATE)
    private Date partialDeliveryDate;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "UPDATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    // Transient fields for display purposes
    @Transient
    private String vendorName;

    @Transient
    private String requisitionNumber;
}
