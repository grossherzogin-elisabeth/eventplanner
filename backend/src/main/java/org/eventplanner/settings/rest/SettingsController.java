package org.eventplanner.settings.rest;

import org.eventplanner.settings.SettingsUseCase;
import org.eventplanner.settings.rest.dto.SettingsRepresentation;
import org.eventplanner.settings.rest.dto.UpdateSettingsRequest;
import org.eventplanner.users.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@EnableMethodSecurity(securedEnabled = true)
public class SettingsController {

    private final UserUseCase userUseCase;
    private final SettingsUseCase settingsUseCase;

    @Autowired
    public SettingsController(
        UserUseCase userUseCase,
        SettingsUseCase settingsUseCase
    ) {
        this.userUseCase = userUseCase;
        this.settingsUseCase = settingsUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<SettingsRepresentation> getSettings() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var settings = settingsUseCase.getSettings(signedInUser);
        return ResponseEntity.ok(SettingsRepresentation.fromDomain(settings));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "")
    public ResponseEntity<SettingsRepresentation> updateSettings(
        @RequestBody UpdateSettingsRequest updateSettingsRequest
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var updatedSettings = settingsUseCase.updateSettings(signedInUser, updateSettingsRequest.toDomain());
        return ResponseEntity.ok(SettingsRepresentation.fromDomain(updatedSettings));
    }
}
