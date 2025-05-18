package com.example.aiarchitectbackend.repository;

import com.example.aiarchitectbackend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Spring Data repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // JpaRepository<EntityType, IDType>
    // Spring Data JPA will automatically implement methods like:
    // save(), findById(), findAll(), deleteById(), etc.

    // Custom query methods can be implemented here if needed later.
    // For example: List<Project> findByName(String name);
}