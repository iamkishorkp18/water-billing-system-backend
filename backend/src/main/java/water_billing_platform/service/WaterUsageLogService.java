package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import water_billing_platform.entity.WaterUsageLog;
import water_billing_platform.repository.WaterUsageLogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WaterUsageLogService {

    private final WaterUsageLogRepository waterUsageLogRepository;

    public WaterUsageLog logReading(WaterUsageLog log) {
        return waterUsageLogRepository.save(log);
    }

    public List<WaterUsageLog> getHistoryForHousehold(Long householdId) {
        return waterUsageLogRepository
                .findByHouseholdIdOrderByReadingDateDesc(householdId);
    }

    public List<WaterUsageLog> getHistoryForApartment(Long apartmentId) {
        return waterUsageLogRepository
                .findByHousehold_Apartment_Id(apartmentId);
    }
}