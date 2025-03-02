package org.eventplanner.events.application.services;

import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.Role;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PositionRepository positionRepository;

    public @NonNull Event addRegistration(
        @NonNull Event event,
        @NonNull CreateRegistrationSpec spec,
        boolean isSignupByUser

    ) {
        var userKey = Objects.requireNonNull(spec.userKey());
        if (event.getRegistrations().stream().anyMatch(r -> spec.userKey().equals(r.getUserKey()))) {
            log.debug(
                "Registration for {} already exists on event {} ({}).",
                userKey,
                event.getName(),
                event.getKey()
            );
            return event;
        }
        var user = userService.getUserByKey(userKey)
            .orElseThrow(() -> new IllegalArgumentException("User with key " + userKey.value() + " does not " +
                                                            "exist"));
        var registration = registrationRepository.createRegistration(spec.toRegistration(), event.getKey());
        notificationService.sendAddedToWaitingListNotification(user, event);

        if (isSignupByUser) {
            notificationService.sendCrewRegistrationAddedNotification(
                Role.TEAM_PLANNER,
                event,
                resolveUserName(registration, user),
                resolvePositionName(registration)
            );
        }

        // reload the event, because registrations have changed
        return this.eventRepository.findByKey(event.getKey()).orElseThrow();
    }

    public @NonNull Event addGuestRegistration(
        @NonNull Event event,
        @NonNull CreateRegistrationSpec spec
    ) {
        Objects.requireNonNull(spec.name());
        if (event.getRegistrations().stream().anyMatch(r -> spec.name().equals(r.getName()))) {
            throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
        }
        registrationRepository.createRegistration(spec.toRegistration(), event.getKey());

        // reload the event, because registrations have changed
        return this.eventRepository.findByKey(event.getKey()).orElseThrow();
    }

    public @NonNull Event removeRegistration(
        @NonNull Event event,
        @NonNull Registration registration,
        boolean isCanceledByUser
    ) {
        log.info("Removing registration {} from event {} ({})", registration.getKey(), event.getName(), event.getKey());
        var hasAssignedSlot = false;
        for (EventSlot slot : event.getSlots()) {
            if (registration.getKey().equals(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
                hasAssignedSlot = true;
            }
        }

        registrationRepository.deleteRegistration(registration, event.getKey());
        event = this.eventRepository.findByKey(event.getKey()).orElseThrow();

        var userKey = registration.getUserKey();
        if (userKey != null) {
            var maybeUser = userService.getUserByKey(userKey);
            if (maybeUser.isEmpty()) {
                log.error("Failed to send notifcations. Cannot find user with key {}", userKey);
                return event;
            }
            if (hasAssignedSlot) {
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
        return event;
    }

    public @NonNull Event updateRegistration(
        @NonNull Event event,
        @NonNull Registration registration,
        @NonNull UpdateRegistrationSpec spec
    ) {
        log.info("Updating registration {} on event {} ({})", registration.getKey(), event.getName(), event.getKey());
        registration.setPosition(spec.positionKey());
        registration.setName(spec.name());
        registration.setNote(spec.note());
        registration.setConfirmedAt(spec.confirmedAt());
        registrationRepository.updateRegistration(registration, event.getKey());
        return this.eventRepository.findByKey(event.getKey()).orElseThrow();
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
