package net.spexity.user

import io.restassured.RestAssured
import io.restassured.http.ContentType
import net.spexity.data.model.public_.Tables
import net.spexity.testutils.IntegrationTestBase
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.verifyUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class ContributorCommunityResourceTest : IntegrationTestBase() {

    @Test
    fun `authenticated user can join community`() {
        registerCurrentUser("memberUser", "member@example.com")
        verifyUser(dslContext, "memberUser")

        val contributorId = dslContext.select(Tables.CONTRIBUTOR.ID)
            .from(Tables.CONTRIBUTOR)
            .where(Tables.CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq("memberUser"))
            .fetchOne(0, UUID::class.java) ?: throw IllegalStateException("Contributor not found")

        val communityId = insertCommunity(dslContext, "TestCommunity", contributorId)

        RestAssured.given()
            .header("Authorization", "Bearer memberUser")
            .header("X-Auth-Email", "member@example.com")
            .contentType(ContentType.JSON)
            .post("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(201)

        val membership = dslContext.selectFrom(Tables.CONTRIBUTOR_COMMUNITY)
            .where(Tables.CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
            .and(Tables.CONTRIBUTOR_COMMUNITY.COMMUNITY_ID.eq(communityId))
            .fetchOne()

        Assertions.assertNotNull(membership)
    }

    @Test
    fun `unauthenticated user cannot join community`() {
        val communityId = UUID.randomUUID()

        RestAssured.given()
            .contentType(ContentType.JSON)
            .post("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(401)
    }

    @Test
    fun `authenticated user can leave community`() {
        registerCurrentUser("leaveUser", "leave@example.com")
        verifyUser(dslContext, "leaveUser")

        val contributorId = dslContext.select(Tables.CONTRIBUTOR.ID)
            .from(Tables.CONTRIBUTOR)
            .where(Tables.CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq("leaveUser"))
            .fetchOne(0, UUID::class.java) ?: throw IllegalStateException("Contributor not found")

        val communityId = insertCommunity(dslContext, "TestCommunity", contributorId)

        dslContext.insertInto(Tables.CONTRIBUTOR_COMMUNITY)
            .set(Tables.CONTRIBUTOR_COMMUNITY.ID, UUID.randomUUID())
            .set(Tables.CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID, contributorId)
            .set(Tables.CONTRIBUTOR_COMMUNITY.COMMUNITY_ID, communityId)
            .set(Tables.CONTRIBUTOR_COMMUNITY.CREATED_AT, OffsetDateTime.now())
            .execute()

        RestAssured.given()
            .header("Authorization", "Bearer leaveUser")
            .header("X-Auth-Email", "leave@example.com")
            .contentType(ContentType.JSON)
            .delete("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(204)

        val count = dslContext.selectCount()
            .from(Tables.CONTRIBUTOR_COMMUNITY)
            .where(Tables.CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
            .and(Tables.CONTRIBUTOR_COMMUNITY.COMMUNITY_ID.eq(communityId))
            .fetchOne(0, Int::class.java)

        Assertions.assertEquals(0, count)
    }

    @Test
    fun `unauthenticated user cannot leave community`() {
        val communityId = UUID.randomUUID()

        RestAssured.given()
            .contentType(ContentType.JSON)
            .delete("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(401)
    }
}
