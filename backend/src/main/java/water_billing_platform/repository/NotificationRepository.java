package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.Notification;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByHouseholdIdOrderByCreatedDateDesc(Long householdId);

    List<Notification>
    findByApartmentIdOrderByCreatedDateDesc(Long apartmentId);
}