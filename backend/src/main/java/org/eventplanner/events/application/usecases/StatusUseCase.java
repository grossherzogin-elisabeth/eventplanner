package org.eventplanner.events.application.usecases;

import java.time.Instant;

import org.eventplanner.events.domain.values.config.SystemStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StatusUseCase {

    private final String buildCommit;
    private final String buildBranch;
    private final Instant buildTime;

    @Autowired
    public StatusUseCase(
        @Nullable @Value("${build.commit}") String buildCommit,
        @Nullable @Value("${build.branch}") String buildBranch,
        @Nullable @Value("${build.time}") String buildTime
    ) {
        this.buildCommit = buildCommit;
        this.buildBranch = buildBranch;
        Instant t;
        try {
            t = Instant.parse(buildTime);
        } catch (Exception _) {
            t = Instant.now();
        }
        this.buildTime = t;
    }

    public @NonNull SystemStatus getSystemStatus() {
        var status = new SystemStatus();
        status.setBuildBranch(buildBranch);
        status.setBuildCommit(buildCommit);
        status.setBuildTime(buildTime);
        return status;
    }
}
