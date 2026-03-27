plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.10"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.fcs"
version = "0.0.1-SNAPSHOT"
description = "notifications-microservice"

val jooqVersion = "3.19.18"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

repositories {
    mavenCentral()
}

val generatedJooqDir = layout.buildDirectory.dir("generated-src/jooq/main")
val schemaToolsDir = layout.buildDirectory.dir("classes/schema-tools")
val migrationDir = layout.projectDirectory.dir("src/main/resources/db/migration")
val schemaTools by configurations.creating
val skipSchemaTasks = providers.gradleProperty("skipSchemaTasks")
    .map(String::toBoolean)
    .orElse(false)

sourceSets {
    named("main") {
        java.srcDir(generatedJooqDir)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.41")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus:1.16.2")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql:42.7.7")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    schemaTools("org.flywaydb:flyway-core")
    schemaTools("org.flywaydb:flyway-database-postgresql")
    schemaTools("org.postgresql:postgresql:42.7.7")
    schemaTools("org.testcontainers:postgresql:1.20.4")
    schemaTools("org.jooq:jooq-codegen:$jooqVersion")
    schemaTools("org.jooq:jooq-meta:$jooqVersion")
    schemaTools("org.jooq:jooq:$jooqVersion")
    schemaTools("org.hibernate.orm:hibernate-core:6.5.2.Final")
    schemaTools("jakarta.persistence:jakarta.persistence-api:3.1.0")
}

val compileSchemaToolsJava by tasks.registering(JavaCompile::class) {
    group = "build setup"
    description = "Compiles local build-time schema tool classes."
    source = fileTree("gradle/schema-tools") {
        include("**/*.java")
    }
    classpath = schemaTools
    destinationDirectory.set(schemaToolsDir)
    options.release.set(21)
}

val generateJooqFromSchema by tasks.registering(JavaExec::class) {
    group = "build"
    description = "Applies Flyway migrations to a temporary PostgreSQL instance and generates jOOQ sources from the resulting schema"
    dependsOn(compileSchemaToolsJava)
    classpath = files(schemaToolsDir) + schemaTools
    mainClass.set("org.fcs.notifications.microservice.schematools.SchemaCodegenMain")
    inputs.files(fileTree("src/main/resources/db/migration"))
    outputs.dir(generatedJooqDir)
    args(
        generatedJooqDir.get().asFile.absolutePath,
        migrationDir.asFile.absolutePath
    )
    onlyIf { !skipSchemaTasks.get() }
}

val validateJpaMappingsAgainstSchema by tasks.registering(JavaExec::class) {
    group = "verification"
    description = "Validates JPA mappings against a schema created from Flyway migrations on a temporary PostgreSQL instance"
    dependsOn(tasks.named("classes"), compileSchemaToolsJava)
    classpath = files(schemaToolsDir) + schemaTools + sourceSets["main"].runtimeClasspath
    mainClass.set("org.fcs.notifications.microservice.schematools.SchemaValidationMain")
    inputs.files(
        fileTree("src/main/resources/db/migration"),
        fileTree("src/main/java/org/fcs/notifications/microservice/entities")
    )
    args(migrationDir.asFile.absolutePath)
    onlyIf { !skipSchemaTasks.get() }
}

tasks.named("compileJava") {
    if (!skipSchemaTasks.get()) {
        dependsOn(generateJooqFromSchema)
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)

    violationRules {
        rule {
            element = "BUNDLE"

            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "1.0".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    if (!skipSchemaTasks.get()) {
        dependsOn(validateJpaMappingsAgainstSchema)
    }
}

val jacocoExcludes = listOf(
    "**/dtos/**",
    "**/entities/**",
    "**/*Application*",
    "**/exceptions/**",
    "**/repositories/**",
    "**/models/**",
    "**/config/**",
    "**/events/**",
    "**/clients/**",
    "**/messaging/**",
    "**/jooq/**"
)

tasks.withType<JacocoReport>().configureEach {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude(jacocoExcludes) }
        })
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}
