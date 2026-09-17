package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.WaterUsageLog;
import water_billing_platform.service.AccessControlService;
import water_billing_platform.service.WaterUsageLogService;

import java.util.List;

@RestController
@RequestMapping("/usage-logs")
@RequiredArgsConstructor
public class WaterUsageLogController {

    private final WaterUsageLogService waterUsageLogService;
    private final AccessControlService accessControlService;

    @PostMapping
    public WaterUsageLog logReading(@RequestBody WaterUsageLog log) {
        return waterUsageLogService.logReading(log);
    }

    @GetMapping("/household/{householdId}")
    public List<WaterUsageLog> getHistory(@PathVariable Long householdId, Authentication authentication) {
        accessControlService.verifyResidentOwnsHousehold(authentication.getName(), householdId);
        return waterUsageLogService.getHistoryForHousehold(householdId);
    }

    @GetMapping("/apartment/{apartmentId}")
    public List<WaterUsageLog> getUsageForApartment(@PathVariable Long apartmentId) {
        return waterUsageLogService.getHistoryForApartment(apartmentId);
    }
}