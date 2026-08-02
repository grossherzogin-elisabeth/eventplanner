package org.eventplanner.events.adapter.jpa.accesskeys;

import org.jspecify.annotations.NonNull;

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
@Table(name = "access_keys")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AccessKeyJpaEntity {

    @Id
    @Column(name = "access_key", nullable = false, updatable = false)
    private @NonNull String key;

    @Column(name = "user_key", nullable = false, updatable = false)
    private @NonNull String userKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private @NonNull String createdAt;
}
