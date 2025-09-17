package net.spexity.security

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal
import io.quarkus.security.identity.SecurityIdentity
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.ForbiddenException
import net.spexity.user.UserService

@ApplicationScoped
class SecurityService(private val userService: UserService) {
    fun validateVerified(authCorrelationId: String) {
        if (!userService.registeredAndVerified(authCorrelationId)) {
            throw ForbiddenException("User not verified")
        }
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
