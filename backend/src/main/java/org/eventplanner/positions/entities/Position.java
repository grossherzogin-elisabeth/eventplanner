package org.eventplanner.positions.entities;

import lombok.*;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Position {
    @NonNull PositionKey key;
    @NonNull String name;
    @NonNull String color;
    int priority;
    @NonNull String imoListRank;
}
