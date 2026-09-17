package water_billing_platform.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.*;
import water_billing_platform.repository.BillRepository;
import water_billing_platform.service.*;
import water_billing_platform.dto.PaymentDTO;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final InvoicePdfService invoicePdfService;
    private final BillRepository billRepository;
    private final EmailService emailService;
    private final AccessControlService accessControlService;

    @PostMapping("/generate")
    public Bill generateBill(@RequestParam Long householdId,
                             @RequestParam String billingMonth) {
        return billingService.generateBill(householdId, billingMonth);
    }

    @GetMapping("/apartment/{apartmentId}")
    public List<Bill> getBillsForApartment(@PathVariable Long apartmentId) {
        return billingService.getBillsForApartment(apartmentId);
    }

    @GetMapping("/household/{householdId}")
    public List<Bill> getBillsForHousehold(
            @PathVariable Long householdId,
            Authentication authentication) {

        accessControlService.verifyResidentOwnsHousehold(
                authentication.getName(), householdId);

        return billRepository.findByHouseholdIdOrderByGeneratedDateDesc(householdId);
    }

    // SINGLE CURRENT BILL
    @GetMapping("/{billId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long billId)
            throws IOException {

        Bill bill = billRepository.findByIdAndIsDeletedFalse(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        return pdf(invoicePdfService.generateCurrentPdf(bill),
                "current_bill_" + billId + ".pdf");
    }

    // SINGLE PAID HISTORY BILL
    @GetMapping("/{billId}/paid-invoice")
    public ResponseEntity<byte[]> downloadPaidInvoice(
            @PathVariable Long billId) throws IOException {

        Bill bill = billRepository.findByIdAndIsDeletedFalse(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!"PAID".equalsIgnoreCase(bill.getStatus()))
            throw new RuntimeException("Bill is not paid");

        Payment payment = billingService
                .getPaymentsForHousehold(bill.getHousehold().getId())
                .stream()
                .filter(p -> p.getBill().getId().equals(billId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return pdf(invoicePdfService.generatePaidPdf(bill, payment),
                "paid_bill_" + billId + ".pdf");
    }

    // ALL CURRENT / PENDING BILLS
    @GetMapping("/household/{householdId}/current-pdf")
    public ResponseEntity<byte[]> downloadAllCurrentBills(
            @PathVariable Long householdId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) throws IOException {

        List<Bill> bills =
                billingService.getCurrentBillsForPdf(householdId, year, month);

        return pdf(invoicePdfService.generateAllCurrentPdf(bills),
                "current_bills.pdf");
    }

    // ALL PAID / HISTORY BILLS
    @GetMapping("/household/{householdId}/history-pdf")
    public ResponseEntity<byte[]> downloadAllPaidBills(
            @PathVariable Long householdId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) throws IOException {

        List<Bill> bills =
                billingService.getPaidBillsForPdf(householdId, year, month);

        return pdf(invoicePdfService.generateAllPaidPdf(bills),
                "paid_bill_history.pdf");
    }

    private ResponseEntity<byte[]> pdf(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @PostMapping("/{billId}/send-email")
    public String emailInvoice(@PathVariable Long billId,
                               @RequestParam String toEmail)
            throws IOException, MessagingException {

        Bill bill = billRepository.findByIdAndIsDeletedFalse(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        byte[] pdf = invoicePdfService.generateCurrentPdf(bill);

        emailService.sendInvoiceEmail(
                toEmail,
                "Water Bill - " + bill.getBillingMonth(),
                "Please find your water bill attached.",
                pdf,
                "invoice_" + billId + ".pdf"
        );

        return "Invoice emailed successfully";
    }

    @PostMapping("/{billId}/mark-paid")
    public Bill markAsPaid(
            @PathVariable Long billId,
            @RequestParam(required = false) String paymentMethod) {
        return billingService.markAsPaid(billId, paymentMethod);
    }

    @GetMapping("/payments/household/{householdId}")
    public List<Payment> getPaymentsForHousehold(
            @PathVariable Long householdId) {
        return billingService.getPaymentsForHousehold(householdId);
    }

    @GetMapping("/payments/apartment/{apartmentId}")
    public List<Payment> getPaymentsForApartment(
            @PathVariable Long apartmentId) {
        return billingService.getPaymentsForApartment(apartmentId);
    }

    @PostMapping("/record-payment")
    public Bill recordManualPayment(
            @RequestBody PaymentDTO dto,
            Authentication authentication) {

        return billingService.recordManualPayment(
                dto.getBillId(),
                dto.getAmount(),
                dto.getPaymentMethod(),
                dto.getRemarks(),
                authentication.getName()
        );
    }

    @GetMapping("/apartment/{apartmentId}/status/{status}")
    public List<Bill> getBillsByStatus(
            @PathVariable Long apartmentId,
            @PathVariable String status) {
        return billingService.getBillsByStatus(apartmentId, status);
    }

    @GetMapping("/apartment/{apartmentId}/search")
    public List<Bill> searchByFlat(
            @PathVariable Long apartmentId,
            @RequestParam String flatNumber) {
        return billingService.searchBillsByFlatNumber(apartmentId, flatNumber);
    }
}