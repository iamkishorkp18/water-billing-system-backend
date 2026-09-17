package water_billing_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import water_billing_platform.entity.Apartment;
import water_billing_platform.entity.BulkWaterPurchase;
import water_billing_platform.entity.TariffPlan;

import water_billing_platform.repository.ApartmentRepository;
import water_billing_platform.repository.BulkWaterPurchaseRepository;
import water_billing_platform.repository.TariffPlanRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class BulkWaterPurchaseService {

    private final BulkWaterPurchaseRepository
            bulkWaterPurchaseRepository;

    private final ApartmentRepository
            apartmentRepository;

    private final TariffPlanRepository
            tariffPlanRepository;

    public BulkWaterPurchaseService(
            BulkWaterPurchaseRepository bulkWaterPurchaseRepository,
            ApartmentRepository apartmentRepository,
            TariffPlanRepository tariffPlanRepository) {

        this.bulkWaterPurchaseRepository =
                bulkWaterPurchaseRepository;

        this.apartmentRepository =
                apartmentRepository;

        this.tariffPlanRepository =
                tariffPlanRepository;
    }

    // =========================================================
    // CREATE PURCHASE
    // =========================================================

    public BulkWaterPurchase createPurchase(
            Long apartmentId,
            LocalDate purchaseDate,
            BigDecimal quantityPurchased,
            String supplierName,
            String remarks) {

        validateQuantity(quantityPurchased);

        Apartment apartment =
                apartmentRepository.findById(apartmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Apartment not found"
                                )
                        );

        TariffPlan tariffPlan =
                getTariffPlan(apartmentId);

        /*
         * AUTOMATIC CALCULATION
         *
         * quantity
         *      ↓
         * tariff plan
         *      ↓
         * tier calculation
         *      ↓
         * total cost
         */

        BigDecimal totalCost =
                calculateTieredCost(
                        quantityPurchased,
                        tariffPlan
                );

        BigDecimal costPerUnit =
                calculateCostPerUnit(
                        totalCost,
                        quantityPurchased
                );

        BulkWaterPurchase purchase =
                new BulkWaterPurchase();

        purchase.setApartment(apartment);

        purchase.setPurchaseDate(
                purchaseDate
        );

        purchase.setQuantityPurchased(
                quantityPurchased
        );

        purchase.setTotalCost(
                totalCost
        );

        purchase.setCostPerUnit(
                costPerUnit
        );

        purchase.setSupplierName(
                supplierName
        );

        purchase.setRemarks(
                remarks
        );

        return bulkWaterPurchaseRepository.save(
                purchase
        );
    }

    // =========================================================
    // UPDATE PURCHASE
    // =========================================================

    public BulkWaterPurchase updatePurchase(
            Long id,
            LocalDate purchaseDate,
            BigDecimal quantityPurchased,
            String supplierName,
            String remarks) {

        validateQuantity(quantityPurchased);

        BulkWaterPurchase purchase =
                bulkWaterPurchaseRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bulk purchase record not found"
                                )
                        );

        Long apartmentId =
                purchase.getApartment().getId();

        TariffPlan tariffPlan =
                getTariffPlan(apartmentId);

        /*
         * Recalculate automatically whenever
         * quantity is changed.
         */

        BigDecimal totalCost =
                calculateTieredCost(
                        quantityPurchased,
                        tariffPlan
                );

        BigDecimal costPerUnit =
                calculateCostPerUnit(
                        totalCost,
                        quantityPurchased
                );

        purchase.setPurchaseDate(
                purchaseDate
        );

        purchase.setQuantityPurchased(
                quantityPurchased
        );

        purchase.setTotalCost(
                totalCost
        );

        purchase.setCostPerUnit(
                costPerUnit
        );

        purchase.setSupplierName(
                supplierName
        );

        purchase.setRemarks(
                remarks
        );

        return bulkWaterPurchaseRepository.save(
                purchase
        );
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deletePurchase(Long id) {

        if (!bulkWaterPurchaseRepository.existsById(id)) {

            throw new RuntimeException(
                    "Bulk purchase record not found"
            );
        }

        bulkWaterPurchaseRepository.deleteById(id);
    }

    // =========================================================
    // GET PURCHASES
    // =========================================================

    public List<BulkWaterPurchase>
    getPurchasesForApartment(Long apartmentId) {

        return bulkWaterPurchaseRepository
                .findByApartmentIdOrderByPurchaseDateDesc(
                        apartmentId
                );
    }

    public List<BulkWaterPurchase>
    getPurchasesForApartmentInRange(
            Long apartmentId,
            LocalDate start,
            LocalDate end) {

        return bulkWaterPurchaseRepository
                .findByApartmentIdAndPurchaseDateBetweenOrderByPurchaseDateDesc(
                        apartmentId,
                        start,
                        end
                );
    }

    // =========================================================
    // GET TARIFF PLAN
    // =========================================================

    private TariffPlan getTariffPlan(
            Long apartmentId) {

        return tariffPlanRepository
                .findByApartmentId(apartmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No tariff plan found for apartment id: "
                                        + apartmentId
                        )
                );
    }

    // =========================================================
    // AUTOMATIC COST PER UNIT
    // =========================================================

    private BigDecimal calculateCostPerUnit(
            BigDecimal totalCost,
            BigDecimal quantity) {

        if (quantity == null ||
                quantity.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        return totalCost.divide(
                quantity,
                4,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // LATEST COST PER UNIT
    // =========================================================

    public BigDecimal getLatestCostPerUnit(
            Long apartmentId) {

        BulkWaterPurchase latestPurchase =
                bulkWaterPurchaseRepository
                        .findFirstByApartmentIdOrderByPurchaseDateDesc(
                                apartmentId
                        )
                        .orElse(null);

        if (latestPurchase != null &&
                latestPurchase.getCostPerUnit() != null) {

            return latestPurchase.getCostPerUnit();
        }

        TariffPlan tariffPlan =
                getTariffPlan(apartmentId);

        return tariffPlan.getTier1Rate()
                .setScale(
                        4,
                        RoundingMode.HALF_UP
                );
    }

    // =========================================================
    // TIERED COST CALCULATION
    // =========================================================

    public BigDecimal calculateTieredCost(
            BigDecimal consumption,
            TariffPlan tariffPlan) {

        if (consumption == null ||
                consumption.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal tier1Limit =
                tariffPlan.getTier1Limit();

        BigDecimal tier2Limit =
                tariffPlan.getTier2Limit();

        BigDecimal tier1Rate =
                tariffPlan.getTier1Rate();

        BigDecimal tier2Rate =
                tariffPlan.getTier2Rate();

        BigDecimal tier3Rate =
                tariffPlan.getTier3Rate();

        BigDecimal total =
                BigDecimal.ZERO;

        // =====================================================
        // TIER 1
        // =====================================================

        BigDecimal tier1Usage =
                consumption.min(tier1Limit);

        total =
                total.add(
                        tier1Usage.multiply(
                                tier1Rate
                        )
                );

        // =====================================================
        // TIER 2
        // =====================================================

        if (consumption.compareTo(
                tier1Limit
        ) > 0) {

            BigDecimal tier2Usage =
                    consumption
                            .min(tier2Limit)
                            .subtract(tier1Limit);

            if (tier2Usage.compareTo(
                    BigDecimal.ZERO
            ) > 0) {

                total =
                        total.add(
                                tier2Usage.multiply(
                                        tier2Rate
                                )
                        );
            }
        }

        // =====================================================
        // TIER 3
        // =====================================================

        if (consumption.compareTo(
                tier2Limit
        ) > 0) {

            BigDecimal tier3Usage =
                    consumption.subtract(
                            tier2Limit
                    );

            total =
                    total.add(
                            tier3Usage.multiply(
                                    tier3Rate
                            )
                    );
        }

        return total.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // VALIDATE QUANTITY
    // =========================================================

    private void validateQuantity(
            BigDecimal quantity) {

        if (quantity == null ||
                quantity.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            throw new IllegalArgumentException(
                    "Quantity purchased must be greater than zero."
            );
        }
    }

    // =========================================================
    // CSV IMPORT
    //
    // NEW CSV FORMAT:
    //
    // purchaseDate,quantityPurchased,supplierName,remarks
    //
    // totalCost is NOT required.
    // costPerUnit is NOT required.
    //
    // Both are calculated automatically.
    // =========================================================

    public int importCsv(
            Long apartmentId,
            MultipartFile file) throws Exception {

        Apartment apartment =
                apartmentRepository.findById(apartmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Apartment not found"
                                )
                        );

        TariffPlan tariffPlan =
                getTariffPlan(apartmentId);

        int count = 0;

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                // =================================================
                // HEADER
                // =================================================

                if (firstLine) {

                    firstLine = false;

                    if (line.toLowerCase()
                            .contains("purchase date")) {

                        continue;
                    }
                }

                // =================================================
                // CSV
                //
                // date,quantity,supplier,remarks
                // =================================================

                String[] values =
                        line.split(",", -1);

                if (values.length < 2) {

                    throw new IllegalArgumentException(
                            "Invalid CSV row. Required format: "
                                    + "purchaseDate,quantityPurchased,supplierName,remarks"
                                    + " | Row: "
                                    + line
                    );
                }

                LocalDate purchaseDate =
                        LocalDate.parse(
                                values[0].trim()
                        );

                BigDecimal quantity =
                        new BigDecimal(
                                values[1].trim()
                        );

                validateQuantity(quantity);

                String supplier =
                        values.length > 2 &&
                        !values[2].trim().isEmpty()
                                ? values[2].trim()
                                : null;

                String remarks =
                        values.length > 3 &&
                        !values[3].trim().isEmpty()
                                ? values[3].trim()
                                : null;

                // =================================================
                // AUTOMATIC COST CALCULATION
                // =================================================

                BigDecimal totalCost =
                        calculateTieredCost(
                                quantity,
                                tariffPlan
                        );

                BigDecimal costPerUnit =
                        calculateCostPerUnit(
                                totalCost,
                                quantity
                        );

                // =================================================
                // CREATE PURCHASE
                // =================================================

                BulkWaterPurchase purchase =
                        new BulkWaterPurchase();

                purchase.setApartment(
                        apartment
                );

                purchase.setPurchaseDate(
                        purchaseDate
                );

                purchase.setQuantityPurchased(
                        quantity
                );

                purchase.setTotalCost(
                        totalCost
                );

                purchase.setCostPerUnit(
                        costPerUnit
                );

                purchase.setSupplierName(
                        supplier
                );

                purchase.setRemarks(
                        remarks
                );

                bulkWaterPurchaseRepository.save(
                        purchase
                );

                count++;
            }
        }

        return count;
    }
}