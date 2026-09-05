package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.dto.UserSummary;
import com.taskmanager.employee_performance_monitoring.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Locked to ROLE_ADMIN in SecurityConfig. */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<UserSummary>> employees() {
        return ResponseEntity.ok(userService.employeeSummaries());
    }
}
