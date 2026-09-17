package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.TariffPlan;
import water_billing_platform.repository.TariffPlanRepository;

@Service
@RequiredArgsConstructor
public class TariffPlanService {

    private final TariffPlanRepository tariffPlanRepository;

    // CREATE
    public TariffPlan createTariffPlan(TariffPlan plan) {

        if (plan.getTier1Limit() == null ||
            plan.getTier1Rate() == null ||
            plan.getTier2Limit() == null ||
            plan.getTier2Rate() == null ||
            plan.getTier3Rate() == null) {

            throw new RuntimeException("All tariff values are required.");
        }

        if (plan.getTier2Limit().compareTo(plan.getTier1Limit()) <= 0) {
            throw new RuntimeException(
                    "Tier 2 limit must be greater than Tier 1 limit."
            );
        }

        return tariffPlanRepository.save(plan);
    }

    // GET TARIFF FOR APARTMENT
    public TariffPlan getTariffForApartment(Long apartmentId) {

        return tariffPlanRepository.findByApartmentId(apartmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No tariff plan found for apartment id: " + apartmentId
                        )
                );
    }

    // UPDATE
    public TariffPlan updateTariffPlan(Long id, TariffPlan updated) {

        TariffPlan existing = tariffPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Tariff plan not found: " + id
                        )
                );

        if (updated.getTier1Limit() == null ||
            updated.getTier1Rate() == null ||
            updated.getTier2Limit() == null ||
            updated.getTier2Rate() == null ||
            updated.getTier3Rate() == null) {

            throw new RuntimeException("All tariff values are required.");
        }

        if (updated.getTier2Limit()
                .compareTo(updated.getTier1Limit()) <= 0) {

            throw new RuntimeException(
                    "Tier 2 limit must be greater than Tier 1 limit."
            );
        }

        existing.setTier1Limit(updated.getTier1Limit());
        existing.setTier1Rate(updated.getTier1Rate());

        existing.setTier2Limit(updated.getTier2Limit());
        existing.setTier2Rate(updated.getTier2Rate());

        existing.setTier3Rate(updated.getTier3Rate());

        return tariffPlanRepository.save(existing);
    }

    // DELETE
    public void deleteTariffPlan(Long id) {

        if (!tariffPlanRepository.existsById(id)) {
            throw new RuntimeException(
                    "Tariff plan not found: " + id
            );
        }

        tariffPlanRepository.deleteById(id);
    }
}