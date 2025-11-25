package net.spexity.community

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.CONTRIBUTOR_COMMUNITY
import net.spexity.testutils.IntegrationTestBase
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.verifyUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class CommunityResourceTest : IntegrationTestBase() {

    @Test
    fun `verified user can create community`() {
        registerCurrentUser("communityUser", "community@example.com")
        verifyUser(dslContext, "communityUser")

        val rawName = "  My New Community ${UUID.randomUUID()}  "
        val expectedName = rawName.trim().replace("\\s+".toRegex(), " ")

        val communityId = UUID.fromString(
            given()
                .header("Authorization", "Bearer communityUser")
                .header("X-Auth-Email", "community@example.com")
                .contentType(ContentType.JSON)
                .body(
                    mapOf(
                        "name" to rawName,
                        "acceptTermsAndConditions" to true
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

        val contributorId = record.createdByContributorId
        val membershipCount = dslContext.selectCount()
            .from(CONTRIBUTOR_COMMUNITY)
            .where(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
            .and(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID.eq(communityId))
            .fetchOne(0, Int::class.java)

        assertEquals(1, membershipCount)
    }

    @Test
    fun `unverified user cannot create community`() {
        registerCurrentUser("unverifiedCommunityUser", "unverified@example.com")

        val communityName = "Blocked Community ${UUID.randomUUID()}"

        given()
            .header("Authorization", "Bearer unverifiedCommunityUser")
            .header("X-Auth-Email", "unverified@example.com")
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "name" to communityName,
                    "acceptTermsAndConditions" to true
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
