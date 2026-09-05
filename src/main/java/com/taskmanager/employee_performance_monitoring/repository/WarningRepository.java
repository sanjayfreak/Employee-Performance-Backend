package com.taskmanager.employee_performance_monitoring.repository;

import com.taskmanager.employee_performance_monitoring.model.Warning;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface WarningRepository extends MongoRepository<Warning, String> {

    List<Warning> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Warning> findFirstByUserIdAndResolvedFalseOrderByCreatedAtDesc(String userId);

    List<Warning> findByResolvedFalse();
}
