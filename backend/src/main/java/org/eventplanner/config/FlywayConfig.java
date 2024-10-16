package org.eventplanner.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    private final String databaseUrl;

    public FlywayConfig(@Value("${data.db-url}") String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
            .dataSource(databaseUrl, null, null)
            .load();
    }
}
