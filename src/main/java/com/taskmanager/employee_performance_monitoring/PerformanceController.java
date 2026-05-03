package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.model.PerformanceLog;
import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.repository.PerformanceLogRepository;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;
import com.taskmanager.employee_performance_monitoring.dto.AIResponse;
import com.taskmanager.employee_performance_monitoring.service.PerformanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/performance")
@CrossOrigin(origins = "http://localhost:5173")
public class PerformanceController {

    @Autowired
    private PerformanceLogRepository repository;

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private TaskRepository taskRepository;

    // =========================
    // 🔹 GET LOGS
    // =========================
    @GetMapping("/{userId}")
    public List<PerformanceLog> getLogs(@PathVariable String userId) {
        return repository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    // =========================
    // 🔹 AI INSIGHT
    // =========================
    @GetMapping("/insight/{userId}")
    public AIResponse getAIInsight(@PathVariable String userId) {
        return performanceService.getAIInsight(userId);
    }

    // =========================
    // 🔹 TREND
    // =========================
    @GetMapping("/trend/{userId}")
    public String getTrend(@PathVariable String userId) {
        return performanceService.getTrend(userId);
    }
    @GetMapping("/history/{userId}")
    public List<PerformanceLog> getHistory(@PathVariable String userId) {
        return repository.findByUserIdOrderByCreatedAtAsc(userId);
    }
    // =========================
    // 🔥 DASHBOARD (MAIN API)
    // =========================
    @GetMapping("/dashboard/{userId}")
    public Map<String, Object> getDashboard(@PathVariable String userId) {

        double score = performanceService.calculatePerformance(userId);
        String trend = performanceService.getTrend(userId);
        AIResponse ai = performanceService.getAIInsight(userId);
        List<Task> tasks = taskRepository.findByAssignedTo(userId);

        Map<String, Object> response = new HashMap<>();

        response.put("score", score);
        response.put("trend", trend);
        response.put("tasks", tasks);
        response.put("aiInsight", ai);

        return response;
    }
}