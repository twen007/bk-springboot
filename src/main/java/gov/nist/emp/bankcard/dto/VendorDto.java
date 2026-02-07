package gov.nist.emp.bankcard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for Vendor response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDto {
    private Integer id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String phone;
    private String fax;
    private String website;
    private Boolean isShared;
}
