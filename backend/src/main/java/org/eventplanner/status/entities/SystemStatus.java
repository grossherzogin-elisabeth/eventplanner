package org.eventplanner.status.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.Instant;

@Getter
@Setter
public class SystemStatus {
    private @NonNull String buildCommit = "";
    private @NonNull String buildBranch = "";
    private @NonNull Instant buildTime = Instant.now();
}
