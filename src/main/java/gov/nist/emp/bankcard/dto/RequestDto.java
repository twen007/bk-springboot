package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;
import java.util.List;

/**
 * DTO for Request with related data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {
    private Integer id;
    private String requisitionNumber;
    private String notes;
    private String description;
    private Integer requesterId;
    private String requesterName;
    private Date createdDate;
    private Date neededByDate;
    private Date submittedDate;
    private Integer routeStatusId;
    private String routeStatusName;
    private Double shippingCost;
    private String deliveryAddress;
    private Double totalAmount;
    private Integer fy;

    // Related entities
    private List<ItemDto> items;
    private List<VendorDto> vendors;
    private RouteDto currentRoute;
}
