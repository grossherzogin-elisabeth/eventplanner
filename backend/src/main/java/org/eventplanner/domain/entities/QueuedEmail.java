package org.eventplanner.domain.entities;

import java.time.Instant;
import java.util.UUID;

import org.eventplanner.domain.values.NotificationType;
import org.eventplanner.domain.values.UserKey;
import org.springframework.lang.NonNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class QueuedEmail {
    private final @NonNull String key;
    private final @NonNull NotificationType type;
    private final @NonNull String to;
    private final @NonNull UserKey userKey;
    private final @NonNull String subject;
    private final @NonNull String body;
    private int retries;
    private @NonNull Instant createdAt;

    public QueuedEmail(
        @NonNull NotificationType type,
        @NonNull String to,
        @NonNull UserKey userKey,
        @NonNull String subject,
        @NonNull String body
    ) {
        this.key = UUID.randomUUID().toString();
        this.type = type;
        this.to = to;
        this.userKey = userKey;
        this.subject = subject;
        this.body = body;
        this.createdAt = Instant.now();
        this.retries = 0;
    }
}
