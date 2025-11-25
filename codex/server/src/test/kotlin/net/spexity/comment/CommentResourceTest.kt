package net.spexity.comment

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import net.spexity.testutils.IntegrationTestBase
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.insertComment
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.insertPost
import net.spexity.testutils.insertUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

private const val EXCLUDED_AUTH_ID = "3561bd91-89f0-4bb3-bf2f-b745fc415b41"

class CommentResourceTest : IntegrationTestBase() {

    @Test
    fun `unauthenticated user cannot create comment`() {
        val contributorId = insertUser(dslContext, authId = "post-author", alias = "Poster", verified = true)
        val communityId = insertCommunity(dslContext, "Anon Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph("Anonymous attempt")))
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(401)
    }

    @Test
    fun `unverified user cannot create comment`() {
        val contributorId = insertUser(dslContext, authId = "unverified", alias = "Unverified", verified = false)
        val communityId = insertCommunity(dslContext, "Unverified Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .headers(authHeaders("unverified"))
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph("Blocked")))
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(403)
    }

    @Test
    fun `verified user can create comment`() {
        val contributorId = insertUser(dslContext, authId = "commenter", alias = "Commenter", verified = true)
        val communityId = insertCommunity(dslContext, "Verified Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val response = given()
            .headers(authHeaders("commenter"))
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph("First comment")))
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        val returnedId = response.getString("id")
        UUID.fromString(returnedId)

        val listResponse = given()
            .get("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertEquals(returnedId, listResponse.getString("items[0].id"))
        assertEquals(false, listResponse.getBoolean("items[0].deleted"))
        assertTrue(listResponse.getString("items[0].bodyHtml").contains("First comment"))
    }

