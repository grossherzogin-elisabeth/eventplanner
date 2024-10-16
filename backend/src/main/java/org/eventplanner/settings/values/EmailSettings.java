package org.eventplanner.settings.values;

import org.eventplanner.common.EncryptedString;
import org.springframework.lang.Nullable;

public record EmailSettings(
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
}
