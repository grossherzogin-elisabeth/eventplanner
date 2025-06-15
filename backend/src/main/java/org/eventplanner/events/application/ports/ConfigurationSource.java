package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.springframework.lang.NonNull;

public interface ConfigurationSource {
    public int getPriority();

    public @NonNull ApplicationConfig getConfig(@NonNull final DecryptFunc decryptFunc);
}
