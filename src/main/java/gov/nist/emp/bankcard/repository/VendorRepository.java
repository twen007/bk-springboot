package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Vendor entity operations.
 */
@Repository
public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    @Query("SELECT v FROM Vendor v WHERE v.isShared = true")
    List<Vendor> findSharedVendors();

    List<Vendor> findByOuId(Integer ouId);

    List<Vendor> findByCreatedBy(Integer createdBy);

    List<Vendor> findByNameContainingIgnoreCase(String name);
}
