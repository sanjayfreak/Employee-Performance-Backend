package com.taskmanager.employee_performance_monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        if (user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()
                || user.getRole() == null || user.getRole().isBlank()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        try {
            String email = user.getEmail().trim().toLowerCase();

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("User already exists");
            }

            user.setEmail(email);
            user.setRole(user.getRole().trim().toUpperCase());
            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully");

        } catch (org.springframework.dao.DuplicateKeyException e) {
            return ResponseEntity.badRequest().body("User already exists");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Register failed: " + e.getMessage());
        }
    }

    // =========================
    // 🔹 LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()
                || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Email or password missing");
        }

        try {
            String email = loginRequest.getEmail().trim().toLowerCase();

            Optional<User> existing = userRepository.findFirstByEmail(email);

            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }

            User dbUser = existing.get();

            if (!loginRequest.getPassword().equals(dbUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }

            if (loginRequest.getRole() != null && !loginRequest.getRole().isBlank()
                    && !loginRequest.getRole().trim().equalsIgnoreCase(dbUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied for this role");
            }

            return ResponseEntity.ok(new LoginResponse(
                    dbUser.getId(),
                    dbUser.getName(),
                    dbUser.getRole()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
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

        public void setEmail(String email) { this.email = email; }
        public void setPassword(String password) { this.password = password; }
        public void setRole(String role) { this.role = role; }
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