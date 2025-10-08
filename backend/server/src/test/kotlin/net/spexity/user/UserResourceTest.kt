package net.spexity.user

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.SecurityAttribute
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import org.hamcrest.CoreMatchers.startsWith
import org.junit.jupiter.api.Test

@QuarkusTest
class UserResourceTest {

    @Test
    @TestSecurity(
        authorizationEnabled = true,
        user = "test1",
        roles = ["user"],
        attributes = [
            SecurityAttribute(key = "sub", value = "test1"),
            SecurityAttribute(key = "email", value = "test1@example.com")
        ]
    )
    fun `test current user authenticated registration`() {
        given()
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(404)
        //Bad Terms and conditions
        registerBadRequest(
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "#1E3A8A"
            )
        )
        registerBadRequest(
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "#1E3A8A",
                "acceptTermsAndConditions" to false
            )
        )
        //Bad text
        registerBadRequest(
            mapOf(
                "alias" to "TEST",
                "avatarText" to "TE",
                "avatarBgColor" to "#1E3A8A",
                "acceptTermsAndConditions" to true
            )
        )
        //Bad bg color
        registerBadRequest(
            mapOf(
                "alias" to "TEST",
                "avatarText" to "🌟👍🏾",
                "avatarBgColor" to "FFFFFF",
                "acceptTermsAndConditions" to true
            )
        )
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
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
            .`when`()
            .get("/api/users/current")
            .then()
            .statusCode(401)
    }

    private fun registerBadRequest(request: Map<String, Any>) {
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("/api/users/current")
            .then()
            .statusCode(400)
    }

}