    @Test
    fun `empty comment is rejected`() {
        val contributorId = insertUser(dslContext, authId = "commenter-limits", alias = "Limiter", verified = true)
        val communityId = insertCommunity(dslContext, "Limit Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        given()
            .headers(authHeaders("commenter-limits"))
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph("   ")))
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(400)
    }

    @Test
    fun `requests are throttled after burst`() {
        val contributorId = insertUser(dslContext, authId = "commenter-throttle", alias = "Throttle", verified = true)
        val communityId = insertCommunity(dslContext, "Throttle Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val payload = mapOf("bodyDocument" to docWithParagraph("Throttle test"))

        given()
            .headers(authHeaders("commenter-throttle"))
            .contentType(ContentType.JSON)
            .body(payload)
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(200)

        given()
            .headers(authHeaders("commenter-throttle"))
            .contentType(ContentType.JSON)
            .body(payload)
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(429)
    }

    @Test
    fun `requests from excluded user are not throttled`() {
        val contributorId = insertUser(dslContext, authId = EXCLUDED_AUTH_ID, alias = "Exempt", verified = true)
        val communityId = insertCommunity(dslContext, "Exempt Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val payload = mapOf("bodyDocument" to docWithParagraph("Exempt comment"))

        repeat(3) {
            given()
                .headers(authHeaders(EXCLUDED_AUTH_ID, "god@example.com"))
                .contentType(ContentType.JSON)
                .body(payload)
                .post("/api/posts/$postId/comments")
                .then()
                .statusCode(200)
        }
    }

    @Test
    fun `comments list is chronological and includes deleted placeholder`() {
        val contributorId = insertUser(dslContext, authId = "commenter-list", alias = "Lister", verified = true)
        val communityId = insertCommunity(dslContext, "List Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val baseTime = java.time.OffsetDateTime.now()
        val firstId = insertComment(
            dslContext,
            postId,
            contributorId,
            "First comment",
            createdAt = baseTime.minusMinutes(5)
        )
        val secondId = createComment(postId, "commenter-list", "Second comment")
        val thirdId = insertComment(
            dslContext,
            postId,
            contributorId,
            "Third comment",
            createdAt = baseTime.plusMinutes(1)
        )

        deleteComment(postId, secondId, "commenter-list")

        val response = given()
            .get("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        val items = response.getList<Any>("items")
        assertEquals(3, items.size)

        assertEquals(firstId.toString(), response.getString("items[0].id"))
        assertFalse(response.getBoolean("items[0].deleted"))
        assertTrue(response.getString("items[0].bodyHtml").contains("First comment"))

        assertEquals(secondId, response.getString("items[1].id"))
        assertTrue(response.getBoolean("items[1].deleted"))
        val secondHtml = response.get("items[1].bodyHtml") as String?
        assertEquals(null, secondHtml)

        assertEquals(thirdId.toString(), response.getString("items[2].id"))
        assertFalse(response.getBoolean("items[2].deleted"))
        assertTrue(response.getString("items[2].bodyHtml").contains("Third comment"))
    }

    @Test
    fun `author can edit up to two times`() {
        val contributorId = insertUser(dslContext, authId = "commenter-edit", alias = "Editor", verified = true)
        val communityId = insertCommunity(dslContext, "Edit Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val commentId = createComment(postId, "commenter-edit", "Original")

        editComment(postId, commentId, "commenter-edit", "Edited once")
            .then()
            .statusCode(200)

        var listResponse = given()
            .get("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
        assertEquals(1, listResponse.getInt("items[0].editCount"))

        editComment(postId, commentId, "commenter-edit", "Edited twice")
            .then()
            .statusCode(200)

        listResponse = given()
            .get("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
        assertEquals(2, listResponse.getInt("items[0].editCount"))

        editComment(postId, commentId, "commenter-edit", "Edited thrice")
            .then()
            .statusCode(409)
    }

    @Test
    fun `non author cannot edit or delete`() {
        val authorContributor = insertUser(dslContext, authId = "author", alias = "Author", verified = true)
        insertUser(dslContext, authId = "intruder", alias = "Intruder", verified = true)
        val communityId = insertCommunity(dslContext, "Guard Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = authorContributor)

        val commentId = insertComment(dslContext, postId, authorContributor, "Protected comment")

        editComment(postId, commentId.toString(), "intruder", "Hacked")
            .then()
            .statusCode(403)

        given()
            .headers(authHeaders("intruder"))
            .delete("/api/posts/$postId/comments/$commentId")
            .then()
            .statusCode(403)
    }

    @Test
    fun `author can soft delete comment`() {
        val contributorId = insertUser(dslContext, authId = "commenter-delete", alias = "Remover", verified = true)
        val communityId = insertCommunity(dslContext, "Delete Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val commentId = createComment(postId, "commenter-delete", "Soon gone")

        given()
            .headers(authHeaders("commenter-delete"))
            .delete("/api/posts/$postId/comments/$commentId")
            .then()
            .statusCode(204)

        val response = given()
            .get("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertTrue(response.getBoolean("items[0].deleted"))
        val deletedHtml = response.get("items[0].bodyHtml") as String?
        assertEquals(null, deletedHtml)
    }

    private fun createComment(postId: UUID, user: String, text: String): String {
        return given()
            .headers(authHeaders(user))
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph(text)))
            .post("/api/posts/$postId/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("id")
    }

    private fun editComment(postId: UUID, commentId: String, user: String, text: String) =
        given()
            .headers(authHeaders(user))
            .contentType(ContentType.JSON)
            .body(mapOf("bodyDocument" to docWithParagraph(text)))
            .patch("/api/posts/$postId/comments/$commentId")

    private fun deleteComment(postId: UUID, commentId: String, user: String) {
        given()
            .headers(authHeaders(user))
            .delete("/api/posts/$postId/comments/$commentId")
            .then()
            .statusCode(204)
    }

    private fun authHeaders(user: String, email: String = "$user@example.com"): Map<String, String> =
        mapOf("Authorization" to "Bearer $user", "X-Auth-Email" to email)
}
