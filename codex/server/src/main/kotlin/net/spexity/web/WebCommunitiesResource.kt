package net.spexity.web

import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.CommunityPreview
import org.jooq.DSLContext
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/web/communities")
class WebCommunitiesResource(
    private val webCommunitiesService: WebCommunitiesService,
    private val dslContext: DSLContext
) {

    @GetMapping
    fun getCommunitiesPageData(): CommunitiesPageData {
        val selected = dslContext.select(
            COMMUNITY.ID,
            COMMUNITY.NAME,
            COMMUNITY.POSTS_COUNT
        )
            .from(COMMUNITY)
            .fetch {
                CommunityPreview(
                    it.get(COMMUNITY.ID),
                    it.get(COMMUNITY.NAME),
                    it.get(COMMUNITY.POSTS_COUNT),
                    false
                )
            }
        return CommunitiesPageData(selected)
    }

    @GetMapping("/{id}")
    fun getCommunityPageData(
        @PathVariable id: UUID,
        authentication: Authentication?
    ): WebCommunitiesService.CommunityPageData {
        val authCorrelationId = optionalAuthCorrelationId(authentication)
        return webCommunitiesService.communityPageData(id, authCorrelationId)
    }

    data class CommunitiesPageData(val communities: List<CommunityPreview>)
}
