package org.eventplanner.events.application.usecases.users;

import java.time.Instant;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.exceptions.UserAlreadyExistsException;
import org.eventplanner.events.domain.specs.CreateUserSpec;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;

    @Transactional
    @PreAuthorize("hasAuthority('users:write')")
    public @NonNull UserDetails createUser(@NonNull final CreateUserSpec spec) {
        if (userService.getUserByEmail(spec.email()).isPresent()) {
            throw new UserAlreadyExistsException(
                "User with email " + spec.email() + " already exists");
        }
        var newUser = new UserDetails(
            new UserKey(),
            Instant.now(),
            Instant.now(),
            spec.firstName(),
            spec.lastName()
        );
        log.info("Creating user {}", newUser.getKey());
        newUser.setEmail(spec.email());
        return userService.createUser(newUser);
    }
}
