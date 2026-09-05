package com.taskmanager.employee_performance_monitoring.repository;

import com.taskmanager.employee_performance_monitoring.model.PerformanceLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceLogRepository extends MongoRepository<PerformanceLog, String> {

    List<PerformanceLog> findByUserIdOrderByCreatedAtAsc(String userId);

    Optional<PerformanceLog> findFirstByUserIdOrderByCreatedAtDesc(String userId);
}
