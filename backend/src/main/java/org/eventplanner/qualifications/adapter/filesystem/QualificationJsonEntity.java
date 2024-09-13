package org.eventplanner.qualifications.adapter.filesystem;

import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record QualificationJsonEntity(
    @NonNull String key,
    @NonNull String name,
    @NonNull String icon,
    @NonNull String description,
    boolean expires

) implements Serializable {
    public Qualification toDomain() {
        return new Qualification(new QualificationKey(key), name, icon, description, expires);
    }
}
