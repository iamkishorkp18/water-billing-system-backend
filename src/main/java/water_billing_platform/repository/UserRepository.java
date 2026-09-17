package water_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import water_billing_platform.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByHouseholdIdAndRoleAndIsDeletedFalse(
            Long householdId,
            User.Role role
    );

    List<User> findByRoleAndHousehold_Apartment_Id(
            User.Role role,
            Long apartmentId
    );

    List<User> findByRoleAndStatusAndIsDeletedFalse(
            User.Role role,
            String status
    );

    List<User> findByRoleAndHousehold_Apartment_IdAndIsDeletedFalse(
            User.Role role,
            Long apartmentId
    );

    List<User> findByRoleAndIsDeletedTrue(
            User.Role role
    );

    List<User> findByIsDeletedTrue();
}