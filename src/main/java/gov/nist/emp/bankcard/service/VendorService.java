package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.Vendor;
import gov.nist.emp.bankcard.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Vendor-related business logic.
 */
@Service
@Transactional
public class VendorService {

    private final VendorRepository vendorRepository;

    public VendorService(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    public Optional<Vendor> findById(Integer vendorId) {
        return vendorRepository.findById(vendorId);
    }

    public List<Vendor> findSharedVendors() {
        return vendorRepository.findSharedVendors();
    }

    public List<Vendor> findByOuId(Integer ouId) {
        return vendorRepository.findByOuId(ouId);
    }

    public Vendor save(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public void deleteById(Integer vendorId) {
        vendorRepository.deleteById(vendorId);
    }

    public List<Vendor> searchByName(String name) {
        return vendorRepository.findByNameContainingIgnoreCase(name);
    }

    // TODO: Add methods for request-vendor associations
}
