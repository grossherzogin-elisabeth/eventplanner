package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PositionRepository {
    @NonNull
    Optional<Position> findByKey(@Nullable PositionKey key);

    @NonNull
    List<Position> findAll();

    void create(@NonNull Position position);

    void update(@NonNull Position position);

    void deleteByKey(@NonNull PositionKey key);

    void deleteAll();
}
