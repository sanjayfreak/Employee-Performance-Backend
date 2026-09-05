package com.taskmanager.employee_performance_monitoring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Calls an OpenAI-compatible chat API (Groq by default).
 *
 * Exceptions are deliberately NOT swallowed here — the caller falls back to
 * the rule-based analysis. The old version caught everything and returned a
 * fixed sentence, so the fallback never ran and the "AI" was always canned.
 */
@Service
public class AiClient {

    private final WebClient webClient;
    private final String model;
    private final boolean enabled;

    public AiClient(@Value("${ai.api.key:}") String apiKey,
                    @Value("${ai.base-url:https://api.groq.com/openai/v1/chat/completions}") String baseUrl,
                    @Value("${ai.model:llama-3.3-70b-versatile}") String model) {

        this.model = model;
        this.enabled = apiKey != null && !apiKey.isBlank() && !"test123".equals(apiKey);

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unchecked")
    public String complete(String prompt) {
        if (!enabled) {
            throw new IllegalStateException("AI key not configured");
        }

        Map<String, Object> request = Map.of(
                "model", model,
                "temperature", 0.3,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Object> response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(20))
                .block();

        if (response == null) {
            throw new IllegalStateException("Empty AI response");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("AI returned no choices");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = message == null ? null : String.valueOf(message.get("content"));

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI returned empty content");
        }
        return content.trim();
    }
}
