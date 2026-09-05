package com.taskmanager.employee_performance_monitoring.service;

import org.springframework.stereotype.Service;

/**
 * Deterministic analysis used when the AI provider is unavailable.
 * Always produces something honest rather than a fixed sentence.
 */
@Service
public class AIService {

    public String generateInsight(double completionRate,
                                  double onTimeRate,
                                  double qualityScore,
                                  boolean qualityRated,
                                  String trend) {

        StringBuilder s = new StringBuilder();

        if (completionRate < 60) {
            s.append(String.format("Task completion is low at %.0f%%. ", completionRate));
        } else {
            s.append(String.format("Completion is healthy at %.0f%%. ", completionRate));
        }

        if (onTimeRate < 60) {
            s.append(String.format("Only %.0f%% of finished work landed on time. ", onTimeRate));
        }

        if (!qualityRated) {
            s.append("No approved task has been rated yet, so quality is not part of this score. ");
        } else if (qualityScore < 60) {
            s.append(String.format("Reviewed quality averages %.0f%%, below expectations. ", qualityScore));
        }

        if ("Declining".equals(trend)) {
            s.append("The trend is downward compared with the previous review. ");
        } else if ("Improving".equals(trend)) {
            s.append("The trend is upward compared with the previous review. ");
        }

        if (qualityRated && qualityScore < 60) {
            s.append("Suggested focus: raise work quality before taking on more tasks.");
        } else if (onTimeRate < 60) {
            s.append("Suggested focus: estimation and deadline management.");
        } else if (completionRate < 60) {
            s.append("Suggested focus: finishing what is already assigned before new work starts.");
        } else {
            s.append("Performance is solid. Keep the current pace.");
        }

        return s.toString();
    }
}
