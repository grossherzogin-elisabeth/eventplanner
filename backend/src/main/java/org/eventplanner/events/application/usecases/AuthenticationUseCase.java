package org.eventplanner.events.application.usecases;

import java.time.Duration;

import org.eventplanner.events.application.ports.AccessKeyRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthenticationUseCase {

    private final AuthenticationService authenticationService;
    private final AccessKeyRepository accessKeyRepository;
    private final Duration accessKeyMaxAge;

    public AuthenticationUseCase(
        @NonNull @Autowired final AuthenticationService authenticationService,
        @NonNull @Autowired final AccessKeyRepository accessKeyRepository,
        @Nullable @Value("${auth.access-key.max-age}") Duration accessKeyMaxAge
    ) {
        this.authenticationService = authenticationService;
        this.accessKeyRepository = accessKeyRepository;
        this.accessKeyMaxAge = accessKeyMaxAge != null ? accessKeyMaxAge : Duration.ofDays(21);
    }

    @PreAuthorize("hasAuthority('account:read')")
    public @NonNull SignedInUser getSignedInUser() throws UnauthorizedException {
        return authenticationService.getSignedInUser();
    }

    @PreAuthorize("hasAuthority('account:read')")
    public @NonNull SignedInUser getSignedInUser(@Nullable final Authentication authentication)
    throws UnauthorizedException {
        return authenticationService.getSignedInUser(authentication);
    }

    @PreAuthorize("hasAuthority('access-keys:delete')")
    @Transactional
    public void deleteExpiredAccessKeys() {
        log.info("Deleting access keys older than {}", accessKeyMaxAge);
        accessKeyRepository.deleteExpired(accessKeyMaxAge);
    }
}
