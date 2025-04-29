package stirling.software.spdf.proprietary.security;

import com.coveo.saml.SamlClient;
import com.coveo.saml.SamlException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import stirling.software.common.model.ApplicationProperties;
import stirling.software.spdf.proprietary.security.sso.saml2.CustomSaml2AuthenticatedPrincipal;
import stirling.software.spdf.proprietary.security.util.CertificateUtil;

@Slf4j
@AllArgsConstructor
// todo: figure out way for OSS version to use this class
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    public static final String LOGOUT_PATH = "/login?logout=true";

    private final ApplicationProperties applicationProperties;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        if (!response.isCommitted()) {
            if (authentication != null) {
                if (authentication instanceof Saml2Authentication samlAuthentication) {
                    // Handle SAML2 logout redirection
                    getRedirect_saml2(request, response, samlAuthentication);
                } else {
                    // Handle unknown authentication types
                    log.error(
                            "Authentication class unknown: {}",
                            authentication.getClass().getSimpleName());
                    getRedirectStrategy().sendRedirect(request, response, LOGOUT_PATH);
                }
            } else {
                // Redirect to login page after logout
                String path = checkForErrors(request);
                getRedirectStrategy().sendRedirect(request, response, path);
            }
        }
    }

    // Redirect for SAML2 authentication logout
    private void getRedirect_saml2(
            HttpServletRequest request,
            HttpServletResponse response,
            Saml2Authentication samlAuthentication)
            throws IOException {

        ApplicationProperties.Security.SAML2 samlConf =
                applicationProperties.getSecurity().getSaml2();
        String registrationId = samlConf.getRegistrationId();

        CustomSaml2AuthenticatedPrincipal principal =
                (CustomSaml2AuthenticatedPrincipal) samlAuthentication.getPrincipal();

        String nameIdValue = principal.name();

        try {
            // Read certificate from the resource
            Resource certificateResource = samlConf.getSpCert();
            X509Certificate certificate = CertificateUtil.readCertificate(certificateResource);

            List<X509Certificate> certificates = new ArrayList<>();
            certificates.add(certificate);

            // Construct URLs required for SAML configuration
            SamlClient samlClient = getSamlClient(registrationId, samlConf, certificates);

            // Read private key for service provider
            Resource privateKeyResource = samlConf.getPrivateKey();
            RSAPrivateKey privateKey = CertificateUtil.readPrivateKey(privateKeyResource);

            // Set service provider keys for the SamlClient
            samlClient.setSPKeys(certificate, privateKey);

            // Redirect to identity provider for logout
            samlClient.redirectToIdentityProvider(response, null, nameIdValue);
        } catch (Exception e) {
            log.error(
                    "Error retrieving logout URL from Provider {} for user {}",
                    samlConf.getProvider(),
                    nameIdValue,
                    e);
            getRedirectStrategy().sendRedirect(request, response, LOGOUT_PATH);
        }
    }

    private static SamlClient getSamlClient(
            String registrationId,
            ApplicationProperties.Security.SAML2 samlConf,
            List<X509Certificate> certificates)
            throws SamlException {
        // todo: move base url to util or configuration class
        String serverUrl = "http://localhost:8080";
        //                SPDFApplication.getStaticBaseUrl() + ":" +
        // SPDFApplication.getStaticPort();

        String relyingPartyIdentifier =
                serverUrl + "/saml2/service-provider-metadata/" + registrationId;

        String assertionConsumerServiceUrl = serverUrl + "/login/saml2/sso/" + registrationId;

        String idpSLOUrl = samlConf.getIdpSingleLogoutUrl();

        String idpIssuer = samlConf.getIdpIssuer();

        // Create SamlClient instance for SAML logout
        return new SamlClient(
                relyingPartyIdentifier,
                assertionConsumerServiceUrl,
                idpSLOUrl,
                idpIssuer,
                certificates,
                SamlClient.SamlIdpBinding.POST);
    }

    /**
     * Handles different error scenarios during logout. Will return a <code>String</code> containing
     * the error request parameter.
     *
     * @param request the user's <code>HttpServletRequest</code> request.
     * @return a <code>String</code> containing the error request parameter.
     */
    private String checkForErrors(HttpServletRequest request) {
        String errorMessage;
        String path = "logout=true";

        if (request.getParameter("oAuth2AuthenticationErrorWeb") != null) {
            path = "errorOAuth=userAlreadyExistsWeb";
        } else if ((errorMessage = request.getParameter("errorOAuth")) != null) {
            path = "errorOAuth=" + sanitizeInput(errorMessage);
        } else if (request.getParameter("oAuth2AutoCreateDisabled") != null) {
            path = "errorOAuth=oAuth2AutoCreateDisabled";
        } else if (request.getParameter("oAuth2AdminBlockedUser") != null) {
            path = "errorOAuth=oAuth2AdminBlockedUser";
        } else if (request.getParameter("userIsDisabled") != null) {
            path = "errorOAuth=userIsDisabled";
        } else if ((errorMessage = request.getParameter("error")) != null) {
            path = "errorOAuth=" + sanitizeInput(errorMessage);
        } else if (request.getParameter("badCredentials") != null) {
            path = "errorOAuth=badCredentials";
        }

        return path;
    }

    /**
     * Sanitize input to avoid potential security vulnerabilities. Will return a sanitised <code>
     * String</code>.
     *
     * @return a sanitised <code>String</code>
     */
    private String sanitizeInput(String input) {
        return input.replaceAll("[^a-zA-Z0-9 ]", "");
    }
}
