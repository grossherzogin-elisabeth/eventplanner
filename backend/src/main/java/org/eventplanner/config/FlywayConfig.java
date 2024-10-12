package org.eventplanner.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return Flyway.configure()
            .dataSource("jdbc:sqlite:data/eventplanner.db", null, null)
            .load();
    }
}
