package org.eventplanner.domain.values;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "email")
@Getter
public class DefaultEmailSettings {
    private final String from;
    private final String fromDisplayName;
    private final String replyTo;
    private final String replyToDisplayName;
    private final String host;
    private final Integer port;
    private final String protocol;
    private final String username;
    private final String password;
    private final Boolean enableStarttls;
    private final Boolean enableSsl;
    private final String footer;

    @ConstructorBinding
    public DefaultEmailSettings(
        String from,
        String fromDisplayName,
        String replyTo,
        String replyToDisplayName,
        String host,
        Integer port,
        String protocol,
        String username,
        String password,
        Boolean enableStarttls,
        Boolean enableSsl,
        String footer
    ) {
        this.from = from;
        this.fromDisplayName = fromDisplayName;
        this.replyTo = replyTo;
        this.replyToDisplayName = replyToDisplayName;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.enableStarttls = enableStarttls;
        this.enableSsl = enableSsl;
        this.footer = footer;
    }
}
