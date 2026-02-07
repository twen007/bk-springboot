package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * Route entity representing approval workflow routing.
 * Maps to the ROUTE table in Oracle database.
 */
@Entity
@Table(name = "ROUTE")
@Data
@NoArgsConstructor
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @Column(name = "APPROVER_ID")
    private Integer approverId;

    @Column(name = "APPROVER_TYPE")
    private String approverType;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "APPROVAL_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvalDate;

    @Column(name = "COMMENTS")
    private String comments;

    @Column(name = "SEQUENCE_NUMBER")
    private Integer sequenceNumber;

    @Column(name = "IS_CURRENT")
    private Boolean isCurrent;

    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "UPDATED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;
}
