package org.eventplanner.events.domain.entities.users;

import static java.util.Objects.requireNonNull;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EncryptedUserQualification implements Serializable {
    private @NonNull Encrypted<QualificationKey> qualificationKey;
    private @Nullable Encrypted<Instant> expiresAt;
    private @Nullable Encrypted<UserQualification.State> state;

    public @NonNull UserQualification decrypt(DecryptFunc decryptFunc) {
        var decryptedKey = requireNonNull(decryptFunc.apply(qualificationKey, QualificationKey.class));
        var decryptedExpiresAt = decryptFunc.apply(expiresAt, Instant.class);
        var decryptedState = decryptFunc.apply(state, UserQualification.State.class);
        if (decryptedState == null) {
            decryptedState = UserQualification.State.VALID;
        }
        return new UserQualification(decryptedKey, decryptedExpiresAt, null, decryptedState);
    }
}
