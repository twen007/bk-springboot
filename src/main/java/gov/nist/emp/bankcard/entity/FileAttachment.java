package gov.nist.emp.bankcard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Date;

/**
 * FileAttachment entity representing uploaded files attached to requests.
 * Maps to the FILE_ATTACHMENT table in Oracle database.
 */
@Entity
@Table(name = "FILE_ATTACHMENT")
@Data
@NoArgsConstructor
public class FileAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_TYPE")
    private String fileType;

    @Column(name = "FILE_SIZE")
    private Long fileSize;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Lob
    @Column(name = "FILE_DATA")
    private byte[] fileData;

    @Column(name = "UPLOADED_BY")
    private Integer uploadedBy;

    @Column(name = "UPLOADED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedDate;

    @Column(name = "DESCRIPTION")
    private String description;
}
