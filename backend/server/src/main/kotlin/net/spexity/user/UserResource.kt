package net.spexity.user

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import net.spexity.security.tokenEmail
import net.spexity.security.tokenSubject
import org.jboss.logging.Logger

@Path("/api/current-user")
class UserResource(private val userService: UserService, private val logger: Logger) {

    @GET
    @Authenticated
    fun getCurrentUser(@Context securityIdentity: SecurityIdentity): UserService.RegResponse {
        return userService.getUser(tokenSubject(securityIdentity)) ?: throw NotFoundException("Please register first")
    }

    @POST
    @Authenticated
    fun registerUser(
        @Valid request: WebUserRegisterRequest, @Context securityIdentity: SecurityIdentity
    ): UserService.RegResponse {
        return userService.register(
            UserService.RegRequest(
                tokenSubject(securityIdentity), tokenEmail(securityIdentity), request.alias
            )
        )
    }

    data class WebUserRegisterRequest(
        @field:Size(min = 3, max = 20) val alias: String
    )

}