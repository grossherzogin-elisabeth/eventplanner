package org.eventplanner.importer.rest.dto;

import java.io.Serializable;

import org.eventplanner.importer.entities.ImportError;
import org.springframework.lang.NonNull;

public record ImportErrorRepresentation(
    @NonNull String eventKey,
    @NonNull String eventName,
    @NonNull String start,
    @NonNull String end,
    @NonNull String message
) implements Serializable {
    public static ImportErrorRepresentation fromDomain(ImportError domain) {
        return new ImportErrorRepresentation(
            domain.eventKey().value(),
            domain.eventName(),
            domain.start().toString(),
            domain.end().toString(),
            domain.message()
        );
    }
}
