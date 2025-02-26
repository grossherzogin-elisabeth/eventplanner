package org.eventplanner.events.adapter.jpa.qualifications;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.QualificationKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "qualifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QualificationJpaEntity implements Serializable {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "icon", nullable = false)
    private String icon;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "expires", nullable = false)
    private boolean expires;

    @Column(name = "grants_position")
    private String grantsPositions;

    public static QualificationJpaEntity fromDomain(Qualification qualification) {
        return new QualificationJpaEntity(
            qualification.getKey().value(),
            qualification.getName(),
            qualification.getIcon(),
            qualification.getDescription(),
            qualification.isExpires(),
            String.join(", ", qualification.getGrantsPositions().stream().map(PositionKey::value).toList())
        );
    }

    public Qualification toDomain() {
        return new Qualification(
            new QualificationKey(key),
            name,
            icon,
            description,
            expires,
            grantsPositions != null
                ? Arrays.stream(grantsPositions.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(PositionKey::new)
                .toList()
                : Collections.emptyList()
        );
    }
}