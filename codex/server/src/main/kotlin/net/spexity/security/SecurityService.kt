package net.spexity.security

import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import org.jooq.DSLContext
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.util.UUID

@Service
class SecurityService(private val dslContext: DSLContext) {

    fun validateVerifiedGetContributorId(authCorrelationId: String): UUID {
        return getContributorId(authCorrelationId, true)
    }

    fun getContributorId(authCorrelationId: String): UUID {
        return getContributorId(authCorrelationId, false)
    }

    private fun getContributorId(authCorrelationId: String, validateVerified: Boolean): UUID {
        val selected = dslContext
            .select(CONTRIBUTOR.ID, CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN)
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetchOne()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "User not fully registered")
        if (validateVerified) {
            val isVerifiedHuman = selected.get(CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN)
            if (!isVerifiedHuman) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "User not verified")
            }
        }
        return selected.get(CONTRIBUTOR.ID)
    }
}

fun authCorrelationId(authentication: Authentication): String {
    return authentication.name
}

fun optionalAuthCorrelationId(authentication: Authentication?): String? {
    if (authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
        return null
    }
    return authentication.name
}

fun tokenEmail(authentication: Authentication): String {
    val principal = authentication.principal
    if (principal is OAuth2AuthenticatedPrincipal) {
        return principal.getAttribute("email") ?: authentication.name
    }
    return authentication.name
}
