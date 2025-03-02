package org.eventplanner.events.rest.settings;

import org.eventplanner.events.application.usecases.SettingsUseCase;
import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.rest.settings.dto.SettingsRepresentation;
import org.eventplanner.events.rest.settings.dto.UpdateSettingsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/settings")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SettingsController {

    private final UserUseCase userUseCase;
    private final SettingsUseCase settingsUseCase;

    @GetMapping("")
    public ResponseEntity<SettingsRepresentation> getSettings() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var settings = settingsUseCase.getSettings(signedInUser);
        return ResponseEntity.ok(SettingsRepresentation.fromDomain(settings));
    }

    @PutMapping("")
    public ResponseEntity<SettingsRepresentation> updateSettings(
        @RequestBody UpdateSettingsRequest updateSettingsRequest
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var updatedSettings = settingsUseCase.updateSettings(signedInUser, updateSettingsRequest.toDomain());
        return ResponseEntity.ok(SettingsRepresentation.fromDomain(updatedSettings));
    }
}
