package com.taskmanager.employee_performance_monitoring.repository;

import com.taskmanager.employee_performance_monitoring.model.Warning;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WarningRepository extends MongoRepository<Warning, String> {
    List<Warning> findByUserId(String userId);
}