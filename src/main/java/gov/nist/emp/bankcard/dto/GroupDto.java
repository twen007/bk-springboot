package gov.nist.emp.bankcard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long ouId;
    private Long divisionId;
    private Long groupId;
    private String name;
    private String code;
    private String acronym;
    private String shortName;
}
