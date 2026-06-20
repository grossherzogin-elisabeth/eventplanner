package org.eventplanner.events.application.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationService {

    private final UserService userService;
    private final List<String> admins;

    public AuthenticationService(
        @NonNull @Autowired final UserService userService,
        @Nullable @Value("${auth.admins}") String admins
    ) {
        this.userService = userService;
        if (admins != null && !admins.isBlank()) {
            this.admins = Arrays.stream(admins.split(",")).map(String::trim).toList();
        } else {
            this.admins = Collections.emptyList();
        }
    }

    public @NonNull SignedInUser getSignedInUser() {
        return getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
    }

    public @NonNull SignedInUser getSignedInUser(@Nullable final Authentication authentication)
    throws UnauthorizedException {
        if (authentication instanceof SignedInUser signedInUser) {
            return signedInUser;
        } else if (authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException();
        } else if (authentication != null) {
            log.error("Got an authentication of unexpected type {}", authentication.getClass().getSimpleName());
        }
        throw new UnauthorizedException();
    }

    public @NonNull SignedInUser authenticate(@NonNull OidcUser oidcUser) {
        return authenticate(
            new AuthKey(oidcUser.getSubject()),
            oidcUser.getEmail(),
            Optional.ofNullable(oidcUser.getGivenName()).orElse("Unknown"),
            Optional.ofNullable(oidcUser.getFamilyName()).orElse("Unknown"),
            oidcUser
        );
    }

    public @NonNull SignedInUser authenticate(@NonNull OAuth2User oAuth2User) {
        var sub = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.SUB))
            .map(Object::toString)
            .orElseThrow(() -> new IllegalArgumentException("Missing sub claim in OAuth2 user"));
        var email = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.EMAIL))
            .map(Object::toString)
            .orElse(null);
        var firstName = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.GIVEN_NAME))
            .map(Object::toString)
            .orElse("Unknown");
        var lastName = Optional.ofNullable(oAuth2User.getAttribute(StandardClaimNames.FAMILY_NAME))
            .map(Object::toString)
            .orElse("Unknown");
        return authenticate(new AuthKey(sub), email, firstName, lastName, oAuth2User);
    }

    private @NonNull SignedInUser authenticate(
        @NonNull AuthKey authKey,
        @Nullable String email,
        @NonNull String firstName,
        @NonNull String lastName,
        @NonNull AuthenticatedPrincipal authentication
    ) {
        var user = userService.getUserByAuthKey(authKey)
            .or(() -> userService.getUserByEmail(email))
            .orElseGet(() -> createUser(authKey, email, firstName, lastName));

        // store login time
        user.setLastLoginAt(Instant.now());

        if (user.getAuthKey() != null && !authKey.equals(user.getAuthKey())) {
            log.error(
                "Prevented linking user {} with external user {}, because user is already linked to external user {}",
                user.getKey(),
                authKey,
                user.getAuthKey()
            );
            throw new UnauthorizedException();
        }
        // check if user needs to be linked
        if (user.getAuthKey() == null) {
            log.info("Linking user {} with external user {} by email", user.getKey(), authKey);
            user.setAuthKey(authKey);
        } else if (!Objects.equals(user.getEmail(), email)) {
            log.info("Updating email of user {} to match linked external user", user.getKey());
            user.setEmail(email);
        }

        log.info("Authenticated user {}", user.getKey());
        // save changes
        userService.updateUser(user);
        if (admins.contains(user.getEmail()) && !user.getRoles().contains(Role.ADMIN)) {
            log.info("Temporarily granting admin role to signed-in user by configuration");
            var roles = new ArrayList<>(user.getRoles());
            roles.add(Role.ADMIN);
            user = user.withRoles(roles);
        }

        // return mapped authorities
        return SignedInUser.fromUser(user, authentication);
    }

    private @NonNull UserDetails createUser(
        @NonNull AuthKey authKey,
        @Nullable String email,
        @NonNull String firstName,
        @NonNull String lastName
    ) {
        try {
            var newUser = new UserDetails(new UserKey(), Instant.now(), Instant.now(), firstName, lastName);
            newUser.setEmail(email);
            newUser.setAuthKey(authKey);
            newUser.setLastLoginAt(Instant.now());
            newUser = userService.createUser(newUser);
            log.info("Created new user with key {}", newUser.getKey());
            return newUser;
        } catch (UserAlreadyExistsException _) {
            // can happen on simultaneous requests
            return userService.getUserByAuthKey(authKey)
                .orElseThrow(UnauthorizedException::new);
        }
    }
}
