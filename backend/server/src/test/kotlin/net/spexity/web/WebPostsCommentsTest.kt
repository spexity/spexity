package net.spexity.web

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import net.spexity.testutils.docWithParagraph
import net.spexity.testutils.insertComment
import net.spexity.testutils.insertCommunity
import net.spexity.testutils.insertPost
import net.spexity.testutils.insertUser
import org.jooq.DSLContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

@QuarkusTest
class WebPostsCommentsTest {

    @Inject
    lateinit var dslContext: DSLContext

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "web-comments",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "web-comments"),
            SecurityAttribute(key = "email", value = "web-comments@example.com")
        ]
    )
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
        val activeId = createComment(postId, "Newest active")
        val deletedUuid = insertComment(
            dslContext,
            postId = postId,
            contributorId = contributorId,
            bodyText = "To delete",
            createdAt = baseTime.plusSeconds(1)
        )
        deleteComment(postId, deletedUuid.toString())

        val response = given()
            .get("/api/web/posts/${postId}")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertEquals(2, response.getInt("post.commentsCount"))

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

    private fun createComment(postId: UUID, text: String): String {
        return given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("bodyDocument" to docWithParagraph(text)))
            .post("/api/posts/${postId}/comments")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()
            .getString("id")
    }

    private fun deleteComment(postId: UUID, commentId: String) {
        given()
            .delete("/api/posts/${postId}/comments/${commentId}")
            .then()
            .statusCode(204)
    }
}
