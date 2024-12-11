package org.eventplanner.positions.entities;

import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
