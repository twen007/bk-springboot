package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.Request;
import gov.nist.emp.bankcard.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for Request-related endpoints.
 */
@RestController
@RequestMapping("/api/requests")
@Tag(name = "Requests", description = "Purchase request management endpoints")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @Operation(summary = "Get requests by criteria")
    public ResponseEntity<List<Request>> getRequestsByCriteria(
            @RequestParam(required = false) Integer ouId,
            @RequestParam(required = false) Integer divisionId,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer fy,
            @RequestParam(required = false) Integer reviewerId,
            @RequestParam(required = false) Integer purchaseTypeId) {
        // TODO: Implement with criteria
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get request by ID")
    public ResponseEntity<Request> getRequestById(@PathVariable Integer requestId) {
        return requestService.findById(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/saved")
    @Operation(summary = "Get saved (draft) requests for current user")
    public ResponseEntity<List<Request>> getSavedRequests() {
        // TODO: Get peopleId from security context
        return ResponseEntity.ok().build();
    }

    @GetMapping("/submitted")
    @Operation(summary = "Get submitted requests for current user")
    public ResponseEntity<List<Request>> getSubmittedRequests() {
        // TODO: Get peopleId from security context
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending approval requests for current user")
    public ResponseEntity<List<Request>> getPendingRequests() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/prepared")
    @Operation(summary = "Get prepared requests for current user")
    public ResponseEntity<List<Request>> getPreparedRequests() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/processed/{fy}")
    @Operation(summary = "Get processed requests by fiscal year")
    public ResponseEntity<List<Request>> getProcessedRequests(
            @PathVariable Integer fy,
            @RequestParam(defaultValue = "false") String showPurchaseWithMissingStmtDt) {
        return ResponseEntity.ok(requestService.findByFiscalYear(fy));
    }

    @GetMapping("/archived")
    @Operation(summary = "Get archived requests for current user")
    public ResponseEntity<List<Request>> getArchivedRequests() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{requestId}/audit-report")
    @Operation(summary = "Get audit report for a request")
    public ResponseEntity<?> getAuditReport(@PathVariable Integer requestId) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{requestId}/summary-report")
    @Operation(summary = "Get summary report for a request")
    public ResponseEntity<?> getSummaryReport(@PathVariable Integer requestId) {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "Create a new request")
    public ResponseEntity<Request> createRequest(@RequestBody Request request) {
        Request saved = requestService.save(request);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{requestId}")
    @Operation(summary = "Update an existing request")
    public ResponseEntity<Request> updateRequest(@PathVariable Integer requestId, @RequestBody Request request) {
        request.setId(requestId);
        Request saved = requestService.save(request);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{requestId}")
    @Operation(summary = "Delete a request")
    public ResponseEntity<?> deleteRequest(@PathVariable Integer requestId) {
        requestService.deleteById(requestId);
        return ResponseEntity.ok().build();
    }
}
