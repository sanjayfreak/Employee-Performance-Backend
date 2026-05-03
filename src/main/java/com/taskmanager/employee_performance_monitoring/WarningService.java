package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.model.Warning;
import com.taskmanager.employee_performance_monitoring.repository.WarningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WarningService {

    @Autowired
    private WarningRepository warningRepository;

    public void evaluateWarning(String userId, double score) {

        Warning warning = new Warning();
        warning.setUserId(userId);
        warning.setCreatedAt(LocalDateTime.now());

        if (score < 20) {
            warning.setLevel(4);
            warning.setMessage("Critical: Termination recommended");
        } else if (score < 40) {
            warning.setLevel(3);
            warning.setMessage("Meet HR immediately");
        } else if (score < 60) {
            warning.setLevel(2);
            warning.setMessage("Upskill required - course recommended");
        } else if (score < 80) {
            warning.setLevel(1);
            warning.setMessage("Performance below expectations");
        } else {
            return; // no warning
        }

        warningRepository.save(warning);

        System.out.println("WARNING GENERATED: Level " + warning.getLevel());
    }
}