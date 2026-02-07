package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * ProjectTask entity representing project task codes for budget tracking.
 * Maps to the PROJECT_TASK table in Oracle database.
 */
@Entity
@Table(name = "PROJECT_TASK")
@Data
@NoArgsConstructor
public class ProjectTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PROJECT_TASK_CODE")
    private String projectTaskCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "OU_ID")
    private Integer ouId;

    @Column(name = "DIVISION_ID")
    private Integer divisionId;

    @Column(name = "GROUP_ID")
    private Integer groupId;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
}
