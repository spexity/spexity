package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.security.optionalAuthCorrelationId

@Path("/api/web/discover")
class WebDiscoverResource(private val webHomeService: WebHomeService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getHomePageData(@Context securityIdentity: SecurityIdentity): WebHomeService.HomePageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        return webHomeService.discoverList(authCorrelationId)
    }

}
