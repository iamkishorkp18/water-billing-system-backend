package water_billing_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Required only for RESIDENT complaints
    @ManyToOne
    @JoinColumn(name = "household_id", nullable = true)
    private Household household;

    // Resident email or Commercial Admin email/name
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_by_role", nullable = false)
    private String createdByRole;

    @Column(name = "complaint_type", nullable = false)
    private String complaintType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "resolved_date")
    private LocalDate resolvedDate;
}