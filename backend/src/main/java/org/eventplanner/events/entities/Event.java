package org.eventplanner.events.entities;

import lombok.*;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
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
    private @NonNull List<Location> locations = new LinkedList<>();
    private @NonNull List<Slot> slots = new LinkedList<>();
    private @NonNull List<Registration> registrations = new LinkedList<>();
}