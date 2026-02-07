package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * User entity representing NIST employees and associates.
 * Maps to the existing user table in Oracle database.
 */
@Entity
@Table(name = "PEOPLE")
@Data
@NoArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PEOPLE_ID")
    private Integer peopleId;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "OU_ID")
    private Integer ouId;

    @Column(name = "OU_CODE")
    private String ouCode;

    @Column(name = "DIVISION_ID")
    private Integer divisionId;

    @Column(name = "DIVISION_CODE")
    private String divisionCode;

    @Column(name = "GROUP_ID")
    private Integer groupId;

    @Column(name = "GROUP_CODE")
    private String groupCode;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "SUPERVISOR")
    private Boolean supervisor;

    @Column(name = "LAST_UPDATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdateDate;

    @Column(name = "STAFF_TYPE")
    private String staffType;

    @Column(name = "BOSS_ID")
    private Integer bossId;

    @Column(name = "ACTIVE")
    private Boolean active;

    /**
     * Returns the full name in "LastName, FirstName" format.
     */
    public String toFullName() {
        if (lastName != null && !lastName.isEmpty() && firstName != null && !firstName.isEmpty()) {
            return String.format("%s, %s", lastName, firstName);
        }
        return "";
    }
}
