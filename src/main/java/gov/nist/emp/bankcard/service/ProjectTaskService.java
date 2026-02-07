package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.ProjectTask;
import gov.nist.emp.bankcard.repository.ProjectTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for ProjectTask-related business logic.
 */
@Service
@Transactional
public class ProjectTaskService {

    private final ProjectTaskRepository projectTaskRepository;

    public ProjectTaskService(ProjectTaskRepository projectTaskRepository) {
        this.projectTaskRepository = projectTaskRepository;
    }

    public Optional<ProjectTask> findById(Integer id) {
        return projectTaskRepository.findById(id);
    }

    public Optional<ProjectTask> findByCode(String code) {
        return projectTaskRepository.findByProjectTaskCode(code);
    }

    public List<ProjectTask> findByOuId(Integer ouId) {
        return projectTaskRepository.findByOuId(ouId);
    }

    public List<ProjectTask> findByDivisionId(Integer divisionId) {
        return projectTaskRepository.findByDivisionId(divisionId);
    }

    public List<ProjectTask> findActive() {
        return projectTaskRepository.findByIsActiveTrue();
    }

    public ProjectTask save(ProjectTask projectTask) {
        return projectTaskRepository.save(projectTask);
    }
}
