package com.taskmanager.employee_performance_monitoring.controller;

import com.taskmanager.employee_performance_monitoring.User;
import com.taskmanager.employee_performance_monitoring.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // =========================
    // 🔹 REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // =========================
    // 🔹 LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        Optional<User> existing = userRepository.findByEmail(loginRequest.getEmail());

        if (existing.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User dbUser = existing.get();

        // ✅ Debug logs — check IntelliJ console
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("DB email:    " + dbUser.getEmail());
        System.out.println("DB password: " + dbUser.getPassword());
        System.out.println("DB role:     " + dbUser.getRole());
        System.out.println("Input email:    " + loginRequest.getEmail());
        System.out.println("Input password: " + loginRequest.getPassword());
        System.out.println("Input role:     " + loginRequest.getRole());
        System.out.println("===================");

        if (!dbUser.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        if (!dbUser.getRole().equals(loginRequest.getRole())) {
            return ResponseEntity.status(403).body("Access denied for this role");
        }

        return ResponseEntity.ok(new LoginResponse(
                dbUser.getId(),
                dbUser.getName(),
                dbUser.getRole()
        ));
    }

    // =========================
    // 🔹 LOGIN REQUEST CLASS
    // =========================
    static class LoginRequest {
        private String email;
        private String password;
        private String role;

        public String getEmail() { return email; }
        public String getPassword() { return password; }
        public String getRole() { return role; }
    }

    // =========================
    // 🔹 LOGIN RESPONSE CLASS
    // =========================
    static class LoginResponse {
        public String userId;
        public String name;
        public String role;

        public LoginResponse(String userId, String name, String role) {
            this.userId = userId;
            this.name = name;
            this.role = role;
        }
    }
}