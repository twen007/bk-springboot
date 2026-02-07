package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * Vendor entity representing suppliers/vendors.
 * Maps to the VENDOR table in Oracle database.
 */
@Entity
@Table(name = "VENDOR")
@Data
@NoArgsConstructor
public class Vendor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "ZIP")
    private String zip;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "WEBSITE")
    private String website;

    @Column(name = "IS_SHARED")
    private Boolean isShared;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "OU_ID")
    private Integer ouId;
}
