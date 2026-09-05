package com.taskmanager.employee_performance_monitoring.dto;

import java.util.List;

public record DashboardResponse(
        double score,
        String trend,
        boolean qualityRated,
        List<TaskResponse> tasks,
        AIResponse aiInsight
) {}
