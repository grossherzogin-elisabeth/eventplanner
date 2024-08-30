package org.eventplanner.positions.adapter;

import java.util.List;

import org.eventplanner.positions.entities.Position;

public interface PositionRepository {
    List<Position> findAll();
}
