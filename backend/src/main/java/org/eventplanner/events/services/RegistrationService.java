package org.eventplanner.events.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eventplanner.events.adapter.EventRepository;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationService {

    private final NotificationService notificationService;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final PositionRepository positionRepository;

    public @NonNull Event addRegistration(
            @NonNull Event event,
            @NonNull CreateRegistrationSpec spec
    ) {
        var userKey = spec.userKey();
        if (userKey != null) {
            if (event.getRegistrations().stream().anyMatch(r -> spec.userKey().equals(r.getUserKey()))) {
                log.debug("Registration for {} already exists on event {} ({}).", userKey, event.getName(), event.getKey());
                return event;
            }
            var user = userService.getUserByKey(userKey)
                    .orElseThrow(() -> new IllegalArgumentException("User with key " + userKey.value() + " does not exist"));
            event.addRegistration(new Registration(
                    new RegistrationKey(),
                    spec.positionKey(),
                    userKey,
                    null,
                    spec.note(),
                    Registration.generateAccessKey(),
                    null));
            notificationService.sendAddedToWaitingListNotification(user, event);
        } else if (spec.name() != null) {
            if (event.getRegistrations().stream().anyMatch(r -> spec.name().equals(r.getName()))) {
                throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
            }
            log.info("Adding registration for guest {} on event {} ({})", spec.name(), event.getName(), event.getKey());
            event.addRegistration(new Registration(
                    new RegistrationKey(),
                    spec.positionKey(),
                    null,
                    spec.name(),
                    spec.note(),
                    Registration.generateAccessKey(),
                    null));
        }
        return this.eventRepository.update(event);
    }

    public @NonNull Event removeRegistration(
            @NonNull Event event,
            @NonNull Registration registration
    ) {
        log.info("Deleting registration {} from event {} ({})", registration.getKey(), event.getName(), event.getKey());
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

        event.removeRegistration(registration.getKey());
        event = this.eventRepository.update(event);

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
                            positionName);
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
        if (Boolean.TRUE.equals(spec.confirmed())) {
            registration.setConfirmedAt(Instant.now());
        } else {
            registration.setConfirmedAt(null);
        }
        event.updateRegistration(registration.getKey(), registration);
        return this.eventRepository.update(event);
    }
}
