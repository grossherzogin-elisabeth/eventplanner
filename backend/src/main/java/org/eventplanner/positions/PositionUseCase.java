package org.eventplanner.positions;

import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
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

        position.setKey(new PositionKey(UUID.randomUUID().toString()));
        positionRepository.create(position);
        return position;
    }

    public Position updatePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        if (!positionKey.equals(position.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        positionRepository.update(position);
        return position;
    }

    public void deletePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        // TODO remove position from all qualifications and events
        // TODO might want soft delete here
        positionRepository.deleteByKey(positionKey);
    }
}
