package water_billing_platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import water_billing_platform.entity.Profile;
import water_billing_platform.entity.User;
import water_billing_platform.repository.ProfileRepository;
import water_billing_platform.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public Profile getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {

                    Profile profile = new Profile();

                    profile.setUser(user);
                    profile.setFullName(user.getFullName());
                    profile.setAge(user.getAge());
                    profile.setPhoneNumber(user.getPhoneNumber());
                    profile.setOccupancyType(user.getOccupancyType());
                    profile.setFamilyMembers(user.getFamilyMembers());

                    return profileRepository.save(profile);
                });
    }

    public Profile updateProfile(
            String email,
            String fullName,
            Integer age,
            String phoneNumber,
            String occupancyType,
            Integer familyMembers) {

        Profile profile = getMyProfile(email);

        if (fullName != null && !fullName.isBlank()) {
            profile.setFullName(fullName.trim());
        }

        if (age != null) {
            profile.setAge(age);
        }

        if (phoneNumber != null) {
            profile.setPhoneNumber(phoneNumber);
        }

        if (occupancyType != null) {
            profile.setOccupancyType(occupancyType);
        }

        if (familyMembers != null) {
            profile.setFamilyMembers(familyMembers);
        }

        return profileRepository.save(profile);
    }

    public Profile updatePhoto(
            String email,
            MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Please select a profile image"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {
            throw new RuntimeException(
                    "Only image files are allowed"
            );
        }

        try {

            Profile profile = getMyProfile(email);

            profile.setProfilePhoto(file.getBytes());
            profile.setProfilePhotoType(contentType);

            return profileRepository.save(profile);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to save profile photo"
            );
        }
    }

    public byte[] getPhoto(String email) {

        Profile profile = getMyProfile(email);

        if (profile.getProfilePhoto() == null ||
                profile.getProfilePhoto().length == 0) {

            throw new RuntimeException(
                    "Profile photo not available"
            );
        }

        return profile.getProfilePhoto();
    }

    public String getPhotoType(String email) {

        Profile profile = getMyProfile(email);

        return profile.getProfilePhotoType();
    }
}