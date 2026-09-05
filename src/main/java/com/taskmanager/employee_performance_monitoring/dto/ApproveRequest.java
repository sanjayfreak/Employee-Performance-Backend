package com.taskmanager.employee_performance_monitoring.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Quality is 30% of the score, so approval now requires a rating.
 * Previously nothing ever set it, which capped every score at 70.
 */
public record ApproveRequest(

        @Min(value = 1, message = "Rate the work from 1 to 5")
        @Max(value = 5, message = "Rate the work from 1 to 5")
        int qualityRating
) {}
