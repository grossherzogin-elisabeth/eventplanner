package org.eventplanner.status;

import org.eventplanner.status.entities.SystemStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StatusUseCase {

    private final String buildCommit;
    private final String buildBranch;
    private final Instant buildTime;

    @Autowired
    public StatusUseCase(
        @Value("${build.commit}") String buildCommit,
        @Value("${build.branch}") String buildBranch,
        @Value("${build.time}") String buildTime
    ) {
        this.buildCommit = buildCommit;
        this.buildBranch = buildBranch;
        Instant t;
        try {
            t = Instant.parse(buildTime);
        } catch (Exception e) {
            t = Instant.now();
        }
        this.buildTime = t;
    }

    public SystemStatus getSystemStatus() {
        var status = new SystemStatus();
        status.setBuildBranch(buildBranch);
        status.setBuildCommit(buildCommit);
        status.setBuildTime(buildTime);
        return status;
    }
}
