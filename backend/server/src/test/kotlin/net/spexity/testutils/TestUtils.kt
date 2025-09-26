package net.spexity.testutils

import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import net.spexity.data.model.public_.Tables.USER_ACCOUNT
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import java.util.UUID
import kotlin.random.Random

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

fun seedUser(
    dslContext: DSLContext,
    authId: String,
    alias: String,
    verified: Boolean = true,
    email: String = "$authId@example.com"
): UUID {
    val accountId = dslContext.insertInto(USER_ACCOUNT)
        .columns(USER_ACCOUNT.AUTH_CORRELATION_ID, USER_ACCOUNT.EMAIL_ADDRESS, USER_ACCOUNT.IS_VERIFIED_HUMAN)
        .values(authId, email, verified)
        .returning(USER_ACCOUNT.ID)
        .fetchOne()!!.id

    return dslContext.insertInto(CONTRIBUTOR)
        .columns(CONTRIBUTOR.USER_ACCOUNT_ID, CONTRIBUTOR.ALIAS, CONTRIBUTOR.DISCRIMINATOR)
        .values(accountId, alias, randomDiscriminator())
        .returning(CONTRIBUTOR.ID)
        .fetchOne()!!.id
}

fun seedPost(
    dslContext: DSLContext,
    communityId: UUID,
    contributorId: UUID,
    subject: String = "Subject ${UUID.randomUUID()}",
    bodyText: String = "Body for $subject",
    bodyJson: Map<String, Any> = docWithParagraph(bodyText)
): UUID {
    return dslContext.insertInto(POST)
        .columns(POST.SUBJECT, POST.BODY_JSON, POST.BODY_TEXT, POST.COMMUNITY_ID, POST.CONTRIBUTOR_ID)
        .values(subject, JSONB.jsonb(ObjectMappers.mapper.writeValueAsString(bodyJson)), bodyText, communityId, contributorId)
        .returning(POST.ID)
        .fetchOne()!!.id
}

fun insertComment(
    dslContext: DSLContext,
    postId: UUID,
    contributorId: UUID,
    bodyText: String,
    createdAt: java.time.OffsetDateTime = java.time.OffsetDateTime.now(),
    deletedAt: java.time.OffsetDateTime? = null,
    deletedByAuthor: Boolean = false,
    editCount: Int = 0
): UUID {
    val id = UUID.randomUUID()
    val bodyJson = docWithParagraph(bodyText)
    dslContext.insertInto(DSL.table("post_comment"))
        .columns(
            DSL.field("id"),
            DSL.field("post_id"),
            DSL.field("contributor_id"),
            DSL.field("body_json"),
            DSL.field("body_text"),
            DSL.field("created_at"),
            DSL.field("edited_at"),
            DSL.field("edit_count"),
            DSL.field("deleted_at"),
            DSL.field("deleted_by_author")
        )
        .values(
            id,
            postId,
            contributorId,
            JSONB.jsonb(ObjectMappers.mapper.writeValueAsString(bodyJson)),
            bodyText,
            createdAt,
            null,
            editCount,
            deletedAt,
            deletedByAuthor
        )
        .execute()
    return id
}

private fun randomDiscriminator(): Int = Random.nextInt(1000, 9999)

private object ObjectMappers {
    val mapper = com.fasterxml.jackson.databind.ObjectMapper()
}
