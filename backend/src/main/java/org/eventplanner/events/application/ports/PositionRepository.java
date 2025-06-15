package org.eventplanner.events.application.ports;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PositionRepository {
    public @NonNull Optional<Position> findByKey(@Nullable final PositionKey key);

    public @NonNull List<Position> findAll();

    public @NonNull Map<PositionKey, Position> findAllAsMap();

    public void create(@NonNull final Position position);

    public void update(@NonNull final Position position);

    public void deleteByKey(@NonNull final PositionKey key);

    public void deleteAll();
}
