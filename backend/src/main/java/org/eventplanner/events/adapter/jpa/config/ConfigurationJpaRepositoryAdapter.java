package org.eventplanner.events.adapter.jpa.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.application.ports.ConfigurationRepository;
import org.eventplanner.events.application.ports.ConfigurationSource;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.functions.DecryptFunc;
import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.config.AuthConfig;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConfigurationJpaRepositoryAdapter implements ConfigurationSource, ConfigurationRepository {

    private final ConfigurationJpaRepository configurationJpaRepository;

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public @NonNull ApplicationConfig getConfig(@NonNull final DecryptFunc decryptFunc) {
        var settingsMap = configurationJpaRepository.findAll().stream()
            .filter(configurationJpaEntity -> configurationJpaEntity.getValue() != null)
            .collect(Collectors.toMap(
                ConfigurationJpaEntity::getKey,
                ConfigurationJpaEntity::getValue
            ));

        var emailPassword = settingsMap.get("email.password");
        if (emailPassword != null) {
            emailPassword = decryptFunc.apply(new Encrypted<>(emailPassword), String.class);
        }

        Integer emailPort = null;
        try {
            emailPort = Integer.parseInt(settingsMap.get("email.port"));
        } catch (NumberFormatException e) {
            // ignore
        }

        var teamsWebhookUrl = settingsMap.get("notifications.teamsWebhookUrl");
        if (teamsWebhookUrl != null) {
            teamsWebhookUrl = decryptFunc.apply(new Encrypted<>(teamsWebhookUrl), String.class);
        }
        var notificationSettings = new NotificationConfig(
            teamsWebhookUrl,
            null
        );

        var emailSettings = new EmailConfig(
            null,
            null,
            null,
            settingsMap.get("email.from"),
            settingsMap.get("email.fromDisplayName"),
            settingsMap.get("email.replyTo"),
            settingsMap.get("email.replyToDisplayName"),
            settingsMap.get("email.host"),
            emailPort,
            Boolean.valueOf(settingsMap.get("email.enableSSL")),
            Boolean.valueOf(settingsMap.get("email.enableStartTLS")),
            settingsMap.get("email.username"),
            emailPassword
        );

        var uiSettings = new FrontendConfig(
            settingsMap.get("ui.menuTitle"),
            settingsMap.get("ui.tabTitle"),
            settingsMap.get("ui.technicalSupportEmail"),
            settingsMap.get("ui.supportEmail"),
            settingsMap.get("ui.url")
        );

        return new ApplicationConfig(
            notificationSettings,
            emailSettings,
            uiSettings,
            new AuthConfig()
        );
    }

    @Override
    public void updateConfig(
        @NonNull final ApplicationConfig.UpdateSpec spec,
        @NonNull final EncryptFunc encryptFunc
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        entities.addAll(mapEmailSettings(spec.email(), encryptFunc));
        entities.addAll(mapNotificationSettings(spec.notifications(), encryptFunc));
        entities.addAll(mapFrontendSettings(spec.frontend()));
        configurationJpaRepository.saveAll(entities);
    }

    private @NonNull List<ConfigurationJpaEntity> mapFrontendSettings(
        @NonNull final FrontendConfig.UpdateSpec spec
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        if (spec.getMenuTitle() != null) {
            entities.add(new ConfigurationJpaEntity("ui.menuTitle", spec.getMenuTitle()));
        }
        if (spec.getTabTitle() != null) {
            entities.add(new ConfigurationJpaEntity("ui.tabTitle", spec.getTabTitle()));
        }
        if (spec.getTechnicalSupportEmail() != null) {
            entities.add(new ConfigurationJpaEntity(
                "ui.technicalSupportEmail",
                spec.getTechnicalSupportEmail()
            ));
        }
        if (spec.getSupportEmail() != null) {
            entities.add(new ConfigurationJpaEntity("ui.supportEmail", spec.getSupportEmail()));
        }
        return entities;
    }

    private @NonNull List<ConfigurationJpaEntity> mapNotificationSettings(
        @NonNull final NotificationConfig.UpdateSpec spec,
        @NonNull final EncryptFunc encryptFunc
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        if (spec.getTeamsWebhookUrl() != null) {
            if (spec.getTeamsWebhookUrl().isBlank()) {
                entities.add(new ConfigurationJpaEntity("notifications.teamsWebhookUrl", null));
            } else {
                var encryptedWebhookUrl = encryptFunc.apply(spec.getTeamsWebhookUrl());
                if (encryptedWebhookUrl != null) {
                    entities.add(new ConfigurationJpaEntity(
                        "notifications.teamsWebhookUrl",
                        encryptedWebhookUrl.value()
                    ));
                }
            }
        }
        return entities;
    }

    private @NonNull List<ConfigurationJpaEntity> mapEmailSettings(
        @NonNull final EmailConfig.UpdateSpec spec,
        @NonNull final EncryptFunc encryptFunc
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        if (spec.getFrom() != null) {
            entities.add(new ConfigurationJpaEntity("email.from", spec.getFrom()));
        }
        if (spec.getFromDisplayName() != null) {
            entities.add(new ConfigurationJpaEntity("email.fromDisplayName", spec.getFromDisplayName()));
        }
        if (spec.getReplyTo() != null) {
            entities.add(new ConfigurationJpaEntity("email.replyTo", spec.getReplyTo()));
        }
        if (spec.getReplyToDisplayName() != null) {
            entities.add(new ConfigurationJpaEntity(
                "email.replyToDisplayName",
                spec.getReplyToDisplayName()
            ));
        }
        if (spec.getHost() != null) {
            entities.add(new ConfigurationJpaEntity("email.host", spec.getHost()));
        }
        if (spec.getPort() != null) {
            entities.add(new ConfigurationJpaEntity("email.port", String.valueOf(spec.getPort())));
        }
        if (spec.getEnableSSL() != null) {
            entities.add(new ConfigurationJpaEntity(
                "email.enableSSL",
                String.valueOf(spec.getEnableSSL())
            ));
        }
        if (spec.getEnableStartTls() != null) {
            entities.add(new ConfigurationJpaEntity(
                "email.enableStartTLS",
                String.valueOf(spec.getEnableStartTls())
            ));
        }
        if (spec.getUsername() != null) {
            entities.add(new ConfigurationJpaEntity("email.username", spec.getUsername()));
        }
        if (spec.getPassword() != null) {
            if (spec.getPassword().isBlank()) {
                entities.add(new ConfigurationJpaEntity("email.password", null));
            } else {
                var encryptedPassword = encryptFunc.apply(spec.getPassword());
                if (encryptedPassword != null) {
                    entities.add(new ConfigurationJpaEntity("email.password", encryptedPassword.value()));
                }
            }
        }
        return entities;
    }
}
