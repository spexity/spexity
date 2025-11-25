package net.spexity.web

import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.CONTRIBUTOR_COMMUNITY
import net.spexity.data.model.public_.Tables.POST
import net.spexity.security.SecurityService
import net.spexity.web.model.CommunityPreview
import net.spexity.web.model.CommunityPreviewPost
import net.spexity.web.model.ContributorRef
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class WebCommunitiesService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
) {

    fun communityPageData(id: UUID, authCorrelationId: String?): CommunityPageData {
        val selectedCommunity = if (authCorrelationId != null) {
            contributorCommunityPreview(id, authCorrelationId)
        } else {
            anonCommunityPreview(id)
        }
        if (selectedCommunity == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found")
        }
        val selectedPosts = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY_TEXT,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.contributor().AVATAR_TEXT,
            POST.contributor().AVATAR_BG_COLOR,
            POST.community().ID,
            POST.community().NAME,
            POST.COMMENTS_COUNT,
        )
            .from(POST)
            .where(POST.community().ID.eq(id))
            .fetch {
                val instant = it.get(POST.CREATED_AT).toInstant()
                CommunityPreviewPost(
                    it.get(POST.ID),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY_TEXT).take(512),
                    ContributorRef(
                        it.get(POST.contributor().ID),
                        it.get(POST.contributor().HANDLE),
                        it.get(POST.contributor().AVATAR_TEXT),
                        it.get(POST.contributor().AVATAR_BG_COLOR)
                    ),
                    it.get(POST.COMMENTS_COUNT)
                )
            }
        return CommunityPageData(selectedCommunity, selectedPosts)
    }

    private fun contributorCommunityPreview(id: UUID, authCorrelationId: String): CommunityPreview? {
        val contributorId = securityService.getContributorId(authCorrelationId)
        return dslContext.select(
            COMMUNITY.NAME,
            COMMUNITY.POSTS_COUNT,
            CONTRIBUTOR_COMMUNITY.ID
        )
            .from(COMMUNITY)
            .leftJoin(CONTRIBUTOR_COMMUNITY)
            .on(
                COMMUNITY.ID.eq(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID)
                    .and(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
            )
            .where(COMMUNITY.ID.eq(id))
            .fetchOne {
                CommunityPreview(
                    id, it.get(COMMUNITY.NAME), it.get(COMMUNITY.POSTS_COUNT),
                    it.get(CONTRIBUTOR_COMMUNITY.ID) != null
                )
            }
    }

    private fun anonCommunityPreview(id: UUID): CommunityPreview? = dslContext.select(
        COMMUNITY.NAME,
        COMMUNITY.POSTS_COUNT,
    )
        .from(COMMUNITY)
        .where(COMMUNITY.ID.eq(id))
        .fetchOne {
            CommunityPreview(id, it.get(COMMUNITY.NAME), it.get(COMMUNITY.POSTS_COUNT), false)
        }

    data class CommunityPageData(
        val community: CommunityPreview,
        val posts: List<CommunityPreviewPost>
    )
}
