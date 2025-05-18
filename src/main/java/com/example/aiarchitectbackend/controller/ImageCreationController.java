package com.example.aiarchitectbackend.controller;

import com.example.aiarchitectbackend.dto.ImageCreationDto;
import com.example.aiarchitectbackend.service.ImageCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/image-creations") // Nested under projects
@CrossOrigin
public class ImageCreationController {

    private final ImageCreationService imageCreationService;

    @Autowired
    public ImageCreationController(ImageCreationService imageCreationService) {
        this.imageCreationService = imageCreationService;
    }

    @GetMapping
    public ResponseEntity<List<ImageCreationDto>> getImageCreationsForProject(@PathVariable Long projectId) {
        List<ImageCreationDto> imageCreations = imageCreationService.getImageCreationsByProjectId(projectId);
        return ResponseEntity.ok(imageCreations);
    }

    @GetMapping("/{creationId}")
    public ResponseEntity<ImageCreationDto> getImageCreationById(@PathVariable Long projectId, @PathVariable Long creationId) {
        // We might want to ensure creationId belongs to projectId in the service or here for more robustness.
        // For now, getImageCreationById only uses creationId.
        ImageCreationDto imageCreationDto = imageCreationService.getImageCreationById(creationId);
        // Add a check: if (!imageCreationDto.getProjectId().equals(projectId)) { throw new AccessDeniedException(...); }
        return ResponseEntity.ok(imageCreationDto);
    }

    // POST endpoint for creating text-to-image will be added
    // e.g., @PostMapping("/text-to-image")
}