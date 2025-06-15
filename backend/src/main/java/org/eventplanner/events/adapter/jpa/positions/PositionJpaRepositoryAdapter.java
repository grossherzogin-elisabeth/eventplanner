package org.eventplanner.events.adapter.jpa.positions;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class PositionJpaRepositoryAdapter implements PositionRepository {

    private final PositionJpaRepository positionJpaRepository;

    public PositionJpaRepositoryAdapter(final PositionJpaRepository positionJpaRepository) {
        this.positionJpaRepository = positionJpaRepository;
    }

    @Override
    public @NonNull Optional<Position> findByKey(@Nullable final PositionKey key) {
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
    public @NonNull Map<PositionKey, Position> findAllAsMap() {
        return positionJpaRepository.findAll().stream()
            .map(PositionJpaEntity::toDomain)
            .collect(toMap(Position::getKey, identity()));
    }

    @Override
    public void create(@NonNull final Position position) {
        if (positionJpaRepository.existsById(position.getKey().value())) {
            throw new IllegalArgumentException("Position with key " + position.getKey().value() + " already exists");
        }
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void update(@NonNull final Position position) {
        positionJpaRepository.save(PositionJpaEntity.fromDomain(position));
    }

    @Override
    public void deleteByKey(@NonNull final PositionKey key) {
        positionJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAll() {
        positionJpaRepository.deleteAll();
    }
}
