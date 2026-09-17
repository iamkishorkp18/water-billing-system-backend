package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.Household;
import water_billing_platform.service.HouseholdService;

import java.util.List;

@RestController
@RequestMapping("/households")
@RequiredArgsConstructor
public class HouseholdController {

    private final HouseholdService householdService;

    @PostMapping
    public Household createHousehold(@RequestBody Household household) {
        return householdService.createHousehold(household);
    }

    @GetMapping("/{id}")
    public Household getHouseholdById(@PathVariable Long id) {
        return householdService.getHouseholdById(id);
    }

    @GetMapping("/apartment/{apartmentId}")
    public List<Household> getHouseholdsByApartment(@PathVariable Long apartmentId) {
        // Updated to filter out deleted households
        return householdService.getHouseholdsByApartment(apartmentId);
    }
}
