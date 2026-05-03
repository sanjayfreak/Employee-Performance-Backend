package com.taskmanager.employee_performance_monitoring.repository;

import com.taskmanager.employee_performance_monitoring.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByAssignedTo(String userId);
    List<Task> findByStatus(String status);
}