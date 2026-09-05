package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.dto.AIResponse;
import com.taskmanager.employee_performance_monitoring.dto.DashboardResponse;
import com.taskmanager.employee_performance_monitoring.dto.TaskResponse;
import com.taskmanager.employee_performance_monitoring.model.PerformanceLog;
import com.taskmanager.employee_performance_monitoring.service.PerformanceService;
import com.taskmanager.employee_performance_monitoring.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All read-only. Nothing here writes to the database any more — the old
 * dashboard endpoint saved a performance log and a warning on every call.
 */
@RestController
@RequestMapping("/performance")
public class PerformanceController {

    private final PerformanceService performanceService;
    private final TaskService taskService;

    public PerformanceController(PerformanceService performanceService, TaskService taskService) {
        this.performanceService = performanceService;
        this.taskService = taskService;
    }

    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardResponse> dashboard(@PathVariable String userId, Authentication auth) {
        TaskController.requireSelfOrAdmin(userId, auth);

        var score = performanceService.computeScore(userId);
        List<TaskResponse> tasks = taskService.forUser(userId).stream()
                .map(TaskResponse::from).toList();

        return ResponseEntity.ok(new DashboardResponse(
                score.value(),
                performanceService.getTrend(userId),
                score.qualityRated(),
                tasks,
                performanceService.getAIInsight(userId)));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<PerformanceLog>> history(@PathVariable String userId, Authentication auth) {
        TaskController.requireSelfOrAdmin(userId, auth);
        return ResponseEntity.ok(performanceService.getHistory(userId));
    }

    @GetMapping("/insight/{userId}")
    public ResponseEntity<AIResponse> insight(@PathVariable String userId, Authentication auth) {
        TaskController.requireSelfOrAdmin(userId, auth);
        return ResponseEntity.ok(performanceService.getAIInsight(userId));
    }

    @GetMapping("/trend/{userId}")
    public ResponseEntity<String> trend(@PathVariable String userId, Authentication auth) {
        TaskController.requireSelfOrAdmin(userId, auth);
        return ResponseEntity.ok(performanceService.getTrend(userId));
    }
}
