package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.aggregates.ApplicationConfig.ApplicationConfigUpdateSpec;
import org.eventplanner.events.domain.functions.EncryptFunc;
import org.jspecify.annotations.NonNull;

public interface ConfigurationRepository {
    public void updateConfig(
        @NonNull final ApplicationConfigUpdateSpec spec,
        @NonNull final EncryptFunc encryptFunc
    );
}
