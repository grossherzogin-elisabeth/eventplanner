package org.eventplanner.events.domain.entities.users;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    private boolean expires;

    public @NonNull EncryptedUserQualification encrypt(@NonNull final EncryptFunc encryptFunc) {
        return new EncryptedUserQualification(
            requireNonNull(encryptFunc.apply(qualificationKey)),
            encryptFunc.apply(expiresAt)
        );
    }
}
