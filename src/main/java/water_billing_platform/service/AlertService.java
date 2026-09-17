package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import water_billing_platform.entity.Alert;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.WaterUsageLog;
import water_billing_platform.repository.AlertRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.WaterUsageLogRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final WaterUsageLogRepository waterUsageLogRepository;
    private final HouseholdRepository householdRepository;
    private final AlertRepository alertRepository;

    // If this reading's consumption is 50% higher than the previous period's, flag it
    //private static final BigDecimal SPIKE_THRESHOLD_PERCENT = new BigDecimal("1.5");

    public List<Alert> checkHouseholdForAnomalies(Long householdId) {

        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new RuntimeException("Household not found: " + householdId));

        List<WaterUsageLog> logs = waterUsageLogRepository.findByHouseholdIdOrderByReadingDateDesc(householdId);

        if (logs.size() < 4) {
            // Need enough history to compute a meaningful average and standard deviation
            return List.of();
        }

        // Build consumption periods (each = difference between consecutive readings)
        List<BigDecimal> consumptions = new java.util.ArrayList<>();
        for (int i = 0; i < logs.size() - 1; i++) {
            BigDecimal diff = logs.get(i).getMeterReading().subtract(logs.get(i + 1).getMeterReading());
            consumptions.add(diff);
        }

        BigDecimal latestConsumption = consumptions.get(0);
        List<BigDecimal> historicalConsumptions = consumptions.subList(1, consumptions.size());

        BigDecimal mean = historicalConsumptions.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historicalConsumptions.size()), 4, java.math.RoundingMode.HALF_UP);

        BigDecimal sumSquaredDiffs = BigDecimal.ZERO;
        for (BigDecimal c : historicalConsumptions) {
            BigDecimal diff = c.subtract(mean);
            sumSquaredDiffs = sumSquaredDiffs.add(diff.multiply(diff));
        }
        BigDecimal variance = sumSquaredDiffs.divide(BigDecimal.valueOf(historicalConsumptions.size()), 4, java.math.RoundingMode.HALF_UP);
        double stdDev = Math.sqrt(variance.doubleValue());

        BigDecimal threshold = mean.add(BigDecimal.valueOf(2 * stdDev));

        if (latestConsumption.compareTo(threshold) > 0) {
            Alert alert = new Alert();
            alert.setHousehold(household);
            alert.setAlertType("HIGH_USAGE");
            alert.setMessage(String.format(
                    "Usage of %.2f units significantly exceeds your typical range (average %.2f ± %.2f std. dev). Possible leak or unusually high usage.",
                    latestConsumption, mean, stdDev
            ));
            alert.setCreatedDate(java.time.LocalDate.now());
            alert.setResolved(false);
            return List.of(alertRepository.save(alert));
        }

        return List.of();
    }
}