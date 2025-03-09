package org.eventplanner.events.application.usecases.events;

import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.EventDetailsRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.aggregates.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfirmParticipationUseCase {

    private final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final EventDetailsRepository eventDetailsRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;

    public void sendParticipationNotificationRequest() {
        var eventsToNotify = getEvents()
            .filter(Event::isUpForFirstParticipationConfirmationRequest)
            .toList();
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify for participation confirmation");
        } else {
            eventsToNotify.forEach(event -> sendParticipationNotifications(event, 0));
        }
    }

    public void sendParticipationNotificationRequestReminder() {
        var eventsToNotify = getEvents()
            .filter(Event::isUpForSecondParticipationConfirmationRequest)
            .toList();
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify for participation confirmation reminder");
        } else {
            eventsToNotify.forEach(event -> sendParticipationNotifications(event, 1));
        }
    }

    private void sendParticipationNotifications(
        @NonNull final Event event,
        final int alreadySentRequests
    ) {
        log.info("Sending participation notification request for event {}", event.details().getName());
        // get a list of all registrations we want to notify
        var registrations = event.getAssignedRegistrations()
            .stream()
            .filter(registration -> registration.getConfirmedAt() == null)
            .filter(registration -> registration.getUserKey() != null)
            .toList();
        // make sure every userKey has a registration with accessKey
        // needed for legacy registrations, where accessKey was not generated
        registrations.stream()
            .filter(registration -> registration.getAccessKey() == null)
            .map(registration -> registration.withAccessKey(Registration.generateAccessKey()))
            .forEach(registrationRepository::updateRegistration);

        for (var registration : registrations) {
            var user = userService.getUserByKey(registration.getUserKey()).orElse(null);
            if (user == null) {
                continue;
            }
            switch (alreadySentRequests) {
                case 0 -> notificationService
                    .sendFirstParticipationConfirmationRequestNotification(user, event, registration);
                case 1 -> notificationService
                    .sendSecondParticipationConfirmationRequestNotification(user, event, registration);
            }
        }

        event.details().setParticipationConfirmationsRequestsSent(alreadySentRequests + 1);
        eventDetailsRepository.update(event.details());
    }

    private Stream<Event> getEvents() {
        var currentYear = Instant.now().atZone(timezone).getYear();
        return Stream.concat(
            eventService.findAllByYear(currentYear + 1).stream(),
            eventService.findAllByYear(currentYear).stream()
        );
    }

    public void confirmRegistration(
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey,
        @NonNull final String accessKey
    ) {
        var event = eventService.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrationByKey(registrationKey).orElseThrow();
        if (!Objects.equals(registration.getAccessKey(), accessKey)) {
            log.warn("User tried to edit registration {} with invalid access key {}", registrationKey, accessKey);
            throw new NoSuchElementException();
        }
        if (registration.getConfirmedAt() != null) {
            log.info("User tried to confirm registration {} another time, but was already confirmed", registrationKey);
            return;
        }
        log.info(
            "User {} confirmed their participation on event {}",
            registration.getUserKey(),
            event.details().getKey()
        );
        registrationService.updateRegistration(
            event, registration, new UpdateRegistrationSpec(
                registration.getPosition(),
                registration.getUserKey(),
                registration.getName(),
                registration.getNote(),
                Instant.now()
            )
        );
    }

    public void declineRegistration(
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey,
        @NonNull final String accessKey
    ) {
        // delete registration, find slot, delete registration key from slot, send email
        var event = eventService.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrationByKey(registrationKey).orElseThrow();
        if (!Objects.equals(registration.getAccessKey(), accessKey)) {
            log.warn("User tried to edit registration {} with invalid access key {}", registrationKey, accessKey);
            throw new NoSuchElementException();
        }
        // TODO should this be allowed?
        if (registration.getConfirmedAt() != null) {
            log.warn("User tried to decline registration {}, but was already confirmed", registrationKey);
            throw new IllegalStateException("User already confirmed their participation on event " + event.details()
                .getKey());
        }

        log.info(
            "User {} declined their participation on event {}",
            registration.getUserKey(),
            event.details().getKey()
        );
        registrationService.removeRegistration(event, registration, true);
    }
}
