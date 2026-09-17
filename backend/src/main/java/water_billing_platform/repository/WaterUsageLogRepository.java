package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.WaterUsageLog;

import java.util.List;
import java.util.Optional;

public interface WaterUsageLogRepository extends JpaRepository<WaterUsageLog, Long> {

    List<WaterUsageLog> findByHouseholdIdOrderByReadingDateDesc(Long householdId);

    List<WaterUsageLog> findByHousehold_Apartment_Id(Long apartmentId);

    Optional<WaterUsageLog> findTopByHouseholdIdOrderByReadingDateDesc(Long householdId);
}