package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(

        @NotBlank(message = "Tell the employee what needs fixing")
        @Size(max = 2000, message = "Comment is too long")
        String adminComment
) {}
