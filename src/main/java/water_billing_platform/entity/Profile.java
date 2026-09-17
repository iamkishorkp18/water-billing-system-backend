package water_billing_platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name")
    private String fullName;

    private Integer age;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "occupancy_type")
    private String occupancyType;

    @Column(name = "family_members")
    private Integer familyMembers;

    @Column(name = "profile_photo", columnDefinition = "bytea")
    private byte[] profilePhoto;

    @Column(name = "profile_photo_type")
    private String profilePhotoType;
}