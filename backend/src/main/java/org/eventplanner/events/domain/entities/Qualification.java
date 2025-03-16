package org.eventplanner.events.domain.entities;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Qualification implements Serializable {
    private @NonNull QualificationKey key;
    private @NonNull String name;
    private @Nullable String icon;
    private @Nullable String description;
    private boolean expires;
    private @NonNull List<PositionKey> grantsPositions = Collections.emptyList();
}
