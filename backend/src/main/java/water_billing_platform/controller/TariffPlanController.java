package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import water_billing_platform.entity.TariffPlan;
import water_billing_platform.service.TariffPlanService;

@RestController
@RequestMapping("/tariff-plans")
@RequiredArgsConstructor
public class TariffPlanController {

    private final TariffPlanService tariffPlanService;

    // CREATE
    @PostMapping
    public TariffPlan createTariffPlan(
            @RequestBody TariffPlan plan) {

        return tariffPlanService.createTariffPlan(plan);
    }

    // GET BY APARTMENT
    @GetMapping("/apartment/{apartmentId}")
    public TariffPlan getTariffForApartment(
            @PathVariable Long apartmentId) {

        return tariffPlanService.getTariffForApartment(apartmentId);
    }

    // UPDATE
    @PutMapping("/{id}")
    public TariffPlan updateTariffPlan(
            @PathVariable Long id,
            @RequestBody TariffPlan plan) {

        return tariffPlanService.updateTariffPlan(id, plan);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String deleteTariffPlan(
            @PathVariable Long id) {

        tariffPlanService.deleteTariffPlan(id);

        return "Tariff plan deleted successfully.";
    }
}