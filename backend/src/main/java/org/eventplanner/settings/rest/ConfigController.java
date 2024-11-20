package org.eventplanner.settings.rest;

import org.eventplanner.settings.SettingsUseCase;
import org.eventplanner.settings.rest.dto.SettingsRepresentation;
import org.eventplanner.users.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/config")
@EnableMethodSecurity(securedEnabled = true)
public class ConfigController {

    private final UserUseCase userUseCase;
    private final SettingsUseCase settingsUseCase;

    @Autowired
    public ConfigController(
        UserUseCase userUseCase,
        SettingsUseCase settingsUseCase
    ) {
        this.userUseCase = userUseCase;
        this.settingsUseCase = settingsUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<SettingsRepresentation.Ui> getConfig() {
        var settings = settingsUseCase.getUiSettings();
        return ResponseEntity.ok(SettingsRepresentation.Ui.fromDomain(settings));
    }
}
