package com.resumeanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resumeanalyzer.config.GroqConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GroqAiService {

    private static final Logger log = LoggerFactory.getLogger(GroqAiService.class);
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final GroqConfig groqConfig;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    public GroqAiService(GroqConfig groqConfig, ObjectMapper objectMapper) {
        this.groqConfig = groqConfig;
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Send a prompt to Groq AI and get a JSON response
     *
     * @param prompt      The prompt text
     * @param temperature Temperature for response generation (0.0 - 1.0)
     * @param maxTokens   Maximum tokens in response
     * @return Parsed JSON response from AI
     */
    public JsonNode chat(String prompt, double temperature, int maxTokens) throws IOException {
        String apiKey = groqConfig.getNextApiKey();

        // Build the request body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", groqConfig.getModel());
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        // Response format
        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestBody.set("response_format", responseFormat);

        // Messages array
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(groqConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, JSON_MEDIA))
                .build();

        log.debug("Sending request to Groq API with model: {}", groqConfig.getModel());

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                log.error("Groq API error ({}): {}", response.code(), responseBody);

                if (response.code() == 400) {
                    throw new GroqApiException(
                            "AI analysis failed. Please ensure your content has clear text.",
                            response.code()
                    );
                }

                // If rate limited (429), try with the next key
                if (response.code() == 429) {
                    log.warn("Rate limited, retrying with next API key...");
                    return retryWithNextKey(prompt, temperature, maxTokens);
                }

                throw new GroqApiException(
                        "Groq API request failed with status " + response.code(),
                        response.code()
                );
            }

            // Parse the response
            JsonNode responseJson = objectMapper.readTree(responseBody);
            String content = responseJson
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.debug("AI Response: {}", content);

            // Clean any markdown formatting from the response
            content = content
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readTree(content);
        }
    }

    /**
     * Retry with the next API key (for rate limiting)
     */
    private JsonNode retryWithNextKey(String prompt, double temperature, int maxTokens) throws IOException {
        String nextKey = groqConfig.getNextApiKey();
        log.info("Retrying with next API key...");

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", groqConfig.getModel());
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);

        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestBody.set("response_format", responseFormat);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.set("messages", messages);

        Request request = new Request.Builder()
                .url(groqConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + nextKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON_MEDIA))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new GroqApiException("Retry also failed with status " + response.code(), response.code());
            }

            JsonNode responseJson = objectMapper.readTree(responseBody);
            String content = responseJson
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText()
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            return objectMapper.readTree(content);
        }
    }

    /**
     * Custom exception for Groq API errors
     */
    public static class GroqApiException extends IOException {
        private final int statusCode;

        public GroqApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
