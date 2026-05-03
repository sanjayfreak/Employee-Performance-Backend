

import com.taskmanager.employee_performance_monitoring.User;
import com.taskmanager.employee_performance_monitoring.UserRepository;
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

        // 🔴 Basic validation
        if (user.getEmail() == null || user.getPassword() == null || user.getRole() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

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

        // 🔴 Null / empty checks
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email or password missing");
        }

        Optional<User> existing = userRepository.findByEmail(loginRequest.getEmail());

        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }

        User dbUser = existing.get();

        // 🔴 Safe comparison
        if (!loginRequest.getPassword().equals(dbUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        if (!loginRequest.getRole().equals(dbUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied for this role");
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