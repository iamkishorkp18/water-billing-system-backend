package water_billing_platform.service;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Bill;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.Notification;
import water_billing_platform.repository.BillRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.NotificationRepository;
import water_billing_platform.repository.WaterUsageLogRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTasksService {

    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;
    private final BillingService billingService;
    private final AlertService alertService;
    private final BillRepository billRepository;
    private final NotificationRepository notificationRepository;

    // =========================================================
    // AUTOMATIC BILL GENERATION
    // =========================================================

    /**
     * Runs ONLY ONCE:
     *
     * At 12:00 AM on the 1st day of every month.
     *
     * Example:
     *
     * August 1 - August 31
     *        ↓
     * Admin enters August's latest meter reading
     *        ↓
     * September 1 at 12:00 AM
     *        ↓
     * August bill is generated
     *
     * Only ONE bill is generated for each:
     *
     * Household + Billing Month
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void generateMonthlyBillsAutomatically()  {

        // Current month
        YearMonth currentMonth = YearMonth.now();

        // Generate bill for previous month
        // Example:
        // September 1 -> generates August bill
        YearMonth billingPeriod = currentMonth.minusMonths(1);

        String billingMonth = billingPeriod.toString();

        int billsGenerated = 0;

        // ---------------------------------------------------------
        // GET ACTIVE HOUSEHOLDS
        // ---------------------------------------------------------

        List<Household> households =
                householdRepository.findAll()
                        .stream()
                        .filter(h ->
                                !Boolean.TRUE.equals(
                                        h.getIsDeleted()
                                )
                        )
                        .toList();

        System.out.println(
                "[Scheduled] Starting automatic bill generation for "
                        + billingMonth
        );

        // ---------------------------------------------------------
        // PROCESS EACH HOUSEHOLD
        // ---------------------------------------------------------

        for (Household household : households) {

            try {

                Long householdId = household.getId();

                // -------------------------------------------------
                // CHECK METER READINGS
                // -------------------------------------------------

                List<?> readings =
                        waterUsageLogRepository
                                .findByHouseholdIdOrderByReadingDateDesc(
                                        householdId
                                );

                /*
                 * At least two readings are required:
                 *
                 * Latest reading
                 *        -
                 * Previous reading
                 *
                 * = Monthly consumption
                 */

                if (readings.size() < 2) {

                    System.out.println(
                            "[Scheduled] Skipping household "
                                    + householdId
                                    + " - previous meter reading not available."
                    );

                    continue;
                }

                // -------------------------------------------------
                // DUPLICATE BILL PROTECTION
                // -------------------------------------------------

                boolean alreadyGenerated =
                        billRepository
                                .findByHouseholdIdOrderByGeneratedDateDesc(
                                        householdId
                                )
                                .stream()
                                .anyMatch(b ->
                                        billingMonth.equals(
                                                b.getBillingMonth()
                                        )
                                                && !b.isDeleted()
                                );

                /*
                 * If a bill already exists for this household
                 * and billing month, DO NOT generate another one.
                 */

                if (alreadyGenerated) {

                    System.out.println(
                            "[Scheduled] Bill already exists for household "
                                    + householdId
                                    + " for "
                                    + billingMonth
                                    + ". Skipping."
                    );

                    continue;
                }

                // -------------------------------------------------
                // GENERATE BILL
                // -------------------------------------------------

                Bill bill =
                        billingService.generateBill(
                                householdId,
                                billingMonth
                        );

                // -------------------------------------------------
                // CHECK BILL RESULT
                // -------------------------------------------------

                if (bill == null) {

                    System.err.println(
                            "[Scheduled] BillingService returned null for household "
                                    + householdId
                    );

                    continue;
                }

                // -------------------------------------------------
                // CREATE NOTIFICATION
                // -------------------------------------------------

                Notification notification =
                        new Notification();

                notification.setHousehold(
                        household
                );

                notification.setApartment(
                        household.getApartment()
                );

                notification.setTitle(
                        "Bill Generated"
                );

                notification.setMessage(
                        "Your water bill for "
                                + billingMonth
                                + " has been generated. "
                                + "Amount: ₹"
                                + bill.getAmount()
                                + ". Due date: "
                                + bill.getDueDate()
                );

                notification.setNotificationType(
                        "BILLING"
                );

                notification.setCreatedDate(
                        LocalDate.now()
                );

                notificationRepository.save(
                        notification
                );

                billsGenerated++;

                System.out.println(
                        "[Scheduled] Bill generated successfully."
                                + " Household: "
                                + householdId
                                + ", Bill ID: "
                                + bill.getId()
                                + ", Billing Month: "
                                + billingMonth
                );

            } catch (Exception e) {

                /*
                 * If one household fails,
                 * continue processing the remaining households.
                 */

                System.err.println(
                        "[Scheduled] Bill generation failed for household "
                                + household.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        System.out.println(
                "[Scheduled] Automatic bill generation complete. "
                        + billsGenerated
                        + " bill(s) generated for "
                        + billingMonth
                        + "."
        );
    }

    // =========================================================
    // DAILY LEAK / ANOMALY DETECTION
    // =========================================================

    /**
     * Runs every day at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void runDailyLeakDetection() {

        List<Household> households =
                householdRepository.findAll();

        int alertsCreated = 0;

        for (Household household : households) {

            try {

                var newAlerts =
                        alertService.checkHouseholdForAnomalies(
                                household.getId()
                        );

                alertsCreated += newAlerts.size();

            } catch (Exception e) {

                System.err.println(
                        "Leak check failed for household "
                                + household.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        System.out.println(
                "[Scheduled] Daily leak detection complete. "
                        + alertsCreated
                        + " new alert(s) created across "
                        + households.size()
                        + " household(s)."
        );
    }

    // =========================================================
    // OVERDUE BILL CHECK
    // =========================================================

    /**
     * Runs every day at 6:00 AM.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void flagOverdueBills() {

        List<Bill> unpaidBills =
                billRepository.findAll()
                        .stream()
                        .filter(b -> !b.isDeleted())
                        .filter(b -> !"PAID".equals(b.getStatus()))
                        .toList();

        int flagged = 0;

        for (Bill bill : unpaidBills) {

            if (bill.getGeneratedDate() != null
                    && bill.getGeneratedDate()
                    .isBefore(
                            LocalDate.now().minusDays(15)
                    )
                    && !"OVERDUE".equals(
                            bill.getStatus()
                    )) {

                bill.setStatus("OVERDUE");

                billRepository.save(bill);

                Notification notification =
                        new Notification();

                notification.setHousehold(
                        bill.getHousehold()
                );

                notification.setApartment(
                        bill.getHousehold().getApartment()
                );

                notification.setTitle(
                        "Bill Overdue"
                );

                notification.setMessage(
                        "Your bill for "
                                + bill.getBillingMonth()
                                + " (₹"
                                + bill.getAmount()
                                + ") is now overdue. "
                                + "Please pay as soon as possible."
                );

                notification.setNotificationType(
                        "BILLING"
                );

                notification.setCreatedDate(
                        LocalDate.now()
                );

                notificationRepository.save(
                        notification
                );

                flagged++;
            }
        }

        System.out.println(
                "[Scheduled] Overdue bill check complete. "
                        + flagged
                        + " bill(s) flagged as overdue."
        );
    }

    // =========================================================
    // BILL DUE SOON REMINDER
    // =========================================================

    /**
     * Runs every day at 8:00 AM.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDueSoonReminders() {

        List<Bill> pendingBills =
                billRepository.findAll()
                        .stream()
                        .filter(b -> !b.isDeleted())
                        .filter(b -> "PENDING".equals(b.getStatus()))
                        .toList();

        int remindersSent = 0;

        for (Bill bill : pendingBills) {

            if (bill.getDueDate() != null
                    && !bill.getDueDate()
                    .isBefore(LocalDate.now())
                    && !bill.getDueDate()
                    .isAfter(
                            LocalDate.now().plusDays(3)
                    )) {

                Notification notification =
                        new Notification();

                notification.setHousehold(
                        bill.getHousehold()
                );

                notification.setApartment(
                        bill.getHousehold().getApartment()
                );

                notification.setTitle(
                        "Bill Due Soon"
                );

                notification.setMessage(
                        "Your bill for "
                                + bill.getBillingMonth()
                                + " (₹"
                                + bill.getAmount()
                                + ") is due on "
                                + bill.getDueDate()
                                + ". Please pay before the due date."
                );

                notification.setNotificationType(
                        "BILLING"
                );

                notification.setCreatedDate(
                        LocalDate.now()
                );

                notificationRepository.save(
                        notification
                );

                remindersSent++;
            }
        }

        System.out.println(
                "[Scheduled] Due-soon reminder check complete. "
                        + remindersSent
                        + " reminder(s) sent."
        );
    }
}