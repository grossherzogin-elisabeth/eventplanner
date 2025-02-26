package org.eventplanner.events.domain.entities;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.EventLocation;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.RegistrationKey;
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
    private @NonNull List<EventLocation> locations = Collections.emptyList();
    private @NonNull List<EventSlot> slots = Collections.emptyList();
    private @NonNull List<Registration> registrations = Collections.emptyList();
    private @NonNull Integer participationConfirmationsRequestsSent = 0;

    public List<Registration> getAssignedRegistrations() {
        var keys = getAssignedRegistrationKeys();
        return registrations.stream().filter(it -> keys.contains(it.getKey())).toList();
    }

    public List<RegistrationKey> getAssignedRegistrationKeys() {
        return getSlots().stream()
            .map(EventSlot::getAssignedRegistration)
            .filter(Objects::nonNull)
            .toList();
    }

    public boolean isUpForFirstParticipationConfirmationRequest() {
        return startsWithinNextDays(14);
    }

    public boolean isUpForSecondParticipationConfirmationRequest() {
        return startsWithinNextDays(7);
    }

    public boolean startsWithinNextDays(int n) {
        return getStart().isBefore(ZonedDateTime.now().plusDays(n).toInstant());
    }
}