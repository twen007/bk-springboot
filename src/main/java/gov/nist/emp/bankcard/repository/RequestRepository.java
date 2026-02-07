package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Request entity operations.
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findByCreatedBy(Integer createdBy);

    List<Request> findByRequesterId(Integer requesterId);

    @Query("SELECT r FROM Request r WHERE r.createdBy = :peopleId AND r.routeStatusId = 1")
    List<Request> findSavedByPeopleId(@Param("peopleId") Integer peopleId);

    @Query("SELECT r FROM Request r WHERE r.createdBy = :peopleId AND r.routeStatusId > 1")
    List<Request> findSubmittedByPeopleId(@Param("peopleId") Integer peopleId);

    @Query("SELECT r FROM Request r WHERE r.fy = :fy")
    List<Request> findByFiscalYear(@Param("fy") Integer fy);

    List<Request> findByOuId(Integer ouId);

    List<Request> findByDivisionId(Integer divisionId);

    List<Request> findByBankcardHolderId(Integer bankcardHolderId);

    // TODO: Add more complex queries for pending, prepared, processed, archived
}
