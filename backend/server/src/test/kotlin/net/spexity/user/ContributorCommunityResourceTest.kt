package net.spexity.user

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.verifyUser
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

@QuarkusTest
class ContributorCommunityResourceTest {

    @Inject
    lateinit var dslContext: DSLContext

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "memberUser",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "memberUser"),
            SecurityAttribute(key = "email", value = "member@example.com")
        ]
    )
    fun `authenticated user can join community`() {
        registerCurrentUser()
        verifyUser(dslContext, "memberUser")

        val contributorId = dslContext.select(Tables.CONTRIBUTOR.ID)
            .from(Tables.CONTRIBUTOR)
            .where(Tables.CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq("memberUser"))
            .fetchOne(0, UUID::class.java) ?: throw IllegalStateException("Contributor not found")

        val communityId = insertCommunity(dslContext, "TestCommunity", contributorId)

        RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON)
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
    @TestSecurity(authorizationEnabled = false)
    fun `unauthenticated user cannot join community`() {
        val communityId = UUID.randomUUID()

        RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON)
            .post("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(403)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "leaveUser",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "leaveUser"),
            SecurityAttribute(key = "email", value = "leave@example.com")
        ]
    )
    fun `authenticated user can leave community`() {
        registerCurrentUser()
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
            .contentType(MediaType.APPLICATION_JSON)
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
    @TestSecurity(authorizationEnabled = false)
    fun `unauthenticated user cannot leave community`() {
        val communityId = UUID.randomUUID()

        RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON)
            .delete("/api/contributors/current/communities/$communityId")
            .then()
            .statusCode(403)
    }

}