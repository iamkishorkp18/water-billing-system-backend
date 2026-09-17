package water_billing_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import water_billing_platform.entity.Profile;

public interface ProfileRepository
        extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);
}
