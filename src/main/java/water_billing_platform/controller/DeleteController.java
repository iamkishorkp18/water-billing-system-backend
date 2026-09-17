package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.Apartment;
import water_billing_platform.entity.Bill;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.User;
import water_billing_platform.repository.ApartmentRepository;
import water_billing_platform.repository.BillRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.UserRepository;
import water_billing_platform.service.DeleteService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeleteController {

    private final DeleteService deleteService;

    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final BillRepository billRepository;


    // =========================================================
    // DELETED ITEMS - TRASH
    // =========================================================

    @GetMapping("/apartments/deleted")
    public List<Apartment> getDeletedApartments() {
    	return apartmentRepository.findByDeletedTrue();
    }

    @GetMapping("/users/commercial-admins/deleted")
    public List<User> getDeletedCommercialAdmins() {
        return userRepository.findByRoleAndIsDeletedTrue(
                User.Role.COMMERCIAL_ADMIN
        );
    }

    @GetMapping("/users/residents/deleted")
    public List<User> getDeletedResidents() {
        return userRepository.findByRoleAndIsDeletedTrue(
                User.Role.RESIDENT
        );
    }

    @GetMapping("/households/deleted")
    public List<Household> getDeletedHouseholds() {
        return householdRepository.findByIsDeletedTrue();
    }

    @GetMapping("/bills/deleted")
    public List<Bill> getDeletedBills() {
        return billRepository.findByIsDeletedTrue();
    }


    // =========================================================
    // APARTMENT
    // =========================================================

    @DeleteMapping("/apartments/{id}")
    public String deleteApartment(
            @PathVariable Long id,
            Authentication auth) {

        deleteService.deleteApartment(id, auth.getName());

        return "Apartment deleted successfully.";
    }

    @PostMapping("/apartments/{id}/restore")
    public String restoreApartment(@PathVariable Long id) {

        deleteService.restoreApartment(id);

        return "Apartment restored successfully.";
    }


    // =========================================================
    // COMMERCIAL ADMIN
    // =========================================================

    @DeleteMapping("/users/commercial-admins/{id}")
    public String deleteCommercialAdmin(
            @PathVariable Long id,
            Authentication auth) {

        deleteService.deleteCommercialAdmin(id, auth.getName());

        return "Commercial Admin deleted successfully.";
    }

    @PostMapping("/users/commercial-admins/{id}/restore")
    public String restoreCommercialAdmin(@PathVariable Long id) {

        deleteService.restoreCommercialAdmin(id);

        return "Commercial Admin restored successfully.";
    }


    // =========================================================
    // RESIDENT
    // =========================================================

    @DeleteMapping("/users/residents/{id}")
    public String deleteResident(
            @PathVariable Long id,
            Authentication auth) {

        deleteService.deleteResident(id, auth.getName());

        return "Resident deleted successfully.";
    }

    @PostMapping("/users/residents/{id}/restore")
    public String restoreResident(@PathVariable Long id) {

        deleteService.restoreResident(id);

        return "Resident restored successfully.";
    }


    // =========================================================
    // HOUSEHOLD
    // =========================================================

    @DeleteMapping("/households/{id}")
    public String deleteHousehold(
            @PathVariable Long id,
            Authentication auth) {

        deleteService.deleteHousehold(id, auth.getName());

        return "Household deleted successfully.";
    }

    @PostMapping("/households/{id}/restore")
    public String restoreHousehold(@PathVariable Long id) {

        deleteService.restoreHousehold(id);

        return "Household restored successfully.";
    }


    // =========================================================
    // BILL
    // =========================================================

    @DeleteMapping("/bills/{id}")
    public String deleteBill(
            @PathVariable Long id,
            Authentication auth) {

        deleteService.deleteBill(id, auth.getName());

        return "Invoice deleted successfully.";
    }

    @PostMapping("/bills/{id}/restore")
    public String restoreBill(@PathVariable Long id) {

        deleteService.restoreBill(id);

        return "Invoice restored successfully.";
    }
}