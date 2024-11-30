package org.eventplanner.notifications.entities;

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
public class QueuedEmail {
    private String key;
    private String email;
    private String subject;
    private String body;
    private int retries;
    private String createdAt;
}
