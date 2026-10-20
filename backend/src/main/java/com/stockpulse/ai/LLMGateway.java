package com.stockpulse.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;

@Slf4j
@Component
public class LLMGateway {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public LLMGateway(
            @Value("${llm.base-url}") String baseUrl,
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.model}") String model) {
        
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return execution.execute(request, body);
                })
                .build();
        
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generateContent(String prompt) {
        try {
            log.debug("Calling Gemini API with prompt: {}", prompt);
            
            String url = String.format("/models/%s:generateContent?key=%s", model, apiKey);
            
            // Create the request body as per Gemini API specification
            String requestBody = String.format("""
                {
                  "contents": [{
                    "parts": [{
                      "text": "%s"
                    }]
                  }],
                  "generationConfig": {
                    "temperature": 0.7,
                    "maxOutputTokens": 1000
                  }
                }
                """, prompt.replace("\"", "\\\""));
            
            log.debug("Request URL: {}", url);
            log.debug("Request Body: {}", requestBody);
            
            String response = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            
            log.debug("Received response from Gemini API: {}", response);
            return response;
            
        } catch (RestClientResponseException e) {
            log.error("Gemini API error: {}", e.getMessage());
            log.error("Response body: {}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return null;
        }
    }
}