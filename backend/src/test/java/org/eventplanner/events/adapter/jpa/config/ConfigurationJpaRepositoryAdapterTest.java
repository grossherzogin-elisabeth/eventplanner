package org.eventplanner.events.adapter.jpa.config;

import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eventplanner.common.Encrypted;
import org.eventplanner.events.domain.aggregates.ApplicationConfig;
import org.eventplanner.events.domain.functions.EncryptFunc;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.eventplanner.events.domain.values.config.FrontendConfig;
import org.eventplanner.events.domain.values.config.NotificationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConfigurationJpaRepositoryAdapterTest {

    private ConfigurationJpaRepository repository;
    private ConfigurationJpaRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(ConfigurationJpaRepository.class);
        adapter = new ConfigurationJpaRepositoryAdapter(repository);
        when(repository.saveAll(anyIterable())).thenAnswer(invocation -> {
            Iterable<ConfigurationJpaEntity> arg = invocation.getArgument(0);
            return StreamSupport.stream(arg.spliterator(), false).toList();
        });
    }

    private static EncryptFunc encrypting() {
        return new EncryptFunc() {
            @Override
            public <T extends java.io.Serializable> Encrypted<T> apply(T plain) {
                if (plain == null) return null;
                return Encrypted.fromString("ENC(" + plain + ")");
            }
        };
    }

    private static EncryptFunc encryptingNull() {
        return new EncryptFunc() {
            @Override
            public <T extends java.io.Serializable> Encrypted<T> apply(T plain) {
                return null;
            }
        };
    }

    // ---------- Frontend (mapFrontendSettings) ----------

    private record FrontendCase(String name, FrontendConfig.UpdateSpec spec, Map<String, String> expectedMap) {}

    static Stream<FrontendCase> frontendCases() {
        return Stream.of(
            new FrontendCase(
                "all fields",
                new FrontendConfig.UpdateSpec("Menu", "Tab", "tech@x", "support@x"),
                Map.of(
                    "ui.menuTitle", "Menu",
                    "ui.tabTitle", "Tab",
                    "ui.technicalSupportEmail", "tech@x",
                    "ui.supportEmail", "support@x"
                )
            ),
            new FrontendCase(
                "partial - tab only",
                new FrontendConfig.UpdateSpec(null, "TabOnly", null, null),
                Map.of("ui.tabTitle", "TabOnly")
            )
        );
    }

    @ParameterizedTest(name = "Frontend: {0}")
    @MethodSource("frontendCases")
    void updateConfig_shouldPersistFrontend(FrontendCase c) {
        var notifications = new NotificationConfig.UpdateSpec(null);
        var email = new EmailConfig.UpdateSpec(null, null, null, null, null, null, null, null, null, null);
        var spec = new ApplicationConfig.UpdateSpec(notifications, email, c.spec);

        adapter.updateConfig(spec, encrypting());

        verify(repository).saveAll(argThat(iterable -> {
            var saved = StreamSupport.stream(iterable.spliterator(), false).toList();
            return saved.size() == c.expectedMap.size() && toMap(saved).equals(c.expectedMap);
        }));
        verifyNoMoreInteractions(repository);
    }

    // ---------- Notifications (mapNotificationSettings) ----------

    private record NotificationCase(String name, String webhook, EncryptFunc encrypt, int expectedCount, String expectedKey, String expectedValue) {}

    static Stream<NotificationCase> notificationCases() {
        return Stream.of(
            new NotificationCase("webhook null -> none", null, encrypting(), 0, null, null),
            new NotificationCase("webhook blank -> null value", "   ", encrypting(), 1, "notifications.teamsWebhookUrl", null),
            new NotificationCase("webhook present -> encrypted", "https://webhook", encrypting(), 1, "notifications.teamsWebhookUrl", "ENC(https://webhook)"),
            new NotificationCase("webhook present but encryption returns null -> none", "https://webhook", encryptingNull(), 0, null, null)
        );
    }

    @ParameterizedTest(name = "Notifications: {0}")
    @MethodSource("notificationCases")
    void updateConfig_shouldPersistNotifications(NotificationCase c) {
        var notifications = new NotificationConfig.UpdateSpec(c.webhook);
        var email = new EmailConfig.UpdateSpec(null, null, null, null, null, null, null, null, null, null);
        var ui = new FrontendConfig.UpdateSpec(null, null, null, null);
        var spec = new ApplicationConfig.UpdateSpec(notifications, email, ui);

        adapter.updateConfig(spec, c.encrypt);

        verify(repository).saveAll(argThat(iterable -> {
            var saved = StreamSupport.stream(iterable.spliterator(), false).toList();
            if (saved.size() != c.expectedCount) return false;
            if (c.expectedCount == 1) {
                var e = saved.getFirst();
                if (!e.getKey().equals(c.expectedKey)) return false;
                if (c.expectedValue == null) return e.getValue() == null;
                return c.expectedValue.equals(e.getValue());
            }
            return true;
        }));
        verifyNoMoreInteractions(repository);
    }

    // ---------- Email (mapEmailSettings) ----------

    private record EmailCase(
        String name,
        EmailConfig.UpdateSpec emailSpec,
        EncryptFunc encrypt,
        Integer expectedCount,
        String expectedKey,
        String expectedValue,
        Map<String, String> expectedMap
    ) {}

    static Stream<EmailCase> emailCases() {
        return Stream.of(
            new EmailCase(
                "all fields",
                new EmailConfig.UpdateSpec(
                    "from@x", "From", "reply@x", "Reply", "smtp.host", 587, true, false, "user", "secret"
                ),
                encrypting(),
                null,
                null,
                null,
                Map.of(
                    "email.from", "from@x",
                    "email.fromDisplayName", "From",
                    "email.replyTo", "reply@x",
                    "email.replyToDisplayName", "Reply",
                    "email.host", "smtp.host",
                    "email.port", "587",
                    "email.enableSSL", "true",
                    "email.enableStartTLS", "false",
                    "email.username", "user",
                    "email.password", "ENC(secret)"
                )
            ),
            new EmailCase(
                "password blank -> store null value",
                new EmailConfig.UpdateSpec(null, null, null, null, null, null, null, null, null, "   "),
                encrypting(),
                1,
                "email.password",
                null,
                null
            ),
            new EmailCase(
                "password null -> no entry",
                new EmailConfig.UpdateSpec(null, null, null, null, null, null, null, null, null, null),
                encrypting(),
                0,
                null,
                null,
                null
            ),
            new EmailCase(
                "password present but encryption returns null -> no entry",
                new EmailConfig.UpdateSpec(null, null, null, null, null, null, null, null, null, "secret"),
                encryptingNull(),
                0,
                null,
                null,
                null
            )
        );
    }

    @ParameterizedTest(name = "Email: {0}")
    @MethodSource("emailCases")
    void updateConfig_shouldPersistEmail(EmailCase c) {
        var notifications = new NotificationConfig.UpdateSpec(null);
        var ui = new FrontendConfig.UpdateSpec(null, null, null, null);
        var spec = new ApplicationConfig.UpdateSpec(notifications, c.emailSpec, ui);

        adapter.updateConfig(spec, c.encrypt);

        verify(repository).saveAll(argThat(iterable -> {
            var saved = StreamSupport.stream(iterable.spliterator(), false).toList();
            if (c.expectedMap != null) {
                return saved.size() == c.expectedMap.size() && toMap(saved).equals(c.expectedMap);
            } else {
                if (saved.size() != c.expectedCount) return false;
                if (c.expectedCount == 1) {
                    var e = saved.getFirst();
                    if (!e.getKey().equals(c.expectedKey)) return false;
                    if (c.expectedValue == null) return e.getValue() == null;
                    return c.expectedValue.equals(e.getValue());
                }
                return true;
            }
        }));
        verifyNoMoreInteractions(repository);
    }

    // Utility
    private static Map<String, String> toMap(List<ConfigurationJpaEntity> entities) {
        var map = new HashMap<String, String>();
        for (var e : entities) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }
}
