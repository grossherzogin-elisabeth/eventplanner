package org.eventplanner.status.rest.dto;

import org.eventplanner.status.entities.SystemStatus;
import org.springframework.lang.NonNull;

import java.io.Serializable;

public record StatusRepresentation(
    @NonNull String buildCommit,
    @NonNull String buildBranch,
    @NonNull String buildDate
) implements Serializable {
    
    public static StatusRepresentation fromDomain(@NonNull SystemStatus status) {
        return new StatusRepresentation(
            status.getBuildCommit(),
            status.getBuildBranch(),
            status.getBuildTime().toString());
    }
}
