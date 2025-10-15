package net.spexity.post

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import net.spexity.testutils.registerCurrentUser
import net.spexity.testutils.verifyUser
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.userAccountId
import java.util.UUID

@QuarkusTest
class PostResourceTest {

    @Inject
    lateinit var dslContext: DSLContext

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "postUser",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "postUser"),
            SecurityAttribute(key = "email", value = "post@example.com")
        ]
    )
    fun `verified user can create post`() {
        registerCurrentUser()
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
                .contentType(MediaType.APPLICATION_JSON)
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
    @TestSecurity(
        authorizationEnabled = true,
        user = "unverifiedPoster",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "unverifiedPoster"),
            SecurityAttribute(key = "email", value = "unverifiedPoster@example.com")
        ]
    )
    fun `unverified user cannot create post`() {
        registerCurrentUser()

        val communityId = insertCommunity(dslContext, "Another Community")
        val subject = "Short note ${UUID.randomUUID()}"

        given()
            .contentType(MediaType.APPLICATION_JSON)
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
