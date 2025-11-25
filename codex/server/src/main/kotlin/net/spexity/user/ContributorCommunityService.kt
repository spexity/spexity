package net.spexity.user

import net.spexity.data.model.public_.Tables.CONTRIBUTOR_COMMUNITY
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ContributorCommunityService(private val dslContext: DSLContext) {

    fun joinCommunity(contributorId: UUID, communityId: UUID): JoinResponse {
        val record = dslContext.insertInto(CONTRIBUTOR_COMMUNITY)
            .set(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID, contributorId)
            .set(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID, communityId)
            .returning()
            .fetchOne()!!

        return JoinResponse(
            record.id,
            record.contributorId,
            record.communityId,
            record.createdAt.toInstant()
        )
    }

    fun leaveCommunity(contributorId: UUID, communityId: UUID) {
        dslContext.deleteFrom(CONTRIBUTOR_COMMUNITY)
            .where(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
            .and(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID.eq(communityId))
            .execute()
    }

    data class JoinResponse(
        val id: UUID,
        val contributorId: UUID,
        val communityId: UUID,
        val createdAt: Instant
    )
}
