package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.Apartment;
import water_billing_platform.entity.Bill;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.User;
import water_billing_platform.repository.ApartmentRepository;
import water_billing_platform.repository.BillRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/trash")
@RequiredArgsConstructor
public class TrashController {

    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;


    // ================================
    // DELETED APARTMENTS
    // ================================

    @GetMapping("/apartments")
    public List<Apartment> getDeletedApartments() {

    	return apartmentRepository.findByDeletedTrue();
    }


    // ================================
    // DELETED HOUSEHOLDS
    // ================================

    @GetMapping("/households")
    public List<Household> getDeletedHouseholds() {

        return householdRepository.findByIsDeletedTrue();
    }


    // ================================
    // DELETED BILLS
    // ================================

    @GetMapping("/bills")
    public List<Bill> getDeletedBills() {

        return billRepository.findByIsDeletedTrue();
    }


    // ================================
    // DELETED COMMERCIAL ADMINS
    // ================================

    @GetMapping("/commercial-admins")
    public List<User> getDeletedCommercialAdmins() {

        return userRepository
                .findByRoleAndIsDeletedTrue(
                        User.Role.COMMERCIAL_ADMIN
                );
    }


    // ================================
    // DELETED RESIDENTS
    // ================================

    @GetMapping("/residents")
    public List<User> getDeletedResidents() {

        return userRepository
                .findByRoleAndIsDeletedTrue(
                        User.Role.RESIDENT
                );
    }
}