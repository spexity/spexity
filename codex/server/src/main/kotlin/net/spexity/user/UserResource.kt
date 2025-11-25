package net.spexity.user

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import net.spexity.security.authCorrelationId
import net.spexity.security.tokenEmail
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/users/current")
@Validated
class UserResource(private val userService: UserService) {

    @GetMapping
    fun getCurrentUser(authentication: Authentication): UserService.RegResponse {
        return userService.getUser(authCorrelationId(authentication))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Please register first")
    }

    @PostMapping
    fun registerUser(
        @Valid @RequestBody request: UserRegisterRequest,
        authentication: Authentication
    ): UserService.RegResponse {
        return userService.register(
            UserService.RegRequest(
                authCorrelationId(authentication), tokenEmail(authentication), request.alias,
                request.avatarText, request.avatarBgColor
            )
        )
    }

    @PutMapping
    fun updateUser(
        @Valid @RequestBody request: UserUpdateRequest,
        authentication: Authentication
    ): UserService.RegResponse {
        return userService.update(
            UserService.UpdateRequest(
                authCorrelationId(authentication), request.alias, request.avatarText, request.avatarBgColor
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
