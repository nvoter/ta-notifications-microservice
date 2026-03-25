package org.fcs.notifications.microservice.schematools;

import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class SchemaCodegenMain {
    private static final String POSTGRES_IMAGE = "postgres:16";
    private static final String GENERATED_PACKAGE = "org.fcs.notifications.microservice.jooq";

    private SchemaCodegenMain() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Expected arguments: <jooq-output-dir> <migration-dir>");
        }

        Path outputDir = Path.of(args[0]).toAbsolutePath().normalize();
        String migrationLocation = "filesystem:" + Path.of(args[1]).toAbsolutePath().normalize();
        recreateDirectory(outputDir);

        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName("notifications_codegen")
                .withUsername("notifications")
                .withPassword("notifications")) {
            postgres.start();

            Flyway.configure()
                    .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations(migrationLocation)
                    .load()
                    .migrate();

            GenerationTool.generate(new Configuration()
                    .withJdbc(new Jdbc()
                            .withDriver("org.postgresql.Driver")
                            .withUrl(postgres.getJdbcUrl())
                            .withUser(postgres.getUsername())
                            .withPassword(postgres.getPassword()))
                    .withGenerator(new Generator()
                            .withDatabase(new Database()
                                    .withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public")
                                    .withIncludes(".*")
                                    .withExcludes("flyway_schema_history"))
                            .withTarget(new Target()
                                    .withPackageName(GENERATED_PACKAGE)
                                    .withDirectory(outputDir.toString()))));
        }
    }

    private static void recreateDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (var walk = Files.walk(directory)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ex) {
                                throw new RuntimeException("Failed to clean generated jOOQ directory: " + path, ex);
                            }
                        });
            }
        }

        Files.createDirectories(directory);
    }
}
