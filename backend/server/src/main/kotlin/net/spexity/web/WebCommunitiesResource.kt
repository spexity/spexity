package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.*
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.CommunityPreview
import net.spexity.web.model.CommunityPreviewPost
import net.spexity.web.model.ContributorRef
import org.jooq.DSLContext
import java.util.*

@Path("/api/web/communities")
class WebCommunitiesResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getCommunitiesPageData(@Context securityIdentity: SecurityIdentity): CommunitiesPageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        val selected = dslContext.select(
            COMMUNITY.ID,
            COMMUNITY.NAME,
            COMMUNITY.POSTS_COUNT
        )
            .from(COMMUNITY)
            .fetch {
                CommunityPreview(
                    it.get(COMMUNITY.ID),
                    it.get(COMMUNITY.NAME),
                    it.get(COMMUNITY.POSTS_COUNT)
                )
            }
        return CommunitiesPageData(selected)
    }

    @GET
    @Path("/{id}")
    @PermitAll
    fun getCommunityPageData(@PathParam("id") id: UUID): CommunityPageData {
        val selectedCommunity = dslContext.select(
            COMMUNITY.NAME,
            COMMUNITY.POSTS_COUNT
        )
            .from(COMMUNITY)
            .where(COMMUNITY.ID.eq(id))
            .fetchOne {
                CommunityPreview(id, it.get(COMMUNITY.NAME), it.get(COMMUNITY.POSTS_COUNT))
            }
        if (selectedCommunity == null) {
            throw NotFoundException("Community not found")
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

    data class CommunitiesPageData(val communities: List<CommunityPreview>)

    data class CommunityPageData(val community: CommunityPreview, val posts: List<CommunityPreviewPost>)

}
