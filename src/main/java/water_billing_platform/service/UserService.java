package water_billing_platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import water_billing_platform.entity.User;
import water_billing_platform.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // CREATE USER
    // =========================================================

    public User createUser(User user) {

        if (user.getRole() == User.Role.SUPER_ADMIN) {
            throw new RuntimeException(
                    "Super Admin registration is not permitted. Contact the system owner."
            );
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "Email already registered: " + user.getEmail()
            );
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        if (user.getRole() == User.Role.COMMERCIAL_ADMIN) {
            user.setStatus("PENDING");
        } else {
            user.setStatus("APPROVED");
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setDeletedBy(null);

        return userRepository.save(user);
    }

    // =========================================================
    // FIND USER BY EMAIL
    // =========================================================

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // =========================================================
    // APPROVE ADMIN
    // =========================================================

    public User approveAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId
                        )
                );

        user.setStatus("APPROVED");

        return userRepository.save(user);
    }

    // =========================================================
    // REJECT ADMIN
    // =========================================================

    public User rejectAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId
                        )
                );

        user.setStatus("REJECTED");

        return userRepository.save(user);
    }

    // =========================================================
    // PENDING COMMERCIAL ADMINS
    // =========================================================

    public List<User> getPendingAdmins() {

        return userRepository
                .findByRoleAndStatusAndIsDeletedFalse(
                        User.Role.COMMERCIAL_ADMIN,
                        "PENDING"
                );
    }

    // =========================================================
    // ACTIVE COMMERCIAL ADMINS
    // =========================================================

    public List<User> getAllCommercialAdmins() {

        return userRepository
                .findByRoleAndStatusAndIsDeletedFalse(
                        User.Role.COMMERCIAL_ADMIN,
                        "APPROVED"
                );
    }

    // =========================================================
    // DELETE COMMERCIAL ADMIN
    // =========================================================

    public User deleteCommercialAdmin(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Commercial Admin not found: " + id
                        )
                );

        if (user.getRole() != User.Role.COMMERCIAL_ADMIN) {
            throw new RuntimeException(
                    "User is not a Commercial Admin"
            );
        }

        if (user.isDeleted()) {
            throw new RuntimeException(
                    "Commercial Admin is already deleted"
            );
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // =========================================================
    // GET ACTIVE RESIDENTS BY APARTMENT
    // =========================================================

    public List<User> getResidentsForApartment(Long apartmentId) {

        return userRepository
                .findByRoleAndHousehold_Apartment_IdAndIsDeletedFalse(
                        User.Role.RESIDENT,
                        apartmentId
                );
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    public User updateProfile(
            String email,
            String fullName,
            Integer age,
            String phoneNumber,
            String occupancyType,
            Integer familyMembers
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        if (age != null && age > 0) {
            user.setAge(age);
        }

        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim());
        }

        if (occupancyType != null) {
            user.setOccupancyType(occupancyType);
        }

        if (familyMembers != null && familyMembers >= 0) {
            user.setFamilyMembers(familyMembers);
        }

        return userRepository.save(user);
    }

    // =========================================================
    // UPDATE PROFILE PHOTO
    // =========================================================

    public User updateProfilePhoto(
            String email,
            MultipartFile photo
    ) throws Exception {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException(
                    "Please select an image"
            );
        }

        String contentType = photo.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new RuntimeException(
                    "Only image files are allowed"
            );
        }

        if (photo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException(
                    "Image size must be below 5 MB"
            );
        }

        user.setProfilePhoto(
                photo.getBytes()
        );

        user.setProfilePhotoType(
                contentType
        );

        return userRepository.save(user);
    }
}