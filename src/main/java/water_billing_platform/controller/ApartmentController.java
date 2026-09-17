package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.Apartment;
import water_billing_platform.service.ApartmentService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentService apartmentService;

    // =========================
    // CREATE APARTMENT
    // =========================

    @PostMapping
    public Apartment createApartment(
            @RequestBody Apartment apartment) {

        return apartmentService.createApartment(apartment);
    }

    // =========================
    // GET ACTIVE APARTMENTS
    // =========================

    @GetMapping
    public List<Apartment> getAllApartments() {

        return apartmentService.getAllApartments();
    }

    // =========================
    // GET APARTMENT BY ID
    // =========================

    @GetMapping("/{id}")
    public Apartment getApartmentById(
            @PathVariable Long id) {

        return apartmentService.getApartmentById(id);
    }

    // =========================
    // GET MY APARTMENTS
    // =========================

    @GetMapping("/my")
    public List<Apartment> getMyApartments(
            Authentication authentication) {

        String email = authentication.getName();

        return apartmentService.getApartmentsForAdmin(email);
    }

    // =========================
    // UPDATE USAGE THRESHOLD
    // =========================

    @PutMapping("/{id}/threshold")
    public Apartment updateThreshold(
            @PathVariable Long id,
            @RequestParam BigDecimal multiplier) {

        Apartment apt =
                apartmentService.getApartmentById(id);

        apt.setUsageThresholdMultiplier(multiplier);

        return apartmentService.updateApartment(apt);
    }
}