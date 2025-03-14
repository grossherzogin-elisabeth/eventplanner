package org.eventplanner.events.application.usecases;

import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.users.SignedInUser;
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

    private final EventService eventService;
    private final RegistrationService registrationService;

    public @NonNull Event addRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final CreateRegistrationSpec spec
    ) {
        var event = this.eventService.findByKey(eventKey).orElseThrow();
        var userKey = spec.userKey();
        if (userKey != null) {
            // validate permission and request for user registration
            if (userKey.equals(signedInUser.key())) {
                signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
                log.info("User {} signed up on event {}", userKey, event.details().getName());
                this.registrationService.addUserRegistration(event, spec, true);
            } else {
                signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
                log.info("Adding registration for user {} to event {}", userKey, event.details().getName());
                this.registrationService.addUserRegistration(event, spec, false);
            }
            return this.eventService.findByKey(eventKey).orElseThrow();
        } else if (spec.name() != null) {
            // validate permission and request for guest registration
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Adding registration for guest {} on event {}", spec.name(), event.details().getName());
            this.registrationService.addGuestRegistration(event, spec);
            return this.eventService.findByKey(eventKey).orElseThrow();
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }
    }

    public @NonNull Event removeRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey
    ) {
        var event = this.eventService.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrationByKey(registrationKey).orElseThrow();
        var isCanceledByUser = signedInUser.key().equals(registration.getUserKey());
        if (isCanceledByUser) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info(
                "User {} removed their registration from event {}",
                signedInUser.key(),
                event.details().getName()
            );
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            if (registration.getUserKey() != null) {
                log.info(
                    "Removing registration of {} from event {}",
                    registration.getUserKey(),
                    event.details().getName()
                );
            } else if (registration.getName() != null) {
                log.info(
                    "Removing guest registration of {} from event {}",
                    registration.getName(),
                    event.details().getName()
                );
            }
        }

        registrationService.removeRegistration(event, registration, isCanceledByUser);
        return this.eventService.findByKey(eventKey).orElseThrow();
    }

    public @NonNull Event updateRegistration(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey,
        @NonNull final UpdateRegistrationSpec spec
    ) {
        var event = this.eventService.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrationByKey(registrationKey).orElseThrow();

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info(
                "User {} updates their registration on event {}",
                registration.getUserKey(),
                event.details().getName()
            );
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Updating registration {} on event {}", registrationKey, event.details().getName());
        }

        registrationService.updateRegistration(event, registration, spec);
        return this.eventService.findByKey(eventKey).orElseThrow();
    }

}
