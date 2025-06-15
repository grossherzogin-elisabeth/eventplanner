package org.eventplanner.events.application.services;

import java.util.Comparator;
import java.util.List;

import org.eventplanner.events.application.ports.ConfigurationRepository;
import org.eventplanner.events.application.ports.ConfigurationSource;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.values.NotificationType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;
    private final List<ConfigurationSource> configurationSources;
    private final EncryptionService encryptionService;
    private ApplicationConfig applicationConfig;

    public boolean isNotificationEnabled(@NonNull final NotificationType type) {
        return getConfig().notifications().isNotificationEnabled(type);
    }

    public @NonNull ApplicationConfig getConfig() {
        if (applicationConfig == null) {
            applicationConfig = readConfig();
        }
        return applicationConfig;
    }

    public @NonNull ApplicationConfig updateConfig(@NonNull final ApplicationConfig.UpdateRequest spec) {
        var changed = spec.clearUnchanged(getConfig());
        configurationRepository.updateConfig(changed, encryptionService::encrypt);
        applicationConfig = readConfig();
        return applicationConfig;
    }

    private @NonNull ApplicationConfig readConfig() {
        return configurationSources.stream()
            .sorted(Comparator.comparing(ConfigurationSource::getPriority))
            .map(s -> s.getConfig(encryptionService::decrypt))
            .reduce(ApplicationConfig::apply)
            .orElse(new ApplicationConfig());
    }
}
