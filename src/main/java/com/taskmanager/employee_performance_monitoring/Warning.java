package com.taskmanager.employee_performance_monitoring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "warnings")
@Data
public class Warning {

    @Id
    private String id;

    private String userId;
    private int level; // 1 to 4
    private String message;
    private LocalDateTime createdAt;
}