package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.AdminApartmentAssignment;
import water_billing_platform.repository.AdminApartmentAssignmentRepository;

import java.util.List;

@RestController
@RequestMapping("/admin-assignments")
@RequiredArgsConstructor
public class AdminApartmentAssignmentController {

    private final AdminApartmentAssignmentRepository assignmentRepository;

    @PostMapping
    public AdminApartmentAssignment createAssignment(@RequestBody AdminApartmentAssignment assignment) {
        return assignmentRepository.save(assignment);
    }

    @GetMapping("/user/{userId}")
    public List<AdminApartmentAssignment> getAssignmentsByUser(@PathVariable Long userId) {
        return assignmentRepository.findByUserId(userId);
    }
}