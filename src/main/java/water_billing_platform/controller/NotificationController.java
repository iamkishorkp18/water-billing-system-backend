package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.Notification;
import water_billing_platform.service.AccessControlService;
import water_billing_platform.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;
    private final AccessControlService accessControlService;

    // =========================================================
    // CREATE / SEND NOTIFICATION
    // =========================================================

    @PostMapping
    @PreAuthorize(
            "hasRole('COMMERCIAL_ADMIN') or " +
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> createNotification(
            @RequestBody Notification notification) {

        try {

            Notification saved =
                    notificationService
                            .createNotification(notification);

            return ResponseEntity.ok(saved);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET NOTIFICATIONS FOR RESIDENT
    // =========================================================

    @GetMapping("/household/{householdId}")
    public ResponseEntity<?> getForHousehold(
            @PathVariable Long householdId,
            Authentication authentication) {

        try {

            /*
             * Resident can only access their own household.
             */
            accessControlService
                    .verifyResidentOwnsHousehold(
                            authentication.getName(),
                            householdId
                    );

            List<Notification> notifications =
                    notificationService
                            .getNotificationsForHousehold(
                                    householdId
                            );

            return ResponseEntity.ok(notifications);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET SENT NOTIFICATIONS FOR APARTMENT
    // =========================================================

    @GetMapping("/apartment/{apartmentId}")
    @PreAuthorize(
            "hasRole('COMMERCIAL_ADMIN') or " +
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> getForApartment(
            @PathVariable Long apartmentId) {

        try {

            List<Notification> notifications =
                    notificationService
                            .getSentForApartment(
                                    apartmentId
                            );

            return ResponseEntity.ok(notifications);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET SINGLE NOTIFICATION
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotification(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    notificationService
                            .getNotificationById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasRole('COMMERCIAL_ADMIN') or " +
            "hasRole('SUPER_ADMIN')"
    )
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id) {

        try {

            notificationService
                    .deleteNotification(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Notification deleted successfully."
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }
}