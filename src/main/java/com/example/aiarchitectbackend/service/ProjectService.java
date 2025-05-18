package com.example.aiarchitectbackend.service;

import com.example.aiarchitectbackend.domain.Project;
import com.example.aiarchitectbackend.dto.CreateProjectRequest;
import com.example.aiarchitectbackend.dto.ProjectDto;
import com.example.aiarchitectbackend.dto.UpdateProjectRequest;
import com.example.aiarchitectbackend.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        // createdAt and updatedAt are managed by @CreationTimestamp and @UpdateTimestamp
        Project savedProject = projectRepository.save(project);
        return ProjectDto.fromEntity(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        return ProjectDto.fromEntity(project);
    }

    @Transactional
    public ProjectDto updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project updatedProject = projectRepository.save(project);
        return ProjectDto.fromEntity(updatedProject);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
        projectRepository.deleteById(projectId);
    }
}