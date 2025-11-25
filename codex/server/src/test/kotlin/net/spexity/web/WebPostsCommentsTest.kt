package net.spexity.web

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import net.spexity.testutils.IntegrationTestBase
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.insertComment
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.insertPost
import net.spexity.testutils.insertUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class WebPostsCommentsTest : IntegrationTestBase() {

    @Test
    fun `post web payload contains comments`() {
        val contributorId = insertUser(dslContext, authId = "web-comments", alias = "Webber", verified = true)
        val communityId = insertCommunity(dslContext, "Web Comments Community")
        val postId = insertPost(dslContext, communityId = communityId, contributorId = contributorId)

        val baseTime = OffsetDateTime.now()
        val oldestId = insertComment(
            dslContext,
            postId = postId,
            contributorId = contributorId,
            bodyText = "Oldest",
            createdAt = baseTime.minusHours(2)
        ).toString()
        val activeId = createComment(postId, "web-comments", "Newest active")
        val deletedUuid = insertComment(
            dslContext,
            postId = postId,
            contributorId = contributorId,
            bodyText = "To delete",
            createdAt = baseTime.plusSeconds(1)
        )
        deleteComment(postId, deletedUuid.toString(), "web-comments")

        val response = given()
            .get("/api/web/posts/$postId")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertEquals(3, response.getInt("post.commentsCount"))

        val items = response.getList<Any>("comments.items")
        assertEquals(3, items.size)

        val firstId = response.getString("comments.items[0].id")
        val secondId = response.getString("comments.items[1].id")
        val thirdId = response.getString("comments.items[2].id")

        assertEquals(oldestId, firstId)
        assertTrue(response.getString("comments.items[0].bodyHtml").contains("Oldest"))

        assertEquals(activeId, secondId)
        assertEquals(false, response.getBoolean("comments.items[1].deleted"))
        assertTrue(response.getString("comments.items[1].bodyHtml").contains("Newest active"))

        assertEquals(deletedUuid.toString(), thirdId)
        assertTrue(response.getBoolean("comments.items[2].deleted"))
        val thirdHtml = response.get("comments.items[2].bodyHtml") as String?
        assertEquals(null, thirdHtml)

        val page = response.getInt("comments.page")
        val pageSize = response.getInt("comments.pageSize")
        assertEquals(1, page)
        assertEquals(100, pageSize)
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
