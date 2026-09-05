package com.taskmanager.employee_performance_monitoring.dto;

/** insight = the written analysis, course = a suggested next step. */
public record AIResponse(String insight, String course) {

    public static AIResponse empty() {
        return new AIResponse(null, null);
    }
}
