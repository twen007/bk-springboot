package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Route entity operations.
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {

    List<Route> findByRequestId(Integer requestId);

    List<Route> findByApproverId(Integer approverId);

    @Query("SELECT r FROM Route r WHERE r.requestId = :requestId AND r.isCurrent = true")
    Optional<Route> findCurrentRouteByRequestId(@Param("requestId") Integer requestId);

    @Query("SELECT r FROM Route r WHERE r.approverId = :approverId AND r.status = 'PENDING'")
    List<Route> findPendingByApproverId(@Param("approverId") Integer approverId);

    List<Route> findByRequestIdOrderBySequenceNumberAsc(Integer requestId);
}
