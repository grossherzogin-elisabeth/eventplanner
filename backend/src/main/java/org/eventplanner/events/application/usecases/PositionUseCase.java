package org.eventplanner.events.application.usecases;

import java.util.List;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PositionUseCase {

    private final PositionRepository positionRepository;

    public @NonNull List<Position> getPosition(@NonNull final SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_POSITIONS);

        return positionRepository.findAll();
    }

    public @NonNull Position createPosition(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Position position
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        position.setKey(new PositionKey());
        log.info("Creating position {}", position.getKey());
        positionRepository.create(position);
        return position;
    }

    public @NonNull Position updatePosition(
        @NonNull final SignedInUser signedInUser,
        @NonNull final PositionKey positionKey,
        @NonNull final Position position
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        if (!positionKey.equals(position.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating position {}", positionKey);
        positionRepository.update(position);
        return position;
    }

    public void deletePosition(
        @NonNull final SignedInUser signedInUser,
        @NonNull final PositionKey positionKey
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        log.info("Deleting position {}", positionKey);
        // TODO remove position from all qualifications and events
        // TODO might want soft delete here
        positionRepository.deleteByKey(positionKey);
    }
}
