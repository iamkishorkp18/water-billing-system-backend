package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.User;
import water_billing_platform.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================================================
    // CREATE USER
    // =========================================================
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // =========================================================
    // PENDING ADMINS
    // =========================================================
    @GetMapping("/pending-admins")
    public List<User> getPendingAdmins() {
        return userService.getPendingAdmins();
    }

    // =========================================================
    // APPROVE ADMIN
    // =========================================================
    @PostMapping("/{id}/approve")
    public User approveAdmin(@PathVariable Long id) {
        return userService.approveAdmin(id);
    }

    // =========================================================
    // REJECT ADMIN
    // =========================================================
    @PostMapping("/{id}/reject")
    public User rejectAdmin(@PathVariable Long id) {
        return userService.rejectAdmin(id);
    }

    // =========================================================
    // COMMERCIAL ADMINS
    // =========================================================
    @GetMapping("/commercial-admins")
    public List<User> getAllCommercialAdmins() {
        return userService.getAllCommercialAdmins();
    }

    // =========================================================
    // RESIDENTS BY APARTMENT
    // =========================================================
    @GetMapping("/residents/apartment/{apartmentId}")
    public List<User> getResidentsForApartment(@PathVariable Long apartmentId) {
        return userService.getResidentsForApartment(apartmentId);
    }
}
