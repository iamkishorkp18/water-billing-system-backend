package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Apartment;
import water_billing_platform.repository.ApartmentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    // =========================
    // CREATE APARTMENT
    // =========================

    public Apartment createApartment(Apartment apartment) {

        // Always create as active
        apartment.setDeleted(false);
        apartment.setDeletedAt(null);
        apartment.setDeletedBy(null);

        // Default threshold
        if (apartment.getUsageThresholdMultiplier() == null) {
            apartment.setUsageThresholdMultiplier(
                    java.math.BigDecimal.valueOf(1.50)
            );
        }

        return apartmentRepository.save(apartment);
    }

    // =========================
    // GET ACTIVE APARTMENTS
    // =========================

    public List<Apartment> getAllApartments() {

        return apartmentRepository.findByDeletedFalse();
    }

    // =========================
    // GET ACTIVE APARTMENT BY ID
    // =========================

    public Apartment getApartmentById(Long id) {

        return apartmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Apartment not found or already deleted: " + id
                        )
                );
    }

    // =========================
    // GET APARTMENTS FOR ADMIN
    // =========================

    public List<Apartment> getApartmentsForAdmin(String email) {

        return apartmentRepository.findByDeletedFalse();
    }

    // =========================
    // UPDATE APARTMENT
    // =========================

    public Apartment updateApartment(Apartment apartment) {

        return apartmentRepository.save(apartment);
    }
}