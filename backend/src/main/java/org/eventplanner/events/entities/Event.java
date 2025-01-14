package org.eventplanner.events.entities;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;
import org.eventplanner.events.values.RegistrationKey;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private @NonNull EventKey key = new EventKey();
    private @NonNull String name = "";
    private @NonNull EventState state = EventState.DRAFT;
    private @NonNull String note = "";
    private @NonNull String description = "";
    private @NonNull Instant start = Instant.now();
    private @NonNull Instant end = Instant.now();
    private @NonNull List<Location> locations = Collections.emptyList();
    private @NonNull List<Slot> slots = Collections.emptyList();
    private @NonNull List<Registration> registrations = Collections.emptyList();
    private @NonNull Integer participationConfirmationsRequestsSent = 0;

    public List<Registration> getAssignedRegistrations() {
        var keys = getAssignedRegistrationKeys();
        return registrations.stream().filter(it -> keys.contains(it.getKey())).toList();
    }

    public List<RegistrationKey> getAssignedRegistrationKeys() {
        return getSlots().stream()
            .map(Slot::getAssignedRegistration)
            .filter(Objects::nonNull)
            .toList();
    }

    public void addRegistration(Registration registration) {
        var mutableList = new LinkedList<>(registrations);
        mutableList.add(registration);
        registrations = mutableList.stream().toList(); // make the list immutable again
    }

    public void removeRegistration(RegistrationKey registrationKey) {
        registrations = registrations.stream().filter(r -> !registrationKey.equals(r.getKey())).toList();
        slots = slots.stream().map(s -> {
            if (registrationKey.equals(s.getAssignedRegistration())) {
                s.setAssignedRegistration(null);
            }

            return s;
        }).toList();
    }

    public void updateRegistration(RegistrationKey registrationKey, Registration registration) {
        registrations = registrations.stream().map(r -> registrationKey.equals(r.getKey()) ? registration : r).toList();
    }
}