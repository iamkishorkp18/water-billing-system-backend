package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import water_billing_platform.entity.User;
import water_billing_platform.repository.UserRepository;
import org.springframework.security.core.Authentication;
import water_billing_platform.service.AccessControlService;


@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserRepository userRepository;

    public void verifyResidentOwnsHousehold(String email, Long requestedHouseholdId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.RESIDENT) {
            return; // Admins are allowed through; this check is Resident-specific
        }

        if (user.getHousehold() == null || !user.getHousehold().getId().equals(requestedHouseholdId)) {
            throw new RuntimeException("Access denied: you can only view your own household's data.");
        }
    }
}