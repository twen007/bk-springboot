package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for FileAttachment entity operations.
 */
@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Integer> {

    List<FileAttachment> findByRequestId(Integer requestId);

    List<FileAttachment> findByItemId(Integer itemId);

    List<FileAttachment> findByUploadedBy(Integer uploadedBy);
}
