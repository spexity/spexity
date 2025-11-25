package net.spexity.post

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import net.spexity.testutils.IntegrationTestBase
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.userAccountId
import net.spexity.testutils.verifyUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class PostResourceTest : IntegrationTestBase() {

    @Test
    fun `verified user can create post`() {
        registerCurrentUser("postUser", "post@example.com")
        verifyUser(dslContext, "postUser")

        val communityId = insertCommunity(dslContext, "Test Community")
        val subject = "A helpful update ${UUID.randomUUID()}"
        val message = "Hello from the test suite."

        val requestBody = mapOf(
            "communityId" to communityId.toString(),
            "subject" to subject,
            "bodyDocument" to docWithParagraph(message),
            "acceptTermsAndConditions" to true
        )

        val postId = UUID.fromString(
            given()
                .header("Authorization", "Bearer postUser")
                .header("X-Auth-Email", "post@example.com")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/api/posts")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("id")
        )

        val record = dslContext.selectFrom(POST)
            .where(POST.ID.eq(postId))
            .fetchOne()

        assertNotNull(record)
        assertEquals(subject, record!!.subject)
        assertEquals(message, record.bodyText.trim())
        assertEquals(communityId, record.communityId)

        val postsCount = dslContext.select(COMMUNITY.POSTS_COUNT)
            .from(COMMUNITY)
            .where(COMMUNITY.ID.eq(communityId))
            .fetchOne(COMMUNITY.POSTS_COUNT)

        assertEquals(1, postsCount)

        val contributorId = dslContext.select(CONTRIBUTOR.ID)
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.USER_ACCOUNT_ID.eq(userAccountId(dslContext, "postUser")))
            .fetchOne(CONTRIBUTOR.ID)

        assertEquals(contributorId, record.contributorId)
    }

    @Test
    fun `unverified user cannot create post`() {
        registerCurrentUser("unverifiedPoster", "unverifiedPoster@example.com")

        val communityId = insertCommunity(dslContext, "Another Community")
        val subject = "Short note ${UUID.randomUUID()}"

        given()
            .header("Authorization", "Bearer unverifiedPoster")
            .header("X-Auth-Email", "unverifiedPoster@example.com")
            .contentType(ContentType.JSON)
            .body(
                mapOf(
                    "communityId" to communityId.toString(),
                    "subject" to subject,
                    "bodyDocument" to docWithParagraph("This should not persist."),
                    "acceptTermsAndConditions" to true
                )
            )
            .post("/api/posts")
            .then()
            .statusCode(403)

        val created = dslContext.selectCount()
            .from(POST)
            .where(POST.SUBJECT.eq(subject))
            .fetchOne(0, Int::class.java)

        assertEquals(0, created)
    }
}
