package gov.nist.oism.asd.empbc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-args constructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L; // Unique identifier for serialization

    private Integer peopleId;
    private String firstName;
    private String lastName;
    private String middleName;
    private Integer ouId;
    private String ouCode;
    private Integer divisionId;
    private String divisionCode;
    private Integer groupId;
    private String groupCode;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean supervisor;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date lastUpdateDate;
    private String staffType;
    private Integer bossId;
    private Boolean active;
    private String trueUsername;
    private Integer truePeopleId;
    private Boolean isDelegating;
    private Boolean detaileeMode;
    private Boolean accessAdmin;

    public String toFullName() {
        StringBuilder builder = new StringBuilder("");
        if (lastName != null && !lastName.isEmpty() && firstName != null && !firstName.isEmpty()) {
            builder.append(String.format("%s, %s", lastName, firstName));
        }
        //some records may used the alt_first_name column, which could contain Middle initial already
        //if (middleName != null && !middleName.isEmpty() && builder.length() > 0) {
        //    builder.append(String.format(" %s", middleName));
        //}
        return builder.toString();
    }
}
