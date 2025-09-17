package net.spexity.post

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import net.spexity.security.SecurityService
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import java.util.*

@ApplicationScoped
class PostService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
    val objectMapper: ObjectMapper
) {

    fun create(request: CreateRequest): CreateResponse {
        securityService.validateVerified(request.authCorrelationId)
        val bodyJson = objectMapper.writeValueAsString(request.body)
        val bodyPreview = BodyTextSerializer.render(request.body)
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
        val subject: String, val body: Doc
    )

    data class CreateResponse(
        val id: UUID
    )

}
