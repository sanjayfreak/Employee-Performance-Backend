package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PerformanceService performanceService;

    // =========================
    // 🔹 ASSIGN TASK
    // =========================
    public Task assignTask(Task task) {
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    // =========================
    // 🔹 UPDATE TASK STATUS
    // only allows forward movement
    // =========================
    public Task updateTask(String taskId, Task updatedTask) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        String currentStatus = task.getStatus();
        String newStatus = updatedTask.getStatus();

        // ✅ Progressive status lock — only forward movement allowed
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new RuntimeException(
                    "Invalid status transition: " + currentStatus + " → " + newStatus
            );
        }

        task.setStatus(newStatus);

        if (updatedTask.getQualityRating() > 0) {
            task.setQualityRating(updatedTask.getQualityRating());
        }

        Task saved = taskRepository.save(task);
        return saved;
    }

    // =========================
    // ✅ SUBMIT PROOF
    // Employee submits proof before COMPLETED
    // =========================
    public Task submitProof(String taskId, String proofLink, String proofDescription) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new RuntimeException("Task must be IN_PROGRESS to submit proof");
        }

        if (proofLink == null || proofLink.isBlank()) {
            throw new RuntimeException("GitHub link is required");
        }

        if (proofDescription == null || proofDescription.isBlank()) {
            throw new RuntimeException("Description is required");
        }

        task.setProofLink(proofLink);
        task.setProofDescription(proofDescription);
        task.setStatus("SUBMITTED");  // ✅ waiting for admin approval

        return taskRepository.save(task);
    }

    // =========================
    // ✅ ADMIN APPROVE
    // =========================
    public Task approveTask(String taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new RuntimeException("Task must be SUBMITTED to approve");
        }

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        // ✅ Recalculate performance after approval
        performanceService.calculatePerformance(task.getAssignedTo());

        return saved;
    }

    // =========================
    // ✅ ADMIN REJECT
    // =========================
    public Task rejectTask(String taskId, String adminComment) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new RuntimeException("Task must be SUBMITTED to reject");
        }

        task.setStatus("IN_PROGRESS");  // ✅ goes back to IN_PROGRESS
        task.setAdminComment(adminComment);
        task.setProofLink(null);         // ✅ clear old proof
        task.setProofDescription(null);

        return taskRepository.save(task);
    }

    // =========================
    // ✅ VALID TRANSITIONS
    // =========================
    private boolean isValidTransition(String current, String next) {
        if (current == null) return false;
        switch (current) {
            case "PENDING":     return "IN_PROGRESS".equals(next);
            case "IN_PROGRESS": return "IN_PROGRESS".equals(next); // status update only
            case "SUBMITTED":   return false; // only admin can change
            case "COMPLETED":   return false; // locked
            default:            return false;
        }
    }

    // =========================
    // 🔹 GET TASKS BY USER
    // =========================
    public List<Task> getTasksByUser(String userId) {
        return taskRepository.findByAssignedTo(userId);
    }

    // =========================
    // 🔹 GET SUBMITTED TASKS (for admin)
    // =========================
    public List<Task> getSubmittedTasks() {
        return taskRepository.findByStatus("SUBMITTED");
    }
}