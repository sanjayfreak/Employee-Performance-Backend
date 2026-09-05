package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.dto.*;
import com.taskmanager.employee_performance_monitoring.exception.ForbiddenException;
import com.taskmanager.employee_performance_monitoring.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ---------- admin ----------

    @PostMapping("/assign")
    public ResponseEntity<TaskResponse> assign(@Valid @RequestBody AssignTaskRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.assign(request)));
    }

    @GetMapping("/submitted")
    public ResponseEntity<List<TaskResponse>> submitted() {
        return ResponseEntity.ok(taskService.submitted().stream().map(TaskResponse::from).toList());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TaskResponse> approve(@PathVariable String id,
                                                @Valid @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.approve(id, request.qualityRating())));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TaskResponse> reject(@PathVariable String id,
                                               @Valid @RequestBody RejectRequest request) {
        return ResponseEntity.ok(TaskResponse.from(taskService.reject(id, request.adminComment())));
    }

    // ---------- employee ----------

    @PostMapping("/{id}/start")
    public ResponseEntity<TaskResponse> start(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(TaskResponse.from(taskService.start(id, auth.getName())));
    }

    @PostMapping("/{id}/submit-proof")
    public ResponseEntity<TaskResponse> submitProof(@PathVariable String id,
                                                    @Valid @RequestBody ProofRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(TaskResponse.from(taskService.submitProof(id, auth.getName(), request)));
    }

    /** A user may only list their own tasks; an admin may list anyone's. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> forUser(@PathVariable String userId, Authentication auth) {
        requireSelfOrAdmin(userId, auth);
        return ResponseEntity.ok(taskService.forUser(userId).stream().map(TaskResponse::from).toList());
    }

    static void requireSelfOrAdmin(String userId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !auth.getName().equals(userId)) {
            throw new ForbiddenException("You can only view your own data");
        }
    }
}
