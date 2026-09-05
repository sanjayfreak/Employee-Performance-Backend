package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.model.Warning;
import com.taskmanager.employee_performance_monitoring.repository.WarningRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Keeps at most one open warning per user.
 *
 * The old version inserted a new row every time a score was calculated,
 * which happened on every dashboard load — so a user could collect dozens
 * of identical "termination recommended" warnings just by refreshing.
 */
@Service
public class WarningService {

    private final WarningRepository warningRepository;

    public WarningService(WarningRepository warningRepository) {
        this.warningRepository = warningRepository;
    }

    public void evaluate(String userId, double score, boolean scoreIsMeaningful) {

        // Too little data to judge anyone on.
        if (!scoreIsMeaningful) return;

        int level = levelFor(score);
        Optional<Warning> open = warningRepository
                .findFirstByUserIdAndResolvedFalseOrderByCreatedAtDesc(userId);

        if (level == 0) {
            open.ifPresent(this::resolve);
            return;
        }

        if (open.isPresent() && open.get().getLevel() == level) {
            return; // already flagged at this level, nothing new to say
        }

        open.ifPresent(this::resolve);

        Warning warning = new Warning();
        warning.setUserId(userId);
        warning.setLevel(level);
        warning.setMessage(messageFor(level));
        warning.setResolved(false);
        warning.setCreatedAt(LocalDateTime.now());
        warningRepository.save(warning);
    }

    private void resolve(Warning w) {
        w.setResolved(true);
        warningRepository.save(w);
    }

    public List<Warning> forUser(String userId) {
        return warningRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Warning> allOpen() {
        return warningRepository.findByResolvedFalse();
    }

    private int levelFor(double score) {
        if (score < 20) return 4;
        if (score < 40) return 3;
        if (score < 60) return 2;
        if (score < 80) return 1;
        return 0;
    }

    private String messageFor(int level) {
        return switch (level) {
            case 4 -> "Critical: performance requires immediate review with your manager";
            case 3 -> "Serious: a formal performance discussion is recommended";
            case 2 -> "Below standard: upskilling recommended";
            default -> "Slightly below expectations: worth a check-in";
        };
    }
}
