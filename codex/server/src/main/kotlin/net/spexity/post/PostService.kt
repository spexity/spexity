package net.spexity.post

import tools.jackson.databind.ObjectMapper
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import net.spexity.security.SecurityService
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
    val objectMapper: ObjectMapper
) {

    fun create(request: CreateRequest): CreateResponse {
        securityService.validateVerifiedGetContributorId(request.authCorrelationId)
        val bodyJson = objectMapper.writeValueAsString(request.bodyDocument)
        val bodyPreview = DocumentToTextSerializer.serialize(request.bodyDocument)
        val insertedId = dslContext.insertInto(
            POST,
            POST.SUBJECT,
            POST.BODY_JSON,
            POST.BODY_TEXT,
            POST.COMMUNITY_ID,
            POST.CONTRIBUTOR_ID
        )
            .select(
                dslContext
                    .select(
                        DSL.`val`(request.subject),
                        DSL.`val`(JSONB.jsonb(bodyJson)),
                        DSL.`val`(bodyPreview),
                        DSL.`val`(request.communityId),
                        CONTRIBUTOR.ID
                    )
                    .from(CONTRIBUTOR)
                    .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(request.authCorrelationId))
            )
            .returning(POST.ID)
            .fetchOne()!!.id
        return CreateResponse(insertedId)
    }

    data class CreateRequest(
        val authCorrelationId: String, val communityId: UUID,
        val subject: String, val bodyDocument: Document
    )

    data class CreateResponse(
        val id: UUID
    )
}
