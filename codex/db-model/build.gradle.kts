import org.flywaydb.core.Flyway
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import org.testcontainers.containers.PostgreSQLContainer

plugins {
    kotlin("jvm")
    id("io.spring.dependency-management")
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jooq:jooq-codegen:3.19.28")
        classpath("org.postgresql:postgresql:42.7.8")
        classpath("org.flywaydb:flyway-core:11.14.1")
        classpath("org.flywaydb:flyway-database-postgresql:11.14.1")
        classpath("org.testcontainers:postgresql:1.21.3")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}

dependencies {
    implementation("org.jooq:jooq")
    implementation("org.postgresql:postgresql")
}

val jooqOutputDir = layout.buildDirectory.dir("generated/jooq")
val migrationsDir = file("src/main/resources/db/migration")

tasks.register("generateJooq") {
    group = "jooq"

    inputs.dir(migrationsDir)
    outputs.dir(jooqOutputDir)

    doLast {
        System.setProperty("testcontainers.ryuk.disabled", "true")
        val db = PostgreSQLContainer("postgres:18")
            .withUsername("test")
            .withPassword("test")
            .withDatabaseName("test")
        db.start()
        try {
            Flyway.configure()
                .dataSource(db.jdbcUrl, db.username, db.password)
                .locations("filesystem:${migrationsDir.absolutePath}")
                .load()
                .migrate()

            val configuration = Configuration()
                .withJdbc(
                    Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl(db.jdbcUrl)
                        .withUser(db.username)
                        .withPassword(db.password)
                )
                .withGenerator(
                    Generator()
                        .withDatabase(
                            Database()
                                .withInputSchema("public")
                                .withExcludes("flyway_schema_history")
                        )
                        .withTarget(
                            Target()
                                .withPackageName("net.spexity.data.model")
                                .withDirectory(jooqOutputDir.get().asFile.absolutePath)
                        )
                )

            GenerationTool.generate(configuration)
        } finally {
            db.stop()
        }
    }
}

sourceSets["main"].java.srcDir(jooqOutputDir)
tasks.named("compileKotlin") {
    dependsOn("generateJooq")
}
tasks.named("compileJava") {
    dependsOn("generateJooq")
}
