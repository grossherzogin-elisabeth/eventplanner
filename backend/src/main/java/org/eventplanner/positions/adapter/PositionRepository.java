package org.eventplanner.positions.adapter;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;

import java.util.List;

public interface PositionRepository {
    List<Position> findAll();
    void create(Position position);
    void update(Position position);
    void deleteByKey(PositionKey key);
    void deleteAll();
}
