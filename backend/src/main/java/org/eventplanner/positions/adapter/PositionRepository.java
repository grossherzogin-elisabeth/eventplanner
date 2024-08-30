package org.eventplanner.positions.adapter;

import org.eventplanner.positions.entities.Position;

import java.util.List;

public interface PositionRepository {
    List<Position> findAll();
}
