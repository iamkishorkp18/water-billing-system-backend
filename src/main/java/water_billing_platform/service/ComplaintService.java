package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Complaint;
import water_billing_platform.repository.ComplaintRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public Complaint createComplaint(Complaint complaint) {

        // Default values
        complaint.setStatus("OPEN");
        complaint.setCreatedDate(LocalDate.now());

        /*
         * COMMERCIAL ADMIN COMPLAINT
         *
         * Commercial Admin does not belong to a household,
         * so household must remain NULL.
         */
        if ("COMMERCIAL_ADMIN".equals(complaint.getCreatedByRole())) {

            complaint.setHousehold(null);

        }

        /*
         * RESIDENT COMPLAINT
         *
         * Resident must send household ID.
         */
        if ("RESIDENT".equals(complaint.getCreatedByRole())) {

            if (complaint.getHousehold() == null ||
                complaint.getHousehold().getId() == null) {

                throw new RuntimeException(
                    "Household is required for resident complaint"
                );
            }
        }

        return complaintRepository.save(complaint);
    }


    // Resident complaints for their household
    public List<Complaint> getComplaintsForHousehold(Long householdId) {

        return complaintRepository
                .findByHouseholdIdOrderByCreatedDateDesc(householdId);
    }


    // Commercial Admin sees resident complaints
    public List<Complaint> getComplaintsForApartment(Long apartmentId) {

        return complaintRepository
                .findByHousehold_Apartment_IdAndCreatedByRoleOrderByCreatedDateDesc(
                        apartmentId,
                        "RESIDENT"
                );
    }


    // Super Admin sees everything
    public List<Complaint> getAllComplaints() {

        return complaintRepository
                .findAllByOrderByCreatedDateDesc();
    }


    // Commercial Admin complaints
    public List<Complaint> getCommercialAdminComplaints() {

        return complaintRepository
                .findByCreatedByRoleOrderByCreatedDateDesc(
                        "COMMERCIAL_ADMIN"
                );
    }


    // Update complaint status
    public Complaint updateStatus(Long complaintId, String status) {

        Complaint complaint =
                complaintRepository.findById(complaintId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Complaint not found: " + complaintId
                    )
                );

        complaint.setStatus(status);

        if ("RESOLVED".equals(status)) {
            complaint.setResolvedDate(LocalDate.now());
        }

        return complaintRepository.save(complaint);
    }
}