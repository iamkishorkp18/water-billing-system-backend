package water_billing_platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "household_id")
    private Household household;

    @Column(nullable = false)
    private String status = "APPROVED";

    @Column(name = "full_name")
    private String fullName;

    private Integer age;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "occupancy_type")
    private String occupancyType;

    @Column(name = "family_members")
    private Integer familyMembers;

    @Column(name = "profile_photo", columnDefinition = "BYTEA")
    private byte[] profilePhoto;

    @Column(name = "profile_photo_type")
    private String profilePhotoType;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public enum Role {
        SUPER_ADMIN,
        COMMERCIAL_ADMIN,
        RESIDENT
    }
}