package org.eventplanner.importer.service;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.common.FileSystemJsonRepository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import static org.eventplanner.common.ObjectUtils.mapNullable;

public class QualificationJsonImporter {

    public static @NonNull List<Qualification> readFromDirectory(
            @NonNull File dir
    ) {
        var repository = new FileSystemJsonRepository<>(QualificationJsonEntity.class, dir);
        return repository.findAll().stream().map(QualificationJsonEntity::toDomain).toList();
    }

    private record QualificationJsonEntity(
            @NonNull String key,
            @NonNull String name,
            @Nullable String icon,
            @Nullable String description,
            boolean expires,
            @Nullable String grantsPosition
    ) implements Serializable {
        public Qualification toDomain() {
            return new Qualification(
                new QualificationKey(key),
                name,
                icon,
                description,
                expires,
                mapNullable(grantsPosition, PositionKey::new)
            );
        }
    }
}
