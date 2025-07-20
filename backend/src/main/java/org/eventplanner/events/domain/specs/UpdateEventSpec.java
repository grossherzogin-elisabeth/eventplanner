package org.eventplanner.events.domain.specs;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.values.events.EventAccessType;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.With;

@With
public record UpdateEventSpec(
    @NonNull EventKey eventKey,
    @Nullable EventType type,
    @Nullable EventAccessType accessType,
    @Nullable String name,
    @Nullable EventState state,
    @Nullable String note,
    @Nullable String description,
    @Nullable Instant start,
    @Nullable Instant end,
    @Nullable List<EventLocation> locations,
    @Nullable List<EventSlot> slots,
    @Nullable List<RegistrationKey> registrationsToRemove,
    @Nullable List<CreateRegistrationSpec> registrationsToAdd,
    @Nullable List<UpdateRegistrationSpec> registrationsToUpdate
) {
    public UpdateEventSpec(@NonNull final EventKey eventKey) {
        this(eventKey, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public @NonNull List<RegistrationKey> getAssignedRegistrationKeys() {
        if (slots == null || slots.isEmpty()) {
            return Collections.emptyList();
        }
        return slots.stream().map(EventSlot::getAssignedRegistration).filter(Objects::nonNull).toList();
    }
}