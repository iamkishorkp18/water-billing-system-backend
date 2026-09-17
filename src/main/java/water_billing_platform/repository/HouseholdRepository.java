package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import water_billing_platform.entity.Household;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository
        extends JpaRepository<Household, Long> {

    List<Household> findByApartmentIdAndIsDeletedFalse(Long apartmentId);

    Optional<Household> findByIdAndIsDeletedFalse(Long id);

    List<Household> findByIsDeletedTrue();
}