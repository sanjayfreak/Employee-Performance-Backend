package com.taskmanager.employee_performance_monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Document(collection = "tasks")
@Getter
@Setter
public class Task {

    @Id
    private String id;

    private String name;
    private String assignedTo;
    private String description;
    private String dueDate;
    private String status;
    private int qualityRating;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // ✅ New proof fields
    private String proofLink;           // GitHub link
    private String proofDescription;    // What they did
    private String adminComment;        // Admin reject reason
}