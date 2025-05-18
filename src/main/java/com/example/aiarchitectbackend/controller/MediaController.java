package com.example.aiarchitectbackend.controller;
import com.example.aiarchitectbackend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/media")
@CrossOrigin
public class MediaController {

    private final FileStorageService fileStorageService;

    @Autowired
    public MediaController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{type}/{projectId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String type,
                                              @PathVariable String projectId,
                                              @PathVariable String filename,
                                              HttpServletRequest request) {
        // 'type' can be 'input' or 'output' corresponding to subfolders
        String subfolder = type + "/" + projectId;
        Resource resource = fileStorageService.loadFileAsResource(filename, subfolder);

        // Try to determine content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Log error or fallback
            System.err.println("Could not determine file type for: " + filename);
        }

        // Fallback to generic content type if unknown
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}