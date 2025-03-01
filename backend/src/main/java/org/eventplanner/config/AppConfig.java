package org.eventplanner.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("org.eventplanner")
@EnableTransactionManagement
@EnableMethodSecurity
public class AppConfig {
}
