package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.model.PerformanceLog;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;
import com.taskmanager.employee_performance_monitoring.repository.PerformanceLogRepository;
import com.taskmanager.employee_performance_monitoring.dto.AIResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PerformanceService {

    @Autowired
    private PerformanceLogRepository performanceLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WarningService warningService;

    @Autowired
    private AIService aiService;

    @Autowired
    private GrokAIService grokAIService;

    // =========================
    // 🔹 CALCULATE PERFORMANCE
    // =========================
    public double calculatePerformance(String userId) {

        List<Task> tasks = taskRepository.findByAssignedTo(userId);

        if (tasks.isEmpty()) return 0;

        long total = tasks.size();

        long completed = tasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        long onTime = tasks.stream()
                .filter(t -> {
                    if (!"COMPLETED".equals(t.getStatus())) return false;
                    if (t.getCompletedAt() == null) return false;
                    if (t.getDueDate() == null) return false;
                    try {
                        // ✅ Parse String dueDate to LocalDateTime
                        LocalDateTime due = LocalDate.parse(t.getDueDate()).atStartOfDay();
                        return !t.getCompletedAt().isAfter(due);
                    } catch (Exception e) {
                        return false; // ✅ skip if date format is wrong
                    }
                })
                .count();

        double completionRate = (double) completed / total * 100;
        double onTimeRate = (double) onTime / total * 100;

        double qualityAvg = tasks.stream()
                .filter(t -> t.getQualityRating() > 0)
                .mapToInt(Task::getQualityRating)
                .average()
                .orElse(0) * 20;

        double finalScore =
                (completionRate * 0.4) +
                        (onTimeRate * 0.3) +
                        (qualityAvg * 0.3);

        System.out.println("Performance Score for " + userId + ": " + finalScore);

        // Warning system
        warningService.evaluateWarning(userId, finalScore);

        // Save performance log
        PerformanceLog log = new PerformanceLog();
        log.setUserId(userId);
        log.setScore(finalScore);
        log.setCompletionRate(completionRate);
        log.setOnTimeRate(onTimeRate);
        log.setQualityScore(qualityAvg);
        log.setCreatedAt(LocalDateTime.now());

        performanceLogRepository.save(log);

        return finalScore;
    }

    // =========================
    // 🔹 TREND
    // =========================
    public String getTrend(String userId) {

        List<PerformanceLog> logs =
                performanceLogRepository.findByUserIdOrderByCreatedAtAsc(userId);

        if (logs.size() < 2) return "Not enough data";

        double last = logs.get(logs.size() - 1).getScore();
        double prev = logs.get(logs.size() - 2).getScore();

        if (last > prev) return "Improving";
        else if (last < prev) return "Declining";
        else return "Stable";
    }

    // =========================
    // 🔹 AI INSIGHT
    // =========================
    public AIResponse getAIInsight(String userId) {

        List<PerformanceLog> logs =
                performanceLogRepository.findByUserIdOrderByCreatedAtAsc(userId);

        if (logs.isEmpty()) return new AIResponse(); // ✅ return empty instead of null

        PerformanceLog latest = logs.get(logs.size() - 1);

        String prompt = "Analyze employee performance:\n" +
                "Completion Rate: " + latest.getCompletionRate() + "%\n" +
                "On-Time Rate: " + latest.getOnTimeRate() + "%\n" +
                "Quality Score: " + latest.getQualityScore() + "%\n" +
                "Trend: " + getTrend(userId) + "\n\n" +
                "Explain issues and suggest improvements and courses.";

        String insight;

        try {
            insight = grokAIService.getAIResponse(prompt);
        } catch (Exception e) {
            insight = aiService.generateInsight(
                    latest.getCompletionRate(),
                    latest.getOnTimeRate(),
                    latest.getQualityScore()
            );
        }

        AIResponse response = new AIResponse();
        response.setInsight(insight);

        if (latest.getQualityScore() < 60) {
            response.setCourse("DSA / Problem Solving (LeetCode)");
        } else if (latest.getOnTimeRate() < 60) {
            response.setCourse("Time Management & Agile");
        } else {
            response.setCourse("Maintain current performance");
        }

        return response;
    }
}