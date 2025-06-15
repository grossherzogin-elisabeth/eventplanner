package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.functions.EncryptFunc;
import org.springframework.lang.NonNull;

public interface ConfigurationRepository {
    public void updateConfig(
        @NonNull final ApplicationConfig.UpdateRequest spec,
        @NonNull final EncryptFunc encryptFunc
    );
}
