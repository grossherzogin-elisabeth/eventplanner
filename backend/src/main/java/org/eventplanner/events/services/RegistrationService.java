package org.eventplanner.events.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.adapter.RegistrationRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.spec.CreateRegistrationSpec;
import org.eventplanner.events.spec.UpdateRegistrationSpec;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Role;
import org.springframework.lang.NonNull;
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
        @NonNull CreateRegistrationSpec spec
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
        registrationRepository.createRegistration(spec.toRegistration(), event.getKey());
        notificationService.sendAddedToWaitingListNotification(user, event);

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
        @NonNull Registration registration
    ) {
        log.info("Removing registration {} from event {} ({})", registration.getKey(), event.getName(), event.getKey());
        var hasAssignedSlot = false;
        for (Slot slot : event.getSlots()) {
            if (registration.getKey().equals(slot.getAssignedRegistration())) {
                slot.setAssignedRegistration(null);
                hasAssignedSlot = true;
            }
        }

        List<UserDetails> admins = Collections.emptyList();
        String positionName = "";
        if (hasAssignedSlot) {
            admins = userService.getUsersByRole(Role.TEAM_PLANNER);
            positionName = positionRepository
                .findByKey(registration.getPosition())
                .map(Position::getName)
                .orElse(registration.getPosition().toString());
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
                for (UserDetails admin : admins) {
                    notificationService.sendCrewRegistrationCanceledNotification(
                        admin,
                        event,
                        maybeUser.get().getFullName(),
                        positionName
                    );
                }
            } else {
                notificationService.sendRemovedFromWaitingListNotification(maybeUser.get(), event);
            }
        } else if (hasAssignedSlot && registration.getName() != null) {
            for (UserDetails admin : admins) {
                notificationService.sendCrewRegistrationCanceledNotification(
                    admin,
                    event,
                    registration.getName(),
                    positionName
                );
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
}
