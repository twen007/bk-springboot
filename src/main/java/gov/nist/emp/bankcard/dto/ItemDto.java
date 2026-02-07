package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for Item response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Integer id;
    private Integer requestId;
    private Integer vendorId;
    private String vendorName;
    private String itemName;
    private String description;
    private String catalogNumber;
    private String unitIssue;
    private Double price;
    private Integer quantity;
    private Double actualPrice;
    private Integer actualQuantity;
    private String purpose;
    private String projectTask;
    private String objectClass;
    private Boolean isTaggableEquipment;
    private Boolean chemical;
    private Boolean biological;
    private Integer itemStatusId;
}
