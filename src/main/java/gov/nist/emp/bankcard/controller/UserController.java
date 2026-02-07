package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.User;
import gov.nist.emp.bankcard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for User-related endpoints.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management and profile endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<?> getProfile() {
        // TODO: Get current user from security context
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ou-members")
    @Operation(summary = "Get all members in user's OU")
    public ResponseEntity<List<User>> getOuMembers(@RequestParam(required = false) String filter) {
        // TODO: Implement with filter
        return ResponseEntity.ok().build();
    }

    @GetMapping("/employees")
    @Operation(summary = "Get all NIST employees")
    public ResponseEntity<List<User>> getEmployees(@RequestParam(required = false) String filter) {
        // TODO: Implement with filter
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviewers")
    @Operation(summary = "Get list of supervisors who can be reviewers")
    public ResponseEntity<List<User>> getReviewers(@RequestParam(required = false) String filter) {
        return ResponseEntity.ok(userService.findSupervisors());
    }

    @GetMapping("/bankcard-holders")
    @Operation(summary = "Get list of bankcard holders for user's division")
    public ResponseEntity<List<User>> getBankcardHolders() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bankcard-approving-officials")
    @Operation(summary = "Get list of bankcard approving officials")
    public ResponseEntity<List<User>> getBankcardApprovingOfficials() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/funds-certifying-officials")
    @Operation(summary = "Get list of funds certifying officials")
    public ResponseEntity<List<User>> getFundsCertifyingOfficials() {
        // TODO: Implement
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles/{peopleId}")
    @Operation(summary = "Get roles for a specific user")
    public ResponseEntity<?> getRolesByUserId(@PathVariable Integer peopleId) {
        // TODO: Implement via NIST Org API call
        return ResponseEntity.ok().build();
    }
}
