package net.spexity.user

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.spexity.security.SecurityService
import net.spexity.security.authCorrelationId
import java.util.*

@Path("/api/contributors/current")
class ContributorCommunityResource(
    private val contributorCommunityService: ContributorCommunityService,
    private val securityService: SecurityService
) {

    @Path("/communities/{communityId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    fun joinCommunity(
        @PathParam("communityId") communityId: UUID,
        @Context securityIdentity: SecurityIdentity
    ): Response {
        val contributorId = securityService.getContributorId(authCorrelationId(securityIdentity))
        val result = contributorCommunityService.joinCommunity(contributorId, communityId)
        return Response.status(201).entity(result).build()
    }

    @Path("/communities/{communityId}")
    @DELETE
    @Authenticated
    fun leaveCommunity(
        @PathParam("communityId") communityId: UUID,
        @Context securityIdentity: SecurityIdentity
    ): Response {
        val contributorId = securityService.getContributorId(authCorrelationId(securityIdentity))
        contributorCommunityService.leaveCommunity(contributorId, communityId)
        return Response.noContent().build()
    }

}
