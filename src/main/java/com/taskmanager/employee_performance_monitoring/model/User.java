package com.taskmanager.employee_performance_monitoring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;

    private String name;

    /** Stored lower-cased. Unique across the collection. */
    @Indexed(unique = true)
    private String email;

    /** BCrypt hash — never the raw password, never returned by any endpoint. */
    private String password;

    /** EMPLOYEE or ADMIN. */
    private String role;

    private String department;
}
