package org.eventplanner.events.domain.values;

import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailSettings {
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
    private @Nullable String footer;
}
