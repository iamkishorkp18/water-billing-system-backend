package water_billing_platform.service;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;
import water_billing_platform.entity.*;
import water_billing_platform.repository.TariffPlanRepository;

import java.io.*;
import java.util.List;

@Service
public class InvoicePdfService {

    private final TariffPlanRepository tariffRepo;

    public InvoicePdfService(TariffPlanRepository tariffRepo) {
        this.tariffRepo = tariffRepo;
    }

    public byte[] generateCurrentPdf(Bill b) throws IOException {
        return singlePdf(b, null, "CURRENT WATER BILL");
    }

    public byte[] generatePaidPdf(Bill b, Payment p) throws IOException {
        return singlePdf(b, p, "PAID WATER BILL");
    }

    public byte[] generateAllCurrentPdf(List<Bill> bills) throws IOException {
        return multiplePdf(bills, "CURRENT WATER BILLS", false);
    }

    public byte[] generateAllPaidPdf(List<Bill> bills) throws IOException {
        return multiplePdf(bills, "PAID WATER BILL HISTORY", true);
    }

    private byte[] singlePdf(Bill b, Payment p, String title) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                float y = 780;
                float x = 50;

                c.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                write(c, title, x, y);
                y -= 40;

                c.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);

                y = write(c, "Apartment: " + b.getHousehold().getApartment().getName(), x, y);
                y = write(c, "Flat Number: " + b.getHousehold().getFlatNumber(), x, y);
                y = write(c, "Billing Month: " + b.getBillingMonth(), x, y);
                y -= 10;

                if (p == null) {
                    y = write(c, "Bill Amount: Rs. " + b.getAmount(), x, y);
                    y = write(c, "Due Date: " + b.getDueDate(), x, y);
                    y = write(c, "Status: " + b.getStatus(), x, y);
                } else {
                    y = write(c, "Paid Amount: Rs. " + p.getAmount(), x, y);
                    y = write(c, "Payment Date: " + p.getPaymentDate(), x, y);
                    y = write(c, "Payment Method: " + p.getPaymentMethod(), x, y);
                    y = write(c, "Transaction ID: " + p.getTransactionId(), x, y);
                    y = write(c, "Status: PAID", x, y);
                }

                y -= 20;
                tariff(c, b, x, y);
            }

            return save(doc);
        }
    }

    private byte[] multiplePdf(List<Bill> bills, String title, boolean paid)
            throws IOException {

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream c = new PDPageContentStream(doc, page)) {
                float y = 780;
                float x = 40;

                c.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                write(c, title, x, y);
                y -= 35;

                c.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);

                if (bills.isEmpty()) {
                    write(c, "No bills found for the selected period.", x, y);
                } else {
                    for (Bill b : bills) {
                        if (y < 100) {
                            c.close();
                            page = new PDPage(PDRectangle.A4);
                            doc.addPage(page);
                            throw new IOException("Too many bills. Please use a smaller date range.");
                        }

                        String line = b.getBillingMonth()
                                + "    Rs. " + b.getAmount()
                                + "    "
                                + (paid ? "PAID" : b.getStatus());

                        write(c, line, x, y);
                        y -= 20;
                    }

                    y -= 15;
                    write(c, "Total Bills: " + bills.size(), x, y);

                    var total = bills.stream()
                            .map(Bill::getAmount)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                    y -= 20;
                    write(c, "Total Amount: Rs. " + total, x, y);

                    y -= 30;
                    tariff(c, bills.get(0), x, y);
                }
            }

            return save(doc);
        }
    }

    private void tariff(PDPageContentStream c, Bill b, float x, float y)
            throws IOException {

        var t = tariffRepo.findByApartmentId(b.getHousehold().getApartment().getId())
                .orElse(null);

        write(c, "Tariff Plan Details", x, y);

        if (t == null) {
            write(c, "Tariff information not available.", x, y - 20);
            return;
        }

        write(c, "Tier 1: " + t.getTier1Limit() + " units - Rs. "
                + t.getTier1Rate(), x, y - 20);

        write(c, "Tier 2: " + t.getTier2Limit() + " units - Rs. "
                + t.getTier2Rate(), x, y - 40);

        write(c, "Tier 3: Above Tier 2 - Rs. "
                + t.getTier3Rate(), x, y - 60);
    }

    private float write(PDPageContentStream c, String text, float x, float y)
            throws IOException {

        c.beginText();
        c.newLineAtOffset(x, y);
        c.showText(text == null ? "" : text);
        c.endText();
        return y - 20;
    }

    private byte[] save(PDDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        return out.toByteArray();
    }
}