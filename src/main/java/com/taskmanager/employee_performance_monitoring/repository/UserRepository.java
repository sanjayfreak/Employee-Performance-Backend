package com.taskmanager.employee_performance_monitoring.repository;

import com.taskmanager.employee_performance_monitoring.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    /** findFirst rather than findBy so legacy duplicate rows can't blow up. */
    Optional<User> findFirstByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(String role);
}
