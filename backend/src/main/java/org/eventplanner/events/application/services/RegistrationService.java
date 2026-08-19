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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
     * Add a registration to the specified event and send notifications when needed.
     * <br>
     * Note: This function does not alter the passed event object in any way. The event either has to be updated
     * separately or reloaded from db to contain the new registration.
     *
     * @param event the event to add the registration on
     * @param spec  registration creation specification
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createRegistration(
        @NonNull final Event event,
        @NonNull final CreateRegistrationSpec spec
    ) throws IllegalArgumentException, IllegalStateException {
        if (spec.userKey() != null) {
            return createUserRegistration(event, spec);
        } else if (spec.name() != null) {
            return createGuestRegistration(event, spec);
        } else {
            throw new IllegalArgumentException("Registration must have a user key or a name");
        }
    }

    /**
     * Add a user registration to the specified event and send notifications to the user and all crew planners, when
     * the registration has been added by the user themselves.
     * <br>
     * Note: This function does not alter the passed event object in any way. The event either has to be updated
     * separately or reloaded from db to contain the new registration.
     *
     * @param event the event to add the registration on
     * @param spec  registration creation specification
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createUserRegistration(
        @NonNull final Event event,
        @NonNull final CreateRegistrationSpec spec
    ) throws IllegalArgumentException {
        if (spec.userKey() == null) {
            throw new IllegalArgumentException("User registration must have a user key");
        }
        log.info(
            "Creating registration {} for user {} on event {}",
            spec.registrationKey(),
            spec.userKey(),
            event.getKey()
        );

        // make sure we don't create a 2nd registration for the same user
        var existingRegistration = event.findRegistrationByUserKey(spec.userKey());
        if (existingRegistration.isPresent()) {
            log.warn("User {} already has a registration on event {}", spec.userKey(), event.getKey());
            return existingRegistration.get();
        }

        var user = userService.getUserByKey(spec.userKey())
            .orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        var registration = registrationRepository.createRegistration(spec.toRegistration(), event);

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
     * Add a guest registration to the specified event.
     * <br>
     * Note: This function does not alter the passed event object in any way. The event either has to be updated
     * separately or reloaded from db to contain the new registration.
     *
     * @param event the event to add the registration on
     * @param spec  registration creation specification
     * @return the newly created registration
     * @throws IllegalArgumentException when the spec is invalid
     */
    public @NonNull Registration createGuestRegistration(
        @NonNull final Event event,
        @NonNull final CreateRegistrationSpec spec
    ) throws IllegalArgumentException {
        if (spec.name() == null) {
            throw new IllegalArgumentException("Guest registration must have a name");
        }
        log.info("Creating guest registration for {} on event {}", spec.name(), event.getKey());

        // make sure we don't create a 2nd registration for the same name
        var existingRegistration = event.findRegistrationByName(spec.name());
        if (existingRegistration.isPresent()) {
            log.warn("Registration for guest with name {} already exists on event {}", spec.name(), event.getKey());
            throw new IllegalArgumentException("Registration for " + spec.name() + " already exists");
        }

        return registrationRepository.createRegistration(spec.toRegistration(), event);
    }

    /**
     * Remove a registration from the specified event and send notifications when needed.
     * <br>
     * Note: This function does not alter the passed event object in any way. The event either has to be
     * updated separately or reloaded from db to no longer contain the deleted registration.
     *
     * @param event           the event to remove the registration from
     * @param registrationKey the key of the registration to remove
     * @throws NoSuchElementException when the registration does not exist on the event
     */
    @Transactional
    public void removeRegistration(
        @NonNull final Event event,
        @NonNull final RegistrationKey registrationKey,
        final boolean isRemovedByUser
    ) throws NoSuchElementException {
        log.info("Removing registration {} from event {}", registrationKey, event.getKey());
        var registration = event.findRegistrationByKey(registrationKey)
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));
        registrationRepository.deleteRegistration(registration.getKey(), event.getKey());

        // send notifications

        var assignedSlot = event.findSlotByAssignedRegistrationKey(registration.getKey());
        var user = userService.getUserByKey(registration.getUserKey());
        if (user.isEmpty()) {
            if (registration.getUserKey() != null) {
                log.error("Cannot send notifications for unknown user with key {}", registration.getUserKey());
            }
            return;
        }

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
     * Note: This function does not alter the event or a potential slot assignment in any way!
     *
     * @param event the event the registration exists on
     * @param spec  the update specification
     * @return the updated registration
     * @throws NoSuchElementException when the registration does not exist on the event
     */
    public @NonNull Registration updateRegistration(
        @NonNull final Event event,
        @NonNull final UpdateRegistrationSpec spec
    ) throws NoSuchElementException {
        log.info("Updating registration {} on event {}", spec.registrationKey(), event.getKey());
        var registration = event.findRegistrationByKey(spec.registrationKey())
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));

        registration.setPosition(spec.positionKey());
        registration.setName(spec.name());
        registration.setNote(spec.note());
        registration.setConfirmedAt(spec.confirmedAt());
        registration.setOvernightStay(spec.overnightStay());
        registration.setArrival(spec.arrival());
        registrationRepository.updateRegistration(registration, event);
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
