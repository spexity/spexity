package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.TopicPreview
import org.jooq.DSLContext

@Path("/api/web/topics")
class WebTopicsResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getTopicsPageData(@Context securityIdentity: SecurityIdentity): TopicsPageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        return TopicsPageData(listOf())
    }

    data class TopicsPageData(val topics: List<TopicPreview>)

}
