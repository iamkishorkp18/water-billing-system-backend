package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.AdminApartmentAssignment;

import java.util.List;

public interface AdminApartmentAssignmentRepository extends JpaRepository<AdminApartmentAssignment, Long> {
    List<AdminApartmentAssignment> findByUserId(Long userId);
    boolean existsByUserIdAndApartmentId(Long userId, Long apartmentId);
}