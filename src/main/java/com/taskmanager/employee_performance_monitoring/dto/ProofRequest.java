package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProofRequest(

        @NotBlank(message = "A repository or PR link is required")
        String proofLink,

        @NotBlank(message = "Describe what you did")
        @Size(max = 2000, message = "Description is too long")
        String proofDescription
) {}
