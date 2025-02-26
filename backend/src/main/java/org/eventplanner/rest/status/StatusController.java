package org.eventplanner.rest.status;

import org.eventplanner.application.usecases.StatusUseCase;
import org.eventplanner.rest.status.dto.StatusRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/status")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class StatusController {

    private final StatusUseCase statusUseCase;

    @GetMapping("")
    public ResponseEntity<StatusRepresentation> getStatus() {
        var status = statusUseCase.getSystemStatus();
        return ResponseEntity.ok(StatusRepresentation.fromDomain(status));
    }
}
