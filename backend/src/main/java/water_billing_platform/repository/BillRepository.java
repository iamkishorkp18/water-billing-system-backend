package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.Bill;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByHouseholdIdOrderByGeneratedDateDesc(Long householdId);

    List<Bill> findByHousehold_Apartment_Id(Long apartmentId);

    List<Bill> findByStatus(String status);

    Optional<Bill> findByRazorpayOrderId(String razorpayOrderId);

    // Active bills only
    List<Bill> findByHousehold_Apartment_IdAndIsDeletedFalse(Long apartmentId);

    // Active bill by ID
    Optional<Bill> findByIdAndIsDeletedFalse(Long id);

    // Deleted bills - Trash
    List<Bill> findByIsDeletedTrue();
}