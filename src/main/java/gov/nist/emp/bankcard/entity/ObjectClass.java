package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * ObjectClass entity representing budget object class codes.
 * Maps to the OBJECT_CLASS table in Oracle database.
 */
@Entity
@Table(name = "OBJECT_CLASS")
@Data
@NoArgsConstructor
public class ObjectClass implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CODE")
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
}
