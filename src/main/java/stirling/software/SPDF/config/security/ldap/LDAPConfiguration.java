package stirling.software.SPDF.config.security.ldap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import stirling.software.SPDF.model.ApplicationProperties;

@Configuration
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LDAPConfiguration {

    private final ApplicationProperties applicationProperties;

    public LDAPConfiguration(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(applicationProperties.getSecurity().getLdap().getUrl());
        contextSource.setBase(applicationProperties.getSecurity().getLdap().getBaseDn());
        
        String managerDn = applicationProperties.getSecurity().getLdap().getManagerDn();
        String managerPassword = applicationProperties.getSecurity().getLdap().getManagerPassword();
        
        if (managerDn != null && !managerDn.isEmpty() && managerPassword != null) {
            contextSource.setUserDn(managerDn);
            contextSource.setPassword(managerPassword);
        }
        
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(LdapContextSource contextSource) {
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(
            contextSource, 
            applicationProperties.getSecurity().getLdap().getGroupSearchBase()
        );
        authoritiesPopulator.setGroupSearchFilter(applicationProperties.getSecurity().getLdap().getGroupSearchFilter());
        authoritiesPopulator.setRolePrefix("ROLE_");
        return authoritiesPopulator;
    }

    @Bean
    public LdapAuthenticator ldapAuthenticator(LdapContextSource contextSource) {
        BindAuthenticator authenticator = new BindAuthenticator(contextSource);
        String userDnPattern = applicationProperties.getSecurity().getLdap().getUserDnPattern();
        
        if (userDnPattern != null && !userDnPattern.isEmpty()) {
            authenticator.setUserDnPatterns(new String[]{userDnPattern});
        } else {
            String userSearchBase = applicationProperties.getSecurity().getLdap().getUserSearchBase();
            String userSearchFilter = applicationProperties.getSecurity().getLdap().getUserSearchFilter();
            if (userSearchBase != null && userSearchFilter != null) {
                FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(
                    userSearchBase, 
                    userSearchFilter, 
                    contextSource
                );
                authenticator.setUserSearch(userSearch);
            } else {
                throw new IllegalStateException("LDAP configuration requires either userDnPattern or userSearchBase and userSearchFilter.");
            }
        }
        return authenticator;
    }

    @Bean
    public AuthenticationProvider ldapAuthenticationProvider(
            LdapAuthenticator authenticator,
            LdapAuthoritiesPopulator authoritiesPopulator) {
        return new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }
}