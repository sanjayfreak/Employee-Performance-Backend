package com.taskmanager.employee_performance_monitoring.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.taskmanager.employee_performance_monitoring.model.Employee;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
}