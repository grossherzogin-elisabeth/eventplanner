package org.eventplanner.importer.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eventplanner.common.FileSystemJsonRepository;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import static org.eventplanner.common.ObjectUtils.streamNullable;

public class QualificationJsonImporter {

    public static @NonNull List<Qualification> readFromDirectory(
            @NonNull File dir
    ) {
        var repository = new FileSystemJsonRepository<>(QualificationJsonEntity.class, dir);
        return repository.findAll().stream().map(QualificationJsonEntity::toDomain).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record QualificationJsonEntity(
            @NonNull String key,
            @NonNull String name,
            @Nullable String icon,
            @Nullable String description,
            boolean expires,
            @Nullable List<String> grantsPositions
    ) implements Serializable {
        public Qualification toDomain() {
            return new Qualification(
                new QualificationKey(key),
                name,
                icon,
                description,
                expires,
                streamNullable(grantsPositions, Stream.empty()).map(PositionKey::new).toList()
            );
        }
    }
}
