package org.eventplanner.positions.adapter;

import java.util.List;
import java.util.Optional;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface PositionRepository {
    @NonNull Optional<Position> findByKey(@Nullable PositionKey key);
    @NonNull List<Position> findAll();
    void create(@NonNull Position position);
    void update(@NonNull Position position);
    void deleteByKey(@NonNull PositionKey key);
    void deleteAll();
}
