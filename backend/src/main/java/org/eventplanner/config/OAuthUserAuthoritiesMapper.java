package org.eventplanner.config;

import org.eventplanner.users.values.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class OAuthUserAuthoritiesMapper implements GrantedAuthoritiesMapper  {

    private final List<String> admins;

    public OAuthUserAuthoritiesMapper(List<String> admins) {
        this.admins = admins;
    }

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities
            .stream()
            .flatMap(authority -> switch (authority) {
                case OidcUserAuthority oidcAuthority -> extractOidcRoles(oidcAuthority);
                case OAuth2UserAuthority oAuthAuthority -> extractOAuthRoles(oAuthAuthority);
                default -> Stream.empty();
            })
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

    private Stream<String> extractOidcRoles(OidcUserAuthority oidcUserAuthority) {
        if (oidcUserAuthority.getAttributes().get("iss") instanceof URL issuerUrl) {
            List<String> roles;
            if (issuerUrl.getAuthority().equals("accounts.google.com")) {
                roles = extractGoogleRoles(oidcUserAuthority);
            } else if (issuerUrl.getAuthority().endsWith(".amazonaws.com")) {
                roles = extractCognitoRoles(oidcUserAuthority);
            } else if (issuerUrl.getAuthority().endsWith(".microsoft.com")) {
                roles = extractMicrosoftRoles(oidcUserAuthority);
            } else if (issuerUrl.getAuthority().endsWith(".apple.com")) {
                roles = extractAppleRoles(oidcUserAuthority);
            } else {
                roles = Collections.emptyList();
            }

            var email = oidcUserAuthority.getIdToken().getEmail();
            if (admins.contains(email)) {
                roles.add(Role.ADMIN.value());
            }

            return roles.isEmpty() ? Stream.of(Role.NONE.value()) : roles.stream();
        }

        // all users will automatically get the role TEAM_MEMBER, if they can be matched to
        // the team members list
        return Stream.of(Role.NONE.value());
    }

    private List<String> extractCognitoRoles(OidcUserAuthority oidcUserAuthority) {
        var cognitoRoles = oidcUserAuthority.getAttributes().get("cognito:groups");
        if (cognitoRoles instanceof Collection<?> collection) {
            return collection.stream().map(r -> "ROLE_" + r).toList();
        }
        return Collections.emptyList();
    }

    private List<String> extractGoogleRoles(OidcUserAuthority oidcUserAuthority) {
        // we currently don't assign roles in google, so the roles in the token don't matter
        return Collections.emptyList();
    }

    private List<String> extractMicrosoftRoles(OidcUserAuthority oidcUserAuthority) {
        // we currently don't assign roles in azure entra id, so the roles in the token don't matter
        return Collections.emptyList();
    }

    private List<String> extractAppleRoles(OidcUserAuthority oidcUserAuthority) {
        // we currently don't assign roles in apple, so the roles in the token don't matter
        return Collections.emptyList();
    }

    private Stream<String> extractOAuthRoles(OAuth2UserAuthority oAuth2UserAuthority) {
        // is this actually needed?
        return Stream.of(Role.NONE.value());
    }
}
