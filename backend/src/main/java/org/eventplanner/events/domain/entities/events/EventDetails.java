package org.eventplanner.events.domain.entities.events;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventLocation;
import org.eventplanner.events.domain.values.EventState;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EventDetails {
    private @NonNull EventKey key = new EventKey();
    private @NonNull String name = "";
    private @NonNull EventState state = EventState.DRAFT;
    private @NonNull String note = "";
    private @NonNull String description = "";
    private @NonNull Instant start = Instant.now();
    private @NonNull Instant end = Instant.now();
    private @NonNull List<EventLocation> locations = Collections.emptyList();
    private @NonNull List<EventSlot> slots = Collections.emptyList();
    private @NonNull Integer participationConfirmationsRequestsSent = 0;
}