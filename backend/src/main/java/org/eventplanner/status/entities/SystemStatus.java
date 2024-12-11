package org.eventplanner.status.entities;

import java.time.Instant;

import org.springframework.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemStatus {
    private @NonNull String buildCommit = "";
    private @NonNull String buildBranch = "";
    private @NonNull Instant buildTime = Instant.now();
}
