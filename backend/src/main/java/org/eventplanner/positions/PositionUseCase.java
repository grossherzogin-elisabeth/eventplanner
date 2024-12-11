package org.eventplanner.positions;

import java.util.List;

import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PositionUseCase {

    private final PositionRepository positionRepository;

    public PositionUseCase(@Autowired PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getPosition(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_POSITIONS);

        return positionRepository.findAll();
    }

    public Position createPosition(@NonNull SignedInUser signedInUser, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        position.setKey(new PositionKey());
        log.info("Creating position {}", position.getKey());
        positionRepository.create(position);
        return position;
    }

    public Position updatePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        if (!positionKey.equals(position.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating position {}", positionKey);
        positionRepository.update(position);
        return position;
    }

    public void deletePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        log.info("Deleting position {}", positionKey);
        // TODO remove position from all qualifications and events
        // TODO might want soft delete here
        positionRepository.deleteByKey(positionKey);
    }
}
