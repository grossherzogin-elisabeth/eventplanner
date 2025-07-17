package org.eventplanner.events.adapter.jpa.events;

import java.time.Instant;
import java.time.LocalDate;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_registrations")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationJpaEntity {
    @Id
    @Column(name = "key", nullable = false, updatable = false)
    public @NonNull String key;

    @Column(name = "event_key", nullable = false)
    public @NonNull String eventKey;

    @Column(name = "position_key", nullable = false)
    public @NonNull String positionKey;

    @Column(name = "user_key")
    public @Nullable String userKey;

    @Column(name = "name")
    public @Nullable String name;

    @Column(name = "note")
    public @Nullable String note;

    @Column(name = "access_key")
    public @Nullable String accessKey;

    @Column(name = "confirmed_at")
    public @Nullable String confirmedAt;

    @Column(name = "overnight_stay")
    public @Nullable Boolean overnightStay;

    @Column(name = "arrival")
    public @Nullable String arrival;

    public static @NonNull RegistrationJpaEntity fromDomain(@NonNull Registration domain, @NonNull EventKey eventKey) {
        return new RegistrationJpaEntity(
            domain.getKey().value(),
            eventKey.value(),
            domain.getPosition().value(),
            domain.getUserKey() != null
                ? domain.getUserKey().value()
                : null,
            domain.getName(),
            domain.getNote(),
            domain.getAccessKey(),
            domain.getConfirmedAt() != null
                ? domain.getConfirmedAt().toString()
                : null,
            domain.getOvernightStay(),
            domain.getArrival() != null
                ? domain.getArrival().toString()
                : null
        );
    }

    public @NonNull Registration toDomain() {
        return new Registration(
            new RegistrationKey(key),
            new PositionKey(positionKey),
            userKey != null
                ? new UserKey(userKey)
                : null,
            name,
            note,
            accessKey,
            confirmedAt != null
                ? Instant.parse(confirmedAt)
                : null,
            overnightStay,
            arrival != null
                ? LocalDate.parse(arrival)
                : null
        );
    }
}
