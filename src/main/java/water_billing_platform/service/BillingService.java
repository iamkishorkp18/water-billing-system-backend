package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import water_billing_platform.entity.Bill;
import water_billing_platform.entity.Household;
import water_billing_platform.entity.Payment;
import water_billing_platform.entity.SharedExpense;
import water_billing_platform.entity.TariffPlan;
import water_billing_platform.entity.WaterUsageLog;
import water_billing_platform.entity.Notification;
import water_billing_platform.entity.User;

import water_billing_platform.repository.BillRepository;
import water_billing_platform.repository.HouseholdRepository;
import water_billing_platform.repository.PaymentRepository;
import water_billing_platform.repository.SharedExpenseRepository;
import water_billing_platform.repository.TariffPlanRepository;
import water_billing_platform.repository.WaterUsageLogRepository;
import water_billing_platform.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository waterUsageLogRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final BillRepository billRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final InvoicePdfService invoicePdfService;
    private final EmailService emailService;


    // =========================================================
    // GENERATE BILL
    // =========================================================

    public Bill generateBill(Long householdId, String billingMonth) {

        Household household = householdRepository
                .findByIdAndIsDeletedFalse(householdId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Household not found or already deleted: "
                                        + householdId
                        )
                );

        List<WaterUsageLog> logs =
                waterUsageLogRepository
                        .findByHouseholdIdOrderByReadingDateDesc(
                                householdId
                        );

        if (logs.size() < 2) {
            throw new RuntimeException(
                    "Not enough meter readings to calculate a bill. "
                            + "Need at least 2 readings."
            );
        }

        // Latest reading = current month's reading entered by admin
        BigDecimal currentReading =
                logs.get(0).getMeterReading();

        // Previous saved reading = previous month's closing reading
        BigDecimal previousReading =
                logs.get(1).getMeterReading();

        // Calculate consumption between the two readings
        BigDecimal consumption =
                currentReading
                        .subtract(previousReading)
                        .abs();

        Long apartmentId =
                household.getApartment().getId();

        TariffPlan tariff =
                tariffPlanRepository
                        .findByApartmentId(apartmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No tariff plan set for this apartment"
                                )
                        );

        BigDecimal individualAmount =
                calculateTieredAmount(
                        consumption,
                        tariff
                );

        BigDecimal sharedShare =
                calculateSharedExpenseShare(
                        apartmentId,
                        billingMonth
                );

        BigDecimal totalAmount = individualAmount.add(sharedShare);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = totalAmount.abs(); // never negative
        }

        // =====================================================
        // CREATE BILL
        // =====================================================

        Bill bill = new Bill();

        bill.setHousehold(household);
        bill.setBillingMonth(billingMonth);

        bill.setPreviousReading(previousReading);
        bill.setCurrentReading(currentReading);
        bill.setConsumption(consumption);

        bill.setAmount(totalAmount);

        bill.setGeneratedDate(LocalDate.now());

        // Due 15 days after bill generation
        bill.setDueDate(
                LocalDate.now().plusDays(15)
        );

        bill.setDeleted(false);

        // =====================================================
        // SAVE BILL FIRST
        // =====================================================

        Bill savedBill =
                billRepository.save(bill);


        // =====================================================
        // CREATE IN-APP NOTIFICATION
        // =====================================================

        try {

            Notification notification =
                    new Notification();

            notification.setApartment(
                    household.getApartment()
            );

            notification.setHousehold(
                    household
            );

            notification.setTitle(
                    "Water Bill Generated"
            );

            notification.setMessage(
                    "Your water bill for "
                            + billingMonth
                            + " has been generated. "
                            + "Total amount: ₹"
                            + savedBill
                            .getAmount()
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
            );

            notification.setNotificationType(
                    "BILL_GENERATED"
            );

            notificationService.createNotification(
                    notification
            );

        } catch (Exception e) {

            // Notification failure should not
            // prevent bill generation

            System.err.println(
                    "Failed to create bill notification: "
                            + e.getMessage()
            );
        }


        // =====================================================
        // FIND RESIDENT AND SEND EMAIL
        // =====================================================

        try {

            userRepository
                    .findFirstByHouseholdIdAndRoleAndIsDeletedFalse(
                            householdId,
                            User.Role.RESIDENT
                    )
                    .ifPresent(resident -> {

                        try {

                            String email =
                                    resident.getEmail();

                            if (email == null ||
                                    email.trim().isEmpty()) {

                                System.err.println(
                                        "Resident email is empty "
                                                + "for household: "
                                                + householdId
                                );

                                return;
                            }

                            // =================================
                            // GENERATE BILL PDF
                            // =================================

                            byte[] pdf =
                                    invoicePdfService
                                            .generateCurrentPdf(
                                                    savedBill
                                            );

                            // =================================
                            // SEND EMAIL
                            // =================================

                            emailService.sendInvoiceEmail(
                                    email,

                                    "Water Bill - "
                                            + billingMonth,

                                    "Dear "
                                            + (
                                            resident.getFullName()
                                                    != null
                                                    ? resident
                                                    .getFullName()
                                                    : "Resident"
                                    )
                                            + ",\n\n"
                                            + "Your water bill for "
                                            + billingMonth
                                            + " has been generated.\n\n"
                                            + "Total Amount: ₹"
                                            + savedBill
                                            .getAmount()
                                            .setScale(
                                                    2,
                                                    RoundingMode.HALF_UP
                                            )
                                            + "\n\n"
                                            + "Please find your "
                                            + "water bill attached "
                                            + "to this email.\n\n"
                                            + "Thank you.",

                                    pdf,

                                    "water_bill_"
                                            + savedBill.getId()
                                            + ".pdf"
                            );

                            System.out.println(
                                    "Bill email sent successfully to: "
                                            + email
                            );

                        } catch (Exception e) {

                            // Email failure should NOT
                            // cancel bill generation

                            System.err.println(
                                    "Failed to send bill email: "
                                            + e.getMessage()
                            );
                        }

                    });

        } catch (Exception e) {

            // User lookup failure should NOT
            // cancel bill generation

            System.err.println(
                    "Failed to find resident for email: "
                            + e.getMessage()
            );
        }


        // =====================================================
        // RETURN SAVED BILL
        // =====================================================

        return savedBill;
    }


    // =========================================================
    // TIERED BILL CALCULATION
    // =========================================================

    private BigDecimal calculateTieredAmount(
            BigDecimal consumption,
            TariffPlan tariff) {

        BigDecimal tier1Limit =
                tariff.getTier1Limit();

        if (consumption.compareTo(tier1Limit) <= 0) {

            return consumption.multiply(
                    tariff.getTier1Rate()
            );

        } else {

            BigDecimal tier1Amount =
                    tier1Limit.multiply(
                            tariff.getTier1Rate()
                    );

            BigDecimal tier2Consumption =
                    consumption.subtract(
                            tier1Limit
                    );

            BigDecimal tier2Amount =
                    tier2Consumption.multiply(
                            tariff.getTier2Rate()
                    );

            return tier1Amount.add(
                    tier2Amount
            );
        }
    }


    // =========================================================
    // SHARED EXPENSE
    // =========================================================

    private BigDecimal calculateSharedExpenseShare(
            Long apartmentId,
            String billingMonth) {

        List<SharedExpense> expenses =
                sharedExpenseRepository
                        .findByApartmentIdAndBillingMonth(
                                apartmentId,
                                billingMonth
                        );

        if (expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSharedCost =
                expenses.stream()
                        .map(
                                SharedExpense::getTotalAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        long householdCount =
                householdRepository
                        .findByApartmentIdAndIsDeletedFalse(
                                apartmentId
                        )
                        .size();

        if (householdCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalSharedCost.divide(
                BigDecimal.valueOf(
                        householdCount
                ),
                2,
                RoundingMode.HALF_UP
        );
    }


    // =========================================================
    // MARK BILL AS PAID
    // =========================================================

    public Bill markAsPaid(
            Long billId,
            String paymentMethod) {

        Bill bill =
                billRepository
                        .findByIdAndIsDeletedFalse(
                                billId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill not found or already deleted: "
                                                + billId
                                )
                        );

        bill.setStatus("PAID");

        billRepository.save(bill);

        String transactionId =
                "TXN"
                        + System.currentTimeMillis();

        Payment payment =
                new Payment();

        payment.setBill(bill);

        payment.setHousehold(
                bill.getHousehold()
        );

        payment.setAmount(
                bill.getAmount()
        );

        payment.setPaymentMethod(
                paymentMethod != null
                        ? paymentMethod
                        : "MANUAL"
        );

        payment.setTransactionId(
                transactionId
        );

        payment.setPaymentDate(
                LocalDate.now()
        );

        paymentRepository.save(payment);

        return bill;
    }


    // =========================================================
    // GET PAYMENTS FOR HOUSEHOLD
    // =========================================================

    public List<Payment> getPaymentsForHousehold(
            Long householdId) {

        return paymentRepository
                .findByHouseholdIdOrderByPaymentDateDesc(
                        householdId
                );
    }


    // =========================================================
    // GET PAYMENTS FOR APARTMENT
    // =========================================================

    public List<Payment> getPaymentsForApartment(
            Long apartmentId) {

        return paymentRepository
                .findByHousehold_Apartment_IdOrderByPaymentDateDesc(
                        apartmentId
                );
    }


    // =========================================================
    // GET PAYMENT FOR BILL
    // =========================================================

    public Payment getPaymentForBill(
            Long billId) {

        return paymentRepository
                .findByBillIdOrderByPaymentDateDesc(
                        billId
                )
                .stream()
                .findFirst()
                .orElse(null);
    }


    // =========================================================
    // RECORD MANUAL PAYMENT
    // =========================================================

    public Bill recordManualPayment(
            Long billId,
            BigDecimal amount,
            String paymentMethod,
            String remarks,
            String collectedBy) {

        Bill bill =
                billRepository
                        .findByIdAndIsDeletedFalse(
                                billId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill not found or already deleted: "
                                                + billId
                                )
                        );

        String newStatus;

        if (amount.compareTo(
                bill.getAmount()
        ) >= 0) {

            newStatus = "PAID";

        } else {

            newStatus = "PARTIALLY_PAID";
        }

        bill.setStatus(newStatus);

        billRepository.save(bill);

        Payment payment =
                new Payment();

        payment.setBill(bill);

        payment.setHousehold(
                bill.getHousehold()
        );

        payment.setAmount(amount);

        payment.setPaymentMethod(
                paymentMethod != null
                        ? paymentMethod
                        : "CASH"
        );

        payment.setTransactionId(
                "TXN"
                        + System.currentTimeMillis()
        );

        payment.setPaymentDate(
                LocalDate.now()
        );

        payment.setCollectedBy(
                collectedBy
        );

        payment.setRemarks(
                remarks
        );

        payment.setStatus(
                newStatus
        );

        paymentRepository.save(payment);

        return bill;
    }


    // =========================================================
    // GET BILLS BY STATUS
    // =========================================================

    public List<Bill> getBillsByStatus(
            Long apartmentId,
            String status) {

        return billRepository
                .findByHousehold_Apartment_IdAndIsDeletedFalse(
                        apartmentId
                )
                .stream()
                .filter(b ->
                        status.equalsIgnoreCase(
                                b.getStatus()
                        )
                )
                .toList();
    }


    // =========================================================
    // SEARCH BILLS BY FLAT NUMBER
    // =========================================================

    public List<Bill> searchBillsByFlatNumber(
            Long apartmentId,
            String flatNumber) {

        return billRepository
                .findByHousehold_Apartment_IdAndIsDeletedFalse(
                        apartmentId
                )
                .stream()
                .filter(b ->
                        b.getHousehold() != null
                                &&
                                b.getHousehold()
                                        .getFlatNumber()
                                        .toLowerCase()
                                        .contains(
                                                flatNumber
                                                        .toLowerCase()
                                        )
                )
                .toList();
    }


    // =========================================================
    // GET ALL ACTIVE BILLS FOR APARTMENT
    // =========================================================

    public List<Bill> getBillsForApartment(
            Long apartmentId) {

        return billRepository
                .findByHousehold_Apartment_IdAndIsDeletedFalse(
                        apartmentId
                );
    }


    // =========================================================
    // GET HISTORY BILLS FOR HOUSEHOLD
    // =========================================================

    public List<Bill> getHistoryBills(
            Long householdId,
            Integer year,
            Integer month) {

        List<Bill> bills =
                billRepository
                        .findByHouseholdIdOrderByGeneratedDateDesc(
                                householdId
                        );

        return bills.stream()

                .filter(b -> !b.isDeleted())

                .filter(b -> {

                    String billingMonth =
                            b.getBillingMonth();

                    if (billingMonth == null) {
                        return false;
                    }

                    if (year != null &&
                            !billingMonth.startsWith(
                                    String.valueOf(year)
                            )) {

                        return false;
                    }

                    if (month != null) {

                        String selectedMonth =
                                String.format(
                                        "%02d",
                                        month
                                );

                        return billingMonth.endsWith(
                                "-" + selectedMonth
                        );
                    }

                    return true;
                })

                .toList();
    }


    // =========================================================
    // CURRENT BILLS FOR PDF
    // =========================================================

    public List<Bill> getCurrentBillsForPdf(
            Long householdId,
            Integer year,
            Integer month) {

        return filterBills(
                billRepository
                        .findByHouseholdIdOrderByGeneratedDateDesc(
                                householdId
                        ),
                year,
                month,
                false
        );
    }


    // =========================================================
    // PAID BILLS FOR PDF
    // =========================================================

    public List<Bill> getPaidBillsForPdf(
            Long householdId,
            Integer year,
            Integer month) {

        return filterBills(
                billRepository
                        .findByHouseholdIdOrderByGeneratedDateDesc(
                                householdId
                        ),
                year,
                month,
                true
        );
    }


    // =========================================================
    // FILTER BILLS
    // =========================================================

    private List<Bill> filterBills(
            List<Bill> bills,
            Integer year,
            Integer month,
            boolean paid) {

        return bills.stream()

                .filter(b ->
                        paid
                                ? "PAID".equalsIgnoreCase(
                                        b.getStatus()
                                )
                                : !"PAID".equalsIgnoreCase(
                                        b.getStatus()
                                )
                )

                .filter(b -> {

                    if (year == null &&
                            month == null) {

                        return true;
                    }

                    try {

                        String[] p =
                                b.getBillingMonth()
                                        .split("-");

                        int y =
                                Integer.parseInt(
                                        p[0]
                                );

                        int m =
                                Integer.parseInt(
                                        p[1]
                                );

                        return
                                (year == null ||
                                        y == year)
                                        &&
                                (month == null ||
                                        m == month);

                    } catch (Exception e) {

                        return false;
                    }
                })

                .toList();
    }
}