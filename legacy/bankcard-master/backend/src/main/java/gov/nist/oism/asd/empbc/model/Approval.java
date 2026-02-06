package gov.nist.oism.asd.empbc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;

/**
 * used for generate route history in the audit report to show required approvals.
 * Note: not used by any API call for now
 * @author xinweiw
 */
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with parameters for all fields
public class Approval implements Serializable {

    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    @NotNull
    private String role;

    @NotNull
    private String name;

    @NotNull
    private String action;

    private Date approvedDate;
}
