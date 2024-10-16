package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.common.Crypto;
import org.eventplanner.common.Encryptable;
import org.eventplanner.common.EncryptedString;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class EncryptedUserQualification implements Encryptable<UserQualification>, Serializable {
    private @NonNull EncryptedString qualificationKey;
    private @Nullable EncryptedString expiresAt;

    @Override
    public @NonNull UserQualification decrypt(@NonNull Crypto crypto) {
        return new UserQualification(
            new QualificationKey(crypto.decrypt(qualificationKey)),
            expiresAt != null ? ZonedDateTime.parse(crypto.decrypt(expiresAt)) : null
        );
    }
}
