package org.eventplanner.events.application.services;

import java.time.Instant;
import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Add a registration to the specified event and send notifications when needed.<br>
     * Note: This function also adds the registration to the event. However, it does not persist the changes to the
     * event.
     *
     * @param spec  registration creation specification
     * @param event the event to add the registration on
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createRegistration(
        @NonNull final CreateRegistrationSpec spec,
        @NonNull final Event event
    ) throws IllegalArgumentException, IllegalStateException {
        if (spec.userKey() != null) {
            return createUserRegistration(spec, event);
        } else if (spec.name() != null) {
            return createGuestRegistration(spec, event);
        } else {
            throw new IllegalArgumentException("Registration must have a user key or a name");
        }
    }

    /**
     * Add a user registration to the specified event and send notifications to the user and all crew planners, when
     * the registration has been added by the user themselves.<br>
     * Note: This function also adds the registration to the event. However, it does not persist the changes to the
     * event.
     *
     * @param spec  registration creation specification
     * @param event the event to add the registration on
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createUserRegistration(
        @NonNull final CreateRegistrationSpec spec,
        @NonNull final Event event
    ) throws IllegalArgumentException {
        if (spec.userKey() == null) {
            throw new IllegalArgumentException("User registration must have a user key");
        }
        log.info(
            "Creating registration {} for user {} on event {}",
            spec.registrationKey(),
            spec.userKey(),
            event.getName()
        );

        // make sure we don't create a 2nd registration for the same user
        var existingRegistration = event.findRegistrationByUserKey(spec.userKey());
        if (existingRegistration.isPresent()) {
            log.warn("User {} already has a registration on event {}", spec.userKey(), event.getName());
            return existingRegistration.get();
        }

        var user = userService.getUserByKey(spec.userKey())
            .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        var registration = registrationRepository.createRegistration(spec.toRegistration(), event.getKey());
        event.addRegistration(registration);

        // send notifications
        notificationService.sendAddedToWaitingListNotification(user, event);
        if (spec.isSelfSignup()
            && EventState.PLANNED.equals(event.getState())
            && event.getStart().isAfter(Instant.now())
        ) {
            notificationService.sendCrewRegistrationAddedNotification(
                Role.TEAM_PLANNER,
                event,
                resolveUserName(registration, user),
                resolvePositionName(registration)
            );
        }

        return registration;
    }

    /**
     * Add a guest registration to the specified event.<br>
     * Note: This function also adds the registration to the event. However, it does not persist the changes to the
     * event.
     *
     * @param spec  registration creation specification
     * @param event the event to add the registration on
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createGuestRegistration(
        @NonNull final CreateRegistrationSpec spec,
        @NonNull final Event event
    ) throws IllegalArgumentException {
        if (spec.name() == null) {
            throw new IllegalArgumentException("Guest registration must have a name");
        }
        log.info("Creating guest registration for {} on event {}", spec.name(), event.getName());

        // make sure we don't create a 2nd registration for the same name
        var existingRegistration = event.findRegistrationByName(spec.name());
        if (existingRegistration.isPresent()) {
            log.warn("Registration for guest with name {} already exists on event {}", spec.name(), event.getName());
            throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
        }

        var registration = registrationRepository.createRegistration(spec.toRegistration(), event.getKey());
        event.addRegistration(registration);
        return registration;
    }

    /**
     * Remove a registration from the specified event and send notifications when needed.<br>
     * Note: This function also removes the registration from the event and clears the slot assignment if present.
     * However, it does not persist the changes to the event.
     *
     * @param registrationKey the key of the registration to remove
     * @param event           the event to remove the registration from
     * @throws NoSuchElementException when the registration does not exist on the event
     */
    @Transactional
    public void removeRegistration(
        @NonNull final RegistrationKey registrationKey,
        @NonNull final Event event,
        final boolean isRemovedByUser
    ) throws NoSuchElementException {
        log.info("Removing registration {} from event {}", registrationKey, event.getName());
        var registration = event.findRegistrationByKey(registrationKey)
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));
        registrationRepository.deleteRegistration(registration.getKey(), event.getKey());
        var assignedSlot = event.findSlotByAssignedRegistrationKey(registration.getKey());

        event.removeRegistration(registration);

        var user = userService.getUserByKey(registration.getUserKey());
        if (user.isEmpty()) {
            if (registration.getUserKey() != null) {
                log.error("Cannot send notifications for unknown user with key {}", registration.getUserKey());
            }
            return;
        }

        // send notifications
        if (assignedSlot.isPresent()) {
            notificationService.sendRemovedFromCrewNotification(user.get(), event);
        } else {
            notificationService.sendRemovedFromWaitingListNotification(user.get(), event);
        }
        if (assignedSlot.isPresent() && isRemovedByUser) {
            notificationService.sendCrewRegistrationCanceledNotification(
                Role.TEAM_PLANNER,
                event,
                resolveUserName(registration, user.get()),
                resolvePositionName(registration)
            );
        }
    }

    /**
     * Updates a registration.<br>
     * Note: This function does not alter a potential slot assignment in any way!
     *
     * @param spec  the update specification
     * @param event the event the registration exists on
     * @return the updated registration
     * @throws NoSuchElementException when the registration does not exist on the event
     */
    public @NonNull Registration updateRegistration(
        @NonNull final UpdateRegistrationSpec spec,
        @NonNull final Event event
    ) throws NoSuchElementException {
        log.info("Updating registration {} on event {}", spec.registrationKey(), event.getName());
        var registration = event.findRegistrationByKey(spec.registrationKey())
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));

        registration.setPosition(spec.positionKey());
        registration.setName(spec.name());
        registration.setNote(spec.note());
        registration.setConfirmedAt(spec.confirmedAt());
        registration.setOvernightStay(spec.overnightStay());
        registration.setArrival(spec.arrival());
        registrationRepository.updateRegistration(registration, event.getKey());
        return registration;
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
        } else if (registration.getName() != null) {
            return registration.getName();
        }
        return "";
    }
}
