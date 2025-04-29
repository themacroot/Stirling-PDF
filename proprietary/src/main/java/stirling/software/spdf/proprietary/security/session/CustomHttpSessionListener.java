package stirling.software.spdf.proprietary.security.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "premium.enabled", havingValue = "true")
public class CustomHttpSessionListener implements HttpSessionListener {

    private final SessionPersistentRegistry sessionPersistentRegistry;

    @Autowired
    public CustomHttpSessionListener(
            @Lazy @Autowired(required = false)
                    SessionPersistentRegistry sessionPersistentRegistry) {
        super();
        this.sessionPersistentRegistry = sessionPersistentRegistry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {}

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        sessionPersistentRegistry.expireSession(se.getSession().getId());
    }
}
