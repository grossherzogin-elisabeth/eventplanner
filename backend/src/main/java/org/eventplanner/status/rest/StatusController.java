package org.eventplanner.status.rest;

import org.eventplanner.status.StatusUseCase;
import org.eventplanner.status.rest.dto.StatusRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/status")
@EnableMethodSecurity(securedEnabled = true)
public class StatusController {

    private final StatusUseCase statusUseCase;

    @Autowired
    public StatusController(StatusUseCase statusUseCase) {
        this.statusUseCase = statusUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<StatusRepresentation> getStatus() {
        var status = statusUseCase.getSystemStatus();
        return ResponseEntity.ok(StatusRepresentation.fromDomain(status));
    }
}
