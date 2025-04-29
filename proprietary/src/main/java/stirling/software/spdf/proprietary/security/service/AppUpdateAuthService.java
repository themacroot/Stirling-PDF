package stirling.software.spdf.proprietary.security.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import stirling.software.common.model.ApplicationProperties;
import stirling.software.spdf.proprietary.security.model.AdminInterface;
import stirling.software.spdf.proprietary.security.persistence.UserEntity;
import stirling.software.spdf.proprietary.security.persistence.repository.UserRepository;

@Service
@ConditionalOnProperty(name = "premium.enabled", havingValue = "true")
class AppUpdateAuthService implements AdminInterface {

    private final UserRepository userRepository;

    private final ApplicationProperties applicationProperties;

    public AppUpdateAuthService(
            @Autowired(required = false) UserRepository userRepository,
            ApplicationProperties applicationProperties) {
        this.userRepository = userRepository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public boolean getShowUpdateOnlyAdmins() {
        boolean showUpdate = applicationProperties.getSystem().isShowUpdate();
        if (!showUpdate) {
            return showUpdate;
        }
        boolean showUpdateOnlyAdmin = applicationProperties.getSystem().getShowUpdateOnlyAdmin();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return !showUpdateOnlyAdmin;
        }
        if (authentication.getName().equalsIgnoreCase("anonymousUser")) {
            return !showUpdateOnlyAdmin;
        }
        Optional<UserEntity> user = userRepository.findByUsername(authentication.getName());
        if (user.isPresent() && showUpdateOnlyAdmin) {
            return "ROLE_ADMIN".equals(user.get().getRolesAsString());
        }
        return showUpdate;
    }
}
