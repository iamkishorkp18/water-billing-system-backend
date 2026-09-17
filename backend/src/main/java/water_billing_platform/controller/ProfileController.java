package water_billing_platform.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import water_billing_platform.entity.Profile;
import water_billing_platform.service.ProfileService;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public Profile getMyProfile(Authentication authentication) {
        return profileService.getMyProfile(authentication.getName());
    }

    @PutMapping("/me")
    public Profile updateProfile(
            Authentication authentication,
            @RequestBody ProfileUpdateRequest request) {

        return profileService.updateProfile(
                authentication.getName(),
                request.getFullName(),
                request.getAge(),
                request.getPhoneNumber(),
                request.getOccupancyType(),
                request.getFamilyMembers()
        );
    }

    @PostMapping(
            value = "/me/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Profile updatePhoto(
            Authentication authentication,
            @RequestParam("photo") MultipartFile photo) {

        return profileService.updatePhoto(
                authentication.getName(),
                photo
        );
    }

    @GetMapping("/me/photo")
    public ResponseEntity<byte[]> getPhoto(
            Authentication authentication) {

        byte[] photo = profileService.getPhoto(
                authentication.getName()
        );

        String photoType = profileService.getPhotoType(
                authentication.getName()
        );

        MediaType type = MediaType.IMAGE_JPEG;

        if (photoType != null && !photoType.isBlank()) {
            try {
                type = MediaType.parseMediaType(photoType);
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(type)
                .body(photo);
    }

    @Data
    public static class ProfileUpdateRequest {

        private String fullName;
        private Integer age;
        private String phoneNumber;
        private String occupancyType;
        private Integer familyMembers;
    }
}