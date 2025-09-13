package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.security.optionalAuthCorrelationId
import org.jooq.DSLContext

@Path("/api/web/topics")
class WebTopicsResource(private val dslContext: DSLContext) {

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getWebTopicsPageData(@Context securityIdentity: SecurityIdentity): WebTopicsPageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        return WebTopicsPageData(listOf())
    }

    data class TopicPreview(val id: String, val name: String)

    data class WebTopicsPageData(val topics: List<TopicPreview>)

}
