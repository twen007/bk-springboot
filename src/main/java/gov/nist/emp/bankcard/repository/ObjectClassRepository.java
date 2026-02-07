package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.ObjectClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ObjectClass entity operations.
 */
@Repository
public interface ObjectClassRepository extends JpaRepository<ObjectClass, Integer> {

    Optional<ObjectClass> findByCode(String code);

    List<ObjectClass> findByIsActiveTrue();
}
