package org.eventplanner.events.application.usecases.events;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.specs.UpdateEventSpec;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateEventUseCase {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final RegistrationService registrationService;
    private final EventRepository eventRepository;

    @PreAuthorize("hasAuthority('events:update-details') " +
        "or hasAuthority('events:update-slots') " +
        "or hasAuthority('registrations:update')")
    public @NonNull Event updateEvent(
        @NonNull final UpdateEventSpec spec
    ) throws NoSuchElementException {
        var signedInUser = authenticationService.getSignedInUser();
        var event = eventRepository.findByKey(spec.eventKey())
            .orElseThrow(() -> new NoSuchElementException("Event does not exist"));
        log.info("Updating event {}", event.getKey());

        var previousState = event.getState();
        List<RegistrationKey> assignedRegistrations = new LinkedList<>();
        List<RegistrationKey> unassignedRegistrations = new LinkedList<>();

        updateEventDetails(signedInUser, event, spec);

        // make sure we have a clean state
        event.removeInvalidSlotAssignments();
        // make sure all registrations are added and up to date  before updating crew assignments
        addRegistrations(signedInUser, event, spec);
        updateRegistrations(signedInUser, event, spec);
        updateCrewAssignments(signedInUser, event, spec, assignedRegistrations, unassignedRegistrations);
        // remove registrations last, so removals don't interfere with crew assignments
        removeRegistrations(signedInUser, event, spec);

        event.removeInvalidSlotAssignments();
        event.optimizeSlots();
        event.removeEmptyImplicitSlots();

        // save changes
        var updatedEvent = eventRepository.update(event);

        // crew planning has just been published, notify all assigned crew members
        if (List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(previousState)
            && EventState.PLANNED.equals(spec.state())) {
            assignedRegistrations = updatedEvent.getAssignedRegistrationKeys();
        }

        sendNotifications(updatedEvent, event.getRegistrations(), assignedRegistrations, unassignedRegistrations);

        return updatedEvent;
    }

    private void updateEventDetails(
        @NonNull final SignedInUser signedInUser,
        @NonNull Event event,
        @NonNull final UpdateEventSpec spec
    ) {
        if (!signedInUser.hasPermission(Permission.UPDATE_EVENT_DETAILS)) {
            return;
        }
        var changedAttributes = new LinkedList<String>();
        if (spec.name() != null && !spec.name().equals(event.getName())) {
            event.setName(spec.name());
            changedAttributes.add("name");
        }
        if (spec.type() != null && !spec.type().equals(event.getType())) {
            event.setType(spec.type());
            changedAttributes.add("type");
        }
        if (spec.signupType() != null && !spec.signupType().equals(event.getSignupType())) {
            event.setSignupType(spec.signupType());
            changedAttributes.add("signupType");
        }
        if (spec.description() != null && !spec.description().equals(event.getDescription())) {
            event.setDescription(spec.description());
            changedAttributes.add("description");
        }
        if (spec.note() != null && !spec.note().equals(event.getNote())) {
            event.setNote(spec.note());
            changedAttributes.add("note");
        }
        if (spec.state() != null && !spec.state().equals(event.getState())) {
            event.setState(spec.state());
            changedAttributes.add("state");
        }
        if (spec.start() != null && !spec.start().equals(event.getStart())) {
            event.setStart(spec.start());
            changedAttributes.add("start");
        }
        if (spec.end() != null && !spec.end().equals(event.getEnd())) {
            event.setEnd(spec.end());
            changedAttributes.add("end");
        }
        if (spec.locations() != null && !spec.locations().equals(event.getLocations())) {
            event.setLocations(spec.locations());
            changedAttributes.add("locations");
        }
        if (!changedAttributes.isEmpty()) {
            log.info("Updated attributes {} on event {}", String.join(", ", changedAttributes), event.getKey());
        }
    }

    private void addRegistrations(
        @NonNull final SignedInUser signedInUser,
        @NonNull Event event,
        @NonNull final UpdateEventSpec spec
    ) {
        if (!signedInUser.hasPermission(Permission.UPDATE_REGISTRATIONS)) {
            return;
        }

        // add new registrations
        List<RegistrationKey> createdRegistrations = new LinkedList<>();
        for (final var createSpec : ofNullable(spec.registrationsToAdd()).orElse(emptyList())) {
            try {
                var registration = registrationService.createRegistration(event, createSpec);
                event.addRegistration(registration);
                createdRegistrations.add(registration.getKey());
            } catch (Exception e) {
                if (createSpec.userKey() != null) {
                    log.error("Failed to add registration for user {}", createSpec.userKey(), e);
                } else {
                    log.error("Failed to add registration for guest {}", createSpec.name(), e);
                }
            }
        }
        if (!createdRegistrations.isEmpty()) {
            log.info("Added {} registrations to event {}", createdRegistrations.size(), event.getKey());
        }
    }

    private void updateRegistrations(
        @NonNull final SignedInUser signedInUser,
        @NonNull Event event,
        @NonNull final UpdateEventSpec spec
    ) {
        if (!signedInUser.hasPermission(Permission.UPDATE_REGISTRATIONS)) {
            return;
        }

        // update registrations
        List<RegistrationKey> updatedRegistrations = new LinkedList<>();
        for (final var updateSpec : ofNullable(spec.registrationsToUpdate()).orElse(emptyList())) {
            try {
                var registration = registrationService.updateRegistration(event, updateSpec);
                updatedRegistrations.add(registration.getKey());
            } catch (Exception e) {
                log.error("Failed to update registration {}", updateSpec.registrationKey(), e);
            }
        }
        if (!updatedRegistrations.isEmpty()) {
            log.info("Updated {} registrations on event {}", updatedRegistrations.size(), event.getKey());
        }
    }

    private void removeRegistrations(
        @NonNull final SignedInUser signedInUser,
        @NonNull Event event,
        @NonNull final UpdateEventSpec spec
    ) {
        if (!signedInUser.hasPermission(Permission.UPDATE_REGISTRATIONS)) {
            return;
        }

        // remove registrations
        List<RegistrationKey> removedRegistrations = new LinkedList<>();
        for (final var registrationKey : ofNullable(spec.registrationsToRemove()).orElse(emptyList())) {
            try {
                registrationService.removeRegistration(event, registrationKey, false);
                event.removeRegistration(registrationKey);
                removedRegistrations.add(registrationKey);
            } catch (Exception e) {
                log.error("Failed to remove registration {}", registrationKey, e);
            }
        }
        if (!removedRegistrations.isEmpty()) {
            log.info("Removed {} registrations from event {}", removedRegistrations.size(), event.getKey());
        }
    }

    private void updateCrewAssignments(
        @NonNull final SignedInUser signedInUser,
        @NonNull Event event,
        @NonNull final UpdateEventSpec spec,
        @NonNull final List<RegistrationKey> assignedRegistrations,
        @NonNull final List<RegistrationKey> unassignedRegistrations
    ) {
        if (!signedInUser.hasPermission(Permission.UPDATE_EVENT_SLOTS) || spec.slots() == null) {
            return;
        }
        var before = event.getAssignedRegistrationKeys();
        var after = spec.getAssignedRegistrationKeys();
        after.stream()
            .filter(key -> !before.contains(key))
            .forEach(assignedRegistrations::add);
        before.stream()
            .filter(key -> !after.contains(key))
            .forEach(unassignedRegistrations::add);

        if (!assignedRegistrations.isEmpty()
            || !unassignedRegistrations.isEmpty()
            || !event.getSlots().equals(spec.slots())) {
            event.setSlots(spec.slots());
            log.info("Updated slots of event {}", event.getKey());
        }
        if (!assignedRegistrations.isEmpty()) {
            log.info(
                "Assigned {} registrations to crew of event {}",
                assignedRegistrations.size(),
                event.getKey()
            );
        }
        if (!unassignedRegistrations.isEmpty()) {
            log.info(
                "Unassigned {} registrations from crew of event {}",
                unassignedRegistrations.size(),
                event.getKey()
            );
        }
    }

    private void sendNotifications(
        @NonNull final Event event,
        @NonNull final List<Registration> registrations,
        @NonNull final List<RegistrationKey> assignedRegistrations,
        @NonNull final List<RegistrationKey> unassignedRegistrations
    ) {
        // only send assignment notifications when the event is in planned state and in the future
        if (!EventState.PLANNED.equals(event.getState()) || event.getStart().isBefore(Instant.now())) {
            return;
        }

        for (final Registration registration : registrations) {
            var user = userService.getUserByKey(registration.getUserKey()).orElse(null);
            if (user == null) {
                if (registration.getUserKey() != null) {
                    log.error(
                        "Skipping notifications for user {}, because user details could not be found",
                        registration.getUserKey()
                    );
                }
                continue;
            }

            if (assignedRegistrations.contains(registration.getKey())) {
                notificationService.sendAddedToCrewNotification(user, event);
                if (event.isUpForConfirmationReminder()) {
                    notificationService.sendConfirmationReminderNotification(user, event, registration);
                } else if (event.isUpForConfirmationRequest()) {
                    notificationService.sendConfirmationRequestNotification(user, event, registration);
                }
            }
            if (unassignedRegistrations.contains(registration.getKey())) {
                notificationService.sendRemovedFromCrewNotification(user, event);
            }
        }
    }
}
