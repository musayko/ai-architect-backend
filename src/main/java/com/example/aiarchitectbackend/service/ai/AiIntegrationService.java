package com.example.aiarchitectbackend.service.ai; // Adjust package as needed

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
//import com.google.protobuf.Value; //
import com.google.protobuf.util.JsonFormat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
public class AiIntegrationService {

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.location}")
    private String gcpLocation;

    @Value("${gcp.publisher}") // Ensure this matches your application.properties
    private String gcpPublisher;

    @Value("${gcp.imagen.model}") // Ensure this matches your application.properties
    private String imagenModelId; // This will hold "imagegeneration@006"


    /**
     * Generates an image from a text prompt using Vertex AI Imagen.
     *
     * @param prompt The text prompt for image generation.
     * @param parametersJson Optional JSON string for additional parameters (e.g., number of samples).
     * @return Byte array of the generated image (first image if multiple are generated).
     * @throws IOException if there's an issue with the API call.
     */
    public byte[] generateImageFromTextVertexAi(String prompt, String parametersJson) throws IOException {
        // The regional endpoint for Vertex AI
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", gcpLocation);

        PredictionServiceSettings predictionServiceSettings =
                PredictionServiceSettings.newBuilder().setEndpoint(endpoint).build();

        try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create(predictionServiceSettings)) {
            // Construct the full model endpoint name using publisher and model
            // CORRECTED METHOD NAME: ofProjectLocationPublisherModelName
            final EndpointName modelEndpointName = EndpointName.ofProjectLocationPublisherModelName(
                    gcpProjectId, gcpLocation, gcpPublisher, imagenModelId);

            // Construct the instance payload for the text-to-image model
            JsonObject instanceJson = new JsonObject();
            instanceJson.addProperty("prompt", prompt);

            // Default parameters or parse from parametersJson
            int sampleCount = 1; // Default to 1 image
            // You can add more parameters here if needed, e.g., aspect ratio, seed, etc.
            if (parametersJson != null && !parametersJson.isEmpty()) {
                try {
                    JsonObject params = JsonParser.parseString(parametersJson).getAsJsonObject();
                    if (params.has("sampleCount") && params.get("sampleCount").isJsonPrimitive() && params.get("sampleCount").getAsJsonPrimitive().isNumber()) {
                        sampleCount = params.get("sampleCount").getAsInt();
                    }
                    // Add logic here to parse other parameters from parametersJson if needed
                } catch (Exception e) {
                    // Log or handle malformed parametersJson
                    System.err.println("Warning: Could not parse parametersJson: " + e.getMessage());
                }
            }

            JsonObject modelParameters = new JsonObject();
            modelParameters.addProperty("sampleCount", sampleCount);
            // Add other parameters supported by the model, e.g., "aspectRatio", "seed"
            // For example: modelParameters.addProperty("aspectRatio", "1:1");

            // Convert instance JSON to protobuf Value
            // CORRECTED: Use fully qualified name for Google's Value
            com.google.protobuf.Value.Builder instanceBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(instanceJson.toString(), instanceBuilder);
            // CORRECTED: Use fully qualified name for Google's Value in the List
            List<com.google.protobuf.Value> instances = List.of(instanceBuilder.build());

            // Convert model parameters JSON to protobuf Value
            // CORRECTED: Use fully qualified name for Google's Value
            com.google.protobuf.Value.Builder parametersBuilder = com.google.protobuf.Value.newBuilder();
            JsonFormat.parser().merge(modelParameters.toString(), parametersBuilder);

            // Make the prediction request
            PredictResponse predictResponse = predictionServiceClient.predict(
                    modelEndpointName, instances, parametersBuilder.build());

            // Process the response
            String b64EncodedImage = "";
            if (!predictResponse.getPredictionsList().isEmpty()) {
                // CORRECTED: Use fully qualified name for Google's Value
                com.google.protobuf.Value predictionValue = predictResponse.getPredictions(0);
                String predictionJsonString = JsonFormat.printer().print(predictionValue);
                JsonObject predictionJsonObject = JsonParser.parseString(predictionJsonString).getAsJsonObject();

                if (predictionJsonObject.has("bytesBase64Encoded")) {
                    b64EncodedImage = predictionJsonObject.get("bytesBase64Encoded").getAsString();
                } else {
                    System.err.println("Unexpected prediction structure: bytesBase64Encoded field missing. Response: " + predictionJsonString);
                    throw new IOException("Failed to parse image from Vertex AI response: bytesBase64Encoded field missing.");
                }
            }

            if (b64EncodedImage.isEmpty()) {
                System.err.println("Vertex AI response did not contain an image or was empty.");
                throw new IOException("Failed to get Base64 encoded image from Vertex AI response (empty or missing).");
            }

            return Base64.getDecoder().decode(b64EncodedImage);

        } catch (IOException e) {
            System.err.println("Vertex AI API call failed: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            throw e; // Re-throw to be handled by the caller
        } catch (Exception e) {
            // Catch any other unexpected errors during the process
            System.err.println("An unexpected error occurred during Vertex AI integration: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("An unexpected error occurred during Vertex AI integration.", e);
        }
    }
}