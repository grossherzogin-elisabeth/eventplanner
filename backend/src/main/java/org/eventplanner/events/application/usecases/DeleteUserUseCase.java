package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserService userService;

    @Transactional
    @PreAuthorize("hasAuthority('users:delete')")
    public void deleteUser(@NonNull final UserKey userKey) {
        log.info("Deleting user {}", userKey);
        userService.deleteUser(userKey);
    }
}
