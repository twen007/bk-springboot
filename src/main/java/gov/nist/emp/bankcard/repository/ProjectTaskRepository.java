package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProjectTask entity operations.
 */
@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Integer> {

    Optional<ProjectTask> findByProjectTaskCode(String projectTaskCode);

    List<ProjectTask> findByOuId(Integer ouId);

    List<ProjectTask> findByDivisionId(Integer divisionId);

    List<ProjectTask> findByIsActiveTrue();
}
