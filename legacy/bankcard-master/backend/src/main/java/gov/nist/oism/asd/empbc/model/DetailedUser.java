/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 *
 * @author xinweiw
 */
@EqualsAndHashCode(callSuper=true)
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with parameters for all fields
public class DetailedUser extends User implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private Integer ouOrgId; // OU_ORG_ID 
    private Integer divOrgId; // DIV_ORG_ID
    private Integer grpOrgId; // GRP_ORG_ID
    private String accessGroup; // ACCESS_GROUP
    private String accessDiv; // ACCESS_DIV
    private String accessOu; // ACCESS_OU
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date validUntilDate; // VALID_UNTIL_DATE
}
