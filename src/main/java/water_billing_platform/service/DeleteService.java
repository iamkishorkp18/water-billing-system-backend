package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Apartment;
import water_billing_platform.entity.Bill;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.User;

import water_billing_platform.repository.ApartmentRepository;
import water_billing_platform.repository.BillRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteService {

    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final BillRepository billRepository;


    // =========================================================
    // APARTMENT
    // =========================================================

    public void deleteApartment(Long id, String deletedBy) {

        Apartment apt = apartmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Apartment not found: " + id
                        )
                );

        List<Household> households =
                householdRepository.findByApartmentIdAndIsDeletedFalse(id);

        if (!households.isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete apartment. Active households still exist."
            );
        }

        apt.setDeleted(true);
        apt.setDeletedAt(LocalDateTime.now());
        apt.setDeletedBy(deletedBy);

        apartmentRepository.save(apt);
    }


    public void restoreApartment(Long id) {

        Apartment apt = apartmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Apartment not found: " + id
                        )
                );

        apt.setDeleted(false);
        apt.setDeletedAt(null);
        apt.setDeletedBy(null);

        apartmentRepository.save(apt);
    }


    // =========================================================
    // COMMERCIAL ADMIN
    // =========================================================

    public void deleteCommercialAdmin(Long id, String deletedBy) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        )
                );

        if (user.getRole() == User.Role.SUPER_ADMIN) {
            throw new RuntimeException(
                    "Cannot delete a Super Admin account."
            );
        }

        if (user.getRole() != User.Role.COMMERCIAL_ADMIN) {
            throw new RuntimeException(
                    "This user is not a Commercial Admin."
            );
        }

        if (user.isDeleted()) {
            throw new RuntimeException(
                    "Commercial Admin is already deleted."
            );
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);

        userRepository.save(user);
    }


    public void restoreCommercialAdmin(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        )
                );

        if (user.getRole() != User.Role.COMMERCIAL_ADMIN) {
            throw new RuntimeException(
                    "This user is not a Commercial Admin."
            );
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        userRepository.save(user);
    }


    // =========================================================
    // RESIDENT
    // =========================================================

    public void deleteResident(Long id, String deletedBy) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        )
                );

        if (user.getRole() != User.Role.RESIDENT) {
            throw new RuntimeException(
                    "This user is not a Resident."
            );
        }

        if (user.isDeleted()) {
            throw new RuntimeException(
                    "Resident is already deleted."
            );
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);

        userRepository.save(user);
    }


    public void restoreResident(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + id
                        )
                );

        if (user.getRole() != User.Role.RESIDENT) {
            throw new RuntimeException(
                    "This user is not a Resident."
            );
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        userRepository.save(user);
    }


    // =========================================================
    // HOUSEHOLD
    // =========================================================

    public void deleteHousehold(Long id, String deletedBy) {

        Household household = householdRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Household not found: " + id
                        )
                );

        // Boolean-safe check
        if (Boolean.TRUE.equals(household.getIsDeleted())) {
            throw new RuntimeException(
                    "Household is already deleted."
            );
        }

        household.setIsDeleted(true);
        household.setDeletedAt(LocalDateTime.now());
        household.setDeletedBy(deletedBy);

        householdRepository.save(household);
    }


    public void restoreHousehold(Long id) {

        Household household = householdRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Household not found: " + id
                        )
                );

        household.setIsDeleted(false);
        household.setDeletedAt(null);
        household.setDeletedBy(null);

        householdRepository.save(household);
    }


    // =========================================================
    // BILL / INVOICE
    // =========================================================

    public void deleteBill(Long id, String deletedBy) {

        Bill bill = billRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found: " + id
                        )
                );

        if (bill.isDeleted()) {
            throw new RuntimeException(
                    "Bill is already deleted."
            );
        }

        bill.setDeleted(true);
        bill.setDeletedAt(LocalDateTime.now());
        bill.setDeletedBy(deletedBy);

        billRepository.save(bill);
    }


    public void restoreBill(Long id) {

        Bill bill = billRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found: " + id
                        )
                );

        bill.setDeleted(false);
        bill.setDeletedAt(null);
        bill.setDeletedBy(null);

        billRepository.save(bill);
    }
}