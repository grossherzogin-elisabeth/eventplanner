package org.eventplanner.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriConsumer;
import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.UserKey;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Slf4j
@AllArgsConstructor
public class ParticipationNotificationUseCase {

    private final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final EventRepository eventRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    public void sendParticipationNotificationRequest() {
        sendNotification((UserDetails user, Event event, Registration registration) -> {
            log.info("Sending participation notification to user {}", user.getEmail());
            notificationService.sendParticipationConfirmationNotification(user, event, registration);
        }, 0);
    }

    public void sendParticipationNotificationRequestReminder() {
        sendNotification((UserDetails user, Event event, Registration registration) -> {
            log.info("Sending participation notification request to user {}", user.getEmail());
            notificationService.sendParticipationConfirmationNotificationRequest(user, event, registration);
        }, 1);
    }

    private void sendNotification(TriConsumer<UserDetails, Event, Registration> function, int alreadySentRequests) {
        var eventsToNotify = getEventsToNotify(alreadySentRequests);

        if (eventsToNotify.isEmpty()) {
            switch (alreadySentRequests) {
                case 0 -> log.info("No events to notify for participation confirmation");
                case 1 -> log.info("No events to notify for participation confirmation reminder");
            }
            return;
        }

        eventsToNotify.forEach(event -> {
            log.info("Sending participation notification request for event {}", event.getName());

            var registrationKeys = event.getSlots()
                    .stream()
                    .map(Slot::getAssignedRegistration)
                    .filter(Objects::nonNull).toList();


            var userKeyAccessKeyMap = new HashMap<UserKey, Registration>();

            event.getRegistrations().stream()
                    .filter(registration -> registrationKeys.contains(registration.getKey()) && registration.getUserKey() != null)
                    .filter(registration -> registration.getConfirmedAt() == null)
                    .forEach(registration -> userKeyAccessKeyMap.put(registration.getUserKey(), registration));

            var users = event.getRegistrations().stream()
                    .filter(registration -> registrationKeys.contains(registration.getKey()))
                    .map(Registration::getUserKey)
                    .filter(Objects::nonNull)
                    .map(userService::getUserByKey)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            // make sure every userKey has a registration with accessKey
            // needed for legacy registrations, where accessKey was not generated
            userKeyAccessKeyMap.entrySet().stream()
                    .filter(entry -> entry.getValue().getAccessKey() == null)
                    .forEach(entry -> entry.getValue().setAccessKey(Registration.generateAccessKey()));

            users.forEach(user -> function.accept(user, event, userKeyAccessKeyMap.get(user.getKey())));

            event.setParticipationConfirmationsRequestsSent(alreadySentRequests + 1);
            eventRepository.update(event);
        });
    }

    private List<Event> getEventsToNotify(int alreadySentRequests) {
        var currentYear = Instant.now().atZone(timezone).getYear();
        return Stream.concat(
                eventRepository.findAllByYear(currentYear + 1).stream(),
                eventRepository.findAllByYear(currentYear).stream())
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

    public void confirmRegistration(EventKey eventKey, RegistrationKey registrationKey, String accessKey) {
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
                .filter(r -> registrationKey.equals(r.getKey()) && accessKey.equals(r.getAccessKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        if (registration.getConfirmedAt() != null) {
            return;
        }

        registration.setConfirmedAt(Instant.now());
        event.updateRegistration(registrationKey, registration);
        eventRepository.update(event);
    }

    public void declineRegistration(EventKey eventKey, RegistrationKey registrationKey, String accessKey) {
        // delete registration, find slot, delete registration key from slot, send email
        var event = eventRepository.findByKey(eventKey).orElseThrow();
        var registration = event.getRegistrations().stream()
                .filter(r -> registrationKey.equals(r.getKey()) && accessKey.equals(r.getAccessKey()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        event.removeRegistration(registration.getKey());

        event.getSlots().stream()
                .filter(s -> registrationKey.equals(s.getAssignedRegistration()))
                .findFirst()
                .ifPresent((slot) -> slot.setAssignedRegistration(null));

        eventRepository.update(event);

        if (registration.getUserKey() != null) {
            notificationService.sendRemovedFromCrewNotification(userService.getUserByKey(registration.getUserKey()).orElseThrow(), event);
        }

        // notify admins about declined registration
        var userName = registration.getUserKey() != null ?
                userService.getUserByKey(registration.getUserKey()).map(UserDetails::getFullName).orElseThrow() :
                registration.getName();

        notificationService.sendDeclinedRegistrationNotification(event, userName);
    }
}
