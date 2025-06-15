package org.eventplanner.events.domain.values.config;

import java.time.Instant;

import org.springframework.lang.NonNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemStatus {
    private @NonNull String buildCommit = "";
    private @NonNull String buildBranch = "";
    private @NonNull Instant buildTime = Instant.now();
}
