package org.eventplanner.domain.entities;

import java.io.Serializable;
import java.time.Instant;

import org.eventplanner.domain.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
