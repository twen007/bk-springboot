package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.ProjectTask;
import gov.nist.emp.bankcard.service.ProjectTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for ProjectTask-related endpoints.
 */
@RestController
@RequestMapping("/api/project-tasks")
@Tag(name = "Project Tasks", description = "Project task code management endpoints")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    public ProjectTaskController(ProjectTaskService projectTaskService) {
        this.projectTaskService = projectTaskService;
    }

    @GetMapping
    @Operation(summary = "Get all active project tasks")
    public ResponseEntity<List<ProjectTask>> getActiveProjectTasks() {
        return ResponseEntity.ok(projectTaskService.findActive());
    }

    @GetMapping("/ou/{ouId}")
    @Operation(summary = "Get project tasks by OU")
    public ResponseEntity<List<ProjectTask>> getProjectTasksByOuId(@PathVariable Integer ouId) {
        return ResponseEntity.ok(projectTaskService.findByOuId(ouId));
    }

    @GetMapping("/division/{divisionId}")
    @Operation(summary = "Get project tasks by division")
    public ResponseEntity<List<ProjectTask>> getProjectTasksByDivisionId(@PathVariable Integer divisionId) {
        return ResponseEntity.ok(projectTaskService.findByDivisionId(divisionId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project task by ID")
    public ResponseEntity<ProjectTask> getProjectTaskById(@PathVariable Integer id) {
        return projectTaskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
