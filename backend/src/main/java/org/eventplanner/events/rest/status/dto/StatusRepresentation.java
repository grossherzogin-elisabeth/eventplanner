package org.eventplanner.events.rest.status.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.entities.SystemStatus;
import org.springframework.lang.NonNull;

public record StatusRepresentation(
    @NonNull String buildCommit,
    @NonNull String buildBranch,
    @NonNull String buildDate
) implements Serializable {

    public static StatusRepresentation fromDomain(@NonNull SystemStatus status) {
        return new StatusRepresentation(
            status.getBuildCommit(),
            status.getBuildBranch(),
            status.getBuildTime().toString()
        );
    }
}
