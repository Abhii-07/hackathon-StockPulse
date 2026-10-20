package com.stockpulse.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LLMGateway {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4)).build();
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final String baseUrl;

    public LLMGateway(ObjectMapper mapper,
                      @Value("${llm.api-key:}") String apiKey,
                      @Value("${llm.model:gemini-2.5-flash}") String model,
                      @Value("${llm.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("LLM_API_KEY is missing");
        }
        try {
            String json = mapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 2048)
            ));
            String apiBase = baseUrl.endsWith("/v1beta") ? baseUrl : baseUrl + "/v1beta";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBase + "/models/" + model + ":generateContent"))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Gemini HTTP " + response.statusCode());
            }
            JsonNode parts = mapper.readTree(response.body())
                    .path("candidates").path(0).path("content").path("parts");
            if (!parts.isArray()) throw new IllegalStateException("Gemini returned no content");
            StringBuilder result = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.path("text").isTextual()) result.append(part.path("text").asText());
            }
            if (result.length() == 0) throw new IllegalStateException("Gemini returned empty text");
            return result.toString();
        } catch (Exception e) {
            // Never log request URL, API key, or response body.
            log.warn("Gemini call failed; rule fallback expected: {}", e.getClass().getSimpleName());
            throw new IllegalStateException("Gemini call failed", e);
        }
    }
}
