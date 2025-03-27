package stirling.software.SPDF.config.security.session;

import org.opensaml.core.config.InitializationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import stirling.software.SPDF.config.security.SAML2ConfigurationInterface;

@Slf4j
@Configuration
public class SAML2Configs {

    @PostConstruct
    public void initOpenSAML() {
        try {
            InitializationService.initialize();
            log.info("✅ OpenSAML initialized successfully");
        } catch (Throwable e) {
            log.error("❌ OpenSAML initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Bean
    public RelyingPartyRegistrationRepository saml2RelyingPartyRegistrations(
            @Autowired(required = false) SAML2ConfigurationInterface saml2Config) {
        log.info("Loaded jar from enterprise! {}", saml2Config.toString());
        return saml2Config.relyingPartyRegistrations();
    }

    @Bean
    public OpenSaml5AuthenticationProvider saml2AuthenticationProvider(
            @Autowired(required = false) SAML2ConfigurationInterface saml2Config) {
        log.info("Loaded jar from enterprise! {}", saml2Config.toString());
        return saml2Config.authenticationProvider();
    }

    @Bean
    public OpenSaml5AuthenticationRequestResolver saml2AuthenticationRequestResolver(
            @Autowired(required = false) SAML2ConfigurationInterface saml2Config) {
        log.info("Loaded jar from enterprise! {}", saml2Config.toString());
        return saml2Config.authenticationRequestResolver();
    }
}
