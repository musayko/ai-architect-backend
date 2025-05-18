package com.example.aiarchitectbackend.controller;

import com.example.aiarchitectbackend.dto.CreateProjectRequest;
import com.example.aiarchitectbackend.dto.ProjectDto;
import com.example.aiarchitectbackend.dto.UpdateProjectRequest;
import com.example.aiarchitectbackend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects") // Base path for all project-related endpoints
@CrossOrigin // Allows cross-origin requests (e.g., from frontend dev server)
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        ProjectDto createdProject = projectService.createProject(createProjectRequest);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long projectId) {
        ProjectDto projectDto = projectService.getProjectById(projectId);
        return ResponseEntity.ok(projectDto);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long projectId, @RequestBody UpdateProjectRequest updateProjectRequest) {
        ProjectDto updatedProject = projectService.updateProject(projectId, updateProjectRequest);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}