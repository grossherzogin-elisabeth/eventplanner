package org.eventplanner.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.eventplanner.Application;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManagerFactory;

class DatabaseStartupIntegrationTest {

    private static final String CURRENT_BASELINE_MIGRATION = "B21__baseline.sql";
    private static final List<String> TABLES = List.of(
        "events", "positions", "qualifications", "users",
        "settings", "queued_emails", "access_keys", "event_registrations"
    );

    @TempDir
    private Path directory;

    @Test
    void shouldStartWithSqliteDatabase() throws Exception {
        // verify the db file does not exist, to make sure the application creates it
        assertThat(directory.resolve("eventplanner.db")).doesNotExist();

        var env = new HashMap<String, Object>();
        env.put("MODE", "production");
        env.put("DATABASE", "sqlite");
        env.put("SERVER_PORT", "0");
        env.put("DATA_DIRECTORY", directory.toString());
        env.put("LOGGING_FILE_PATH", directory.resolve("logs").toString());
        env.put("OAUTH_ISSUER_URI", "http://localhost/unused-test-issuer");
        env.put("OAUTH_CLIENT_ID", "startup-test");
        env.put("OAUTH_CLIENT_SECRET", "startup-test");
        env.put("EMAIL_ENABLED", "false");

        try (var context = startApplication(env)) {
            assertThat(directory.resolve("eventplanner.db")).isRegularFile();
            assertFlyWayMigrationsHaveRun(context, CURRENT_BASELINE_MIGRATION);
            assertDatabaseNameEquals(context, "SQLite");
            assertAllTablesExist(context, TABLES);
            assertAllTablesAreReadable(context);
        }
    }

    @Test
    void shouldStartWithPostgresDatabase() throws Exception {
        try (var postgres = new PostgreSQLContainer("postgres:18")) {
            postgres.start();

            var env = new HashMap<String, Object>();
            env.put("MODE", "production");
            env.put("DATABASE", "postgres");
            env.put("DATABASE_URL", postgres.getJdbcUrl());
            env.put("DATABASE_USERNAME", postgres.getUsername());
            env.put("DATABASE_PASSWORD", postgres.getPassword());
            env.put("SERVER_PORT", "0");
            env.put("LOGGING_FILE_PATH", directory.resolve("logs").toString());
            env.put("OAUTH_ISSUER_URI", "http://localhost/unused-test-issuer");
            env.put("OAUTH_CLIENT_ID", "startup-test");
            env.put("OAUTH_CLIENT_SECRET", "startup-test");
            env.put("EMAIL_ENABLED", "false");

            try (var context = startApplication(env)) {
                assertFlyWayMigrationsHaveRun(context, CURRENT_BASELINE_MIGRATION);
                assertDatabaseNameEquals(context, "PostgreSQL");
                assertAllTablesExist(context, TABLES);
                assertAllTablesAreReadable(context);
            }
        }
    }

    private void assertFlyWayMigrationsHaveRun(ApplicationContext context, String baselineMigration) {
        var flyway = context.getBean(Flyway.class);
        assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
        assertThat(flyway.info().pending()).isEmpty();
        assertThat(flyway.info().current()).isNotNull();
        assertThat(flyway.info().applied())
            .extracting(MigrationInfo::getScript)
            .contains(baselineMigration);
    }

    private void assertDatabaseNameEquals(ApplicationContext context, String db) throws Exception {
        try (var connection = context.getBean(DataSource.class).getConnection();
            var statement = connection.createStatement()) {
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo(db);
        }
    }

    private void assertAllTablesExist(ApplicationContext context, List<String> tables) throws Exception {
        try (var connection = context.getBean(DataSource.class).getConnection();
            var statement = connection.createStatement()) {
            for (var table : tables) {
                try (var result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getLong(1)).as("%s should start empty", table).isZero();
                }
            }
        }
    }

    private void assertAllTablesAreReadable(ApplicationContext context) {
        try (var entityManager = context.getBean(EntityManagerFactory.class).createEntityManager()) {
            for (var entity : entityManager.getMetamodel().getEntities()) {
                assertThat(entityManager.createQuery("from " + entity.getName()).getResultList())
                    .as("Hibernate should read %s", entity.getName())
                    .isEmpty();
            }
        }
    }

    private ConfigurableApplicationContext startApplication(Map<String, Object> env) {
        var environment = new StandardEnvironment();
        environment.getPropertySources().replace(
            StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, env)
        );

        var contextBuilder = new SpringApplicationBuilder(Application.class)
            .environment(environment);
        var context = contextBuilder.run();
        assertThat(context.isActive()).isTrue();
        return context;
    }
}
