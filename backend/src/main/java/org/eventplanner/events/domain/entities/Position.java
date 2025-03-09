package org.eventplanner.events.domain.entities;

import org.eventplanner.events.domain.values.PositionKey;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Position {
    @NonNull PositionKey key;
    @NonNull String name;
    @NonNull String color;
    int priority;
    @NonNull String officialName;
}
