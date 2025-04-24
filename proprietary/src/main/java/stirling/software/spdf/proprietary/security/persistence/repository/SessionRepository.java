package stirling.software.spdf.proprietary.security.persistence.repository;

import java.util.Date;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import stirling.software.spdf.proprietary.security.persistence.SessionEntity;

@Repository
@ConditionalOnProperty(name = "premium.proFeatures.database", havingValue = "true")
public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    List<SessionEntity> findByPrincipalName(String principalName);

    List<SessionEntity> findByExpired(boolean expired);

    SessionEntity findBySessionId(String sessionId);

    @Modifying
    @Transactional
    @Query(
            "UPDATE SessionEntity s SET s.expired = :expired, s.lastRequest = :lastRequest WHERE s.principalName = :principalName")
    void saveByPrincipalName(
            @Param("expired") boolean expired,
            @Param("lastRequest") Date lastRequest,
            @Param("principalName") String principalName);
}
