package org.eventplanner.qualifications.entities;

import java.util.Collections;
import java.util.List;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.values.QualificationKey;
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
public class Qualification {
    private @NonNull QualificationKey key;
    private @NonNull String name;
    private @Nullable String icon;
    private @Nullable String description;
    private boolean expires;
    private @NonNull List<PositionKey> grantsPositions = Collections.emptyList();
}
