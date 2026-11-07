package org.eventplanner;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableScheduling
@SpringBootApplication
public class Application {

    public static void main(@NonNull String[] args) {
        try {
            SpringApplication.run(Application.class, args);
        } catch (Exception e) {
            // this is not a logger call, because the logger is already shut down at this point
            e.printStackTrace();
            throw e;
        }
    }
}
