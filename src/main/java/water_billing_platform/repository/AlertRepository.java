package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.Alert;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByHouseholdIdOrderByCreatedDateDesc(Long householdId);
    List<Alert> findByResolvedFalseOrderByCreatedDateDesc();
}