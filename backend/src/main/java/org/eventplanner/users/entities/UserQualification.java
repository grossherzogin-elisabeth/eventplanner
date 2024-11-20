package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class UserQualification implements Serializable {
    private @NonNull QualificationKey qualificationKey;
    private @Nullable Instant expiresAt;
    private boolean expires;
}
