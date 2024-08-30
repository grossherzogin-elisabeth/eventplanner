package org.eventplanner.positions;

import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.exceptions.NotImplementedException;
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

        return this.positionRepository.findAll();
    }

    public Position createPosition(@NonNull SignedInUser signedInUser, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }

    public Position updatePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey, Position position) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }

    public void deletePosition(@NonNull SignedInUser signedInUser, PositionKey positionKey) {
        signedInUser.assertHasPermission(Permission.WRITE_POSITIONS);

        throw new NotImplementedException("Positions are still hard coded in this version");
    }
}
