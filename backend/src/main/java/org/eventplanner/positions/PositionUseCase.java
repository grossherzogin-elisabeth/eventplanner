package org.eventplanner.positions;

import org.eventplanner.exceptions.NotImplementedException;
import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

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

        positionRepository.create(position);
        return position;
    }

    public Position updatePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        positionRepository.update(position);
        return position;
    }

    public void deletePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        positionRepository.deleteByKey(positionKey);
    }
}
