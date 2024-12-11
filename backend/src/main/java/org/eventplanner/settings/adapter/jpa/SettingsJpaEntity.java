package org.eventplanner.settings.adapter.jpa;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettingsJpaEntity {
    @Id
    @Column(name = "key", nullable = false)
    private @NonNull String key;

    @Column(name = "value")
    private @Nullable String value;
}
