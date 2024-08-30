package org.eventplanner.events;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.spec.CreateEventSpec;
import org.eventplanner.events.spec.UpdateEventSpec;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.exceptions.NotImplementedException;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
public class EventUseCase {
    private final EventRepository eventRepository;

    public EventUseCase(@Autowired EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public @NonNull List<Event> getEvents(@NonNull SignedInUser signedInUser, int year) {
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var currentYear = Instant.now().atZone(ZoneId.of("Europe/Berlin")).getYear();
        if (year < currentYear - 10 || year > currentYear + 10) {
            throw new IllegalArgumentException("Invalid year");
        }
        return this.eventRepository.findAllByYear(year);
    }

    public @NonNull Event createEvent(@NonNull SignedInUser signedInUser, @NonNull CreateEventSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        throw new NotImplementedException();
    }

    public @NonNull Event updateEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey, @NonNull UpdateEventSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        throw new NotImplementedException();
    }

    public @NonNull Event updateEventTeam(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey, @NonNull List<Registration> slots) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);

        throw new NotImplementedException();
    }

    public void deleteEvent(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        throw new NotImplementedException();
    }

    public @NonNull Event addUserToWaitingList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey, @NonNull UserKey userKey, @NonNull PositionKey positionkey) {
        if (userKey.equals(signedInUser.key())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
        }

        throw new NotImplementedException();
    }

    public @NonNull Event removeUserFromWaitingList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey, @NonNull UserKey userKey) {
        if (userKey.equals(signedInUser.key())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
        }

        throw new NotImplementedException();
    }

    public @NonNull Event removeUserFromTeam(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey, @NonNull UserKey userKey) {
        if (userKey.equals(signedInUser.key())) {
            signedInUser.assertHasAnyPermission(Permission.JOIN_LEAVE_EVENT_TEAM, Permission.WRITE_EVENT_TEAM);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_EVENT_TEAM);
        }

        throw new NotImplementedException();
    }
}
