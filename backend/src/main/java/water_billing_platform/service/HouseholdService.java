package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Household;
import water_billing_platform.repository.HouseholdRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;

    // =========================================================
    // CREATE HOUSEHOLD
    // =========================================================

    public Household createHousehold(Household household) {

        // New household is always active
        household.setIsDeleted(false);
        household.setDeletedAt(null);
        household.setDeletedBy(null);

        return householdRepository.save(household);
    }


    // =========================================================
    // GET ACTIVE HOUSEHOLDS BY APARTMENT
    // =========================================================

    public List<Household> getHouseholdsByApartment(Long apartmentId) {

        return householdRepository
                .findByApartmentIdAndIsDeletedFalse(apartmentId);
    }


    // =========================================================
    // GET ACTIVE HOUSEHOLD BY ID
    // =========================================================

    public Household getHouseholdById(Long id) {

        return householdRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Household not found or already deleted: " + id
                        )
                );
    }


    // =========================================================
    // GET DELETED HOUSEHOLDS
    // =========================================================

    public List<Household> getDeletedHouseholds() {

        return householdRepository.findByIsDeletedTrue();
    }
}