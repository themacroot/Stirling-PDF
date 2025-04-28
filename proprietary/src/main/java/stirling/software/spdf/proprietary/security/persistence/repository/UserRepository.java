package stirling.software.spdf.proprietary.security.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import stirling.software.spdf.proprietary.security.persistence.UserEntity;

@Repository
@ConditionalOnProperty(name = "premium.proFeatures.database", havingValue = "true")
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    @Query(
            "FROM UserEntity u LEFT JOIN FETCH u.settings where upper(u.username) = upper(:username)")
    Optional<UserEntity> findByUsernameIgnoreCaseWithSettings(@Param("username") String username);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByApiKey(String apiKey);

    List<UserEntity> findByAuthenticationTypeIgnoreCase(String authenticationType);
}
