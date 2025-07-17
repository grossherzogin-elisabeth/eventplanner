package org.eventplanner.events.rest.positions;

import java.util.List;

import org.eventplanner.events.application.usecases.PositionUseCase;
import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.rest.positions.dto.CreatePositionRequest;
import org.eventplanner.events.rest.positions.dto.PositionRepresentation;
import org.eventplanner.events.rest.positions.dto.UpdatePositionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/positions")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class PositionController {

    private final UserUseCase userUseCase;
    private final PositionUseCase positionUseCase;

    @PostMapping("")
    public ResponseEntity<PositionRepresentation> createPosition(@RequestBody CreatePositionRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = spec.toDomain();
        var position = positionUseCase.createPosition(signedInUser, positionSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(PositionRepresentation.fromDomain(position));
    }

    @GetMapping("")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positions = positionUseCase.getPosition(signedInUser).stream()
            .map(PositionRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(positions);
    }

    @PutMapping("/{positionKey}")
    public ResponseEntity<PositionRepresentation> updatePosition(
        @PathVariable String positionKey,
        @RequestBody UpdatePositionRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var positionSpec = spec.toDomain(positionKey);
        var position = positionUseCase.updatePosition(signedInUser, positionSpec.getKey(), positionSpec);
        return ResponseEntity.ok(PositionRepresentation.fromDomain(position));
    }

    @DeleteMapping("/{positionKey}")
    public ResponseEntity<Void> deletePosition(@PathVariable String positionKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        positionUseCase.deletePosition(signedInUser, new PositionKey(positionKey));
        return ResponseEntity.ok().build();
    }
}
