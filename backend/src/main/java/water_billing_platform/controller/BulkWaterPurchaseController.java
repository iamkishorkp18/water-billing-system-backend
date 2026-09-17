package water_billing_platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import water_billing_platform.entity.BulkWaterPurchase;
import water_billing_platform.service.BulkWaterPurchaseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bulk-purchases")
@CrossOrigin(origins = "http://localhost:5173")
public class BulkWaterPurchaseController {

    private final BulkWaterPurchaseService bulkWaterPurchaseService;

    public BulkWaterPurchaseController(
            BulkWaterPurchaseService bulkWaterPurchaseService) {

        this.bulkWaterPurchaseService =
                bulkWaterPurchaseService;
    }

    // =========================================================
    // CREATE BULK PURCHASE
    // TOTAL COST IS NOW CALCULATED AUTOMATICALLY
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('COMMERCIAL_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createPurchase(
            @RequestBody Map<String, Object> body) {

        try {

            Long apartmentId =
                    Long.valueOf(
                            body.get("apartmentId").toString()
                    );

            LocalDate purchaseDate =
                    LocalDate.parse(
                            body.get("purchaseDate").toString()
                    );

            BigDecimal quantityPurchased =
                    new BigDecimal(
                            body.get("quantityPurchased").toString()
                    );

            String supplierName =
                    body.get("supplierName") != null
                            ? body.get("supplierName").toString()
                            : null;

            String remarks =
                    body.get("remarks") != null
                            ? body.get("remarks").toString()
                            : null;

            BulkWaterPurchase saved =
                    bulkWaterPurchaseService.createPurchase(
                            apartmentId,
                            purchaseDate,
                            quantityPurchased,
                            supplierName,
                            remarks
                    );

            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to record purchase: "
                                            + e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // UPDATE BULK PURCHASE
    // TOTAL COST IS CALCULATED AGAIN AUTOMATICALLY
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMMERCIAL_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updatePurchase(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        try {

            LocalDate date =
                    LocalDate.parse(
                            body.get("purchaseDate").toString()
                    );

            BigDecimal quantity =
                    new BigDecimal(
                            body.get("quantityPurchased").toString()
                    );

            String supplier =
                    body.get("supplierName") == null
                            ? null
                            : body.get("supplierName").toString();

            String remarks =
                    body.get("remarks") == null
                            ? null
                            : body.get("remarks").toString();

            BulkWaterPurchase updated =
                    bulkWaterPurchaseService.updatePurchase(
                            id,
                            date,
                            quantity,
                            supplier,
                            remarks
                    );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to update purchase: "
                                            + e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COMMERCIAL_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deletePurchase(
            @PathVariable Long id) {

        try {

            bulkWaterPurchaseService.deletePurchase(id);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Purchase record deleted."
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(500)
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =========================================================
    // GET PURCHASES
    // =========================================================

    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<BulkWaterPurchase>>
    getForApartment(
            @PathVariable Long apartmentId) {

        return ResponseEntity.ok(
                bulkWaterPurchaseService
                        .getPurchasesForApartment(apartmentId)
        );
    }

    // =========================================================
    // GET LATEST AUTOMATIC COST PER UNIT
    // =========================================================

    @GetMapping("/apartment/{apartmentId}/cost-per-unit")
    public ResponseEntity<Map<String, BigDecimal>>
    getLatestCostPerUnit(
            @PathVariable Long apartmentId) {

        return ResponseEntity.ok(
                Map.of(
                        "costPerUnit",
                        bulkWaterPurchaseService
                                .getLatestCostPerUnit(apartmentId)
                )
        );
    }

    // =========================================================
    // CSV UPLOAD
    //
    // CSV NOW NEEDS ONLY:
    //
    // purchaseDate,quantityPurchased,supplierName,remarks
    //
    // NO totalCost
    // NO costPerUnit
    // =========================================================

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('COMMERCIAL_ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> uploadCsv(
            @RequestParam("apartmentId") Long apartmentId,
            @RequestParam("file") MultipartFile file) {

        try {

            if (file == null || file.isEmpty()) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "CSV file is empty."
                                )
                        );
            }

            String filename =
                    file.getOriginalFilename();

            if (filename == null ||
                    !filename.toLowerCase().endsWith(".csv")) {

                return ResponseEntity.badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Only CSV files are allowed."
                                )
                        );
            }

            int count =
                    bulkWaterPurchaseService.importCsv(
                            apartmentId,
                            file
                    );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            count +
                                    " bulk purchase records imported successfully with automatic cost calculation.",
                            "count",
                            count
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Failed to import CSV: "
                                            + e.getMessage()
                            )
                    );
        }
    }
}