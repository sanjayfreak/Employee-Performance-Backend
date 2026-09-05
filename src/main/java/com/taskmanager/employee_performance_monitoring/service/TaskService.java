package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.dto.AssignTaskRequest;
import com.taskmanager.employee_performance_monitoring.dto.ProofRequest;
import com.taskmanager.employee_performance_monitoring.exception.BadRequestException;
import com.taskmanager.employee_performance_monitoring.exception.ForbiddenException;
import com.taskmanager.employee_performance_monitoring.exception.NotFoundException;
import com.taskmanager.employee_performance_monitoring.model.Task;
import com.taskmanager.employee_performance_monitoring.model.User;
import com.taskmanager.employee_performance_monitoring.repository.TaskRepository;
import com.taskmanager.employee_performance_monitoring.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PerformanceService performanceService;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       PerformanceService performanceService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.performanceService = performanceService;
    }

    // =========================================================

    public Task assign(AssignTaskRequest request) {

        User assignee = userRepository.findById(request.assignedTo())
                .orElseThrow(() -> new NotFoundException("That employee does not exist"));

        if (!"EMPLOYEE".equalsIgnoreCase(assignee.getRole())) {
            throw new BadRequestException("Tasks can only be assigned to employees");
        }

        Task task = new Task();
        task.setName(request.name().trim());
        task.setDescription(request.description());
        task.setAssignedTo(request.assignedTo());
        task.setDueDate(request.dueDate());
        task.setStatus("PENDING");
        task.setCreatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        // A new task changes the completion rate, so snapshot it.
        performanceService.recordSnapshot(saved.getAssignedTo());
        return saved;
    }

    public Task start(String taskId, String userId) {

        Task task = ownedTask(taskId, userId);

        if (!"PENDING".equals(task.getStatus())) {
            throw new BadRequestException("Only a not-started task can be started");
        }

        task.setStatus("IN_PROGRESS");
        return taskRepository.save(task);
    }

    public Task submitProof(String taskId, String userId, ProofRequest request) {

        Task task = ownedTask(taskId, userId);

        if (!"IN_PROGRESS".equals(task.getStatus())) {
            throw new BadRequestException("The task must be in progress before you submit proof");
        }

        task.setProofLink(request.proofLink().trim());
        task.setProofDescription(request.proofDescription().trim());
        task.setStatus("SUBMITTED");
        return taskRepository.save(task);
    }

    // =========================================================
    // Admin actions
    // =========================================================

    /**
     * Approval now requires a 1-5 quality rating. Nothing used to set this
     * field, so quality stayed at 0 for everyone and capped every score at 70.
     */
    public Task approve(String taskId, int qualityRating) {

        Task task = submittedTask(taskId);

        task.setStatus("COMPLETED");
        task.setQualityRating(qualityRating);
        task.setCompletedAt(LocalDateTime.now());
        task.setAdminComment(null);

        Task saved = taskRepository.save(task);
        performanceService.recordSnapshot(saved.getAssignedTo());
        return saved;
    }

    /**
     * The proof is kept on rejection. The old version nulled it out, so the
     * employee could not see what they had submitted or what to fix.
     */
    public Task reject(String taskId, String adminComment) {

        Task task = submittedTask(taskId);

        task.setStatus("IN_PROGRESS");
        task.setAdminComment(adminComment.trim());

        Task saved = taskRepository.save(task);
        performanceService.recordSnapshot(saved.getAssignedTo());
        return saved;
    }

    // =========================================================

    public List<Task> forUser(String userId) {
        return taskRepository.findByAssignedTo(userId);
    }

    public List<Task> submitted() {
        return taskRepository.findByStatus("SUBMITTED");
    }

    // =========================================================

    private Task ownedTask(String taskId, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!task.getAssignedTo().equals(userId)) {
            throw new ForbiddenException("That task is not assigned to you");
        }
        return task;
    }

    private Task submittedTask(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!"SUBMITTED".equals(task.getStatus())) {
            throw new BadRequestException("Only a submitted task can be reviewed");
        }
        return task;
    }
}
