package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    List<User> findByOuId(Integer ouId);

    List<User> findByDivisionId(Integer divisionId);

    List<User> findByGroupId(Integer groupId);

    @Query("SELECT u FROM User u WHERE u.supervisor = true AND u.active = true")
    List<User> findSupervisors();

    @Query("SELECT u FROM User u WHERE u.ouId = :ouId AND u.active = true")
    List<User> findActiveByOuId(@Param("ouId") Integer ouId);

    @Query("SELECT u FROM User u WHERE u.divisionCode = :divisionCode AND u.active = true")
    List<User> findActiveByDivisionCode(@Param("divisionCode") String divisionCode);

    List<User> findByActiveTrue();

    // TODO: Add more queries as needed for BCH, BAO, FCO lookups
}
