package org.eventplanner.events.application.services;

import java.util.Optional;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import static java.util.Objects.requireNonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final RegistrationRepository registrationRepository;
    private final PositionRepository positionRepository;

    public @NonNull void addUserRegistration(
        @NonNull final Event event,
        @NonNull final CreateRegistrationSpec spec,
        final boolean isSignupByUser
    ) {
        var userKey = requireNonNull(spec.userKey());
        if (event.getRegistrationByUserKey(userKey).isPresent()) {
            log.debug("Registration for user {} already exists on event {}", userKey, event.details().getName());
            return;
        }
        var user = userService.getUserByKey(userKey)
            .orElseThrow(() -> new IllegalArgumentException("User with key " + userKey.value() + " does not " +
                "exist"));

        var registration = registrationRepository.createRegistration(spec.toRegistration());
        notificationService.sendAddedToWaitingListNotification(user, event);

        if (isSignupByUser) {
            notificationService.sendCrewRegistrationAddedNotification(
                Role.TEAM_PLANNER,
                event,
                resolveUserName(registration, user),
                resolvePositionName(registration)
            );
        }
    }

    public @NonNull void addGuestRegistration(
        @NonNull final Event event,
        @NonNull final CreateRegistrationSpec spec
    ) {
        requireNonNull(spec.name());
        if (event.registrations().stream().anyMatch(r -> spec.name().equals(r.getName()))) {
            throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
        }
        registrationRepository.createRegistration(spec.toRegistration());
    }

    public @NonNull void removeRegistration(
        @NonNull final Event event,
        @NonNull final Registration registration,
        final boolean isCanceledByUser
    ) {
        log.info("Removing registration {} from event {}", registration.getKey(), event.details().getName());
        var slot = event.getSlotByRegistrationKey(registration.getKey());
        slot.ifPresent(s -> s.setAssignedRegistration(null));

        registrationRepository.deleteRegistration(registration);

        var userKey = registration.getUserKey();
        if (userKey != null) {
            var maybeUser = userService.getUserByKey(userKey);
            if (maybeUser.isEmpty()) {
                log.error("Failed to send notifcations. Cannot find user with key {}", userKey);
                return;
            }
            if (slot.isPresent()) {
                notificationService.sendRemovedFromCrewNotification(maybeUser.get(), event);
                if (isCanceledByUser) {
                    notificationService.sendCrewRegistrationCanceledNotification(
                        Role.TEAM_PLANNER,
                        event,
                        resolveUserName(registration, maybeUser.get()),
                        resolvePositionName(registration)
                    );
                }
            } else {
                notificationService.sendRemovedFromWaitingListNotification(maybeUser.get(), event);
            }
        }
    }

    public @NonNull void updateRegistration(
        @NonNull final Event event,
        @NonNull final Registration registration,
        @NonNull final UpdateRegistrationSpec spec
    ) {
        log.info("Updating registration {} on event {}", registration.getKey(), event.details().getName());
        registration.setPosition(spec.positionKey());
        registration.setName(spec.name());
        registration.setNote(spec.note());
        registration.setConfirmedAt(spec.confirmedAt());
        registrationRepository.updateRegistration(registration);
    }

    private @NonNull String resolvePositionName(@NonNull final Registration registration) {
        return positionRepository
            .findByKey(registration.getPosition())
            .map(Position::getName)
            .orElse(registration.getPosition().toString());
    }

    private @NonNull String resolveUserName(
        @NonNull final Registration registration,
        @Nullable final UserDetails user
    ) {
        if (user != null) {
            return user.getFullName();
        }
        return Optional.ofNullable(registration.getName()).orElse("");
    }
}
