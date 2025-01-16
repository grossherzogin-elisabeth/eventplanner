package org.eventplanner.notifications.adapter.jpa;

import java.time.Instant;

import org.eventplanner.notifications.entities.QueuedEmail;
import org.eventplanner.notifications.values.NotificationType;
import org.eventplanner.users.values.UserKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "queued_emails")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class QueuedEmailJpaEntity {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "user_key", nullable = false)
    private String userKey;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "retries", nullable = false)
    private Integer retries;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    public QueuedEmail toDomain() {
        return new QueuedEmail(
            key,
            NotificationType.fromString(type).orElseThrow(),
            email,
            new UserKey(userKey),
            subject,
            body,
            retries,
            Instant.parse(createdAt)
        );
    }
}
