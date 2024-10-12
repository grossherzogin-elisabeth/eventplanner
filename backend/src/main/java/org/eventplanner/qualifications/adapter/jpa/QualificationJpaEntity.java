package org.eventplanner.qualifications.adapter.jpa;

import java.io.Serializable;

import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;

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

    public static QualificationJpaEntity fromDomain(Qualification qualification) {
        return new QualificationJpaEntity(
            qualification.getKey().value(),
            qualification.getName(),
            qualification.getIcon(),
            qualification.getDescription(),
            qualification.isExpires()
        );
    }

    public Qualification toDomain() {
        return new Qualification(new QualificationKey(key), name, icon, description, expires);
    }
}