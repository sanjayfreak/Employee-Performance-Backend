package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.dto.AIResponse;
import com.taskmanager.employee_performance_monitoring.model.PerformanceLog;
import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.repository.PerformanceLogRepository;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PerformanceService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);

    private static final double W_COMPLETION = 0.4;
    private static final double W_ON_TIME    = 0.3;
    private static final double W_QUALITY    = 0.3;

    private final PerformanceLogRepository performanceLogRepository;
    private final TaskRepository taskRepository;
    private final WarningService warningService;
    private final AIService aiService;
    private final AiClient aiClient;

    public PerformanceService(PerformanceLogRepository performanceLogRepository,
                              TaskRepository taskRepository,
                              WarningService warningService,
                              AIService aiService,
                              AiClient aiClient) {
        this.performanceLogRepository = performanceLogRepository;
        this.taskRepository = taskRepository;
        this.warningService = warningService;
        this.aiService = aiService;
        this.aiClient = aiClient;
    }

    /** Result of a calculation. Nothing here touches the database. */
    public record Score(double value,
                        double completionRate,
                        double onTimeRate,
                        double qualityScore,
                        boolean qualityRated,
                        boolean meaningful) {}

    // =========================================================
    // READ ONLY — safe to call from a GET
    // =========================================================

    /**
     * Only components with real data are counted, and the weights are
     * renormalised over them.
     *
     * The previous version divided on-time by *total* tasks (so anyone with
     * pending work scored 0% on time) and treated an unrated task as quality
     * zero (so 30% of every score was permanently lost, capping everyone at 70).
     */
    public Score computeScore(String userId) {

        List<Task> tasks = taskRepository.findByAssignedTo(userId);

        if (tasks.isEmpty()) {
            return new Score(0, 0, 0, 0, false, false);
        }

        long total = tasks.size();
        List<Task> completed = tasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .toList();

        double completionRate = (double) completed.size() / total * 100;

        long onTime = completed.stream().filter(this::wasOnTime).count();
        double onTimeRate = completed.isEmpty()
                ? 0
                : (double) onTime / completed.size() * 100;

        List<Task> rated = tasks.stream().filter(t -> t.getQualityRating() > 0).toList();
        boolean qualityRated = !rated.isEmpty();
        double qualityScore = qualityRated
                ? rated.stream().mapToInt(Task::getQualityRating).average().orElse(0) * 20
                : 0;

        double weighted = completionRate * W_COMPLETION;
        double weightSum = W_COMPLETION;

        if (!completed.isEmpty()) {
            weighted += onTimeRate * W_ON_TIME;
            weightSum += W_ON_TIME;
        }
        if (qualityRated) {
            weighted += qualityScore * W_QUALITY;
            weightSum += W_QUALITY;
        }

        double value = weightSum == 0 ? 0 : weighted / weightSum;

        return new Score(round(value), round(completionRate), round(onTimeRate),
                round(qualityScore), qualityRated, true);
    }

    public String getTrend(String userId) {
        List<PerformanceLog> logs = performanceLogRepository.findByUserIdOrderByCreatedAtAsc(userId);
        if (logs.size() < 2) return "Not enough data";

        double last = logs.get(logs.size() - 1).getScore();
        double prev = logs.get(logs.size() - 2).getScore();

        if (last > prev) return "Improving";
        if (last < prev) return "Declining";
        return "Stable";
    }

    public List<PerformanceLog> getHistory(String userId) {
        return performanceLogRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }

    // =========================================================
    // WRITE — only called when something actually changed
    // =========================================================

    /**
     * Saves a score snapshot and re-evaluates warnings.
     *
     * Call this from task state changes only. It used to run on every
     * dashboard GET, which filled the history chart with duplicate points
     * and generated a fresh warning row on each page refresh.
     */
    public Score recordSnapshot(String userId) {

        Score score = computeScore(userId);

        PerformanceLog entry = new PerformanceLog();
        entry.setUserId(userId);
        entry.setScore(score.value());
        entry.setCompletionRate(score.completionRate());
        entry.setOnTimeRate(score.onTimeRate());
        entry.setQualityScore(score.qualityScore());
        entry.setQualityRated(score.qualityRated());
        entry.setCreatedAt(LocalDateTime.now());
        performanceLogRepository.save(entry);

        warningService.evaluate(userId, score.value(), score.meaningful());

        log.info("Recorded score {} for user {}", score.value(), userId);
        return score;
    }

    // =========================================================
    // AI
    // =========================================================

    public AIResponse getAIInsight(String userId) {

        Score score = computeScore(userId);
        if (!score.meaningful()) return AIResponse.empty();

        String trend = getTrend(userId);
        String insight;

        String prompt = """
                You are reviewing one employee's work performance.

                Completion rate: %.0f%% of assigned tasks are finished
                On-time rate: %.0f%% of finished tasks met their due date
                Quality: %s
                Trend versus last review: %s

                In 3 to 4 sentences, explain plainly what is going well, what is not,
                and the single most useful thing to work on next. Be specific and
                practical. Do not invent numbers that were not given.
                """.formatted(
                        score.completionRate(),
                        score.onTimeRate(),
                        score.qualityRated()
                                ? String.format("%.0f%% average from reviewed work", score.qualityScore())
                                : "no approved task has been rated yet",
                        trend);

        try {
            insight = aiClient.complete(prompt);
        } catch (Exception e) {
            // Genuine fallback — the old client swallowed its own errors,
            // so this branch could never run.
            log.warn("AI unavailable ({}), using rule-based analysis", e.getMessage());
            insight = aiService.generateInsight(
                    score.completionRate(), score.onTimeRate(),
                    score.qualityScore(), score.qualityRated(), trend);
        }

        return new AIResponse(insight, suggestCourse(score));
    }

    private String suggestCourse(Score score) {
        if (score.qualityRated() && score.qualityScore() < 60) {
            return "Code quality and problem solving practice";
        }
        if (score.onTimeRate() < 60 && score.completionRate() > 0) {
            return "Time estimation and agile planning";
        }
        if (score.completionRate() < 60) {
            return "Task prioritisation and focus";
        }
        return "Maintain current performance";
    }

    // =========================================================

    private boolean wasOnTime(Task task) {
        if (task.getCompletedAt() == null || task.getDueDate() == null) return false;
        return !task.getCompletedAt().toLocalDate().isAfter(task.getDueDate());
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
