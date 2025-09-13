package net.spexity.security

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal
import io.quarkus.security.identity.SecurityIdentity

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
