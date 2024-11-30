package org.eventplanner.notifications.adapter.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "queued_emails")
public class QueuedEmailJpaEntity {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "retries", nullable = false)
    private Integer retries;

    @Column(name = "created_at", nullable = false)
    private String createdAt;
}
