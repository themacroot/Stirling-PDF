package stirling.software.SPDF.config.security;

import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;

public interface SAML2ConfigurationInterface {

    OpenSaml5AuthenticationProvider authenticationProvider();

    RelyingPartyRegistrationRepository relyingPartyRegistrations();

    OpenSaml5AuthenticationRequestResolver authenticationRequestResolver();
}
