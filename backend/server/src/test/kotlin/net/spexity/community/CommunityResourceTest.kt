package net.spexity.community

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.verifyUser
import java.util.UUID

@QuarkusTest
class CommunityResourceTest {

    @Inject
    lateinit var dslContext: DSLContext

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "communityUser",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "communityUser"),
            SecurityAttribute(key = "email", value = "community@example.com")
        ]
    )
    fun `verified user can create community`() {
        registerCurrentUser("CommunityTester")
        verifyUser(dslContext, "communityUser")

        val rawName = "  My New Community ${UUID.randomUUID()}  "
        val expectedName = rawName.trim().replace("\\s+".toRegex(), " ")

        val communityId = UUID.fromString(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "name" to rawName,
                        "conformToTermsAndConditions" to true
                    )
                )
                .post("/api/communities")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("id")
        )

        val record = dslContext.selectFrom(COMMUNITY)
            .where(COMMUNITY.ID.eq(communityId))
            .fetchOne()

        assertNotNull(record)
        assertEquals(expectedName, record!!.name)
        assertEquals(0, record.postsCount)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "unverifiedCommunityUser",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "unverifiedCommunityUser"),
            SecurityAttribute(key = "email", value = "unverified@example.com")
        ]
    )
    fun `unverified user cannot create community`() {
        registerCurrentUser("UnverifiedAlias")

        val communityName = "Blocked Community ${UUID.randomUUID()}"

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "name" to communityName,
                    "conformToTermsAndConditions" to true
                )
            )
            .post("/api/communities")
            .then()
            .statusCode(403)

        val created = dslContext.selectCount()
            .from(COMMUNITY)
            .where(COMMUNITY.NAME.eq(communityName))
            .fetchOne(0, Int::class.java)

        assertEquals(0, created)
    }
}
