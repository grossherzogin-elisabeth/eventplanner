package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationUseCase {

    private final AuthenticationService authenticationService;

    public @NonNull SignedInUser getSignedInUser() throws UnauthorizedException {
        return authenticationService.getSignedInUser();
    }

    public @NonNull SignedInUser getSignedInUser(@Nullable final Authentication authentication)
    throws UnauthorizedException {
        return authenticationService.getSignedInUser(authentication);
    }
}
