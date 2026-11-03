package org.eventplanner.events.rest.events.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.events.EventFlags;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record EventFlagsRepresentation(
    @Nullable Boolean enableDirectSignup,
    @Nullable Boolean enableRegistrationConfirmation,
    @Nullable Boolean enableOvernightStaySelection,
    @Nullable Boolean enableArrivalDaySelection
) implements Serializable {

    public static @NonNull EventFlagsRepresentation fromDomain(@NonNull EventFlags eventFlags) {
        return new EventFlagsRepresentation(
            eventFlags.isEnableDirectSignup(),
            eventFlags.isEnableRegistrationConfirmation(),
            eventFlags.isEnableOvernightStaySelection(),
            eventFlags.isEnableArrivalDaySelection()
        );
    }

    public @NonNull EventFlags toDomain() {
        return new EventFlags(
            Boolean.TRUE.equals(enableDirectSignup),
            Boolean.TRUE.equals(enableRegistrationConfirmation),
            Boolean.TRUE.equals(enableOvernightStaySelection),
            Boolean.TRUE.equals(enableArrivalDaySelection)
        );
    }
}
