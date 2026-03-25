package org.fcs.notifications.microservice.schematools;

import org.flywaydb.core.Flyway;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Environment;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Path;
import java.util.Map;

public final class SchemaValidationMain {
    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String[] ENTITY_CLASSES = {
            "org.fcs.notifications.microservice.entities.Notification"
    };

    private SchemaValidationMain() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected argument: <migration-dir>");
        }

        String migrationLocation = "filesystem:" + Path.of(args[0]).toAbsolutePath().normalize();

        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("notifications_validation")
                .withUsername("notifications")
                .withPassword("notifications")) {
            postgres.start();

            Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(migrationLocation)
                    .load()
                    .migrate();

            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .applySettings(Map.of(
                            Environment.DRIVER, "org.postgresql.Driver",
                            Environment.URL, postgres.getJdbcUrl(),
                            Environment.USER, postgres.getUsername(),
                            Environment.PASS, postgres.getPassword(),
                            Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect",
                            Environment.HBM2DDL_AUTO, "validate",
                            AvailableSettings.SHOW_SQL, "false"
                    ))
                    .build();

            try {
                MetadataSources metadataSources = new MetadataSources(registry);
                for (String entityClassName : ENTITY_CLASSES) {
                    metadataSources.addAnnotatedClass(Class.forName(entityClassName));
                }

                try (SessionFactory ignored = metadataSources.buildMetadata().buildSessionFactory()) {
                }
            } finally {
                StandardServiceRegistryBuilder.destroy(registry);
            }
        }
    }
}
