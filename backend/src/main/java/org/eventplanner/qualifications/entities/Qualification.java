package org.eventplanner.qualifications.entities;

import lombok.*;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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
    private @Nullable PositionKey grantsPosition;
}
