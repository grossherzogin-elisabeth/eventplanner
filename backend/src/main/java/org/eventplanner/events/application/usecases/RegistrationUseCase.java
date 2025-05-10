package org.eventplanner.events.application.usecases;

import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationUseCase {

    private final EventRepository eventRepository;
    private final RegistrationService registrationService;

    /**
     * @param signedInUser the user performing this action
     * @param spec         the specification for the registration to add
     * @return the updated event
     * @throws IllegalArgumentException when the spec is invalid
     * @throws NoSuchElementException   when the event does not exist
     * @throws IllegalStateException    when the event cannot be reloaded after adding the registration
     */
    public @NonNull Event createRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final CreateRegistrationSpec spec
    ) throws IllegalArgumentException, NoSuchElementException, IllegalStateException {
        var event = eventRepository.findByKey(spec.eventKey())
            .orElseThrow(() -> new NoSuchElementException("Event does not exist"));

        if (spec.isSelfSignup()) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info("User {} signed up on event {}", spec.userKey(), event.getName());
            registrationService.createUserRegistration(spec, event);
        } else if (spec.userKey() != null) {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Adding registration for user {} on event {}", spec.userKey(), event.getName());
            registrationService.createUserRegistration(spec, event);
        } else if (spec.name() != null) {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Adding registration for guest {} on event {}", spec.name(), event.getName());
            registrationService.createGuestRegistration(spec, event);
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }

        return eventRepository.findByKey(spec.eventKey())
            .orElseThrow(() -> new IllegalStateException("Event does not exist after update"));
    }

    /**
     * @param signedInUser    the user performing this action
     * @param eventKey        the key of the event to remove the registration from
     * @param registrationKey the key of the registration to remove
     * @return the updated event
     * @throws NoSuchElementException when the event or the registration does not exist
     */
    public @NonNull Event removeRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey
    ) throws NoSuchElementException {
        var event = eventRepository.findByKey(eventKey)
            .orElseThrow(() -> new NoSuchElementException("Event does not exist"));
        var registration = event.findRegistrationByKey(registrationKey)
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));

        var isRemovedByUser = signedInUser.key().equals(registration.getUserKey());
        if (isRemovedByUser) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info("User {} removed their registration from event {}", signedInUser.key(), event.getName());
        } else if (registration.getUserKey() != null) {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Removing registration of {} from event {}", registration.getUserKey(), event.getName());
        } else if (registration.getName() != null) {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Removing guest registration of {} from event {}", registration.getName(), event.getName());
        }

        registrationService.removeRegistration(registrationKey, event, isRemovedByUser);
        return eventRepository.update(event);
    }

    /**
     * @param signedInUser the user performing this action
     * @param spec         the update specification
     * @return the updated event
     * @throws NoSuchElementException when the event or the registration does not exist
     * @throws IllegalStateException  when the event cannot be reloaded after removing the registration
     */
    public @NonNull Event updateRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final UpdateRegistrationSpec spec
    ) throws NoSuchElementException, IllegalStateException {
        var event = eventRepository.findByKey(spec.eventKey())
            .orElseThrow(() -> new NoSuchElementException("Event does not exist"));
        var registration = event.findRegistrationByKey(spec.registrationKey())
            .orElseThrow(() -> new NoSuchElementException("Registration does not exist"));

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info(
                "User {} updates their registration on event {}",
                registration.getUserKey(),
                event.getName()
            );
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Updating registration {} on event {}", spec.registrationKey(), event.getName());
        }

        registrationService.updateRegistration(spec, event);
        return eventRepository.findByKey(spec.eventKey())
            .orElseThrow(() -> new IllegalStateException("Event does not exist after update"));
    }

}
