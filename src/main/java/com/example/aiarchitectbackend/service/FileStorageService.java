package com.example.aiarchitectbackend.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootStoragePath;
    private final String baseStoragePathString;

    // Inject the storage path from application.properties
    public FileStorageService(@Value("${app.image-storage-path}") String storagePathValue) {
        this.baseStoragePathString = storagePathValue;
        this.rootStoragePath = Paths.get(storagePathValue).toAbsolutePath().normalize();
    }

    @PostConstruct // This method will be called after the bean is initialized
    public void init() {
        try {
            Files.createDirectories(this.rootStoragePath); // Ensures the root storage directory exists
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + this.rootStoragePath.toString(), e);
        }
    }

    /**
     * Stores a file in a specified subfolder.
     *
     * @param file      The file to store.
     * @param subfolder The subfolder within the root storage path (e.g., "input/project_id" or "output/project_id").
     * @return The unique filename generated for the stored file.
     * @throws RuntimeException if storing fails.
     */
    public String storeFile(MultipartFile file, String subfolder) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + (extension != null ? "." + extension : "");

        try {
            Path subfolderPath = this.rootStoragePath.resolve(subfolder).normalize();
            Files.createDirectories(subfolderPath); // Ensure subfolder exists [cite: 28]

            Path targetLocation = subfolderPath.resolve(uniqueFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + originalFilename + " in " + subfolder, ex);
        }
    }

    /**
     * Loads a file as a resource from a specified subfolder.
     *
     * @param filename  The name of the file to load.
     * @param subfolder The subfolder where the file is located.
     * @return The file as a Resource.
     * @throws RuntimeException if the file is not found or cannot be read.
     */
    public Resource loadFileAsResource(String filename, String subfolder) {
        try {
            Path filePath = this.rootStoragePath.resolve(subfolder).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename + " in " + subfolder);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Could not read file: " + filename + " in " + subfolder, ex);
        }
    }

    /**
     * Deletes a file from a specified subfolder.
     *
     * @param filename  The name of the file to delete.
     * @param subfolder The subfolder where the file is located.
     * @return true if the file was deleted, false otherwise.
     */
    public boolean deleteFile(String filename, String subfolder) {
        try {
            Path filePath = this.rootStoragePath.resolve(subfolder).resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log this exception or handle it as appropriate
            System.err.println("Error deleting file: " + filename + " in " + subfolder + " - " + ex.getMessage());
            return false;
        }
    }

    // for storing AI-generated images (which will be byte arrays),
    // later will be added another storeFile method like:
    // public String storeFile(byte[] fileBytes, String originalFilename, String subfolder)
}