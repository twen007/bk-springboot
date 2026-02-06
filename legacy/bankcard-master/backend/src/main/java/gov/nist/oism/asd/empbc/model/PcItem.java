package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PcItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer requestId;
    private Integer fy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date createdDate;
    private String requisitionNumber;
    private Integer ouId;
    private Integer divId;
    private Integer grpId;
    private String ou;
    private String division;
    private String group;
    private String vendor;
    private Integer itemId;
    private String catelogNumber;
    private String itemName;
    private String itemDescription;
    private Double price;
    private Integer quantity;
    private String purpose;
    private String isChemical;
    private Integer shoppingCartFileId;
    private String itemStatus;
    private Integer itemStatusId;
    private String projectTask;
    private String objectClass;
    private String isTaggableEquipment;
    private Double priceOrdered;
    private Integer quantityOrdered;
    private String itemNotes;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date dateReceived;
    private String transactionNumber;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date statementDate;
    private String unitIssue;
    private Integer purchaseTypeId;
}

