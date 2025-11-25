package net.spexity.user

import net.spexity.security.SecurityService
import net.spexity.security.authCorrelationId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/contributors/current")
class ContributorCommunityResource(
    private val contributorCommunityService: ContributorCommunityService,
    private val securityService: SecurityService
) {

    @PostMapping("/communities/{communityId}")
    fun joinCommunity(
        @PathVariable communityId: UUID,
        authentication: Authentication
    ): ResponseEntity<ContributorCommunityService.JoinResponse> {
        val contributorId = securityService.getContributorId(authCorrelationId(authentication))
        val result = contributorCommunityService.joinCommunity(contributorId, communityId)
        return ResponseEntity.status(201).body(result)
    }

    @DeleteMapping("/communities/{communityId}")
    fun leaveCommunity(
        @PathVariable communityId: UUID,
        authentication: Authentication
    ): ResponseEntity<Void> {
        val contributorId = securityService.getContributorId(authCorrelationId(authentication))
        contributorCommunityService.leaveCommunity(contributorId, communityId)
        return ResponseEntity.noContent().build()
    }
}
