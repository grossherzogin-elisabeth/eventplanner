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
import org.springframework.lang.Nullable;
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
                                                    .filter(configurationJpaEntity -> configurationJpaEntity.getValue()
                                                            != null)
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
        } catch (NumberFormatException _) {
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
        addIfNotNull(entities, UI_MENU_TITLE, spec.getMenuTitle());
        addIfNotNull(entities, UI_TAB_TITLE, spec.getTabTitle());
        addIfNotNull(entities, UI_TEC_SUPPORT_EMAIL, spec.getTechnicalSupportEmail());
        addIfNotNull(entities, UI_SUPPORT_EMAIL, spec.getSupportEmail());
        return entities;
    }

    private @NonNull List<ConfigurationJpaEntity> mapNotificationSettings(
            @NonNull final NotificationConfig.UpdateSpec spec,
            @NonNull final EncryptFunc encryptFunc
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        addEncryptedNullable(entities, TEAMS_WEBHOOK_URL, spec.getTeamsWebhookUrl(), encryptFunc);
        return entities;
    }

    private @NonNull List<ConfigurationJpaEntity> mapEmailSettings(
            @NonNull final EmailConfig.UpdateSpec spec,
            @NonNull final EncryptFunc encryptFunc
    ) {
        var entities = new ArrayList<ConfigurationJpaEntity>();
        addIfNotNull(entities, EMAIL_FROM, spec.getFrom());
        addIfNotNull(entities, EMAIL_FROM_DISPLAY_NAME, spec.getFromDisplayName());
        addIfNotNull(entities, EMAIL_REPLY_TO, spec.getReplyTo());
        addIfNotNull(entities, EMAIL_REPLY_TO_DISPLAY_NAME, spec.getReplyToDisplayName());
        addIfNotNull(entities, EMAIL_HOST, spec.getHost());
        addIfNotNull(entities, EMAIL_PORT, spec.getPort());
        addIfNotNull(entities, EMAIL_ENABLE_SSL, spec.getEnableSSL());
        addIfNotNull(entities, EMAIL_ENABLE_TLS, spec.getEnableStartTls());
        addIfNotNull(entities, EMAIL_USERNAME, spec.getUsername());
        addEncryptedNullable(entities, EMAIL_PASSWORD, spec.getPassword(), encryptFunc);
        return entities;
    }

    private void addIfNotNull(
            @NonNull final List<ConfigurationJpaEntity> entities,
            @NonNull final String key,
            @Nullable final String value
    ) {
        if (value != null) {
            entities.add(new ConfigurationJpaEntity(key, value));
        }
    }

    private void addIfNotNull(
            @NonNull final List<ConfigurationJpaEntity> entities,
            @NonNull final String key,
            @Nullable final Integer value
    ) {
        if (value != null) {
            entities.add(new ConfigurationJpaEntity(key, String.valueOf(value)));
        }
    }

    private void addIfNotNull(
            @NonNull final List<ConfigurationJpaEntity> entities,
            @NonNull final String key,
            @Nullable final Boolean value
    ) {
        if (value != null) {
            entities.add(new ConfigurationJpaEntity(key, String.valueOf(value)));
        }
    }

    private void addEncryptedNullable(
            @NonNull final List<ConfigurationJpaEntity> entities,
            @NonNull final String key,
            @Nullable final String rawValue,
            @NonNull final EncryptFunc encryptFunc
    ) {
        if (rawValue == null) {
            return;
        }
        if (rawValue.isBlank()) {
            entities.add(new ConfigurationJpaEntity(key, null));
            return;
        }
        var encrypted = encryptFunc.apply(rawValue);
        if (encrypted != null) {
            entities.add(new ConfigurationJpaEntity(key, encrypted.value()));
        }
    }
}
