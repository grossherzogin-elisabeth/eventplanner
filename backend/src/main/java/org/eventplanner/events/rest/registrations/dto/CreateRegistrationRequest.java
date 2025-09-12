package org.eventplanner.events.rest.registrations.dto;

import static org.eventplanner.common.ObjectUtils.mapNullable;

import java.io.Serializable;
import java.time.LocalDate;

import org.eventplanner.events.domain.specs.CreateRegistrationSpec;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.validation.constraints.NotNull;

public record CreateRegistrationRequest(
    @Nullable String registrationKey,
    @NotNull @NonNull String positionKey,
    @Nullable String userKey,
    @Nullable String name,
    @Nullable String note,
    @Nullable Boolean overnightStay,
    @Nullable LocalDate arrival
) implements Serializable {
    public CreateRegistrationSpec toDomain(@NonNull EventKey eventKey, boolean isSelfSignup) {
        return new CreateRegistrationSpec(
            new RegistrationKey(registrationKey),
            eventKey,
            new PositionKey(positionKey),
            mapNullable(userKey, UserKey::new),
            name,
            note,
            isSelfSignup,
            overnightStay,
            arrival
        );
    }
}
