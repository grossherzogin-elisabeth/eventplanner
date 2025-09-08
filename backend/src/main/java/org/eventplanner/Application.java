package org.eventplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableScheduling
@SpringBootApplication(exclude = JdbcRepositoriesAutoConfiguration.class)
public class Application {

    public static void main(@NonNull String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
