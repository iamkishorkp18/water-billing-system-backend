package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import water_billing_platform.entity.Apartment;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    // Active apartments
    List<Apartment> findByDeletedFalse();

    // Active apartment by ID
    Optional<Apartment> findByIdAndDeletedFalse(Long id);

    // Deleted apartments
    List<Apartment> findByDeletedTrue();
}