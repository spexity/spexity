package net.spexity.user

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import net.spexity.security.authCorrelationId
import net.spexity.security.tokenEmail

@Path("/api/current-user")
class UserResource(private val userService: UserService) {

    @GET
    @Authenticated
    fun getCurrentUser(@Context securityIdentity: SecurityIdentity): UserService.RegResponse {
        return userService.getUser(authCorrelationId(securityIdentity))
            ?: throw NotFoundException("Please register first")
    }

    @POST
    @Authenticated
    fun registerUser(
        @Valid request: UserRegisterRequest, @Context securityIdentity: SecurityIdentity
    ): UserService.RegResponse {
        return userService.register(
            UserService.RegRequest(
                authCorrelationId(securityIdentity), tokenEmail(securityIdentity), request.alias,
                request.avatarText, request.avatarBgColor
            )
        )
    }

    @PUT
    @Authenticated
    fun updateUser(
        @Valid request: UserUpdateRequest, @Context securityIdentity: SecurityIdentity
    ): UserService.RegResponse {
        return userService.update(
            UserService.UpdateRequest(
                authCorrelationId(securityIdentity), request.alias, request.avatarText, request.avatarBgColor
            )
        )
    }

    data class UserRegisterRequest(
        @field:Size(min = 3, max = 20) val alias: String,
        @field:ValidAvatarText val avatarText: String,
        @field:ValidAvatarBgColor val avatarBgColor: String,
        @field:AssertTrue(message = "Terms and conditions must be accepted") val acceptTermsAndConditions: Boolean
    )

    data class UserUpdateRequest(
        @field:Size(min = 3, max = 20) val alias: String,
        @field:ValidAvatarText val avatarText: String,
        @field:ValidAvatarBgColor val avatarBgColor: String
    )

}
