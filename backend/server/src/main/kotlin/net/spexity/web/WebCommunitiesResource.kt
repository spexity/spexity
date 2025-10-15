package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.CommunityPreview
import org.jooq.DSLContext
import java.util.*

@Path("/api/web/communities")
class WebCommunitiesResource(
    private val webCommunitiesService: WebCommunitiesService,
    private val dslContext: DSLContext
) {

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
                    it.get(COMMUNITY.POSTS_COUNT),
                    false
                )
            }
        return CommunitiesPageData(selected)
    }

    @GET
    @Path("/{id}")
    @PermitAll
    fun getCommunityPageData(
        @PathParam("id") id: UUID,
        @Context securityIdentity: SecurityIdentity
    ): WebCommunitiesService.CommunityPageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        return webCommunitiesService.communityPageData(id, authCorrelationId)
    }

    data class CommunitiesPageData(val communities: List<CommunityPreview>)

}
