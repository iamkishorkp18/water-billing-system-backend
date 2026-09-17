package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.User;
import water_billing_platform.security.JwtUtil;
import water_billing_platform.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new RuntimeException("Email is required");
        }

        if (request.getPassword() == null ||
                request.getPassword().isEmpty()) {

            throw new RuntimeException("Password is required");
        }

        String email = request.getEmail().trim();

        User user = userService.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        // =====================================================
        // CHECK DELETED ACCOUNT
        // =====================================================

        if (user.isDeleted()) {

            throw new RuntimeException(
                    "This account has been deleted."
            );
        }

        // =====================================================
        // PASSWORD CHECK
        // =====================================================

        String storedPassword = user.getPassword();

        boolean passwordMatches = false;

        if (storedPassword != null &&
                !storedPassword.isBlank()) {

            /*
             * Passwords created/reset through the application
             * are stored using BCrypt.
             */
            if (storedPassword.startsWith("$2a$") ||
                    storedPassword.startsWith("$2b$") ||
                    storedPassword.startsWith("$2y$")) {

                passwordMatches = passwordEncoder.matches(
                        request.getPassword(),
                        storedPassword
                );

            } else {

                /*
                 * Supports an existing manually-created plaintext
                 * password. This is only for legacy accounts.
                 *
                 * Once that account uses Forgot Password,
                 * its password will become BCrypt.
                 */
                passwordMatches = request.getPassword().equals(
                        storedPassword
                );
            }
        }

        System.out.println(
                "LOGIN EMAIL: " + user.getEmail()
        );

        System.out.println(
                "PASSWORD MATCH: " + passwordMatches
        );

        if (!passwordMatches) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        // =====================================================
        // APPROVAL STATUS
        // =====================================================

        if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {

            if ("PENDING".equalsIgnoreCase(user.getStatus())) {

                throw new RuntimeException(
                        "Your account is pending approval from the Super Admin."
                );
            }

            if ("REJECTED".equalsIgnoreCase(user.getStatus())) {

                throw new RuntimeException(
                        "Your account has been rejected by the Super Admin."
                );
            }

            throw new RuntimeException(
                    "Your account is not approved for login."
            );
        }

        // =====================================================
        // GENERATE JWT
        // =====================================================

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        Map<String, String> response = new HashMap<>();

        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());

        if (user.getHousehold() != null) {

            response.put(
                    "householdId",
                    user.getHousehold().getId().toString()
            );
        }

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // LOGIN REQUEST
    // =========================================================

    public static class LoginRequest {

        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}