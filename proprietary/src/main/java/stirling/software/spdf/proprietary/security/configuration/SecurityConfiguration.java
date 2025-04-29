package stirling.software.spdf.proprietary.security.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml4AuthenticationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import stirling.software.common.model.ApplicationProperties;
import stirling.software.spdf.proprietary.security.FirstLoginFilter;
import stirling.software.spdf.proprietary.security.IPRateLimitingFilter;
import stirling.software.spdf.proprietary.security.UserAuthenticationFilter;
import stirling.software.spdf.proprietary.security.persistence.repository.JPATokenRepositoryImpl;
import stirling.software.spdf.proprietary.security.persistence.repository.PersistentLoginRepository;
import stirling.software.spdf.proprietary.security.service.CustomUserDetailsService;
import stirling.software.spdf.proprietary.security.service.LoginAttemptService;
import stirling.software.spdf.proprietary.security.service.UserService;
import stirling.software.spdf.proprietary.security.session.SessionPersistentRegistry;
import stirling.software.spdf.proprietary.security.sso.saml2.CustomSaml2AuthenticationFailureHandler;
import stirling.software.spdf.proprietary.security.sso.saml2.CustomSaml2AuthenticationSuccessHandler;
import stirling.software.spdf.proprietary.security.sso.saml2.CustomSaml2ResponseAuthenticationConverter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@DependsOn("runningProOrHigher")
public class SecurityConfiguration {

    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final boolean loginEnabledValue;
    private final boolean runningProOrHigher;

    private final ApplicationProperties applicationProperties;
    private final UserAuthenticationFilter userAuthenticationFilter;
    private final LoginAttemptService loginAttemptService;
    private final FirstLoginFilter firstLoginFilter;
    private final SessionPersistentRegistry sessionRegistry;
    private final PersistentLoginRepository persistentLoginRepository;
    private final GrantedAuthoritiesMapper oAuth2userAuthoritiesMapper;
    private final RelyingPartyRegistrationRepository saml2RelyingPartyRegistrations;
    private final OpenSaml4AuthenticationRequestResolver saml2AuthenticationRequestResolver;

    public SecurityConfiguration(
            @Lazy @Autowired(required = false) PersistentLoginRepository persistentLoginRepository,
            CustomUserDetailsService userDetailsService,
            @Lazy @Autowired(required = false) UserService userService,
            @Qualifier("loginEnabled") boolean loginEnabledValue,
            @Qualifier("runningProOrHigher") boolean runningProOrHigher,
            ApplicationProperties applicationProperties,
            @Lazy @Autowired(required = false) UserAuthenticationFilter userAuthenticationFilter,
            LoginAttemptService loginAttemptService,
            @Lazy @Autowired(required = false) FirstLoginFilter firstLoginFilter,
            @Lazy @Autowired(required = false) SessionPersistentRegistry sessionRegistry,
            @Autowired(required = false) GrantedAuthoritiesMapper oAuth2userAuthoritiesMapper,
            @Autowired(required = false)
                    RelyingPartyRegistrationRepository saml2RelyingPartyRegistrations,
            @Autowired(required = false)
                    OpenSaml4AuthenticationRequestResolver saml2AuthenticationRequestResolver) {
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.loginEnabledValue = loginEnabledValue;
        this.runningProOrHigher = runningProOrHigher;
        this.applicationProperties = applicationProperties;
        this.userAuthenticationFilter = userAuthenticationFilter;
        this.loginAttemptService = loginAttemptService;
        this.firstLoginFilter = firstLoginFilter;
        this.sessionRegistry = sessionRegistry;
        this.persistentLoginRepository = persistentLoginRepository;
        this.oAuth2userAuthoritiesMapper = oAuth2userAuthoritiesMapper;
        this.saml2RelyingPartyRegistrations = saml2RelyingPartyRegistrations;
        this.saml2AuthenticationRequestResolver = saml2AuthenticationRequestResolver;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (applicationProperties.getSecurity().getCsrfDisabled()
                || !loginEnabledValue) {
            http.csrf(csrf -> csrf.disable());
        }

        if (loginEnabledValue) {
            // Handle SAML
            if (applicationProperties.getSecurity().isSaml2Active()
                    && runningProOrHigher) {
                // Configure the authentication provider
                OpenSaml4AuthenticationProvider authenticationProvider =
                        new OpenSaml4AuthenticationProvider();
                authenticationProvider.setResponseAuthenticationConverter(
                        new CustomSaml2ResponseAuthenticationConverter(userService));
                http.authenticationProvider(authenticationProvider)
                        .saml2Login(
                                saml2 -> {
                                    try {
                                        saml2.loginPage("/saml2")
                                                .relyingPartyRegistrationRepository(
                                                        saml2RelyingPartyRegistrations)
                                                .authenticationManager(
                                                        new ProviderManager(authenticationProvider))
                                                .successHandler(
                                                        new CustomSaml2AuthenticationSuccessHandler(
                                                                applicationProperties
                                                                        .getSecurity()
                                                                        .getSaml2()
                                                                        .getAutoCreateUser(),
                                                                applicationProperties
                                                                        .getSecurity()
                                                                        .getSaml2()
                                                                        .getBlockRegistration(),
                                                                loginAttemptService,
                                                                userService))
                                                .failureHandler(
                                                        new CustomSaml2AuthenticationFailureHandler())
                                                .authenticationRequestResolver(
                                                        saml2AuthenticationRequestResolver);
                                    } catch (Exception e) {
                                        log.error("Error configuring SAML 2 login", e);
                                        throw new RuntimeException(e);
                                    }
                                });
            }
        } else {
            log.info("Login is not enabled. Using default authorisation.");
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        }
        return http.build();
    }

    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public IPRateLimitingFilter rateLimitingFilter() {
        // Example limit TODO add config level
        int maxRequestsPerIp = 1000000;
        return new IPRateLimitingFilter(maxRequestsPerIp, maxRequestsPerIp);
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        return new JPATokenRepositoryImpl(persistentLoginRepository);
    }

    @Bean
    public boolean activeSecurity() {
        return true;
    }
}
