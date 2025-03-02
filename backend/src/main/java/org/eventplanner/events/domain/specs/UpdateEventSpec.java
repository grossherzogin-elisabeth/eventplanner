package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.domain.entities.EventSlot;
import org.eventplanner.events.domain.values.EventLocation;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.springframework.lang.Nullable;

import lombok.NonNull;
import lombok.With;

@With
public record UpdateEventSpec(
    @Nullable String name,
    @Nullable EventState state,
    @Nullable String note,
    @Nullable String description,
    @Nullable Instant start,
    @Nullable Instant end,
    @Nullable List<EventLocation> locations,
    @Nullable List<EventSlot> slots
) {
    public UpdateEventSpec() {
        this(null, null, null, null, null, null, null, null);
    }

    public @NonNull List<RegistrationKey> getAssignedRegistrationKeys() {
        if (slots() == null || slots().isEmpty()) {
            return Collections.emptyList();
        }
        return slots().stream().map(EventSlot::getAssignedRegistration).filter(Objects::nonNull).toList();
    }
}