package org.eventplanner.positions.adapter.jpa;

import java.util.List;

import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.stereotype.Component;

@Component
public class PositionJpaRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository positionJpaRepository;

    public PositionJpaRepositoryAdapter(final PositionJpaRepository positionJpaRepository) {
        this.positionJpaRepository = positionJpaRepository;
    }

    @Override
    public List<Position> findAll() {
        return positionJpaRepository.findAll().stream().map(PositionJpaEntity::toDomain).toList();
    }

    @Override
    public void create(Position position) {
        if (positionJpaRepository.existsById(position.key().value())) {
            throw new IllegalArgumentException("Position with key " + position.key().value() + " already exists");
        }
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void update(Position position) {
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void deleteByKey(PositionKey key) {
        positionJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAll() {
        positionJpaRepository.deleteAll();
    }
}
