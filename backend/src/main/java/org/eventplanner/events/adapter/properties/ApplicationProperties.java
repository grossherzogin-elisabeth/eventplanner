package org.eventplanner.events.adapter.properties;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "")
@Setter
@Getter
public class ApplicationProperties {
    @JsonProperty("notifications")
    private Map<String, NotificationProperties> notifications;

    @Value("${auth.login-success-url}")
    private String authLoginSuccessUrl;

    @Value("${auth.logout-success-url}")
    private String authLogoutSuccessUrl;

    @Value("${auth.admins}")
    private String authAdmins;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${email.enabled}")
    private Boolean emailEnabled;

    @Value("${email.title-prefix}")
    private String emailTitlePrefix;

    @Value("${email.recipients-whitelist}")
    private String emailRecipientsWhitelist;

    public record NotificationProperties(
        @NonNull String name,
        boolean enabled
    ) {
    }
}
