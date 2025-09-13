package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.security.optionalAuthCorrelationId
import org.jooq.DSLContext

@Path("/api/web/communities")
class WebCommunitiesResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getWebTopicsPageData(@Context securityIdentity: SecurityIdentity): WebCommunitiesPageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        val selected = dslContext.select(
            COMMUNITY.ID,
            COMMUNITY.NAME
        )
            .from(COMMUNITY)
            .fetch {
                CommunityPreview(
                    it.get(COMMUNITY.ID).toString(),
                    it.get(COMMUNITY.NAME)
                )
            }
        return WebCommunitiesPageData(selected)
    }

    data class CommunityPreview(val id: String, val name: String)

    data class WebCommunitiesPageData(val communities: List<CommunityPreview>)

}
