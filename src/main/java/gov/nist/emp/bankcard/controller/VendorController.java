package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.Vendor;
import gov.nist.emp.bankcard.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for Vendor-related endpoints.
 */
@RestController
@RequestMapping("/api/vendors")
@Tag(name = "Vendors", description = "Vendor management endpoints")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping("/shared")
    @Operation(summary = "Get all shared vendors")
    public ResponseEntity<List<Vendor>> getSharedVendors() {
        return ResponseEntity.ok(vendorService.findSharedVendors());
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "Get vendors for a specific request")
    public ResponseEntity<?> getVendorsForRequest(@PathVariable Integer requestId) {
        // TODO: Implement via request-vendor join
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{vendorId}")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Integer vendorId) {
        return vendorService.findById(vendorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a new vendor")
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        Vendor saved = vendorService.save(vendor);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{vendorId}")
    @Operation(summary = "Update an existing vendor")
    public ResponseEntity<Vendor> updateVendor(@PathVariable Integer vendorId, @RequestBody Vendor vendor) {
        vendor.setId(vendorId);
        Vendor saved = vendorService.save(vendor);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{vendorId}")
    @Operation(summary = "Delete a vendor")
    public ResponseEntity<?> deleteVendor(@PathVariable Integer vendorId) {
        vendorService.deleteById(vendorId);
        return ResponseEntity.ok().build();
    }
}
