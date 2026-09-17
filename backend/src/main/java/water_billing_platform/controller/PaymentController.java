package water_billing_platform.controller;

import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import water_billing_platform.entity.Bill;
import water_billing_platform.repository.BillRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final BillRepository billRepository;

    public PaymentController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @PostMapping("/create-order/{billId}")
    public ResponseEntity<?> createOrder(@PathVariable Long billId) {
        try {
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));

            if ("PAID".equalsIgnoreCase(bill.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "This bill is already paid."));
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // Razorpay wants amount in paise (smallest unit) as an integer
            BigDecimal amountInPaise = bill.getAmount().multiply(BigDecimal.valueOf(100))
                    .setScale(0, RoundingMode.HALF_UP);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise.intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "bill_" + bill.getId());

            Order order = client.orders.create(orderRequest);

            bill.setRazorpayOrderId(order.get("id"));
            billRepository.save(bill);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("keyId", keyId);
            response.put("billId", bill.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Could not create payment order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> payload) {
        try {
            String orderId = payload.get("razorpay_order_id");
            String paymentId = payload.get("razorpay_payment_id");
            String signature = payload.get("razorpay_signature");

            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            if (!isValid) {
                return ResponseEntity.status(400).body(Map.of("message", "Payment verification failed."));
            }

            Bill bill = billRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Bill not found for this order"));

            bill.setStatus("PAID");
            bill.setRazorpayPaymentId(paymentId);
            bill.setPaidAt(LocalDateTime.now());
            billRepository.save(bill);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment verified. Bill marked as paid.",
                    "billId", bill.getId(),
                    "status", bill.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Verification error: " + e.getMessage()));
        }
    }
}