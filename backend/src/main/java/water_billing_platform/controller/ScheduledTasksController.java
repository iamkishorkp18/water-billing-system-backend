package water_billing_platform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import water_billing_platform.service.ScheduledTasksService;

@RestController
@RequestMapping("/scheduled-tasks")
@RequiredArgsConstructor
public class ScheduledTasksController {

    private final ScheduledTasksService scheduledTasksService;

    @PostMapping("/run-leak-detection")
    public String runLeakDetectionNow() {
        scheduledTasksService.runDailyLeakDetection();
        return "Leak detection job triggered manually.";
    }

    @PostMapping("/run-overdue-check")
    public String runOverdueCheckNow() {
        scheduledTasksService.flagOverdueBills();
        return "Overdue bill check triggered manually.";
    }

    @PostMapping("/run-due-soon-reminders")
    public String runDueSoonRemindersNow() {
        scheduledTasksService.sendDueSoonReminders();
        return "Due-soon reminder job triggered manually.";
    }
}