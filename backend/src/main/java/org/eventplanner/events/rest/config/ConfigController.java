package org.eventplanner.events.rest.config;

import org.eventplanner.events.application.usecases.SettingsUseCase;
import org.eventplanner.events.rest.config.dto.ConfigRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/config")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class ConfigController {

    private final SettingsUseCase settingsUseCase;

    @GetMapping("")
    public ResponseEntity<ConfigRepresentation> getConfig() {
        var settings = settingsUseCase.getUiSettings();
        return ResponseEntity.ok(ConfigRepresentation.fromDomain(settings));
    }
}
