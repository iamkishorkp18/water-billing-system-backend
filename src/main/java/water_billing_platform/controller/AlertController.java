package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // ✅ import Authentication
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.Alert;
import water_billing_platform.repository.AlertRepository;
import water_billing_platform.service.AlertService;
import water_billing_platform.service.AccessControlService; // ✅ import your access control service

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final AccessControlService accessControlService; // ✅ inject it

    @PostMapping("/check/{householdId}")
    public List<Alert> checkForAnomalies(@PathVariable Long householdId) {
        return alertService.checkHouseholdForAnomalies(householdId);
    }

  
    @GetMapping("/household/{householdId}")
    public List<Alert> getAlertsForHousehold(@PathVariable Long householdId, Authentication authentication) {
        accessControlService.verifyResidentOwnsHousehold(authentication.getName(), householdId);
        return alertRepository.findByHouseholdIdOrderByCreatedDateDesc(householdId);
    }

    @GetMapping("/unresolved")
    public List<Alert> getAllUnresolvedAlerts() {
        return alertRepository.findByResolvedFalseOrderByCreatedDateDesc();
    }
    
}
