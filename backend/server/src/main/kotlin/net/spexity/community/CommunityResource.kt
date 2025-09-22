package net.spexity.community

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import net.spexity.security.authCorrelationId

@Path("/api/communities")
class CommunityResource(private val communityService: CommunityService) {

    @POST
    @Authenticated
    fun createCommunity(
        @Valid request: CommunityCreateRequest, @Context securityIdentity: SecurityIdentity
    ): CommunityService.CreateResponse {
        return communityService.create(
            CommunityService.CreateRequest(authCorrelationId(securityIdentity), request.name)
        )
    }

    data class CommunityCreateRequest(
        @field:Size(min = 3, max = 64) val name: String,
        @field:AssertTrue(message = "Conform to terms and conditions must be accepted") val conformToTermsAndConditions: Boolean
    )

}
