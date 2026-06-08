package org.eventplanner.events.application.usecases;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.User;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadUserUseCase {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PreAuthorize("hasAuthority('users:read')")
    public @NonNull List<User> getUsers() {
        log.debug("Reading user list");
        return userService.getUsers();
    }

    @PreAuthorize("hasAuthority('users:read-details')")
    public @NonNull List<UserDetails> getDetailedUsers() {
        log.debug("Reading user details list");
        return userService.getDetailedUsers();
    }

    @PreAuthorize("hasAuthority('users:read-details') or hasAuthority('users:read-details-self')")
    public @NonNull Optional<UserDetails> getUserByKey(@NonNull final UserKey key) {
        var signedInUser = authenticationService.getSignedInUser();
        if (!signedInUser.key().equals(key)) {
            signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        }
        log.debug("Reading details of user {}", key);
        return userService.getUserByKey(key);
    }
}
