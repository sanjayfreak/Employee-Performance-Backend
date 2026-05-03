package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // =========================
    // 🔹 ASSIGN TASK
    // =========================
    @PostMapping("/assign")
    public ResponseEntity<Task> assign(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.assignTask(task));
    }

    // =========================
    // 🔹 UPDATE STATUS
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(
            @PathVariable String id,
            @RequestBody Task updatedTask) {
        try {
            return ResponseEntity.ok(taskService.updateTask(id, updatedTask));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
    // ✅ SUBMIT PROOF
    // =========================
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectTask(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(taskService.rejectTask(id, body.get("adminComment")));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================
// 🔹 GET USER TASKS
// =========================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getUserTasks(@PathVariable String userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    // =========================
// ✅ GET SUBMITTED TASKS
// =========================
    @GetMapping("/submitted")
    public ResponseEntity<List<Task>> getSubmittedTasks() {
        return ResponseEntity.ok(taskService.getSubmittedTasks());
    }
}