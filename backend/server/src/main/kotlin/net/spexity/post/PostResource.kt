package net.spexity.post

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import net.spexity.security.authCorrelationId
import java.util.*

@Path("/api/posts")
class PostResource(private val postService: PostService) {

    @POST
    @Authenticated
    fun createPost(
        @Valid request: PostCreateRequest, @Context securityIdentity: SecurityIdentity
    ): PostService.CreateResponse {
        return postService.create(
            PostService.CreateRequest(
                authCorrelationId(securityIdentity), request.communityId,
                request.subject, request.body
            )
        )
    }

    data class PostCreateRequest(
        val communityId: UUID,
        @field:Size(min = 10, max = 512) val subject: String,
        val body: Doc,
        @field:AssertTrue(message = "Conform to terms and conditions must be accepted") val conformToTermsAndConditions: Boolean
    )

}
