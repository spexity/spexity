package net.spexity.user

import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import net.spexity.testutils.IntegrationTestBase
import org.hamcrest.CoreMatchers.startsWith
import org.junit.jupiter.api.Test

class UserResourceTest : IntegrationTestBase() {

    @Test
    fun `test current user authenticated registration`() {
        given()
            .header("Authorization", "Bearer test1")
            .header("X-Auth-Email", "test1@example.com")
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(404)

        registerBadRequest(
            "test1",
            "test1@example.com",
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "#1E3A8A"
            )
        )
        registerBadRequest(
            "test1",
            "test1@example.com",
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "#1E3A8A",
                "acceptTermsAndConditions" to false
            )
        )
        registerBadRequest(
            "test1",
            "test1@example.com",
            mapOf(
                "alias" to "TEST",
                "avatarText" to "TE",
                "avatarBgColor" to "#1E3A8A",
                "acceptTermsAndConditions" to true
            )
        )
        registerBadRequest(
            "test1",
            "test1@example.com",
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "FFFFFF",
                "acceptTermsAndConditions" to true
            )
        )
        given()
            .header("Authorization", "Bearer test1")
            .header("X-Auth-Email", "test1@example.com")
            .header("Content-Type", ContentType.JSON)
            .body(
                mapOf(
                    "alias" to "TEST",
                    "avatarText" to "🌟👍🏾",
                    "avatarBgColor" to "#1E3A8A",
                    "acceptTermsAndConditions" to true
                )
            )
            .`when`()
            .post("/api/users/current")
            .then()
            .statusCode(200)
            .body("contributor.handle", startsWith("TEST#"))
        given()
            .header("Authorization", "Bearer test1")
            .header("X-Auth-Email", "test1@example.com")
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(200)
            .body("contributor.handle", startsWith("TEST#"))
    }

    @Test
    fun `get current user unauthenticated fails`() {
        given()
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(401)
        given()
            .header("Authorization", "Bearer asd")
            .header("X-Auth-Email", "asd@example.com")
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(404)
    }

    private fun registerBadRequest(authId: String, email: String, request: Map<String, Any>) {
        given()
            .header("Authorization", "Bearer $authId")
            .header("X-Auth-Email", email)
            .header("Content-Type", ContentType.JSON)
            .body(request)
            .`when`()
            .post("/api/users/current")
            .then()
            .statusCode(400)
    }
}
