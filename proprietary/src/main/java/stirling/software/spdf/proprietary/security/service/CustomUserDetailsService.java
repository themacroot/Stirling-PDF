package stirling.software.spdf.proprietary.security.service;

import java.util.Collection;
import java.util.Set;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import stirling.software.spdf.proprietary.security.persistence.AuthorityEntity;
import stirling.software.spdf.proprietary.security.persistence.UserEntity;
import stirling.software.spdf.proprietary.security.persistence.repository.UserRepository;

@Service
@ConditionalOnProperty(name = "premium.enabled", havingValue = "true")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final LoginAttemptService loginAttemptService;

    public CustomUserDetailsService(
            UserRepository userRepository, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "No user found with username: " + username));
        if (loginAttemptService.isBlocked(username)) {
            throw new LockedException(
                    "Your account has been locked due to too many failed login attempts.");
        }
        if (!user.hasPassword()) {
            throw new IllegalArgumentException("Password must not be null");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                getAuthorities(user.getAuthorities()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Set<AuthorityEntity> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .toList();
    }
}
