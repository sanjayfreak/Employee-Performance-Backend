package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.User;
import com.taskmanager.employee_performance_monitoring.UserRepository;
import com.taskmanager.employee_performance_monitoring.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:5173") // ✅ corrected port
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/employees")
    public List<Map<String, Object>> getAllEmployees() {

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : users) {
            if ("EMPLOYEE".equals(user.getRole())) {

                double score = performanceService.calculatePerformance(user.getId());

                Map<String, Object> emp = new HashMap<>();
                emp.put("id", user.getId());
                emp.put("name", user.getName());
                emp.put("email", user.getEmail());
                emp.put("score", score);

                result.add(emp);
            }
        }

        return result;
    }
}