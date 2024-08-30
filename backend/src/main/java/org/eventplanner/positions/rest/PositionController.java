package org.eventplanner.positions.rest;

import org.eventplanner.positions.PositionUseCase;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.rest.dto.PositionRepresentation;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/positions")
@EnableMethodSecurity(securedEnabled = true)
public class PositionController {

    private final UserUseCase userUseCase;
    private final PositionUseCase positionUseCase;

    public PositionController(
        @Autowired UserUseCase userUseCase,
        @Autowired PositionUseCase positionUseCase
    ) {
        this.userUseCase = userUseCase;
        this.positionUseCase = positionUseCase;
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<PositionRepresentation> createPosition(@RequestBody PositionRepresentation spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = new Position(new PositionKey(""), spec.name(), spec.color(), spec.prio());
        var position = positionUseCase.createPosition(signedInUser, positionSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(PositionRepresentation.fromDomain(position));
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positions = positionUseCase.getPosition(signedInUser).stream()
            .map(PositionRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(positions);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{positionKey}")
    public ResponseEntity<PositionRepresentation> updatePosition(@PathVariable String positionKey, @RequestBody PositionRepresentation spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = new Position(new PositionKey(positionKey), spec.name(), spec.color(), spec.prio());
        var position = positionUseCase.updatePosition(signedInUser, positionSpec.key(), positionSpec);
        return ResponseEntity.ok(PositionRepresentation.fromDomain(position));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{positionKey}")
    public ResponseEntity<Void> deletePosition(@PathVariable String positionKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        positionUseCase.deletePosition(signedInUser, new PositionKey(positionKey));
        return ResponseEntity.ok().build();
    }
}
