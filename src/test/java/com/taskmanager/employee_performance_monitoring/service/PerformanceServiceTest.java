package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.repository.PerformanceLogRepository;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for the scoring rules — no Spring context, no database,
 * so they run anywhere including CI.
 */
class PerformanceServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final PerformanceLogRepository logRepository = mock(PerformanceLogRepository.class);
    private final WarningService warningService = mock(WarningService.class);
    private final AiClient aiClient = mock(AiClient.class);

    private final PerformanceService service = new PerformanceService(
            logRepository, taskRepository, warningService, new AIService(), aiClient);

    private Task task(String status, int rating, LocalDate due, LocalDateTime completed) {
        Task t = new Task();
        t.setAssignedTo("u1");
        t.setStatus(status);
        t.setQualityRating(rating);
        t.setDueDate(due);
        t.setCompletedAt(completed);
        return t;
    }

    @Test
    void noTasksMeansNoMeaningfulScore() {
        when(taskRepository.findByAssignedTo(anyString())).thenReturn(List.of());

        var score = service.computeScore("u1");

        assertEquals(0, score.value());
        assertFalse(score.meaningful());
        assertFalse(score.qualityRated());
    }

    @Test
    void unratedWorkDoesNotDragTheScoreDown() {
        // Everything finished on time, nothing rated yet.
        when(taskRepository.findByAssignedTo(anyString())).thenReturn(List.of(
                task("COMPLETED", 0, LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1, 9, 12, 0)),
                task("COMPLETED", 0, LocalDate.of(2026, 1, 12), LocalDateTime.of(2026, 1, 11, 12, 0))
        ));

        var score = service.computeScore("u1");

        // Old behaviour treated unrated as quality 0 and capped this at 70.
        assertEquals(100.0, score.value(), 0.01);
        assertFalse(score.qualityRated());
    }

    @Test
    void pendingWorkDoesNotCountAsLate() {
        when(taskRepository.findByAssignedTo(anyString())).thenReturn(List.of(
                task("COMPLETED", 5, LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1, 9, 12, 0)),
                task("PENDING", 0, LocalDate.of(2026, 2, 10), null)
        ));

        var score = service.computeScore("u1");

        // One of two done, and the finished one was on time.
        assertEquals(50.0, score.completionRate(), 0.01);
        assertEquals(100.0, score.onTimeRate(), 0.01);
        assertTrue(score.qualityRated());
    }

    @Test
    void lateWorkLowersTheOnTimeRate() {
        when(taskRepository.findByAssignedTo(anyString())).thenReturn(List.of(
                task("COMPLETED", 4, LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1, 15, 12, 0))
        ));

        var score = service.computeScore("u1");

        assertEquals(100.0, score.completionRate(), 0.01);
        assertEquals(0.0, score.onTimeRate(), 0.01);
    }

    @Test
    void readingAScoreNeverWritesToTheDatabase() {
        when(taskRepository.findByAssignedTo(anyString())).thenReturn(List.of(
                task("COMPLETED", 3, LocalDate.of(2026, 1, 10), LocalDateTime.of(2026, 1, 9, 12, 0))
        ));

        service.computeScore("u1");

        verifyNoInteractions(logRepository);
        verifyNoInteractions(warningService);
    }
}
