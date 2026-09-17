package water_billing_platform.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import water_billing_platform.entity.PasswordResetToken;
import water_billing_platform.entity.User;
import water_billing_platform.repository.PasswordResetTokenRepository;
import water_billing_platform.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int TOKEN_VALID_MINUTES = 30;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // =========================================================
    // REQUEST PASSWORD RESET
    // =========================================================

    public void requestReset(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required.");
        }

        User user = userRepository.findByEmail(email.trim()).orElse(null);

        if (user == null) {
            return;
        }

        // Residents cannot use Forgot Password.
        if (user.getRole() == User.Role.RESIDENT) {
            throw new RuntimeException(
                    "Forgot password is available only for Super Admin and Commercial Admin."
            );
        }

        // Only Super Admin and Commercial Admin are allowed.
        if (user.getRole() != User.Role.SUPER_ADMIN &&
                user.getRole() != User.Role.COMMERCIAL_ADMIN) {

            throw new RuntimeException(
                    "Forgot password is not available for this account."
            );
        }

        // Account must be approved.
        if (!"APPROVED".equalsIgnoreCase(user.getStatus())) {
            throw new RuntimeException(
                    "Your account is not approved."
            );
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(TOKEN_VALID_MINUTES)
        );
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        String resetLink =
                "http://localhost:5173/reset-password?token=" + token;

        String subject = "Reset your AquaLedger password";

        String body =
                "Hello,\n\n"
                + "We received a request to reset your AquaLedger password.\n\n"
                + "Click the link below to create a new password:\n"
                + resetLink
                + "\n\n"
                + "This link is valid for "
                + TOKEN_VALID_MINUTES
                + " minutes.\n\n"
                + "If you did not request this, you can safely ignore this email.\n\n"
                + "— AquaLedger";

        try {

            emailService.sendSimpleEmail(
                    user.getEmail(),
                    subject,
                    body
            );

        } catch (jakarta.mail.MessagingException e) {

            throw new RuntimeException(
                    "Failed to send reset email."
            );
        }
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Transactional
    public void resetPassword(String token, String newPassword) {

        if (token == null || token.isBlank()) {
            throw new RuntimeException(
                    "Reset token is required."
            );
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException(
                    "New password is required."
            );
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException(
                    "Password must be at least 6 characters."
            );
        }

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid or expired reset link."
                                )
                        );

        if (resetToken.isUsed()) {
            throw new RuntimeException(
                    "This reset link has already been used."
            );
        }

        if (resetToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "This reset link has expired. Please request a new one."
            );
        }

        User user = resetToken.getUser();

        // Residents cannot reset passwords.
        if (user.getRole() == User.Role.RESIDENT) {
            throw new RuntimeException(
                    "Residents cannot reset passwords using this option."
            );
        }

        // Only Super Admin and Commercial Admin.
        if (user.getRole() != User.Role.SUPER_ADMIN &&
                user.getRole() != User.Role.COMMERCIAL_ADMIN) {

            throw new RuntimeException(
                    "Password reset is not available for this account."
            );
        }

        // =====================================================
        // SAVE NEW PASSWORD AS BCRYPT
        // =====================================================

        String encodedPassword =
                passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);

        userRepository.saveAndFlush(user);

        // Verify the password that was just saved.
        User savedUser =
                userRepository.findByEmail(user.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found after password reset."
                                )
                        );

        boolean passwordSavedCorrectly =
                passwordEncoder.matches(
                        newPassword,
                        savedUser.getPassword()
                );

        if (!passwordSavedCorrectly) {
            throw new RuntimeException(
                    "Password reset failed."
            );
        }

        // Token can never be reused.
        resetToken.setUsed(true);
        tokenRepository.saveAndFlush(resetToken);
    }
}