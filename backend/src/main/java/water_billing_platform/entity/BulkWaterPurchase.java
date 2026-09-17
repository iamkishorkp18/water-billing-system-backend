package water_billing_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_water_purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkWaterPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(
        name = "quantity_purchased",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal quantityPurchased;

    @Column(
        name = "total_cost",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalCost;

    @Column(
        name = "cost_per_unit",
        nullable = false,
        precision = 12,
        scale = 4
    )
    private BigDecimal costPerUnit;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}