package net.spexity.testutils

import io.restassured.RestAssured
import net.spexity.data.model.public_.Tables
import org.jooq.DSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import javax.sql.DataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.flywaydb.core.Flyway

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    protected lateinit var dslContext: DSLContext

    @Autowired
    protected lateinit var dataSource: DataSource

    @BeforeEach
    fun setupBase() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        cleanDatabase()
    }

    protected fun cleanDatabase() {
        dslContext.query(
            "TRUNCATE TABLE " +
                    "${Tables.POST_COMMENT_REVISION.qualifiedName}," +
                    "${Tables.POST_COMMENT.qualifiedName}," +
                    "${Tables.POST.qualifiedName}," +
                    "${Tables.CONTRIBUTOR_COMMUNITY.qualifiedName}," +
                    "${Tables.COMMUNITY.qualifiedName}," +
                    "${Tables.CONTRIBUTOR.qualifiedName}," +
                    "${Tables.USER_ACCOUNT.qualifiedName}," +
                    "${Tables.CONTRIBUTOR_ALIAS_META.qualifiedName} RESTART IDENTITY CASCADE"
        ).execute()
    }

    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val postgres = PostgreSQLContainer("postgres:18")
    }
}
