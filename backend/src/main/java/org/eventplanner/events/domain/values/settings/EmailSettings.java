package org.eventplanner.events.domain.values.settings;

import java.util.List;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public record EmailSettings(
    @Nullable Boolean enabled,
    @Nullable List<String> recipientsWhitelist,
    @Nullable String titlePrefix,
    @Nullable String from,
    @Nullable String fromDisplayName,
    @Nullable String replyTo,
    @Nullable String replyToDisplayName,
    @Nullable String host,
    @Nullable Integer port,
    @Nullable Boolean enableSSL,
    @Nullable Boolean enableStartTls,
    @Nullable String username,
    @Nullable String password
) {
    public EmailSettings() {
        this(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    public @NonNull EmailSettings apply(@NonNull final EmailSettings other) {
        return new EmailSettings(
            other.enabled != null ? other.enabled : enabled,
            other.recipientsWhitelist != null ? other.recipientsWhitelist : recipientsWhitelist,
            other.titlePrefix != null ? other.titlePrefix : titlePrefix,
            other.from != null ? other.from : from,
            other.fromDisplayName != null ? other.fromDisplayName : fromDisplayName,
            other.replyTo != null ? other.replyTo : replyTo,
            other.replyToDisplayName != null ? other.replyToDisplayName : replyToDisplayName,
            other.host != null ? other.host : host,
            other.port != null ? other.port : port,
            other.enableSSL != null ? other.enableSSL : enableSSL,
            other.enableStartTls != null ? other.enableStartTls : enableStartTls,
            other.username != null ? other.username : username,
            other.password != null ? other.password : password
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class UpdateRequest {
        private @Nullable String from;
        private @Nullable String fromDisplayName;
        private @Nullable String replyTo;
        private @Nullable String replyToDisplayName;
        private @Nullable String host;
        private @Nullable Integer port;
        private @Nullable Boolean enableSSL;
        private @Nullable Boolean enableStartTls;
        private @Nullable String username;
        private @Nullable String password;

        public @NonNull UpdateRequest clearUnchanged(@NonNull final EmailSettings current) {
            if (Objects.equals(from, current.from)) {
                from = null;
            }
            if (Objects.equals(fromDisplayName, current.fromDisplayName)) {
                fromDisplayName = null;
            }
            if (Objects.equals(replyTo, current.replyTo)) {
                replyTo = null;
            }
            if (Objects.equals(replyToDisplayName, current.replyToDisplayName)) {
                replyToDisplayName = null;
            }
            if (Objects.equals(host, current.host)) {
                host = null;
            }
            if (Objects.equals(port, current.port)) {
                port = null;
            }
            if (Objects.equals(enableSSL, current.enableSSL)) {
                enableSSL = null;
            }
            if (Objects.equals(enableStartTls, current.enableStartTls)) {
                enableStartTls = null;
            }
            if (Objects.equals(username, current.username)) {
                username = null;
            }
            if (Objects.equals(password, current.password)) {
                password = null;
            }
            return this;
        }
    }
}
