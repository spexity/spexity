package net.spexity.community

import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.user.ContributorCommunityService
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CommunityService(
    private val dslContext: DSLContext,
    private val contributorCommunityService: ContributorCommunityService
) {

    fun create(request: CreateRequest): CreateResponse {
        val name = request.name.trim().replace("\\s+".toRegex(), " ")
        val insertedId = dslContext.insertInto(COMMUNITY)
            .set(COMMUNITY.NAME, name)
            .set(COMMUNITY.CREATED_BY_CONTRIBUTOR_ID, request.contributorId)
            .returning(COMMUNITY.ID)
            .fetchOne()!!.id
        contributorCommunityService.joinCommunity(request.contributorId, insertedId)
        return CreateResponse(insertedId)
    }

    data class CreateRequest(val contributorId: UUID, val name: String)

    data class CreateResponse(val id: UUID)
}
