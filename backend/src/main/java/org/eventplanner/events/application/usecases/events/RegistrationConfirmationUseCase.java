package org.eventplanner.events.application.usecases.events;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationConfirmationUseCase {

    private final ZoneId timezone = ZoneId.of("Europe/Berlin");

    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final RegistrationService registrationService;

    @PreAuthorize("hasAuthority('events:send-confirmation-requests')")
    public void sendConfirmationRequests() {
        var currentYear = Instant.now().atZone(timezone).getYear();
        var eventsToNotify = Stream.concat(
                eventRepository.findAllByYear(currentYear + 1).stream(),
                eventRepository.findAllByYear(currentYear).stream()
            )
            .filter(event -> event.getState().equals(EventState.PLANNED))
            // only events that start in the future within the next 2 weeks
            .filter(event -> event.getStart().isAfter(Instant.now()))
            .filter(event -> {
                var start = event.getStart().atZone(timezone);
                return start.minusWeeks(2).isBefore(ZonedDateTime.now(timezone));
            })
            .toList();
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify for registration confirmation request");
        } else {
            eventsToNotify.forEach(this::sendConfirmationRequests);
        }
    }

    private void sendConfirmationRequests(@NonNull final Event event) {
        if (event.getConfirmationsRequestsSent() >= 2) {
            log.debug("All confirmation requests for event {} have already been sent", event.getKey());
            // all requests have been sent
            return;
        }
        if (event.getConfirmationsRequestsSent() >= 1 && !event.isUpForConfirmationReminder()) {
            log.debug(
                "1st confirmation requests for event {} have already been sent and 2nd is not yet due",
                event.getKey()
            );
            // first request has been sent and 2nd is not yet due
            return;
        }

        try {
            log.info("Sending registration confirmation requests for event {}", event.getKey());
            // get a list of unconfirmed registrations
            var registrations = event.getAssignedRegistrations().stream()
                .filter(registration -> registration.getConfirmedAt() == null)
                .toList();

            var users = registrations.stream()
                .map(Registration::getUserKey)
                .distinct()
                .map(userService::getUserByKey)
                .flatMap(Optional::stream)
                .toList();

            users.forEach(user -> sendConfirmationRequest(event, user));

            if (event.isUpForConfirmationReminder()) {
                event.setConfirmationsRequestsSent(2);
            } else {
                event.setConfirmationsRequestsSent(1);
            }
            eventRepository.update(event);
        } catch (Exception e) {
            log.error(
                "Failed to send registration confirmation requests for event {}",
                event.getKey(),
                e
            );
        }
    }

    private void sendConfirmationRequest(@NonNull final Event event, @NonNull final UserDetails user) {
        try {
            var registration = event.findRegistrationByUserKey(user.getKey())
                .orElseThrow(() -> new NoSuchElementException("Cannot find registration for user"));
            if (event.isUpForConfirmationReminder()) {
                notificationService
                    .sendConfirmationReminderNotification(
                        user,
                        event,
                        registration
                    );
            } else if (event.isUpForConfirmationRequest()) {
                notificationService
                    .sendConfirmationRequestNotification(
                        user,
                        event,
                        registration
                    );
            }
        } catch (Exception e) {
            log.error("Failed to create registration confirmation notification for user {}", user.getKey(), e);
        }
    }

    @PreAuthorize("hasAuthority('registrations:write') " +
        "or hasAuthority('registrations:write-self') " +
        "or hasAuthority('registrations:confirm-self')")
    public void confirmRegistration(
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey
    ) {
        var event = eventRepository.findByKey(eventKey)
            .orElseThrow(() -> new NoSuchElementException("Event not found"));
        var registration = event.findRegistrationByKey(registrationKey)
            .orElseThrow(() -> new NoSuchElementException("Registration not found"));
        var signedInUser = authenticationService.getSignedInUser();
        if (!Objects.equals(registration.getUserKey(), signedInUser.key())) {
            log.error("User tried to confirm registration {} of other user", registrationKey);
            throw new MissingPermissionException("Registration belongs to another user");
        }

        if (registration.getConfirmedAt() != null) {
            log.info("User tried to confirm registration {}, which was already confirmed", registrationKey);
            return;
        }
        log.info("User {} confirmed their participation on event {}", registration.getUserKey(), event.getKey());
        registrationService.updateRegistration(
            new UpdateRegistrationSpec(
                event.getKey(),
                registration.getKey(),
                registration.getPosition(),
                registration.getUserKey(),
                registration.getName(),
                registration.getNote(),
                Instant.now(),
                registration.getOvernightStay(),
                registration.getArrival()
            ),
            event
        );
    }

    @PreAuthorize("hasAuthority('registrations:write') " +
        "or hasAuthority('registrations:write-self') " +
        "or hasAuthority('registrations:decline-self')")
    public void declineRegistration(
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey
    ) {
        var event = eventRepository.findByKey(eventKey)
            .orElseThrow(() -> new NoSuchElementException("Event not found"));
        var registration = event.findRegistrationByKey(registrationKey)
            .orElseThrow(() -> new NoSuchElementException("Registration not found"));
        var signedInUser = authenticationService.getSignedInUser();
        if (!Objects.equals(registration.getUserKey(), signedInUser.key())) {
            log.error("User tried to decline registration {} of other user", registrationKey);
            throw new MissingPermissionException("Registration belongs to another user");
        }

        // TODO should this be allowed?
        if (registration.getConfirmedAt() != null) {
            log.warn("User tried to decline registration {}, but was already confirmed", registrationKey);
            throw new IllegalStateException("User already confirmed their participation on event " + event.getKey());
        }

        log.info("User {} declined their participation on event {}", registration.getUserKey(), event.getKey());
        registrationService.removeRegistration(registration.getKey(), event, true);
        eventRepository.update(event);
    }
}
