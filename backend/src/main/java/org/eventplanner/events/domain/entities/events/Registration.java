package org.eventplanner.events.domain.entities.events;

import java.time.Instant;
import java.util.UUID;

import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class Registration {
    private @NonNull RegistrationKey key;
    private @NonNull PositionKey position;
    private @Nullable UserKey userKey;
    private @Nullable String name;
    private @Nullable String note;
    private @Nullable String accessKey;
    private @Nullable Instant confirmedAt;

    public static String generateAccessKey() {
        return UUID.randomUUID().toString();
    }
}
