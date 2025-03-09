package org.eventplanner.testdata;

import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.Position;
import org.eventplanner.events.domain.exceptions.NotImplementedException;
import org.eventplanner.events.domain.values.PositionKey;

public class PositionMockRepository implements PositionRepository {

    private List<Position> positions = List.of(
        new Position(PositionKeys.KAPITAEN, "kapitaen", "#fafafa", 11, "kapitaen"),
        new Position(PositionKeys.STM, "stm", "#fafafa", 10, "stm"),
        new Position(PositionKeys.NOA, "noa", "#fafafa", 9, "noa"),
        new Position(PositionKeys.MASCHINIST, "maschinist", "#fafafa", 8, "maschinist"),
        new Position(PositionKeys.MOA, "moa", "#fafafa", 7, "moa"),
        new Position(PositionKeys.KOCH, "koch", "#fafafa", 6, "koch"),
        new Position(PositionKeys.AUSBILDER, "ausbilder", "#fafafa", 5, "ausbilder"),
        new Position(PositionKeys.MATROSE, "matrose", "#fafafa", 4, "matrose"),
        new Position(PositionKeys.LEICHTMATROSE, "leichtmatrose", "#fafafa", 3, "leichtmatrose"),
        new Position(PositionKeys.DECKSHAND, "deckshand", "#fafafa", 2, "deckshand"),
        new Position(PositionKeys.BACKSCHAFT, "backschaft", "#fafafa", 1, "backschaft")
    );

    @Override
    public Optional<Position> findByKey(final PositionKey key) {
        return positions.stream().filter(position -> position.getKey().equals(key)).findFirst();
    }

    @Override
    public List<Position> findAll() {
        return positions;
    }

    @Override
    public void create(final Position position) {
        throw new NotImplementedException();
    }

    @Override
    public void update(final Position position) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteByKey(final PositionKey key) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteAll() {
        throw new NotImplementedException();
    }
}
