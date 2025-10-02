package net.spexity.user

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Context
import net.spexity.security.authCorrelationId
import net.spexity.security.tokenEmail
import org.jboss.logging.Logger
import kotlin.random.Random

@Path("/api/current-user")
class UserResource(private val userService: UserService, private val logger: Logger) {

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
                randomEmoji() + randomEmoji(), randomColor()
            )
        )
    }

    data class UserRegisterRequest(
        @field:Size(min = 3, max = 20) val alias: String,
        @field:AssertTrue(message = "Terms and conditions must be accepted") val acceptTermsAndConditions: Boolean
    )

    fun randomEmoji(): String {
        //Face and hand emoji set.
        val codePoint = Random.nextInt(0x1F600, 0x1F64F + 1)
        return String(Character.toChars(codePoint))
    }

    fun randomColor(): String {
        return emojiBackgrounds.random()
    }

    val emojiBackgrounds = listOf(
        "#1E3A8A", // deep blue
        "#2563EB", // medium blue
        "#38BDF8", // sky blue
        "#0EA5E9", // cyan-blue
        "#0284C7", // strong blue
        "#0369A1", // ocean
        "#9333EA", // violet
        "#7C3AED", // purple
        "#A855F7", // lavender
        "#C084FC", // soft purple
        "#14B8A6", // teal
        "#0D9488", // deep teal
        "#10B981", // emerald green
        "#22C55E", // spring green
        "#4ADE80", // fresh green
        "#84CC16", // lime
        "#65A30D", // olive green
        "#F97316", // warm orange
        "#EA580C", // deep orange
        "#DC2626", // red
        "#EF4444", // bright red
        "#F87171", // coral red
        "#64748B", // slate grey
        "#475569", // deep slate
        "#1E293B"  // charcoal
    )

}
