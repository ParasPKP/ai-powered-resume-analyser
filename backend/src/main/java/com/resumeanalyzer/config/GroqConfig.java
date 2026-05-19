package com.resumeanalyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class GroqConfig {

    @Value("${groq.api.keys}")
    private String apiKeysRaw;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private List<String> apiKeys;
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        apiKeys = Arrays.stream(apiKeysRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (apiKeys.isEmpty()) {
            throw new IllegalStateException("No Groq API keys configured! Set groq.api.keys in application.properties");
        }

        System.out.println("Loaded " + apiKeys.size() + " Groq API key(s)");
    }

    /**
     * Round-robin API key rotation (thread-safe)
     */
    public String getNextApiKey() {
        int index = currentKeyIndex.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(index);
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }
}
