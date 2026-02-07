package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.FileAttachment;
import gov.nist.emp.bankcard.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * REST controller for FileAttachment-related endpoints.
 */
@RestController
@RequestMapping("/api/attachments")
@Tag(name = "Attachments", description = "File attachment management endpoints")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get all attachments for a request")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByRequestId(@PathVariable Integer requestId) {
        return ResponseEntity.ok(attachmentService.findByRequestId(requestId));
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Get all attachments for an item")
    public ResponseEntity<List<FileAttachment>> getAttachmentsByItemId(@PathVariable Integer itemId) {
        return ResponseEntity.ok(attachmentService.findByItemId(itemId));
    }

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Get attachment metadata by ID")
    public ResponseEntity<FileAttachment> getAttachmentById(@PathVariable Integer attachmentId) {
        return attachmentService.findById(attachmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download attachment file")
    public ResponseEntity<?> downloadAttachment(@PathVariable Integer attachmentId) {
        // TODO: Implement file download
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request/{requestId}")
    @Operation(summary = "Upload attachment for a request")
    public ResponseEntity<?> uploadForRequest(@PathVariable Integer requestId,
            @RequestParam("file") MultipartFile file) {
        // TODO: Implement file upload
        return ResponseEntity.ok().build();
    }

    @PostMapping("/item/{itemId}")
    @Operation(summary = "Upload attachment for an item")
    public ResponseEntity<?> uploadForItem(@PathVariable Integer itemId, @RequestParam("file") MultipartFile file) {
        // TODO: Implement file upload
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<?> deleteAttachment(@PathVariable Integer attachmentId) {
        attachmentService.deleteById(attachmentId);
        return ResponseEntity.ok().build();
    }
}
