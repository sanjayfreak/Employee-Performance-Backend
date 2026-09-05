package com.taskmanager.employee_performance_monitoring.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "warnings")
@Data
public class Warning {

    @Id
    private String id;

    @Indexed
    private String userId;

    private int level;
    private String message;

    /** Set when the score recovers, so old warnings stop showing. */
    private boolean resolved;

    private LocalDateTime createdAt;
}
