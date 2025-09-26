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
import net.spexity.testutils.seedPost
import net.spexity.testutils.seedUser
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
        val contributorId = seedUser(dslContext, authId = "web-comments", alias = "Webber", verified = true)
        val communityId = insertCommunity(dslContext, "Web Comments Community")
        val postId = seedPost(dslContext, communityId = communityId, contributorId = contributorId)

        val oldestId = insertComment(
            dslContext,
            postId = postId,
            contributorId = contributorId,
            bodyText = "Oldest",
            createdAt = OffsetDateTime.now().minusHours(2)
        )
        val activeId = createComment(postId, "Newest active")
        val deletedId = createComment(postId, "To delete")
        deleteComment(postId, deletedId)

        val response = given()
            .get("/api/web/posts/${postId}")
            .then()
            .statusCode(200)
            .extract()
            .jsonPath()

        assertEquals(2, response.getInt("post.commentsCount"))

        val total = response.getInt("comments.total")
        assertEquals(3, total)

        val items = response.getList<Any>("comments.items")
        assertEquals(3, items.size)

        val firstId = response.getString("comments.items[0].id")
        val secondId = response.getString("comments.items[1].id")
        val thirdId = response.getString("comments.items[2].id")

        assertEquals(oldestId.toString(), firstId)
        assertTrue(response.getString("comments.items[0].html").contains("Oldest"))

        assertEquals(activeId, secondId)
        assertEquals(false, response.getBoolean("comments.items[1].deleted"))
        assertTrue(response.getString("comments.items[1].html").contains("Newest active"))

        assertEquals(deletedId, thirdId)
        assertTrue(response.getBoolean("comments.items[2].deleted"))

        val page = response.getInt("comments.page")
        val pageSize = response.getInt("comments.pageSize")
        assertEquals(1, page)
        assertEquals(100, pageSize)
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

    private fun deleteComment(postId: UUID, commentId: String) {
        given()
            .delete("/api/posts/${postId}/comments/${commentId}")
            .then()
            .statusCode(204)
    }
}
