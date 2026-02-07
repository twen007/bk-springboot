package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

/**
 * Repository interface for Item entity operations.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByRequestId(Integer requestId);

    List<Item> findByVendorId(Integer vendorId);

    @Query("SELECT i FROM Item i WHERE i.isTaggableEquipment = true")
    List<Item> findTaggableEquipment();

    @Query("SELECT i FROM Item i WHERE i.chemical = true")
    List<Item> findChemicalItems();

    @Query("SELECT i FROM Item i WHERE i.biological = true")
    List<Item> findBiologicalItems();

    // TODO: Add EA item and PC item queries with date ranges
}
