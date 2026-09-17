package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.Complaint;
import water_billing_platform.service.ComplaintService;

import java.util.List;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
@CrossOrigin
public class ComplaintController {

    private final ComplaintService complaintService;

    // ==========================================
    // CREATE COMPLAINT
    // Resident OR Commercial Admin
    // ==========================================
    @PostMapping
    public Complaint createComplaint(@RequestBody Complaint complaint) {
        return complaintService.createComplaint(complaint);
    }

    // ==========================================
    // RESIDENT:
    // Get complaints for a household
    // ==========================================
    @GetMapping("/household/{householdId}")
    public List<Complaint> getForHousehold(
            @PathVariable Long householdId) {

        return complaintService
                .getComplaintsForHousehold(householdId);
    }

    // ==========================================
    // COMMERCIAL ADMIN:
    // Get resident complaints for apartment
    // ==========================================
    @GetMapping("/apartment/{apartmentId}")
    public List<Complaint> getForApartment(
            @PathVariable Long apartmentId) {

        return complaintService
                .getComplaintsForApartment(apartmentId);
    }

    // ==========================================
    // SUPER ADMIN:
    // Get ALL complaints
    // ==========================================
    @GetMapping("/all")
    public List<Complaint> getAll() {

        return complaintService.getAllComplaints();
    }

    // ==========================================
    // SUPER ADMIN:
    // Get only Commercial Admin complaints
    // ==========================================
    @GetMapping("/commercial-admin")
    public List<Complaint> getCommercialAdminComplaints() {

        return complaintService
                .getCommercialAdminComplaints();
    }

    // ==========================================
    // UPDATE STATUS
    // ==========================================
    @PostMapping("/{id}/status")
    public Complaint updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return complaintService.updateStatus(id, status);
    }
}