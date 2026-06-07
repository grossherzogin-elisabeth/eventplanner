package org.eventplanner.events.rest.positions;

import java.util.List;

import org.eventplanner.events.application.usecases.positions.PositionUseCase;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.rest.positions.dto.CreatePositionRequest;
import org.eventplanner.events.rest.positions.dto.PositionRepresentation;
import org.eventplanner.events.rest.positions.dto.UpdatePositionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/positions")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class PositionController {

    private final PositionUseCase positionUseCase;

    @PostMapping("")
    public ResponseEntity<PositionRepresentation> createPosition(@Valid @RequestBody CreatePositionRequest spec) {
        var positionSpec = spec.toDomain();
        var position = positionUseCase.createPosition(positionSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(PositionRepresentation.fromDomain(position));
    }

    @GetMapping("")
    public ResponseEntity<List<PositionRepresentation>> getPositions() {
        var positions = positionUseCase.getPosition().stream()
            .map(PositionRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(positions);
    }

    @PutMapping("/{positionKey}")
    public ResponseEntity<PositionRepresentation> updatePosition(
        @PathVariable String positionKey,
        @Valid @RequestBody UpdatePositionRequest spec
    ) {
        var positionSpec = spec.toDomain(positionKey);
        var position = positionUseCase.updatePosition(positionSpec.getKey(), positionSpec);
        return ResponseEntity.ok(PositionRepresentation.fromDomain(position));
    }

    @DeleteMapping("/{positionKey}")
    public ResponseEntity<Void> deletePosition(@PathVariable String positionKey) {
        positionUseCase.deletePosition(new PositionKey(positionKey));
        return ResponseEntity.ok().build();
    }
}
