package net.spexity.post

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import net.spexity.security.authCorrelationId
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/posts")
@Validated
class PostResource(private val postService: PostService) {

    @PostMapping
    fun createPost(
        @Valid @RequestBody request: PostCreateRequest,
        authentication: Authentication
    ): PostService.CreateResponse {
        return postService.create(
            PostService.CreateRequest(
                authCorrelationId(authentication), request.communityId,
                request.subject, request.bodyDocument
            )
        )
    }

    data class PostCreateRequest(
        val communityId: UUID,
        @field:Size(min = 10, max = 512) val subject: String,
        val bodyDocument: Document,
        @field:AssertTrue(message = "Terms and conditions must be accepted") val acceptTermsAndConditions: Boolean
    )
}
