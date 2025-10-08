package net.spexity.community

import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.COMMUNITY
import org.jooq.DSLContext
import java.util.*

@ApplicationScoped
class CommunityService(private val dslContext: DSLContext) {

    fun create(request: CreateRequest): CreateResponse {
        val name = request.name.trim().replace("\\s+".toRegex(), " ")
        val insertedId = dslContext.insertInto(COMMUNITY)
            .set(COMMUNITY.NAME, name)
            .set(COMMUNITY.CREATED_BY_CONTRIBUTOR_ID, request.contributorId)
            .returning(COMMUNITY.ID)
            .fetchOne()!!.id
        return CreateResponse(insertedId)
    }

    data class CreateRequest(val contributorId: UUID, val name: String)

    data class CreateResponse(val id: UUID)

}
