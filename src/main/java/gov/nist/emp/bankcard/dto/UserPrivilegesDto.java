package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for user privileges/roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivilegesDto {
    private boolean isAdmin;
    private boolean isBankcardHolder;
    private boolean isBankcardApprovingOfficial;
    private boolean isFundsCertifyingOfficial;
    private boolean isItSecurityOfficer;
    private boolean isReviewer;
    private boolean isRequester;
}
