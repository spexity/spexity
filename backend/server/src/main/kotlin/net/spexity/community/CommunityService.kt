package net.spexity.community

import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.security.SecurityService
import org.jooq.DSLContext
import java.util.*

@ApplicationScoped
class CommunityService(private val dslContext: DSLContext, private val securityService: SecurityService) {

    fun create(request: CreateRequest): CreateResponse {
        val contributorId = securityService.validateVerifiedGetContributorId(request.authCorrelationId)
        val name = request.name.trim().replace("\\s+".toRegex(), " ")
        val insertedId = dslContext.insertInto(COMMUNITY)
            .set(COMMUNITY.NAME, name)
            .set(COMMUNITY.CREATED_BY_CONTRIBUTOR_ID, contributorId)
            .returning(COMMUNITY.ID)
            .fetchOne()!!.id
        return CreateResponse(insertedId)
    }

    data class CreateRequest(val authCorrelationId: String, val name: String)

    data class CreateResponse(val id: UUID)

}
