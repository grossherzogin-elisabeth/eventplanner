package org.eventplanner.events.domain.entities.users;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;

import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class UserQualification implements Serializable {
    private @NonNull QualificationKey qualificationKey;
    private @Nullable Instant expiresAt;
    private @Nullable Boolean expires;
    private @Nullable State state;

    public @NonNull EncryptedUserQualification encrypt(@NonNull final EncryptFunc encryptFunc) {
        return new EncryptedUserQualification(
            requireNonNull(encryptFunc.apply(qualificationKey)),
            encryptFunc.apply(expiresAt),
            encryptFunc.apply(state)
        );
    }

    public boolean isExpired() {
        if (!Boolean.TRUE.equals(expires)) {
            return false;
        }
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }

    public boolean willExpireSoon() {
        if (!Boolean.TRUE.equals(expires)) {
            return false;
        }
        return expiresAt != null
            && expiresAt.isAfter(Instant.now()) // not yet expired
            && expiresAt.isBefore(Instant.now().plus(60, ChronoUnit.DAYS)); // will expire within 2 months
    }

    public enum State {
        VALID("valid"),
        WILL_EXPIRE_SOON("will_expire_soon"),
        EXPIRED("expired");

        private final String value;

        State(@NonNull final String value) {
            this.value = value;
        }

        @JsonCreator
        public static @NonNull State parse(@Nullable final String value) {
            // fallback to valid to prevent false alerts
            return fromString(value).orElse(State.VALID);
        }

        public static @NonNull Optional<State> fromString(@Nullable final String value) {
            return Arrays.stream(values())
                .filter(state -> state.toString().equals(value))
                .findFirst();
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }
    }
}
