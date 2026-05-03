package com.taskmanager.employee_performance_monitoring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GrokAIService {

    private final WebClient webClient;
    public GrokAIService(@Value("${ai.api.key}") String apiKey){
        this.webClient = WebClient.builder()
                .baseUrl("https://api.x.ai/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String getAIResponse(String prompt) {

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> request = new HashMap<>();
            request.put("model", "grok-2-latest");
            request.put("messages", List.of(message));

            Map response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) response.get("choices");

            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> msg = (Map<String, Object>) firstChoice.get("message");

            return msg.get("content").toString();

        } catch (Exception e) {
            System.out.println("AI ERROR: " + e.getMessage());
            return "Performance analysis indicates issues with task completion, delays, or quality. Focus on improving consistency, meeting deadlines, and enhancing technical skills.";
        }
    }
}