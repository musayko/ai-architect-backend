package com.example.aiarchitectbackend.controller;

import com.example.aiarchitectbackend.dto.CreateTextToImageRequest;
import com.example.aiarchitectbackend.dto.ImageCreationDto;
import com.example.aiarchitectbackend.service.ImageCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/image-creations")
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
        ImageCreationDto imageCreationDto = imageCreationService.getImageCreationById(creationId);
        // Optional: Add a check to ensure imageCreationDto.getProjectId().equals(projectId)
        return ResponseEntity.ok(imageCreationDto);
    }

    @PostMapping("/text-to-image") //
    public ResponseEntity<ImageCreationDto> createTextToImage(
            @PathVariable Long projectId,
            @RequestBody CreateTextToImageRequest request) { //

        ImageCreationDto imageCreationDto = imageCreationService.createAndProcessTextToImage(projectId, request); //

        // Determine appropriate HTTP status based on the outcome
        // If the DTO contains a status field, we can check it.
        // For simplicity, if an error occurs in the service that isn't caught and re-thrown
        // as a specific HTTP error, it might result in a 500 from GlobalExceptionHandler.
        // If it completes (even if status is FAILED in DTO), we created a record.
        if (imageCreationDto.getStatus() == com.example.aiarchitectbackend.domain.enums.CreationStatus.FAILED) { //
            // Still return 201 Created as the resource (ImageCreation record) was created,
            // but the client can check the status field in the response body.
            // Alternatively, you could return a 500 or 202 if it's truly an async process,
            // but for now, we're synchronous.
            return new ResponseEntity<>(imageCreationDto, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(imageCreationDto, HttpStatus.CREATED); //
    }
}