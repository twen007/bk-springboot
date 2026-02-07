package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.Route;
import gov.nist.emp.bankcard.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for Route-related endpoints (approval workflow).
 */
@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "Approval workflow management endpoints")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get approval route for a request")
    public ResponseEntity<List<Route>> getRoutesByRequestId(@PathVariable Integer requestId) {
        return ResponseEntity.ok(routeService.findByRequestId(requestId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals for current user")
    public ResponseEntity<List<Route>> getPendingApprovals() {
        // TODO: Get approverId from security context
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve a request")
    public ResponseEntity<?> approveRequest(@PathVariable Integer requestId,
            @RequestBody(required = false) String comments) {
        // TODO: Implement approval logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject a request")
    public ResponseEntity<?> rejectRequest(@PathVariable Integer requestId, @RequestBody String comments) {
        // TODO: Implement rejection logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/pull-back")
    @Operation(summary = "Pull back a submitted request")
    public ResponseEntity<?> pullBackRequest(@PathVariable Integer requestId) {
        // TODO: Implement pull-back logic
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{requestId}/reroute")
    @Operation(summary = "Reroute a request to different approvers")
    public ResponseEntity<?> rerouteRequest(@PathVariable Integer requestId, @RequestBody Object rerouteData) {
        // TODO: Implement reroute logic
        return ResponseEntity.ok().build();
    }
}
