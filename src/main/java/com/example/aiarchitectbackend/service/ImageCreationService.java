package com.example.aiarchitectbackend.service;

import com.example.aiarchitectbackend.domain.ImageCreation;
import com.example.aiarchitectbackend.domain.Project;
import com.example.aiarchitectbackend.domain.enums.CreationStatus;
import com.example.aiarchitectbackend.domain.enums.InputType;
import com.example.aiarchitectbackend.dto.CreateTextToImageRequest;
import com.example.aiarchitectbackend.dto.ImageCreationDto;
import com.example.aiarchitectbackend.repository.ImageCreationRepository;
import com.example.aiarchitectbackend.repository.ProjectRepository;
import com.example.aiarchitectbackend.service.ai.AiIntegrationService;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ImageCreationService {

    private final ImageCreationRepository imageCreationRepository;
    private final ProjectRepository projectRepository;
    private final AiIntegrationService aiIntegrationService; // Inject AiIntegrationService
    private final FileStorageService fileStorageService;   // Inject FileStorageService

    @Autowired
    public ImageCreationService(ImageCreationRepository imageCreationRepository,
                                ProjectRepository projectRepository,
                                AiIntegrationService aiIntegrationService,
                                FileStorageService fileStorageService) {
        this.imageCreationRepository = imageCreationRepository;
        this.projectRepository = projectRepository;
        this.aiIntegrationService = aiIntegrationService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Creates an ImageCreation record, processes it via AI for text-to-image,
     * stores the output, and updates the record.
     */

    // This method involves multiple database operations
    @Transactional
    public ImageCreationDto createAndProcessTextToImage(Long projectId, CreateTextToImageRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // 1. Create initial ImageCreation record
        ImageCreation imageCreation = ImageCreation.builder()
                .project(project)
                .inputType(InputType.TEXT_TO_IMAGE)
                .promptText(request.getPromptText())
                .parametersJson(request.getParametersJson())
                .status(CreationStatus.PENDING)
                // aiModelUsed can be set later or passed if known
                .build();
        imageCreation = imageCreationRepository.save(imageCreation);

        try {
            // 2. Call AI Integration Service to generate image
            // We can pass request.getParametersJson() to AiIntegrationService if it uses it
            byte[] imageBytes = aiIntegrationService.generateImageFromTextVertexAi(
                    imageCreation.getPromptText(),
                    imageCreation.getParametersJson()
            );

            // 3. Save the returned image bytes using FileStorageService
            // Subfolder structure: output/{projectId}/
            String subfolder = "output/" + project.getId().toString();
            // Provide a filename hint. Let's use the image creation ID and prompt snippet.
            String filenameHint = "image_creation_" + imageCreation.getId() + "_output.png";
            String outputImageFileName = fileStorageService.storeFile(imageBytes, filenameHint, subfolder);

            // 4. Update ImageCreation record with output filename and status
            imageCreation.setOutputImageFileName(outputImageFileName);
            imageCreation.setStatus(CreationStatus.COMPLETED);
            imageCreation.setAiModelUsed("Imagen_VertexAI"); // Example: Set the model used
            imageCreation = imageCreationRepository.save(imageCreation);

        } catch (IOException | RuntimeException e) { // Catch exceptions from AI service or file storage
            System.err.println("Error during AI image generation or file storage: " + e.getMessage());
            e.printStackTrace();
            imageCreation.setStatus(CreationStatus.FAILED);
            imageCreationRepository.save(imageCreation); // Save failure status
            // Re-throw or handle more gracefully for the controller, perhaps returning a specific error DTO
            // For now, we'll let it propagate and be caught by GlobalExceptionHandler if it's a RuntimeException
            // or ensure the controller returns ImageCreationDto even on failure.
        }

        return ImageCreationDto.fromEntity(imageCreation);
    }

    // The method above is now the primary one for text-to-image creation and processing.

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