package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.security.optionalAuthCorrelationId

@Path("/api/web/home")
class WebHomeResource(private val webHomeService: WebHomeService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getHomePageData(@Context securityIdentity: SecurityIdentity): WebHomeService.HomePageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        if (authCorrelationId != null) {
            return webHomeService.homeList(authCorrelationId)
        } else {
            return webHomeService.discoverList(null)
        }
    }


}
