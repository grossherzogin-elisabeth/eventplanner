package org.eventplanner.importer.service;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.eventplanner.common.FileSystemJsonRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.positions.values.PositionKey;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class PositionJsonImporter {

    public static @NonNull List<Position> readFromDirectory(
            @NonNull File dir
    ) {
        var repository = new FileSystemJsonRepository<>(PositionJsonEntity.class, dir);
        return repository.findAll().stream().map(PositionJsonEntity::toDomain).toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PositionJsonEntity(
            @NonNull String key,
            @NonNull String name,
            @NonNull String color,
            int prio,
            @NonNull String imoListRank
    ) implements Serializable {
        public Position toDomain() {
            return new Position(new PositionKey(key), name, color, prio, imoListRank);
        }
    }
}

