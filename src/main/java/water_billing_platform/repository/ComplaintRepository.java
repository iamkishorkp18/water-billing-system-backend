package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import water_billing_platform.entity.Complaint;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Resident complaints for a particular household
    List<Complaint> findByHouseholdIdOrderByCreatedDateDesc(Long householdId);

    // Resident complaints for an apartment
    List<Complaint> findByHousehold_Apartment_IdAndCreatedByRoleOrderByCreatedDateDesc(
            Long apartmentId,
            String createdByRole
    );

    // All complaints - Super Admin
    List<Complaint> findAllByOrderByCreatedDateDesc();

    // Complaints created by a particular role
    List<Complaint> findByCreatedByRoleOrderByCreatedDateDesc(
            String createdByRole
    );

    // Complaints created by a particular user
    List<Complaint> findByCreatedByOrderByCreatedDateDesc(
            String createdBy
    );
}