package org.eventplanner.events.application.usecases;

import java.util.List;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PositionUseCase {

    private final PositionRepository positionRepository;

    @PreAuthorize("hasAuthority('positions:read')")
    public @NonNull List<Position> getPosition() {
        log.debug("Reading positions");
        return positionRepository.findAll();
    }

    @PreAuthorize("hasAuthority('positions:write')")
    public @NonNull Position createPosition(@NonNull final Position position) {
        position.setKey(new PositionKey());
        log.info("Creating position {}", position.getKey());
        positionRepository.create(position);
        return position;
    }

    @PreAuthorize("hasAuthority('positions:write')")
    public @NonNull Position updatePosition(
        @NonNull final PositionKey positionKey,
        @NonNull final Position position
    ) {
        if (!positionKey.equals(position.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating position {}", positionKey);
        positionRepository.update(position);
        return position;
    }

    @PreAuthorize("hasAuthority('positions:write')")
    public void deletePosition(
        @NonNull final PositionKey positionKey
    ) {
        log.info("Deleting position {}", positionKey);
        // TODO remove position from all qualifications and events
        // TODO might want soft delete here
        positionRepository.deleteByKey(positionKey);
    }
}
