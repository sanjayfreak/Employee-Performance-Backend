package com.taskmanager.employee_performance_monitoring;
import com.taskmanager.employee_performance_monitoring.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}