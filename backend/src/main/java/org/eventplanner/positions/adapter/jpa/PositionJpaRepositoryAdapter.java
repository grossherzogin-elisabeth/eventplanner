package org.eventplanner.positions.adapter.jpa;

import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PositionJpaRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository positionJpaRepository;

    public PositionJpaRepositoryAdapter(final PositionJpaRepository positionJpaRepository) {
        this.positionJpaRepository = positionJpaRepository;
    }

    @Override
    public @NonNull Optional<Position> findByKey(@Nullable PositionKey key) {
        if (key == null) {
            return Optional.empty();
        }
        return positionJpaRepository.findById(key.value()).map(PositionJpaEntity::toDomain);
    }

    @Override
    public @NonNull List<Position> findAll() {
        return positionJpaRepository.findAll().stream().map(PositionJpaEntity::toDomain).toList();
    }

    @Override
    public void create(@NonNull Position position) {
        if (positionJpaRepository.existsById(position.getKey().value())) {
            throw new IllegalArgumentException("Position with key " + position.getKey().value() + " already exists");
        }
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void update(@NonNull Position position) {
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void deleteByKey(@NonNull PositionKey key) {
        positionJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAll() {
        positionJpaRepository.deleteAll();
    }
}
