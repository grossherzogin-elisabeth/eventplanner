package org.eventplanner.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.values.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OAuthUserAuthoritiesMapper implements GrantedAuthoritiesMapper {

    // private final UserService userService;
    private final List<String> admins;

    public OAuthUserAuthoritiesMapper(
        // @Autowired UserService userService,
        @Value("${auth.admins}") String admins
    ) {
        // this.userService = userService;
        this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
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
            .toList();
    }

    private Stream<? extends GrantedAuthority> extractOidcRoles(OidcUserAuthority oidcUserAuthority) {
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(oidcUserAuthority);
        authorities.addAll(getRolesByEmail(oidcUserAuthority.getIdToken().getEmail()));
        return authorities.stream();
    }

    private Stream<? extends GrantedAuthority> extractOAuthRoles(OAuth2UserAuthority oAuthAuthority) {
        var authorities = new ArrayList<GrantedAuthority>();
        authorities.add(oAuthAuthority);
        authorities.addAll(getRolesByEmail(oAuthAuthority.getAttributes().get("email")));
        return authorities.stream();
    }

    private @NonNull List<? extends GrantedAuthority> getRolesByEmail(@Nullable Object email) {
        var authorities = new ArrayList<GrantedAuthority>();
        if (email instanceof String) {
            if (admins.contains(email)) {
                authorities.add(new SimpleGrantedAuthority(Role.ADMIN.toString()));
            }
            // var user = userService.getUserByEmail(email.toString());
            // if (user.isPresent()) {
            //     authorities.add(SignedInUser.fromUser(user.get()));
            //     user.get().getRoles().stream()
            //         .map(Role::toString)
            //         .distinct()
            //         .map(SimpleGrantedAuthority::new)
            //         .filter(authority -> !authorities.contains(authority))
            //         .forEach(authorities::add);
            // }
        }
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority(Role.NONE.toString()));
        }
        return authorities;
    }
}
