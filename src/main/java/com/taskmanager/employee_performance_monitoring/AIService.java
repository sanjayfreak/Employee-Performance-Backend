package com.taskmanager.employee_performance_monitoring.service;

import org.springframework.stereotype.Service;

@Service
public class AIService {

    public String generateInsight(double completionRate,
                                  double onTimeRate,
                                  double qualityScore) {

        StringBuilder insight = new StringBuilder();

        // 🔍 ANALYSIS
        if (completionRate < 60) {
            insight.append("Low task completion. ");
        }

        if (onTimeRate < 60) {
            insight.append("Frequent delays in task delivery. ");
        }

        if (qualityScore < 60) {
            insight.append("Task quality is below expectations. ");
        }

        // 💡 RECOMMENDATION
        if (qualityScore < 60) {
            insight.append("Recommended: Improve coding standards and practice problem-solving.");
        } else if (onTimeRate < 60) {
            insight.append("Recommended: Learn time management and task prioritization.");
        } else if (completionRate < 60) {
            insight.append("Recommended: Focus on task consistency and discipline.");
        } else {
            insight.append("Performance is good. Keep maintaining consistency.");
        }

        return insight.toString();
    }
}