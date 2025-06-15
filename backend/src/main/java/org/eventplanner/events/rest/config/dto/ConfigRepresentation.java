package org.eventplanner.events.rest.config.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.values.settings.FrontendSettings;
import org.springframework.lang.Nullable;

public record ConfigRepresentation(
    @Nullable String menuTitle,
    @Nullable String tabTitle,
    @Nullable String technicalSupportEmail,
    @Nullable String supportEmail
) implements Serializable {
    public static ConfigRepresentation fromDomain(FrontendSettings domain) {
        return new ConfigRepresentation(
            domain.menuTitle(),
            domain.tabTitle(),
            domain.technicalSupportEmail(),
            domain.supportEmail()
        );
    }
}
