package stirling.software.spdf.proprietary.security.persistence.repository;

import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stirling.software.spdf.proprietary.security.persistence.AuthorityEntity;

@Repository
@ConditionalOnProperty(name = "premium.proFeatures.database", havingValue = "true")
public interface AuthorityRepository extends JpaRepository<AuthorityEntity, Long> {
    // Set<Authority> findByUsername(String username);
    Set<AuthorityEntity> findByUser_Username(String username);

    AuthorityEntity findByUserId(long user_id);
}
