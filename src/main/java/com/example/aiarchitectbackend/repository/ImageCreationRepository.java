package com.example.aiarchitectbackend.repository;

import com.example.aiarchitectbackend.domain.ImageCreation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageCreationRepository extends JpaRepository<ImageCreation, Long> {
    // JpaRepository<EntityType, IDType>

    // Example of a custom query method we might need later,
    // as per "Fetch and display list of ImageCreations for the project"
    List<ImageCreation> findByProjectId(Long projectId);
}