package org.eventplanner.events.entities;

import lombok.*;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;
import org.eventplanner.events.values.RegistrationKey;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private @NonNull EventKey key = new EventKey();
    private @NonNull String name = "";
    private @NonNull EventState state = EventState.DRAFT;
    private @NonNull String note = "";
    private @NonNull String description = "";
    private @NonNull ZonedDateTime start = ZonedDateTime.now();
    private @NonNull ZonedDateTime end = ZonedDateTime.now();
    private @NonNull List<Location> locations = Collections.emptyList();
    private @NonNull List<Slot> slots = Collections.emptyList();
    private @NonNull List<Registration> registrations = Collections.emptyList();

    public void addRegistration(Registration registration) {
        var mutableList = new LinkedList<>(registrations);
        mutableList.add(registration);
        registrations = mutableList.stream().toList(); // make the list immutable again
    }

    public void removeRegistration(RegistrationKey registrationKey) {
        registrations = registrations.stream().filter(r -> !registrationKey.equals(r.getKey())).toList();
    }

    public void updateRegistration(RegistrationKey registrationKey, Registration registration) {
        registrations = registrations.stream().map(r -> registrationKey.equals(r.getKey()) ? registration : r).toList();
    }
}