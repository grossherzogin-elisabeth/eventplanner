package org.eventplanner.events.application.usecases;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.RegistrationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.specs.UpdateRegistrationSpec;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
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
    private final UserService userService;
    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;

    public void sendConfirmationRequests() {
        var eventsToNotify = getEventsToNotify(0);
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify for registration confirmation request");
        } else {
            for (final Event event : eventsToNotify) {
                try {
                    sendNotifications(event, 0);
                } catch (Exception e) {
                    log.error(
                        "Failed to send registration confirmation requests for event {}",
                        event.getName(),
                        e
                    );
                }
            }
        }
    }

    public void sendConfirmationReminders() {
        var eventsToNotify = getEventsToNotify(1);
        if (eventsToNotify.isEmpty()) {
            log.debug("No events to notify for registration confirmation reminder");
        } else {
            for (final Event event : eventsToNotify) {
                try {
                    sendNotifications(event, 1);
                } catch (Exception e) {
                    log.error(
                        "Failed to send registration confirmation reminder for event {}",
                        event.getName(),
                        e
                    );
                }
            }
        }
    }

    private void sendNotifications(@NonNull final Event event, final int alreadySentRequests) {
        log.info(
            "Sending registration confirmation requests #{} for event {}",
            alreadySentRequests + 1,
            event.getName()
        );
        var registrations = event.getAssignedRegistrations().stream()
            .filter(registration -> registration.getConfirmedAt() == null)
            .toList();

        // make sure every userKey has a registration with accessKey
        // needed for legacy registrations, where accessKey was not generated
        registrations.stream()
            .filter(registration -> registration.getAccessKey() == null)
            .forEach(registration -> {
                log.info("Generating missing access key for registration {}", registration.getKey());
                registration.setAccessKey(Registration.generateAccessKey());
                registrationRepository.updateRegistration(registration, event.getKey());
            });

        var userKeyRegistrationMap = new HashMap<UserKey, Registration>();
        registrations.forEach(registration -> userKeyRegistrationMap.put(registration.getUserKey(), registration));
        var users = registrations.stream()
            .map(Registration::getUserKey)
            .distinct()
            .map(userService::getUserByKey)
            .flatMap(Optional::stream)
            .toList();

        for (var user : users) {
            var registration = userKeyRegistrationMap.get(user.getKey());
            try {
                if (alreadySentRequests == 0) {
                    notificationService
                        .sendConfirmationRequestNotification(
                            user,
                            event,
                            registration
                        );
                } else if (alreadySentRequests == 1) {
                    notificationService
                        .sendConfirmationReminderNotification(
                            user,
                            event,
                            registration
                        );
                }
            } catch (Exception e) {
                log.error("Failed to create registration confirmation notification for user {}", user.getKey(), e);
            }
        }

        event.setParticipationConfirmationsRequestsSent(alreadySentRequests + 1);
        eventRepository.update(event);
    }

    private List<Event> getEventsToNotify(final int alreadySentRequests) {
        var currentYear = Instant.now().atZone(timezone).getYear();
        return Stream.concat(
                eventRepository.findAllByYear(currentYear + 1).stream(),
                eventRepository.findAllByYear(currentYear).stream()
            )
            .filter(event -> event.getState().equals(EventState.PLANNED))
            .filter(event -> event.getEnd().isAfter(Instant.now()))
            .filter(event -> event.getParticipationConfirmationsRequestsSent() == alreadySentRequests)
            .filter(event -> {
                var start = event.getStart().atZone(timezone);
                if (alreadySentRequests == 0) {
                    return start.minusWeeks(2).isBefore(ZonedDateTime.now(timezone));
                }
                if (alreadySentRequests == 1) {
                    return start.minusWeeks(1).isBefore(ZonedDateTime.now(timezone));
                }
                return false;
            })
            .toList();
    }

    public Event getEventByAccessKey(
        @NonNull final EventKey eventKey,
        @NonNull final String accessKey
    ) {
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
            .filter(r -> accessKey.equals(r.getAccessKey()))
            .findFirst();
        if (registration.isPresent()) {
            // clear assigned registrations on slots
            event.getSlots().forEach(slot -> slot.setAssignedRegistration(null));
            // clear notes of all others
            event.getRegistrations().stream()
                .filter(it -> it.getNote() != null)
                .filter(it -> !Objects.equals(it.getAccessKey(), accessKey))
                .forEach(it -> it.setNote(null));
            return event;
        }
        throw new NoSuchElementException();
    }

    public void confirmRegistration(
        @NonNull final EventKey eventKey,
        @NonNull final RegistrationKey registrationKey,
        @NonNull final String accessKey
    ) {
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        var registration = getRegistrationByKey(event, registrationKey);
        if (!Objects.equals(registration.getAccessKey(), accessKey)) {
            log.warn("User tried to confirm registration {} with invalid access key {}", registrationKey, accessKey);
            throw new NoSuchElementException();
        }
        if (registration.getConfirmedAt() != null) {
            log.info("User tried to confirm registration {}, which was already confirmed", registrationKey);
            return;
        }
        log.info("User {} confirmed their participation on event {}", registration.getUserKey(), event.getName());
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
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        var registration = getRegistrationByKey(event, registrationKey);
        if (!Objects.equals(registration.getAccessKey(), accessKey)) {
            log.warn("User tried to edit registration {} with invalid access key {}", registrationKey, accessKey);
            throw new NoSuchElementException();
        }
        // TODO should this be allowed?
        if (registration.getConfirmedAt() != null) {
            log.warn("User tried to decline registration {}, but was already confirmed", registrationKey);
            throw new IllegalStateException("User already confirmed their participation on event " + event.getName());
        }

        log.info("User {} declined their participation on event {}", registration.getUserKey(), event.getName());
        registrationService.removeRegistration(event, registration, true);
    }

    private Registration getRegistrationByKey(
        @NonNull final Event event,
        @NonNull final RegistrationKey registrationKey
    ) {
        return event.getRegistrations().stream()
            .filter(r -> registrationKey.equals(r.getKey()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Registration not found"));
    }
}
