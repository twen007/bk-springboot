package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

/**
 * DTO for Route/approval workflow response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDto {
    private Integer id;
    private Integer requestId;
    private Integer approverId;
    private String approverName;
    private String approverType;
    private String status;
    private Date approvalDate;
    private String comments;
    private Integer sequenceNumber;
    private Boolean isCurrent;
}
