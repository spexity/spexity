package net.spexity.testutils

import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.USER_ACCOUNT
import org.jooq.DSLContext
import java.util.UUID

fun registerCurrentUser(alias: String) {
    given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            mapOf(
                "alias" to alias,
                "acceptTermsAndConditions" to true
            )
        )
        .post("/api/current-user")
        .then()
        .statusCode(200)
}

fun verifyUser(dslContext: DSLContext, authCorrelationId: String) {
    dslContext.update(USER_ACCOUNT)
        .set(USER_ACCOUNT.IS_VERIFIED_HUMAN, true)
        .where(USER_ACCOUNT.AUTH_CORRELATION_ID.eq(authCorrelationId))
        .execute()
}

fun insertCommunity(dslContext: DSLContext, namePrefix: String): UUID {
    val name = "$namePrefix ${UUID.randomUUID()}"
    return dslContext.insertInto(COMMUNITY)
        .columns(COMMUNITY.NAME)
        .values(name)
        .returning(COMMUNITY.ID)
        .fetchOne()!!.id
}

fun docWithParagraph(text: String): Map<String, Any> = mapOf(
    "type" to "doc",
    "content" to listOf(
        mapOf(
            "type" to "paragraph",
            "content" to listOf(
                mapOf(
                    "type" to "text",
                    "text" to text
                )
            )
        )
    )
)

fun userAccountId(dslContext: DSLContext, authCorrelationId: String): UUID {
    return dslContext.select(USER_ACCOUNT.ID)
        .from(USER_ACCOUNT)
        .where(USER_ACCOUNT.AUTH_CORRELATION_ID.eq(authCorrelationId))
        .fetchOne(USER_ACCOUNT.ID)!!
}
