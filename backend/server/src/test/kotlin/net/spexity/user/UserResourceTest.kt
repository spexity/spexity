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
    fun `get current user authenticated succeeds`() {
        given()
            .`when`()
            .get("/api/current-user")
            .then()
            .statusCode(404)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(mapOf("alias" to "TEST"))
            .`when`()
            .post("/api/current-user")
            .then()
            .statusCode(400)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "alias" to "TEST",
                    "acceptTermsAndConditions" to true
                )
            )
            .`when`()
            .post("/api/current-user")
            .then()
            .statusCode(200)
            .body("contributorHandle", startsWith("TEST#"))
        given()
            .`when`()
            .get("/api/current-user")
            .then()
            .statusCode(200)
            .body("contributorHandle", startsWith("TEST#"))
    }

    @Test
    fun `get current user unauthenticated fails`() {
        given()
            .`when`()
            .get("/api/current-user")
            .then()
            .statusCode(401)
        given()
            .header("Authorization", "Bearer asd")
            .`when`()
            .get("/api/current-user")
            .then()
            .statusCode(401)
    }

}