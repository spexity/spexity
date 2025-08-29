package net.spexity

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.PostgreSQLContainer

class KPostgresContainer : PostgreSQLContainer<KPostgresContainer>("postgres:17-alpine")

class DatabaseTestContainersManager : QuarkusTestResourceLifecycleManager {

    companion object {
        private val pg = KPostgresContainer()
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
    }


    override fun start(): Map<String, String> {
        pg.start()
        return mapOf(
            "quarkus.datasource.jdbc.url" to pg.jdbcUrl,
            "quarkus.datasource.username" to pg.username,
            "quarkus.datasource.password" to pg.password,
        )
    }

    override fun stop() {
        pg.stop()
    }
}