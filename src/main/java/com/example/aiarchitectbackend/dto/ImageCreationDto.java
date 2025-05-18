package com.example.aiarchitectbackend.dto;

import com.example.aiarchitectbackend.domain.enums.CreationStatus;
import com.example.aiarchitectbackend.domain.enums.InputType;
import com.example.aiarchitectbackend.domain.ImageCreation;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageCreationDto {
    private Long id;
    private Long projectId; // To indicate which project it belongs to
    private InputType inputType;
    private String promptText;
    private String inputImageFileName;
    private String outputImageFileName;
    private String generatedText;
    private CreationStatus status;
    private String aiModelUsed;
    private String parametersJson;
    private LocalDateTime createdAt;

    public static ImageCreationDto fromEntity(ImageCreation imageCreation) {
        return new ImageCreationDto(
                imageCreation.getId(),
                imageCreation.getProject().getId(), // Get project ID
                imageCreation.getInputType(),
                imageCreation.getPromptText(),
                imageCreation.getInputImageFileName(),
                imageCreation.getOutputImageFileName(),
                imageCreation.getGeneratedText(),
                imageCreation.getStatus(),
                imageCreation.getAiModelUsed(),
                imageCreation.getParametersJson(),
                imageCreation.getCreatedAt()
        );
    }
}
