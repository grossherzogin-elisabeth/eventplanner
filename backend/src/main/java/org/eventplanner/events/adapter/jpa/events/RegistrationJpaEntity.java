package org.eventplanner.events.adapter.jpa.events;

import java.time.Instant;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import static java.util.Optional.ofNullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import static org.eventplanner.common.ObjectUtils.mapNullable;

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

    public static @NonNull RegistrationJpaEntity fromDomain(@NonNull Registration domain) {
        return new RegistrationJpaEntity(
            domain.getKey().value(),
            domain.getEventKey().value(),
            domain.getPosition().value(),
            mapNullable(domain.getUserKey(), UserKey::value),
            domain.getName(),
            domain.getNote(),
            domain.getAccessKey(),
            ofNullable(domain.getConfirmedAt()).map(Instant::toString).orElse(null)
        );
    }

    public @NonNull Registration toDomain() {
        return new Registration(
            new RegistrationKey(key),
            new PositionKey(positionKey),
            new EventKey(eventKey),
            mapNullable(userKey, UserKey::new),
            name,
            note,
            accessKey,
            ofNullable(confirmedAt).map(Instant::parse).orElse(null)
        );
    }
}
