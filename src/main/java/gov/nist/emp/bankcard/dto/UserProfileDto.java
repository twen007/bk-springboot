package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for User profile response with privileges.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Integer peopleId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String username;
    private String phoneNumber;
    private Integer ouId;
    private String ouCode;
    private Integer divisionId;
    private String divisionCode;
    private Integer groupId;
    private String groupCode;
    private String staffType;
    private UserPrivilegesDto privileges;
}
