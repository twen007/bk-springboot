package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.FileAttachment;
import gov.nist.emp.bankcard.repository.FileAttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for FileAttachment-related business logic.
 */
@Service
@Transactional
public class AttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;

    public AttachmentService(FileAttachmentRepository fileAttachmentRepository) {
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    public Optional<FileAttachment> findById(Integer attachmentId) {
        return fileAttachmentRepository.findById(attachmentId);
    }

    public List<FileAttachment> findByRequestId(Integer requestId) {
        return fileAttachmentRepository.findByRequestId(requestId);
    }

    public List<FileAttachment> findByItemId(Integer itemId) {
        return fileAttachmentRepository.findByItemId(itemId);
    }

    public FileAttachment save(FileAttachment attachment) {
        return fileAttachmentRepository.save(attachment);
    }

    public void deleteById(Integer attachmentId) {
        fileAttachmentRepository.deleteById(attachmentId);
    }

    // TODO: Add methods for:
    // - uploadFile(MultipartFile file, Integer requestId)
    // - downloadFile(Integer attachmentId)
    // - getFileContent(Integer attachmentId)
}
