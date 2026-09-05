package com.taskmanager.employee_performance_monitoring.service;

import com.taskmanager.employee_performance_monitoring.config.JwtService;
import com.taskmanager.employee_performance_monitoring.dto.AuthResponse;
import com.taskmanager.employee_performance_monitoring.dto.LoginRequest;
import com.taskmanager.employee_performance_monitoring.dto.RegisterRequest;
import com.taskmanager.employee_performance_monitoring.dto.UserSummary;
import com.taskmanager.employee_performance_monitoring.exception.BadRequestException;
import com.taskmanager.employee_performance_monitoring.exception.ForbiddenException;
import com.taskmanager.employee_performance_monitoring.exception.NotFoundException;
import com.taskmanager.employee_performance_monitoring.model.User;
import com.taskmanager.employee_performance_monitoring.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PerformanceService performanceService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PerformanceService performanceService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.performanceService = performanceService;
    }

    public void register(RegisterRequest request) {

        String email = normalise(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with that email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role().trim().toUpperCase());
        user.setDepartment(request.department());

        try {
            userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw new BadRequestException("An account with that email already exists");
        }
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findFirstByEmail(normalise(request.email()))
                .orElseThrow(() -> new ForbiddenException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ForbiddenException("Invalid email or password");
        }

        if (request.role() != null && !request.role().isBlank()
                && !request.role().trim().equalsIgnoreCase(user.getRole())) {
            throw new ForbiddenException("This account is not registered as "
                    + request.role().trim().toLowerCase());
        }

        String token = jwtService.issue(user.getId(), user.getRole(), user.getName());

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    /** Admin employee table. Scores are computed, never written, here. */
    public List<UserSummary> employeeSummaries() {
        return userRepository.findByRole("EMPLOYEE").stream()
                .map(u -> {
                    var score = performanceService.computeScore(u.getId());
                    return new UserSummary(u.getId(), u.getName(), u.getEmail(),
                            u.getDepartment(), score.value(), score.qualityRated());
                })
                .sorted(Comparator.comparingDouble(UserSummary::score).reversed())
                .toList();
    }

    public User requireById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
