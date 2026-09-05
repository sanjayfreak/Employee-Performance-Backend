package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.dto.WarningResponse;
import com.taskmanager.employee_performance_monitoring.service.WarningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warnings")
public class WarningController {

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<WarningResponse>> all() {
        return ResponseEntity.ok(warningService.allOpen().stream().map(WarningResponse::from).toList());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<WarningResponse>> forUser(@PathVariable String userId, Authentication auth) {
        TaskController.requireSelfOrAdmin(userId, auth);
        return ResponseEntity.ok(warningService.forUser(userId).stream().map(WarningResponse::from).toList());
    }
}
