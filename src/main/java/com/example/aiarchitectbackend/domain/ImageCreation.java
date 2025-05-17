package com.example.aiarchitectbackend.domain;

import com.example.aiarchitectbackend.domain.enums.CreationStatus;
import com.example.aiarchitectbackend.domain.enums.InputType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_creations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageCreation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) // Stores the enum as a String
    @Column(nullable = false)
    private InputType inputType;

    @Lob // For longer text
    @Column(columnDefinition = "TEXT") // For promptText
    private String promptText;

    private String inputImageFileName;

    private String outputImageFileName;

    @Lob // For longer text
    @Column(columnDefinition = "TEXT") // For generatedText
    private String generatedText;

    @Enumerated(EnumType.STRING) // Stores the enum as a String
    @Column(nullable = false)
    private CreationStatus status;

    private String aiModelUsed; //

    @Lob // For longer text
    @Column(columnDefinition = "TEXT") // For parametersJson
    private String parametersJson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // Defines the Many-to-One relationship
    @JoinColumn(name = "project_id", nullable = false) // Specifies the foreign key column
    private Project project;
}