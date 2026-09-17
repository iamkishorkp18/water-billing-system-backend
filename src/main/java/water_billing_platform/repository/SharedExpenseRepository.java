package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.SharedExpense;

import java.util.List;

public interface SharedExpenseRepository extends JpaRepository<SharedExpense, Long> {
    List<SharedExpense> findByApartmentIdAndBillingMonth(Long apartmentId, String billingMonth);
}