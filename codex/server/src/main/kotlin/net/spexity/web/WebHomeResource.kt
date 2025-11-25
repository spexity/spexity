package net.spexity.web

import net.spexity.security.optionalAuthCorrelationId
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/web/home")
class WebHomeResource(private val webHomeService: WebHomeService) {

    @GetMapping
    fun getHomePageData(authentication: Authentication?): WebHomeService.HomePageData {
        val authCorrelationId = optionalAuthCorrelationId(authentication)
        return if (authCorrelationId != null) {
            webHomeService.homeList(authCorrelationId)
        } else {
            webHomeService.discoverList(null)
        }
    }
}
