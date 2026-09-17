package water_billing_platform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDTO {
    private Long billId;
    private BigDecimal amount;
    private String paymentMethod;
    private String remarks;
}