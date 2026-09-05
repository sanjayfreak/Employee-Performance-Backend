package com.taskmanager.employee_performance_monitoring.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "tasks")
@Getter
@Setter
public class Task {

    @Id
    private String id;

    private String name;
    private String description;

    @Indexed
    private String assignedTo;

    /** Real date type — no more string parsing guesswork. */
    private LocalDate dueDate;

    /** PENDING, IN_PROGRESS, SUBMITTED, COMPLETED. */
    @Indexed
    private String status;

    /** 1-5, set by the admin when approving. 0 means not rated. */
    private int qualityRating;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private String proofLink;
    private String proofDescription;

    /** Kept after a rejection so the employee can see what to fix. */
    private String adminComment;
}
