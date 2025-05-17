package com.example.aiarchitectbackend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generated ID
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob // Specifies that this should be stored as a Large Object (TEXT type)
    @Column(columnDefinition = "TEXT") // Ensures TEXT type for longer descriptions
    private String description;

    @CreationTimestamp // Automatically set on creation
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // Automatically set on update
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // If you want a bidirectional relationship, you'd add:
    // @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<ImageCreation> imageCreations;
}