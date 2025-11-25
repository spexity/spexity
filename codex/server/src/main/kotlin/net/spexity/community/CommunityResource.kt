package net.spexity.community

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import net.spexity.security.SecurityService
import net.spexity.security.authCorrelationId
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/communities")
@Validated
class CommunityResource(
    private val communityService: CommunityService,
    private val securityService: SecurityService
) {

    @PostMapping
    fun createCommunity(
        @Valid @RequestBody request: CommunityCreateRequest,
        authentication: Authentication
    ): CommunityService.CreateResponse {
        val contributorId = securityService.validateVerifiedGetContributorId(authCorrelationId(authentication))
        return communityService.create(
            CommunityService.CreateRequest(contributorId, request.name)
        )
    }

    data class CommunityCreateRequest(
        @field:Size(min = 3, max = 64) val name: String,
        @field:AssertTrue(message = "Terms and conditions must be accepted") val acceptTermsAndConditions: Boolean
    )
}
