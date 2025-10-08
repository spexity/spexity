package net.spexity.security

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal
import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.ForbiddenException
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import org.jooq.DSLContext
import java.util.*

@ApplicationScoped
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
        if (selected == null) {
            throw ForbiddenException("User not fully registered")
        }
        if (validateVerified) {
            val isVerifiedHuman = selected.get(CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN)
            if (!isVerifiedHuman) {
                throw ForbiddenException("User not verified")
            }
        }
        return selected.get(CONTRIBUTOR.ID)
    }
}


fun authCorrelationId(securityIdentity: SecurityIdentity): String {
    return securityIdentity.principal.name
}

fun optionalAuthCorrelationId(securityIdentity: SecurityIdentity): String? {
    if (securityIdentity.isAnonymous) {
        return null
    }
    return securityIdentity.principal?.name
}

fun tokenEmail(securityIdentity: SecurityIdentity): String {
    if (securityIdentity.principal is OidcJwtCallerPrincipal) {
        val principal = securityIdentity.principal as OidcJwtCallerPrincipal
        return principal.claim<String>("email").get()
    }
    return securityIdentity.getAttribute("email")
}
