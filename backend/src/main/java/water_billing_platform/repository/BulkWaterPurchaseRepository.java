package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.BulkWaterPurchase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BulkWaterPurchaseRepository
        extends JpaRepository<BulkWaterPurchase, Long> {

    List<BulkWaterPurchase>
    findByApartmentIdOrderByPurchaseDateDesc(Long apartmentId);

    List<BulkWaterPurchase>
    findByApartmentIdAndPurchaseDateBetweenOrderByPurchaseDateDesc(
            Long apartmentId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<BulkWaterPurchase>
    findFirstByApartmentIdOrderByPurchaseDateDesc(Long apartmentId);
}