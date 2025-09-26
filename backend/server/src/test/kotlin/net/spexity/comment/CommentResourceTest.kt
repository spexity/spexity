package net.spexity.comment

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.insertComment
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.seedPost
import net.spexity.testutils.seedUser
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class CommentResourceTest {

    @Inject
    lateinit var dslContext: DSLContext

    @Test
    fun `unauthenticated user cannot create comment`() {
        val contributorId = seedUser(dslContext, authId = "post-author", alias = "Poster", verified = true)
        val communityId = insertCommunity(dslContext, "Anon Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph("Anonymous attempt")))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(401)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "unverified",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "unverified"),
            SecurityAttribute(key = "email", value = "unverified@example.com")
        ]
    )
    fun `unverified user cannot create comment`() {
        val contributorId = seedUser(dslContext, authId = "unverified", alias = "Unverified", verified = false)
        val communityId = insertCommunity(dslContext, "Unverified Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph("Blocked")))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(403)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter"),
            SecurityAttribute(key = "email", value = "commenter@example.com")
        ]
    )
    fun `verified user can create comment`() {
        val contributorId = seedUser(dslContext, authId = "commenter", alias = "Commenter", verified = true)
        val communityId = insertCommunity(dslContext, "Verified Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val response = given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph("First comment")))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        val returnedHtml = response.getString("html")
        val returnedId = response.getString("id")
        val edited = response.getBoolean("edited")
        val deleted = response.getBoolean("deleted")

        assertTrue(returnedHtml.contains("<p>First comment</p>"))
        assertTrue(UUID.fromString(returnedId).toString().isNotEmpty())
        assertEquals(false, edited)
        assertEquals(false, deleted)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter-limits",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter-limits"),
            SecurityAttribute(key = "email", value = "commenter-limits@example.com")
        ]
    )
    fun `empty comment is rejected`() {
        val contributorId = seedUser(dslContext, authId = "commenter-limits", alias = "Limiter", verified = true)
        val communityId = insertCommunity(dslContext, "Limit Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph("   ")))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(400)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter-throttle",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter-throttle"),
            SecurityAttribute(key = "email", value = "commenter-throttle@example.com")
        ]
    )
    fun `requests are throttled after burst`() {
        val contributorId = seedUser(dslContext, authId = "commenter-throttle", alias = "Throttle", verified = true)
        val communityId = insertCommunity(dslContext, "Throttle Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val payload = mapOf("body" to docWithParagraph("Throttle test"))

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(429)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter-list",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter-list"),
            SecurityAttribute(key = "email", value = "commenter-list@example.com")
        ]
    )
    fun `comments list is chronological and includes deleted placeholder`() {
        val contributorId = seedUser(dslContext, authId = "commenter-list", alias = "Lister", verified = true)
        val communityId = insertCommunity(dslContext, "List Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val firstId = createComment(postId, "First comment")
        val secondId = createComment(postId, "Second comment")
        val thirdId = insertComment(
            dslContext,
            postId,
            contributorId,
            "Third comment",
            createdAt = java.time.OffsetDateTime.now().plusSeconds(1)
        )

        deleteComment(postId, secondId)

        val response = given()
            .get("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        val items = response.getList<Any>("items")
        assertEquals(3, items.size)

        assertEquals(firstId, response.getString("items[0].id"))
        assertFalse(response.getBoolean("items[0].deleted"))
        assertTrue(response.getString("items[0].html").contains("First comment"))

        assertEquals(secondId, response.getString("items[1].id"))
        assertTrue(response.getBoolean("items[1].deleted"))
        assertTrue(response.getString("items[1].html").contains("Comment deleted"))

        assertEquals(thirdId.toString(), response.getString("items[2].id"))
        assertFalse(response.getBoolean("items[2].deleted"))
        assertTrue(response.getString("items[2].html").contains("Third comment"))
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter-edit",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter-edit"),
            SecurityAttribute(key = "email", value = "commenter-edit@example.com")
        ]
    )
    fun `author can edit up to two times`() {
        val contributorId = seedUser(dslContext, authId = "commenter-edit", alias = "Editor", verified = true)
        val communityId = insertCommunity(dslContext, "Edit Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val commentId = createComment(postId, "Original")

        editComment(postId, commentId, "Edited once")
            .then()
            .statusCode(200)
            .body("edited", org.hamcrest.Matchers.equalTo(true))

        editComment(postId, commentId, "Edited twice")
            .then()
            .statusCode(200)

        editComment(postId, commentId, "Edited thrice")
            .then()
            .statusCode(409)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "intruder",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "intruder"),
            SecurityAttribute(key = "email", value = "intruder@example.com")
        ]
    )
    fun `non author cannot edit or delete`() {
        val authorContributor = seedUser(dslContext, authId = "author", alias = "Author", verified = true)
        seedUser(dslContext, authId = "intruder", alias = "Intruder", verified = true)
        val communityId = insertCommunity(dslContext, "Guard Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = authorContributor)

        val commentId = insertComment(dslContext, postId, authorContributor, "Protected comment")

        editComment(postId, commentId.toString(), "Hacked")
            .then()
            .statusCode(403)

        given()
            .delete("/api/posts/${postId}/comments/${commentId}")
            .then()
            .statusCode(403)
    }

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "commenter-delete",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "commenter-delete"),
            SecurityAttribute(key = "email", value = "commenter-delete@example.com")
        ]
    )
    fun `author can soft delete comment`() {
        val contributorId = seedUser(dslContext, authId = "commenter-delete", alias = "Remover", verified = true)
        val communityId = insertCommunity(dslContext, "Delete Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val commentId = createComment(postId, "Soon gone")

        given()
            .delete("/api/posts/${postId}/comments/${commentId}")
            .then()
            .statusCode(204)

        val response = given()
            .get("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertTrue(response.getBoolean("items[0].deleted"))
        assertTrue(response.getString("items[0].html").contains("Comment deleted"))
    }

    private fun createComment(postId: UUID, text: String): String {
        return given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph(text)))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("id")
    }

    private fun editComment(postId: UUID, commentId: String, text: String) =
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("body" to docWithParagraph(text)))
            .patch("/api/posts/${postId}/comments/${commentId}")

    private fun deleteComment(postId: UUID, commentId: String) {
        given()
            .delete("/api/posts/${postId}/comments/${commentId}")
            .then()
            .statusCode(204)
    }
}
