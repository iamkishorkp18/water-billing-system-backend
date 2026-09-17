package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByHouseholdIdOrderByPaymentDateDesc(Long householdId);

    List<Payment> findByHousehold_Apartment_IdOrderByPaymentDateDesc(Long apartmentId);

    List<Payment> findByHousehold_Apartment_IdAndPaymentMethodOrderByPaymentDateDesc(
            Long apartmentId, String method);

    List<Payment> findByHousehold_FlatNumberContainingIgnoreCase(String flatNumber);

    // Payment details for a particular bill
    List<Payment> findByBillIdOrderByPaymentDateDesc(Long billId);
}