package org.eventplanner.importer.service;

import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.utils.FileSystemJsonRepository;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class PositionJsonImporter {

    public static @NonNull List<Position> readFromDirectory(
            @NonNull File dir
    ) {
        var repository = new FileSystemJsonRepository<>(PositionJsonEntity.class, dir);
        return repository.findAll().stream().map(PositionJsonEntity::toDomain).toList();
    }

    private record PositionJsonEntity(
            @NonNull String key,
            @NonNull String name,
            @NonNull String color,
            int prio
    ) implements Serializable {
        public Position toDomain() {
            return new Position(new PositionKey(key), name, color, prio);
        }
    }
}

