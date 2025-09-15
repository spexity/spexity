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
import java.time.ZoneOffset
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
            COMMUNITY.NAME
        )
            .from(COMMUNITY)
            .fetch {
                CommunityPreview(
                    it.get(COMMUNITY.ID),
                    it.get(COMMUNITY.NAME)
                )
            }
        return CommunitiesPageData(selected)
    }

    @GET
    @Path("/{id}")
    @PermitAll
    fun getCommunityPageData(@PathParam("id") id: UUID): CommunityPageData {
        val selectedCommunity = dslContext.select(
            COMMUNITY.NAME
        )
            .from(COMMUNITY)
            .where(COMMUNITY.ID.eq(id))
            .fetchOne {
                CommunityPreview(id, it.get(COMMUNITY.NAME))
            }
        if (selectedCommunity == null) {
            throw NotFoundException("Community not found")
        }
        val selectedPosts = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.community().ID,
            POST.community().NAME,
        )
            .from(POST)
            .where(POST.community().ID.eq(id))
            .fetch {
                val instant = it.get(POST.CREATED_AT).toInstant(ZoneOffset.UTC)
                CommunityPreviewPost(
                    it.get(POST.ID),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY), //TODO; how to trim to only preview
                    ContributorRef(it.get(CONTRIBUTOR.ID), it.get(CONTRIBUTOR.HANDLE))
                )
            }
        return CommunityPageData(selectedCommunity, selectedPosts)
    }

    data class CommunitiesPageData(val communities: List<CommunityPreview>)

    data class CommunityPageData(val community: CommunityPreview, val posts: List<CommunityPreviewPost>)

}
