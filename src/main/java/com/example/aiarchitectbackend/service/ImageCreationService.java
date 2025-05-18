package com.example.aiarchitectbackend.service;
import com.example.aiarchitectbackend.domain.ImageCreation;
import com.example.aiarchitectbackend.domain.Project;
import com.example.aiarchitectbackend.domain.enums.CreationStatus;
import com.example.aiarchitectbackend.domain.enums.InputType;
import com.example.aiarchitectbackend.dto.CreateTextToImageRequest;
import com.example.aiarchitectbackend.dto.ImageCreationDto;
import com.example.aiarchitectbackend.repository.ImageCreationRepository;
import com.example.aiarchitectbackend.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ImageCreationService {

    private final ImageCreationRepository imageCreationRepository;
    private final ProjectRepository projectRepository;
    // We might inject FileStorageService here later when we save actual files

    @Autowired
    public ImageCreationService(ImageCreationRepository imageCreationRepository,
                                ProjectRepository projectRepository) {
        this.imageCreationRepository = imageCreationRepository;
        this.projectRepository = projectRepository;
    }

    // This method is for creating the record, AI processing will be separate
    @Transactional
    public ImageCreationDto createTextToImageRecord(Long projectId, CreateTextToImageRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        ImageCreation newImageCreation = ImageCreation.builder()
                .project(project)
                .inputType(InputType.TEXT_TO_IMAGE) // Specific to this request type
                .promptText(request.getPromptText())
                .parametersJson(request.getParametersJson())
                .status(CreationStatus.PENDING) // Initial status [cite: 68]
                // outputImageFileName will be set after AI processing
                .build();

        ImageCreation savedImageCreation = imageCreationRepository.save(newImageCreation);
        return ImageCreationDto.fromEntity(savedImageCreation);
    }

    @Transactional(readOnly = true)
    public ImageCreationDto getImageCreationById(Long creationId) {
        ImageCreation imageCreation = imageCreationRepository.findById(creationId)
                .orElseThrow(() -> new EntityNotFoundException("ImageCreation not found with id: " + creationId));
        return ImageCreationDto.fromEntity(imageCreation);
    }

    @Transactional(readOnly = true)
    public List<ImageCreationDto> getImageCreationsByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }
        return imageCreationRepository.findByProjectId(projectId).stream()
                .map(ImageCreationDto::fromEntity)
                .collect(Collectors.toList());
    }
}