package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.model.Warning;
import com.taskmanager.employee_performance_monitoring.repository.WarningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warnings")
public class WarningController {

    @Autowired
    private WarningRepository warningRepository;

    @GetMapping("/{userId}")
    public List<Warning> getUserWarnings(@PathVariable String userId) {
        return warningRepository.findByUserId(userId);
    }

    @GetMapping("/all")
    public List<Warning> getAllWarnings() {
        return warningRepository.findAll();
    }
}