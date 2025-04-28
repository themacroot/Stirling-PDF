package stirling.software.spdf.proprietary.security.persistence.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stirling.software.spdf.proprietary.security.persistence.PersistentLoginEntity;

@Repository
@ConditionalOnProperty(name = "premium.proFeatures.database", havingValue = "true")
public interface PersistentLoginRepository extends JpaRepository<PersistentLoginEntity, String> {
    void deleteByUsername(String username);
}
