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

    private static final String EMAIL_FROM = "email.from";
    private static final String EMAIL_FROM_DISPLAY_NAME = "email.fromDisplayName";
    private static final String EMAIL_REPLY_TO = "email.replyTo";
    private static final String EMAIL_REPLY_TO_DISPLAY_NAME = "email.replyToDisplayName";
    private static final String EMAIL_HOST = "email.host";
    private static final String EMAIL_PORT = "email.port";
    private static final String EMAIL_USERNAME = "email.username";
    private static final String EMAIL_PASSWORD = "email.password";
    private static final String EMAIL_ENABLE_SSL = "email.enableSSL";
    private static final String EMAIL_ENABLE_TLS = "email.enableStartTLS";
    private static final String UI_MENU_TITLE = "ui.menuTitle";
    private static final String UI_TAB_TITLE = "ui.tabTitle";
    private static final String UI_TEC_SUPPORT_EMAIL = "ui.technicalSupportEmail";
    private static final String UI_SUPPORT_EMAIL = "ui.supportEmail";
    private static final String UI_URL = "ui.url";
    private static final String TEAMS_WEBHOOK_URL = "notifications.teamsWebhookUrl";

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

        var emailPassword = settingsMap.get(EMAIL_PASSWORD);
        if (emailPassword != null) {
            emailPassword = decryptFunc.apply(new Encrypted<>(emailPassword), String.class);
        }

        Integer emailPort = null;
        try {
            emailPort = Integer.parseInt(settingsMap.get(EMAIL_PORT));
        } catch (NumberFormatException e) {
            // ignore
        }

        var teamsWebhookUrl = settingsMap.get(TEAMS_WEBHOOK_URL);
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
            settingsMap.get(EMAIL_FROM),
            settingsMap.get(EMAIL_FROM_DISPLAY_NAME),
            settingsMap.get(EMAIL_REPLY_TO),
            settingsMap.get(EMAIL_REPLY_TO_DISPLAY_NAME),
            settingsMap.get(EMAIL_HOST),
            emailPort,
            Boolean.valueOf(settingsMap.get(EMAIL_ENABLE_SSL)),
            Boolean.valueOf(settingsMap.get(EMAIL_ENABLE_TLS)),
            settingsMap.get(EMAIL_USERNAME),
            emailPassword
        );

        var uiSettings = new FrontendConfig(
            settingsMap.get(UI_MENU_TITLE),
            settingsMap.get(UI_TAB_TITLE),
            settingsMap.get(UI_TEC_SUPPORT_EMAIL),
            settingsMap.get(UI_SUPPORT_EMAIL),
            settingsMap.get(UI_URL)
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
            entities.add(new ConfigurationJpaEntity(UI_MENU_TITLE, spec.getMenuTitle()));
        }
        if (spec.getTabTitle() != null) {
            entities.add(new ConfigurationJpaEntity(UI_TAB_TITLE, spec.getTabTitle()));
        }
        if (spec.getTechnicalSupportEmail() != null) {
            entities.add(new ConfigurationJpaEntity(UI_TEC_SUPPORT_EMAIL, spec.getTechnicalSupportEmail()));
        }
        if (spec.getSupportEmail() != null) {
            entities.add(new ConfigurationJpaEntity(UI_SUPPORT_EMAIL, spec.getSupportEmail()));
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
                entities.add(new ConfigurationJpaEntity(TEAMS_WEBHOOK_URL, null));
            } else {
                var encryptedWebhookUrl = encryptFunc.apply(spec.getTeamsWebhookUrl());
                if (encryptedWebhookUrl != null) {
                    entities.add(new ConfigurationJpaEntity(TEAMS_WEBHOOK_URL, encryptedWebhookUrl.value()));
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
            entities.add(new ConfigurationJpaEntity(EMAIL_FROM, spec.getFrom()));
        }
        if (spec.getFromDisplayName() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_FROM_DISPLAY_NAME, spec.getFromDisplayName()));
        }
        if (spec.getReplyTo() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_REPLY_TO, spec.getReplyTo()));
        }
        if (spec.getReplyToDisplayName() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_REPLY_TO_DISPLAY_NAME, spec.getReplyToDisplayName()));
        }
        if (spec.getHost() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_HOST, spec.getHost()));
        }
        if (spec.getPort() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_PORT, String.valueOf(spec.getPort())));
        }
        if (spec.getEnableSSL() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_ENABLE_SSL, String.valueOf(spec.getEnableSSL())));
        }
        if (spec.getEnableStartTls() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_ENABLE_TLS, String.valueOf(spec.getEnableStartTls())));
        }
        if (spec.getUsername() != null) {
            entities.add(new ConfigurationJpaEntity(EMAIL_USERNAME, spec.getUsername()));
        }
        if (spec.getPassword() != null) {
            if (spec.getPassword().isBlank()) {
                entities.add(new ConfigurationJpaEntity(EMAIL_PASSWORD, null));
            } else {
                var encryptedPassword = encryptFunc.apply(spec.getPassword());
                if (encryptedPassword != null) {
                    entities.add(new ConfigurationJpaEntity(EMAIL_PASSWORD, encryptedPassword.value()));
                }
            }
        }
        return entities;
    }
}
