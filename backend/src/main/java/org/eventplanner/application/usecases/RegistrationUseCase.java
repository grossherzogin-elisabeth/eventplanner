package org.eventplanner.application.usecases;

import org.eventplanner.application.ports.EventRepository;
import org.eventplanner.domain.entities.Event;
import org.eventplanner.application.services.RegistrationService;
import org.eventplanner.domain.specs.CreateRegistrationSpec;
import org.eventplanner.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.domain.values.EventKey;
import org.eventplanner.domain.values.RegistrationKey;
import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.values.Permission;
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

    public @NonNull Event addRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull CreateRegistrationSpec spec
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var userKey = spec.userKey();
        if (userKey != null) {
            // validate permission and request for user registration
            if (userKey.equals(signedInUser.key())) {
                signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
                log.info("User {} signed up on event {} ({})", userKey, event.getName(), eventKey);
            } else {
                signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
                log.info("Adding registration for user {} to event {} ({})", userKey, event.getName(), eventKey);
            }
            return this.registrationService.addRegistration(event, spec);
        } else if (spec.name() != null) {
            // validate permission and request for guest registration
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Adding registration for guest {} on event {} ({})", spec.name(), event.getName(), eventKey);
            return this.registrationService.addGuestRegistration(event, spec);
        } else {
            throw new IllegalArgumentException("Either a user key or a name must be provided");
        }
    }

    public @NonNull Event removeRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull RegistrationKey registrationKey
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
            .filter(r -> registrationKey.equals(r.getKey()))
            .findFirst()
            .orElseThrow();

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info(
                "User {} removed their registration from event {} ({})",
                signedInUser.key(),
                event.getName(),
                eventKey
            );
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            if (registration.getUserKey() != null) {
                log.info(
                    "Removing registration of {} from event {} ({})",
                    registration.getUserKey(),
                    event.getName(),
                    eventKey
                );
            } else if (registration.getName() != null) {
                log.info(
                    "Removing guest registration of {} from event {} ({})",
                    registration.getName(),
                    event.getName(),
                    eventKey
                );
            }
        }

        return registrationService.removeRegistration(event, registration);
    }

    public @NonNull Event updateRegistration(
        @NonNull SignedInUser signedInUser,
        @NonNull EventKey eventKey,
        @NonNull RegistrationKey registrationKey,
        @NonNull UpdateRegistrationSpec spec
    ) {
        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
            .filter(r -> registrationKey.equals(r.getKey()))
            .findFirst()
            .orElseThrow();

        if (signedInUser.key().equals(registration.getUserKey())) {
            signedInUser.assertHasAnyPermission(Permission.WRITE_OWN_REGISTRATIONS, Permission.WRITE_REGISTRATIONS);
            log.info(
                "User {} updates their registration on event {} ({})",
                registration.getUserKey(),
                event.getName(),
                eventKey
            );
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_REGISTRATIONS);
            log.info("Updating registration {} on event {} ({})", registrationKey, event.getName(), eventKey);
        }

        return registrationService.updateRegistration(event, registration, spec);
    }

}
