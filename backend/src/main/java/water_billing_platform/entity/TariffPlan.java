package water_billing_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TariffPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    // =========================
    // TIER 1
    // =========================

    @Column(name = "tier1_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal tier1Limit;

    @Column(name = "tier1_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal tier1Rate;

    // =========================
    // TIER 2
    // =========================

    @Column(name = "tier2_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal tier2Limit;

    @Column(name = "tier2_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal tier2Rate;

    // =========================
    // TIER 3
    // =========================

    @Column(name = "tier3_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal tier3Rate;
}