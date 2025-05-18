package com.example.aiarchitectbackend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Based on: { "promptText": "A futuristic cityscape", "parameters": { ... } }
// For simplicity, parameters can be a JSON string, aligning with ImageCreation.parametersJson
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTextToImageRequest {
    private String promptText;
    private String parametersJson; // Or Map<String, Object> if you prefer to handle conversion in controller/service
}